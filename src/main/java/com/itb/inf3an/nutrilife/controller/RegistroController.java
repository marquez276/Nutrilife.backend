package com.itb.inf3an.nutrilife.controller;

import com.itb.inf3an.nutrilife.model.entity.*;
import com.itb.inf3an.nutrilife.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class RegistroController {

    @Autowired
    private RegistroRefeicaoRepository refeicaoRepo;

    @Autowired
    private PesagemRepository pesagemRepo;

    @Autowired
    private ConsultaRepository consultaRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private AnamneseRepository anamneseRepo;

    //  REFEIÇÕES DIÁRIAS 

    @GetMapping("/registros/refeicoes/{clienteId}")
    public List<RegistroRefeicao> listarRefeicoes(@PathVariable Long clienteId) {
        return refeicaoRepo.findByClienteId(clienteId);
    }

    @PostMapping("/registros/refeicoes")
    public ResponseEntity<?> salvarRefeicao(@RequestBody RegistroRefeicao refeicao) {
        if (refeicao.getData() == null) refeicao.setData(LocalDate.now());
        return ResponseEntity.ok(refeicaoRepo.save(refeicao));
    }

    @PutMapping("/registros/refeicoes/{id}")
    public ResponseEntity<?> atualizarRefeicao(@PathVariable Long id, @RequestBody RegistroRefeicao dados) {
        return refeicaoRepo.findById(id).map(r -> {
            r.setMeal(dados.getMeal());
            r.setItems(dados.getItems());
            r.setCalories(dados.getCalories());
            r.setTime(dados.getTime());
            return ResponseEntity.ok(refeicaoRepo.save(r));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/registros/refeicoes/{id}")
    public ResponseEntity<?> deletarRefeicao(@PathVariable Long id) {
        refeicaoRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // PESAGENS 

    @GetMapping("/registros/pesagens/{clienteId}")
    public List<Pesagem> listarPesagens(@PathVariable Long clienteId) {
        return pesagemRepo.findByClienteId(clienteId);
    }

    @PostMapping("/registros/pesagens")
    public ResponseEntity<?> salvarPesagem(@RequestBody Pesagem pesagem) {
        if (pesagem.getData() == null) pesagem.setData(LocalDate.now());
        return ResponseEntity.ok(pesagemRepo.save(pesagem));
    }

    // CONSULTAS / AGENDA

    @GetMapping("/consultas/cliente/{clienteId}")
    public List<Consulta> listarConsultasCliente(@PathVariable Long clienteId) {
        return consultaRepo.findByClienteId(clienteId);
    }

    @GetMapping("/consultas/nutricionista/{nutricionistaId}")
    public List<Consulta> listarConsultasNutri(@PathVariable Long nutricionistaId) {
        return consultaRepo.findByNutricionistaId(nutricionistaId);
    }

    @PostMapping("/consultas")
    public ResponseEntity<?> salvarConsulta(@RequestBody Consulta consulta) {
        return ResponseEntity.ok(consultaRepo.save(consulta));
    }

    @PutMapping("/consultas/{id}")
    public ResponseEntity<?> atualizarConsulta(@PathVariable Long id, @RequestBody Consulta dados) {
        return consultaRepo.findById(id).map(c -> {
            c.setDataConsulta(dados.getDataConsulta());
            c.setHorario(dados.getHorario());
            c.setCodStatus(dados.isCodStatus());
            return ResponseEntity.ok(consultaRepo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/consultas/{id}")
    public ResponseEntity<?> deletarConsulta(@PathVariable Long id) {
        consultaRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ATUALIZAR USUÁRIO

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<?> atualizarUsuario(@PathVariable Long id, @RequestBody Map<String, String> dados) {
        return usuarioRepo.findById(id).map(u -> {
            if (dados.containsKey("nomeCompleto")) u.setNomeCompleto(dados.get("nomeCompleto"));
            if (dados.containsKey("telefone"))     u.setTelefone(dados.get("telefone"));
            if (dados.containsKey("crn"))          u.setCrn(dados.get("crn"));
            return ResponseEntity.ok(usuarioRepo.save(u));
        }).orElse(ResponseEntity.notFound().build());
    }

    // UPLOAD DE IMAGEM

    @PostMapping("/usuarios/{id}/imagem")
    public ResponseEntity<?> uploadImagem(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return usuarioRepo.findById(id).map(u -> {
            try {
                u.setImagem(file.getBytes());
                usuarioRepo.save(u);
                return ResponseEntity.ok(Map.of("message", "Imagem salva com sucesso."));
            } catch (IOException e) {
                return ResponseEntity.internalServerError().body(Map.of("message", "Erro ao salvar imagem."));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    // ATUALIZAR ANAMNESE

    @PutMapping("/anamnese/{id}")
    public ResponseEntity<?> atualizarAnamnese(@PathVariable Long id, @RequestBody Anamnese dados) {
        return anamneseRepo.findById(id).map(a -> {
            if (dados.getPeso()          != null) a.setPeso(dados.getPeso());
            if (dados.getAltura()        != null) a.setAltura(dados.getAltura());
            if (dados.getSexo()          != null) a.setSexo(dados.getSexo());
            if (dados.getObjetivo()      != null) a.setObjetivo(dados.getObjetivo());
            if (dados.getAtividade()     != null) a.setAtividade(dados.getAtividade());
            if (dados.getSono()          != null) a.setSono(dados.getSono());
            if (dados.getComorbidades()  != null) a.setComorbidades(dados.getComorbidades());
            if (dados.getRestricoes()    != null) a.setRestricoes(dados.getRestricoes());
            return ResponseEntity.ok(anamneseRepo.save(a));
        }).orElse(ResponseEntity.notFound().build());
    }


    
}
