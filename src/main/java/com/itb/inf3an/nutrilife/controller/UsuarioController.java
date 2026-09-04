package com.itb.inf3an.nutrilife.controller;

import com.itb.inf3an.nutrilife.model.entity.Anamnese;
import com.itb.inf3an.nutrilife.model.entity.Cliente;
import com.itb.inf3an.nutrilife.model.entity.Nutricionista;
import com.itb.inf3an.nutrilife.model.entity.Usuario;
import com.itb.inf3an.nutrilife.repository.AnamneseRepository;
import com.itb.inf3an.nutrilife.repository.ClienteRepository;
import com.itb.inf3an.nutrilife.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private AnamneseRepository anamneseRepository;

    @PostMapping("/usuarios/paciente")
    public ResponseEntity<?> cadastrarPaciente(@Valid @RequestBody Cliente cliente) {
        if (usuarioRepository.findByEmail(cliente.getEmail()) != null) {
            return ResponseEntity.badRequest().body(Map.of("message", "E-mail já cadastrado."));
        }
        cliente.setSenha(encoder.encode(cliente.getSenha()));
        cliente.setCodStatus(true);
        Cliente salvo = clienteRepository.save(cliente);
        log.info("Paciente cadastrado: id={} email={}", salvo.getId(), salvo.getEmail());
        return ResponseEntity.ok(Map.of("id", salvo.getId(), "nomeCompleto", salvo.getNomeCompleto(), "email", salvo.getEmail()));
    }

    @PostMapping("/usuarios/nutricionista")
    public ResponseEntity<?> cadastrarNutricionista(@Valid @RequestBody Nutricionista nutricionista) {
        if (usuarioRepository.findByEmail(nutricionista.getEmail()) != null) {
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
        nutricionista.setSenha(encoder.encode(nutricionista.getSenha()));
        nutricionista.setCodStatus(false);
        Nutricionista salvo = (Nutricionista) usuarioRepository.save(nutricionista);
        log.info("Nutricionista cadastrado: id={} email={} crn={}", salvo.getId(), salvo.getEmail(), salvo.getCrn());
        return ResponseEntity.ok(Map.of("id", salvo.getId(), "nomeCompleto", salvo.getNomeCompleto(), "email", salvo.getEmail()));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String senha = body.get("senha");

        if (email == null || senha == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "E-mail e senha são obrigatórios."));
        }

        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario == null) {
            log.warn("Tentativa de login com e-mail inexistente: {}", email);
            return ResponseEntity.status(401).body(Map.of("message", "E-mail ou senha inválidos."));
        }

        // suporta senha em BCrypt e senha em texto puro (para usuários antigos)
        boolean senhaValida = encoder.matches(senha, usuario.getSenha()) || usuario.getSenha().equals(senha);
        if (!senhaValida) {
            log.warn("Senha incorreta para: {}", email);
            return ResponseEntity.status(401).body(Map.of("message", "E-mail ou senha inválidos."));
        }

        log.info("Login realizado: id={} tipo={}", usuario.getId(), usuario.getTipoUsuario());
        return ResponseEntity.ok(Map.of("usuario", usuario));
    }

    @PostMapping("/anamnese")
    public ResponseEntity<?> salvarAnamnese(@Valid @RequestBody Anamnese anamnese) {
        if (anamnese.getCliente() == null || anamnese.getCliente().getId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cliente é obrigatório na anamnese."));
        }
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
        Anamnese salva = anamneseRepository.save(anamnese);
        log.info("Anamnese criada: id={} clienteId={}", salva.getId(), salva.getCliente().getId());
        return ResponseEntity.ok(salva);
    }
}
