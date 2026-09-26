package com.itb.inf3an.nutrilife.controller;

import com.itb.inf3an.nutrilife.model.entity.Anamnese;
import com.itb.inf3an.nutrilife.model.entity.Cliente;
import com.itb.inf3an.nutrilife.model.entity.Nutricionista;
import com.itb.inf3an.nutrilife.model.entity.Usuario;
import com.itb.inf3an.nutrilife.model.repository.AnamneseRepository;
import com.itb.inf3an.nutrilife.model.repository.ClienteRepository;
import com.itb.inf3an.nutrilife.model.repository.UsuarioRepository;
import com.itb.inf3an.nutrilife.model.services.Auditoria;
import com.itb.inf3an.nutrilife.security.config.Authz;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private AnamneseRepository anamneseRepository;
    @Autowired private Authz authz;
    @Autowired private Auditoria auditoria;

    @PostMapping("/usuarios/paciente")
    public ResponseEntity<?> cadastrarPaciente(@Valid @RequestBody Cliente cliente) {
        if (usuarioRepository.findByEmail(cliente.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "E-mail já cadastrado."));
        }
        cliente.setId(null);
        cliente.setSenha(encoder.encode(cliente.getSenha()));
        cliente.setCodStatus(true);
        Cliente salvo = clienteRepository.save(cliente);
        log.info("Paciente cadastrado: id={} email={}", salvo.getId(), salvo.getEmail());
        auditoria.registrar(salvo.getNomeCompleto(), "Cadastro de paciente", "Sucesso");
        return ResponseEntity.ok(Map.of("id", salvo.getId(), "nomeCompleto", salvo.getNomeCompleto(), "email", salvo.getEmail()));
    }

    @PostMapping("/usuarios/nutricionista")
    public ResponseEntity<?> cadastrarNutricionista(@Valid @RequestBody Nutricionista nutricionista) {
        if (usuarioRepository.findByEmail(nutricionista.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "E-mail já cadastrado."));
        }
        if (nutricionista.getCrn() == null || nutricionista.getCrn().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "CRN é obrigatório."));
        }
        if (nutricionista.getEspecialidade() == null || nutricionista.getEspecialidade().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Especialidade é obrigatória."));
        }
        // Verifica duplicidade de CRN
        if (usuarioRepository.findByTipoUsuario(com.itb.inf3an.nutrilife.model.enums.TipoUsuario.NUTRICIONISTA)
                .stream().anyMatch(u -> nutricionista.getCrn().equalsIgnoreCase(u.getCrn()))) {
            return ResponseEntity.badRequest().body(Map.of("message", "CRN já cadastrado no sistema."));
        }
        nutricionista.setId(null);
        nutricionista.setSenha(encoder.encode(nutricionista.getSenha()));
        nutricionista.setCodStatus(false);
        Nutricionista salvo = (Nutricionista) usuarioRepository.save(nutricionista);
        log.info("Nutricionista cadastrado: id={} email={} crn={}", salvo.getId(), salvo.getEmail(), salvo.getCrn());
        auditoria.registrar(salvo.getNomeCompleto(), "Cadastro de nutricionista (CRN " + salvo.getCrn() + ")", "Sucesso");
        return ResponseEntity.ok(Map.of("id", salvo.getId(), "nomeCompleto", salvo.getNomeCompleto(), "email", salvo.getEmail()));
    }

    @PostMapping("/anamnese")
    public ResponseEntity<?> salvarAnamnese(@Valid @RequestBody Anamnese anamnese) {
        if (anamnese.getCliente() == null || anamnese.getCliente().getId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cliente é obrigatório na anamnese."));
        }
        authz.check(anamnese.getCliente().getId());
        // Upsert: update existing record if one already exists for this client
        Anamnese existing = anamneseRepository.findByClienteId(anamnese.getCliente().getId());
        if (existing != null) {
            existing.setPeso(anamnese.getPeso());
            existing.setAltura(anamnese.getAltura());
            existing.setIdade(anamnese.getIdade());
            existing.setSexo(anamnese.getSexo());
            existing.setObjetivo(anamnese.getObjetivo());
            existing.setAtividade(anamnese.getAtividade());
            existing.setSono(anamnese.getSono());
            existing.setComorbidades(anamnese.getComorbidades());
            existing.setRestricoes(anamnese.getRestricoes());
            Anamnese salva = anamneseRepository.save(existing);
            log.info("Anamnese atualizada (upsert): id={} clienteId={}", salva.getId(), salva.getCliente().getId());
            return ResponseEntity.ok(salva);
        }
        anamnese.setId(null);
        Anamnese salva = anamneseRepository.save(anamnese);
        log.info("Anamnese criada: id={} clienteId={}", salva.getId(), salva.getCliente().getId());
        return ResponseEntity.ok(salva);
    }
}
