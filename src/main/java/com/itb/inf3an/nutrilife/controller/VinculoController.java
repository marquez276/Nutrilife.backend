package com.itb.inf3an.nutrilife.controller;

import com.itb.inf3an.nutrilife.exceptions.BadRequest;
import com.itb.inf3an.nutrilife.exceptions.NotFound;
import com.itb.inf3an.nutrilife.model.entity.Cliente;
import com.itb.inf3an.nutrilife.model.entity.Nutricionista;
import com.itb.inf3an.nutrilife.model.entity.Usuario;
import com.itb.inf3an.nutrilife.model.entity.Vinculo;
import com.itb.inf3an.nutrilife.model.enums.OrigemVinculo;
import com.itb.inf3an.nutrilife.model.enums.StatusVinculo;
import com.itb.inf3an.nutrilife.model.repository.VinculoRepository;
import com.itb.inf3an.nutrilife.security.config.Authz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@RestController
public class VinculoController {

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sem O/0/I/1
    private final SecureRandom random = new SecureRandom();

    @Autowired
    private VinculoRepository vinculoRepo;

    @Autowired
    private Authz authz;

    private Usuario usuarioAutenticado() {
        return (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Map<String, Object> toMap(Vinculo v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", v.getId());
        m.put("clienteId", v.getCliente() != null ? v.getCliente().getId() : null);
        m.put("clienteNome", v.getCliente() != null ? v.getCliente().getNomeCompleto() : null);
        m.put("clienteEmail", v.getCliente() != null ? v.getCliente().getEmail() : null);
        m.put("nutricionistaId", v.getNutricionista() != null ? v.getNutricionista().getId() : null);
        m.put("nutricionistaNome", v.getNutricionista() != null ? v.getNutricionista().getNomeCompleto() : null);
        m.put("origem", v.getOrigem());
        m.put("status", v.getStatus());
        m.put("codigoConvite", v.getCodigoConvite());
        m.put("dataCriacao", v.getDataCriacao());
        m.put("dataResposta", v.getDataResposta());
        return m;
    }

    // ── Fluxo A: paciente solicita acompanhamento ──────────────────────

    @PostMapping("/vinculos/solicitar")
    public ResponseEntity<?> solicitar(@RequestBody Map<String, Object> body) {
        Usuario atual = usuarioAutenticado();
        Long nutricionistaId = Long.valueOf(String.valueOf(body.get("nutricionistaId")));

        if (vinculoRepo.existsByClienteIdAndNutricionistaIdAndStatusIn(
                atual.getId(), nutricionistaId, List.of(StatusVinculo.PENDENTE, StatusVinculo.ATIVO))) {
            throw new BadRequest("Já existe uma solicitação ou vínculo com este nutricionista.");
        }

        Cliente cliente = new Cliente();
        cliente.setId(atual.getId());
        Nutricionista nutri = new Nutricionista();
        nutri.setId(nutricionistaId);

        Vinculo v = new Vinculo();
        v.setCliente(cliente);
        v.setNutricionista(nutri);
        v.setOrigem(OrigemVinculo.PACIENTE);
        v.setStatus(StatusVinculo.PENDENTE);
        return ResponseEntity.ok(toMap(recarregar(vinculoRepo.save(v))));
    }

    // Entidades recém-montadas a partir de só um ID (cliente/nutricionista acima) não têm nome
    // carregado; recarregar busca a linha salva com os relacionamentos EAGER já resolvidos.
    private Vinculo recarregar(Vinculo v) {
        return vinculoRepo.findById(v.getId()).orElse(v);
    }

    // ── Fluxo B: nutricionista gera convite, paciente resgata ──────────

    @PostMapping("/vinculos/convite")
    public ResponseEntity<?> gerarConvite() {
        Usuario atual = usuarioAutenticado();
        Nutricionista nutri = new Nutricionista();
        nutri.setId(atual.getId());

        Vinculo v = new Vinculo();
        v.setNutricionista(nutri);
        v.setOrigem(OrigemVinculo.NUTRICIONISTA);
        v.setStatus(StatusVinculo.PENDENTE);
        v.setCodigoConvite(gerarCodigoUnico());
        return ResponseEntity.ok(toMap(recarregar(vinculoRepo.save(v))));
    }

    private String gerarCodigoUnico() {
        String codigo;
        do {
            StringBuilder sb = new StringBuilder("NUTRI-");
            for (int i = 0; i < 6; i++) sb.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
            codigo = sb.toString();
        } while (vinculoRepo.findByCodigoConvite(codigo).isPresent());
        return codigo;
    }

    // ponytail: resgate ativa o vínculo direto (o nutricionista já confirmou ao gerar e compartilhar
    // o código). Se precisar de uma segunda confirmação do nutricionista depois do resgate, trocar o
    // status aqui para PENDENTE em vez de ATIVO e notificar o nutricionista para aceitar/recusar.
    @PostMapping("/vinculos/convite/resgatar")
    public ResponseEntity<?> resgatarConvite(@RequestBody Map<String, Object> body) {
        Usuario atual = usuarioAutenticado();
        String codigo = String.valueOf(body.get("codigoConvite"));

        Vinculo v = vinculoRepo.findByCodigoConvite(codigo)
                .orElseThrow(() -> new NotFound("Código de convite inválido."));
        if (v.getStatus() != StatusVinculo.PENDENTE || v.getCliente() != null) {
            throw new BadRequest("Este código de convite já foi utilizado.");
        }

        Cliente cliente = new Cliente();
        cliente.setId(atual.getId());
        v.setCliente(cliente);
        v.setStatus(StatusVinculo.ATIVO);
        v.setDataResposta(LocalDateTime.now());
        return ResponseEntity.ok(toMap(recarregar(vinculoRepo.save(v))));
    }

    // ── Consulta ─────────────────────────────────────────────────────

    @PreAuthorize("@authz.self(#nutricionistaId)")
    @GetMapping("/vinculos/nutricionista/{nutricionistaId}")
    public ResponseEntity<?> porNutricionista(@PathVariable Long nutricionistaId) {
        List<Map<String, Object>> result = new ArrayList<>();
        vinculoRepo.findByNutricionistaId(nutricionistaId).forEach(v -> result.add(toMap(v)));
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("@authz.self(#clienteId)")
    @GetMapping("/vinculos/cliente/{clienteId}")
    public ResponseEntity<?> porCliente(@PathVariable Long clienteId) {
        List<Map<String, Object>> result = new ArrayList<>();
        vinculoRepo.findByClienteId(clienteId).forEach(v -> result.add(toMap(v)));
        return ResponseEntity.ok(result);
    }

    // ── Aceitar / recusar (nutricionista decide sobre solicitação do paciente) ──

    @PutMapping("/vinculos/{id}/aceitar")
    public ResponseEntity<?> aceitar(@PathVariable Long id) {
        return vinculoRepo.findById(id).map(v -> {
            authz.check(v.getNutricionista().getId());
            v.setStatus(StatusVinculo.ATIVO);
            v.setDataResposta(LocalDateTime.now());
            return ResponseEntity.ok(toMap(vinculoRepo.save(v)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/vinculos/{id}/recusar")
    public ResponseEntity<?> recusar(@PathVariable Long id) {
        return vinculoRepo.findById(id).map(v -> {
            authz.check(v.getNutricionista().getId());
            v.setStatus(StatusVinculo.RECUSADO);
            v.setDataResposta(LocalDateTime.now());
            return ResponseEntity.ok(toMap(vinculoRepo.save(v)));
        }).orElse(ResponseEntity.notFound().build());
    }
}
