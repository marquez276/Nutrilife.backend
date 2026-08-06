package com.itb.inf3an.nutrilife.controller;

import com.itb.inf3an.nutrilife.model.entity.*;
import com.itb.inf3an.nutrilife.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@RestController
@CrossOrigin(origins = "*")
public class RegistroController {

    private static final Logger log = LoggerFactory.getLogger(RegistroController.class);

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

    @Autowired
    private MetasDiariasRepository metasRepo;

    @Autowired
    private PlanoAlimentarRepository planoRepo;

    //  REFEIÇÕES DIÁRIAS 

    @GetMapping("/registros/refeicoes/{clienteId}")
    public List<RegistroRefeicao> listarRefeicoes(@PathVariable Long clienteId) {
        return refeicaoRepo.findByClienteIdAndData(clienteId, LocalDate.now());
    }

    @GetMapping("/registros/refeicoes/{clienteId}/historico")
    public List<RegistroRefeicao> historicoRefeicoes(@PathVariable Long clienteId) {
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
        return pesagemRepo.findByClienteIdOrderByDataAsc(clienteId);
    }

    @PostMapping("/registros/pesagens")
    public ResponseEntity<?> salvarPesagem(@RequestBody Pesagem pesagem) {
        if (pesagem.getData() == null) pesagem.setData(LocalDate.now());
        Pesagem salva = pesagemRepo.save(pesagem);
        if (salva.getCliente() != null) recalcularMetas(salva.getCliente().getId());
        return ResponseEntity.ok(salva);
    }

    @DeleteMapping("/registros/pesagens/{id}")
    public ResponseEntity<?> removerPesagem(@PathVariable Long id) {
        return pesagemRepo.findById(id).map(p -> {
            Long clienteId = p.getCliente() != null ? p.getCliente().getId() : null;
            pesagemRepo.deleteById(id);
            if (clienteId != null) recalcularMetas(clienteId);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/clientes/{clienteId}/progresso")
    public ResponseEntity<?> getProgresso(@PathVariable Long clienteId) {
        List<Pesagem> historico = pesagemRepo.findByClienteIdOrderByDataAsc(clienteId);
        Anamnese anamnese = anamneseRepo.findByClienteId(clienteId);
        MetasDiarias metas = metasRepo.findByClienteId(clienteId);

        double pesoInicial = metas != null && metas.getPesoInicial() != null
            ? metas.getPesoInicial()
            : (anamnese != null && anamnese.getPeso() != null ? anamnese.getPeso() : 0.0);

        double pesoAtual = !historico.isEmpty()
            ? historico.get(historico.size() - 1).getPeso()
            : pesoInicial;

        double pesoIdeal = metas != null && metas.getPesoIdeal() != null ? metas.getPesoIdeal() : 0.0;
        double perdido   = Math.round((pesoInicial - pesoAtual) * 10.0) / 10.0;
        double paraaMeta = Math.round((pesoAtual - pesoIdeal) * 10.0) / 10.0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pesoAtual",   pesoAtual);
        result.put("pesoInicial", pesoInicial);
        result.put("perdido",     perdido);
        result.put("pesoIdeal",   pesoIdeal);
        result.put("paraaMeta",   paraaMeta);
        result.put("imc",         metas != null ? metas.getImc() : null);
        result.put("statusPeso",  metas != null ? metas.getStatusPeso() : null);
        result.put("historico",   historico);
        return ResponseEntity.ok(result);
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

    // METAS CALCULADAS

    @GetMapping("/anamnese/cliente/{clienteId}/metas")
    public ResponseEntity<?> calcularMetas(@PathVariable Long clienteId) {
        Anamnese a = anamneseRepo.findByClienteId(clienteId);
        if (a == null) return ResponseEntity.notFound().build();
        recalcularMetas(clienteId);
        MetasDiarias m = metasRepo.findByClienteId(clienteId);
        Map<String, Object> metas = new LinkedHashMap<>();
        metas.put("metaCalorias", m.getMetaCalorias());
        metas.put("tdee",         m.getTdee());
        metas.put("bmr",          m.getBmr());
        metas.put("pesoIdeal",    m.getPesoIdeal());
        metas.put("pesoInicial",  m.getPesoInicial());
        metas.put("imc",          m.getImc());
        metas.put("statusPeso",   m.getStatusPeso());
        return ResponseEntity.ok(metas);
    }

    // ATUALIZAR ANAMNESE

    @GetMapping("/anamnese/cliente/{clienteId}")
    public ResponseEntity<?> buscarAnamnese(@PathVariable Long clienteId) {
        Anamnese a = anamneseRepo.findByClienteId(clienteId);
        if (a == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(a);
    }

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
            Anamnese salva = anamneseRepo.save(a);
            Long clienteId = salva.getCliente().getId();
            recalcularMetas(clienteId);
            planoRepo.findTopByClienteIdOrderByDataInicioDesc(clienteId).ifPresent(plano -> {
                plano.setAnamneseHash(null);
                planoRepo.save(plano);
                log.info("Hash do plano invalidado para clienteId={} após atualização de anamnese", clienteId);
            });
            return ResponseEntity.ok(salva);
        }).orElse(ResponseEntity.notFound().build());
    }

    private void recalcularMetas(Long clienteId) {
        Anamnese a = anamneseRepo.findByClienteId(clienteId);
        if (a == null) return;
        List<Pesagem> pesagens = pesagemRepo.findByClienteIdOrderByDataAsc(clienteId);
        double pesoAtual = !pesagens.isEmpty()
            ? pesagens.get(pesagens.size() - 1).getPeso()
            : (a.getPeso() != null ? a.getPeso() : 70.0);
        double altura = a.getAltura() != null ? a.getAltura() : 170.0;
        int    idade  = a.getIdade()  != null ? a.getIdade()  : 30;
        String sexo   = a.getSexo()   != null ? a.getSexo().toUpperCase() : "M";
        String ativ   = a.getAtividade() != null ? a.getAtividade().toLowerCase() : "sedentario";
        String obj    = a.getObjetivo()  != null ? a.getObjetivo().toLowerCase() : "manutencao";
        double pesoBase = a.getPeso() != null ? a.getPeso() : pesoAtual;
        double bmr = sexo.startsWith("M")
            ? 10 * pesoBase + 6.25 * altura - 5 * idade + 5
            : 10 * pesoBase + 6.25 * altura - 5 * idade - 161;
        double fator = switch (ativ) {
            case "leve"     -> 1.375;
            case "moderado" -> 1.55;
            case "intenso"  -> 1.725;
            default         -> 1.2;
        };
        double tdee = bmr * fator;
        double metaCalorias = switch (obj) {
            case "emagrecimento" -> tdee - 500;
            case "hipertrofia"   -> tdee + 300;
            case "performance"   -> tdee + 200;
            default              -> tdee;
        };
        double alturaM = altura / 100.0;
        double pesoIdeal = sexo.startsWith("M")
            ? 50.0 + 2.3 * ((altura - 152.4) / 2.54)
            : 45.5 + 2.3 * ((altura - 152.4) / 2.54);
        pesoIdeal = Math.max(pesoIdeal, 30.0);
        double bmi = pesoAtual / (alturaM * alturaM);
        String statusPeso;
        if      (bmi < 18.5) statusPeso = "Abaixo do peso";
        else if (bmi < 25.0) statusPeso = "Peso Ideal";
        else if (bmi < 30.0) statusPeso = "Sobrepeso";
        else                  statusPeso = "Obesidade";
        MetasDiarias m = metasRepo.findByClienteId(clienteId);
        if (m == null) { m = new MetasDiarias(); Cliente c = new Cliente(); c.setId(clienteId); m.setCliente(c); }
        m.setMetaCalorias((int) Math.round(metaCalorias));
        m.setTdee((int) Math.round(tdee));
        m.setBmr((int) Math.round(bmr));
        m.setPesoIdeal(Math.round(pesoIdeal * 10.0) / 10.0);
        // Only set pesoInicial once — the first time metas are created
        if (m.getPesoInicial() == null && a.getPeso() != null) {
            m.setPesoInicial(a.getPeso());
        }
        m.setImc(Math.round(bmi * 10.0) / 10.0);
        m.setStatusPeso(statusPeso);
        m.setAtualizadoEm(LocalDateTime.now());
        metasRepo.save(m);
    }

    // PRONTUÁRIO - OBSERVAÇÕES CLÍNICAS
    // Usa o próprio campo observations da Consulta para guardar notas por paciente.
    // Endpoint dedicado: GET/POST /prontuario/{nutricionistaId}/{pacienteNome}

    @GetMapping("/prontuario/{nutricionistaId}/{pacienteNome}")
    public ResponseEntity<?> buscarProntuario(
            @PathVariable Long nutricionistaId,
            @PathVariable String pacienteNome) {
        // Busca a consulta mais recente deste nutricionista para este paciente que tenha observações
        return consultaRepo.findByNutricionistaId(nutricionistaId).stream()
            .filter(c -> pacienteNome.equalsIgnoreCase(c.getPacienteNome()) && c.getObservations() != null)
            .max(java.util.Comparator.comparing(c -> c.getDataConsulta() != null ? c.getDataConsulta() : java.time.LocalDate.MIN))
            .map(c -> ResponseEntity.ok(Map.of(
                "id", c.getId(),
                "observations", c.getObservations() != null ? c.getObservations() : ""
            )))
            .orElse(ResponseEntity.ok(Map.of("id", -1, "observations", "")));
    }

    @PostMapping("/prontuario")
    public ResponseEntity<?> salvarProntuario(@RequestBody Map<String, Object> body) {
        // Cria ou atualiza uma consulta de prontuário (sem data/hora, só observações)
        Long consultaId = body.containsKey("consultaId") && body.get("consultaId") != null
            ? Long.parseLong(body.get("consultaId").toString()) : null;

        Consulta c = consultaId != null && consultaId > 0
            ? consultaRepo.findById(consultaId).orElse(new Consulta())
            : new Consulta();

        if (c.getDataConsulta() == null) c.setDataConsulta(java.time.LocalDate.now());
        if (c.getHorario() == null) c.setHorario(java.time.LocalTime.now());
        c.setObservations(body.get("observations") != null ? body.get("observations").toString() : null);
        if (body.containsKey("pacienteNome"))
            c.setPacienteNome(body.get("pacienteNome").toString());
        if (body.containsKey("nutricionistaId") && body.get("nutricionistaId") != null) {
            Nutricionista n = new Nutricionista();
            n.setId(Long.parseLong(body.get("nutricionistaId").toString()));
            c.setNutricionista(n);
        }
        c.setCodStatus(true);
        Consulta salva = consultaRepo.save(c);
        return ResponseEntity.ok(Map.of("id", salva.getId(), "observations", salva.getObservations() != null ? salva.getObservations() : ""));
    }

    // PESAGENS DO PACIENTE (para o nutricionista ver evolução)

    @PostMapping("/registros/pesagens/nutricionista")
    public ResponseEntity<?> salvarPesagemPaciente(@RequestBody Map<String, Object> body) {
        Pesagem p = new Pesagem();
        if (body.containsKey("peso")) p.setPeso(Double.parseDouble(body.get("peso").toString()));
        if (body.containsKey("data") && body.get("data") != null)
            p.setData(java.time.LocalDate.parse(body.get("data").toString()));
        else p.setData(java.time.LocalDate.now());
        if (body.containsKey("clienteId") && body.get("clienteId") != null) {
            Cliente cli = new Cliente();
            cli.setId(Long.parseLong(body.get("clienteId").toString()));
            p.setCliente(cli);
        }
        return ResponseEntity.ok(pesagemRepo.save(p));
    }

    // METAS SALVAS (leitura sem recalcular)

    @GetMapping("/clientes/{clienteId}/metas")
    public ResponseEntity<?> buscarMetasSalvas(@PathVariable Long clienteId) {
        MetasDiarias m = metasRepo.findByClienteId(clienteId);
        if (m == null) return ResponseEntity.notFound().build();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("metaCalorias", m.getMetaCalorias());
        result.put("tdee",         m.getTdee());
        result.put("bmr",          m.getBmr());
        result.put("pesoIdeal",    m.getPesoIdeal());
        result.put("pesoInicial",  m.getPesoInicial());
        result.put("imc",          m.getImc());
        result.put("statusPeso",   m.getStatusPeso());
        result.put("atualizadoEm", m.getAtualizadoEm());
        return ResponseEntity.ok(result);
    }
}

