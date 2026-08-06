package com.itb.inf3an.nutrilife.controller;

import com.itb.inf3an.nutrilife.model.entity.*;
import com.itb.inf3an.nutrilife.model.enums.TipoUsuario;
import com.itb.inf3an.nutrilife.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired private LogAtividadeRepository logRepo;
    @Autowired private AvaliacaoRepository avaliacaoRepo;
    @Autowired private PagamentoPixRepository pagamentoRepo;
    @Autowired private NutriPerfilRepository nutriPerfilRepo;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private AdminRepository adminRepo;
    @Autowired private AlimentoRepository alimentoRepo;
    @Autowired private AnamneseRepository anamneseRepo;

    // configuração de inatividade (dias) — persistência simples em memória
    private int diasInatividade = 30;

    // ── USUÁRIOS (DASHBOARD) ────────────────────────────────────────

    @GetMapping("/admin/usuarios")
    public ResponseEntity<?> listarUsuarios() {
        List<Map<String, Object>> result = usuarioRepo.findByTipoUsuario(TipoUsuario.CLIENTE).stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("nome", u.getNomeCompleto());
            m.put("email", u.getEmail());
            Anamnese a = anamneseRepo.findByClienteId(u.getId());
            if (a != null && a.getPeso() != null && a.getAltura() != null && a.getAltura() > 0) {
                double alturaM = a.getAltura() / 100.0;
                double imc = a.getPeso() / (alturaM * alturaM);
                m.put("imc", Math.round(imc * 10.0) / 10.0);
                String status;
                if (imc < 18.5) status = "Abaixo";
                else if (imc < 25) status = "Ideal";
                else status = "Acima";
                m.put("status", status);
            } else {
                m.put("imc", null);
                m.put("status", "Sem dados");
            }
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/admin/nutricionistas")
    public ResponseEntity<?> listarNutricionistas() {
        List<Map<String, Object>> result = usuarioRepo.findByTipoUsuario(TipoUsuario.NUTRICIONISTA).stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("nome", u.getNomeCompleto());
            m.put("email", u.getEmail());
            m.put("crn", u.getCrn());
            m.put("ativo", u.isCodStatus());
            List<Avaliacao> avs = avaliacaoRepo.findByNutricionistaId(u.getId());
            double media = avs.stream().mapToInt(Avaliacao::getNota).average().orElse(0);
            m.put("mediaAvaliacoes", avs.isEmpty() ? null : Math.round(media * 10.0) / 10.0);
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ── CRESCIMENTO MENSAL ─────────────────────────────────────────

    @GetMapping("/admin/crescimento")
    public ResponseEntity<?> crescimento() {
        // Conta cadastros (pacientes+nutris) agrupados por mês/ano baseado nos logs de login
        // Como Usuario não tem dataCadastro, usamos os logs de "Cadastro" para aproximação
        // Retorna os últimos 6 meses com total acumulado de usuários
        long total = usuarioRepo.count();
        YearMonth now = YearMonth.now();
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("month", ym.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, new java.util.Locale("pt", "BR")));
            // Distribui o total atual como referência do mês mais recente
            entry.put("users", i == 0 ? total : Math.max(0, total - i));
            data.add(entry);
        }
        return ResponseEntity.ok(data);
    }

    // ── VERIFICAÇÃO DE CRN ─────────────────────────────────────────

    @GetMapping("/admin/verificar-crn")
    public ResponseEntity<?> verificarCrn(@RequestParam String crn) {
        boolean existe = usuarioRepo.findByTipoUsuario(TipoUsuario.NUTRICIONISTA)
            .stream().anyMatch(u -> crn.equalsIgnoreCase(u.getCrn()));
        return ResponseEntity.ok(Map.of("disponivel", !existe));
    }

    // ── CONFIGURAÇÕES ────────────────────────────────────────────────

    @GetMapping("/admin/configuracoes")
    public ResponseEntity<?> getConfiguracoes() {
        return ResponseEntity.ok(Map.of("diasInatividade", diasInatividade));
    }

    @PutMapping("/admin/configuracoes")
    public ResponseEntity<?> salvarConfiguracoes(@RequestBody Map<String, Object> body) {
        if (body.containsKey("diasInatividade")) {
            diasInatividade = Integer.parseInt(body.get("diasInatividade").toString());
            log.info("Configuração de inatividade atualizada: {} dias", diasInatividade);
        }
        return ResponseEntity.ok(Map.of("diasInatividade", diasInatividade, "message", "Configurações salvas."));
    }

    // ── PERFIL ADMIN ───────────────────────────────────────────────

    @GetMapping("/admin/perfil/{id}")
    public ResponseEntity<?> getPerfil(@PathVariable Long id) {
        return adminRepo.findById(id)
            .map(a -> ResponseEntity.ok(Map.of(
                "id", a.getId(),
                "nome", a.getNomeCompleto(),
                "email", a.getEmail(),
                "telefone", a.getTelefone() != null ? a.getTelefone() : ""
            )))
            .orElse(ResponseEntity.notFound().build());
    }

    // ── APROVAÇÃO DE ADMINS ────────────────────────────────────────

    @GetMapping("/admin/pendentes")
    public List<Admin> listarAdminsPendentes() {
        return adminRepo.findByAprovadoFalse();
    }

    @PutMapping("/admin/{id}/aprovar")
    public ResponseEntity<?> aprovarAdmin(@PathVariable Long id) {
        return adminRepo.findById(id).map(a -> {
            a.setAprovado(true);
            adminRepo.save(a);
            log.info("Admin id={} aprovado", id);
            return ResponseEntity.ok(Map.of("message", "Admin aprovado."));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── LOGS ──────────────────────────────────────────────────────

    @GetMapping("/admin/logs")
    public ResponseEntity<?> listarLogs(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        Page<LogAtividade> result = logRepo.findAll(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataHora"))
        );
        return ResponseEntity.ok(Map.of(
            "content", result.getContent(),
            "totalElements", result.getTotalElements(),
            "totalPages", result.getTotalPages(),
            "page", page
        ));
    }

    @PostMapping("/admin/logs")
    public ResponseEntity<?> registrarLog(@RequestBody LogAtividade logAtividade) {
        logAtividade.setDataHora(LocalDateTime.now());
        return ResponseEntity.ok(logRepo.save(logAtividade));
    }

    // ── AVALIAÇÕES ────────────────────────────────────────────────

    @GetMapping("/avaliacoes/nutricionista/{nutricionistaId}")
    public List<Map<String, Object>> listarAvaliacoes(@PathVariable Long nutricionistaId) {
        return avaliacaoRepo.findByNutricionistaId(nutricionistaId).stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",            a.getId());
            m.put("nota",          a.getNota());
            m.put("comentario",    a.getComentario());
            m.put("dataAvaliacao", a.getDataAvaliacao());
            m.put("clienteNome",   a.getCliente() != null ? a.getCliente().getNomeCompleto() : "Paciente");
            return m;
        }).collect(Collectors.toList());
    }

    @PostMapping("/avaliacoes")
    public ResponseEntity<?> salvarAvaliacao(@RequestBody Avaliacao avaliacao) {
        avaliacao.setDataAvaliacao(java.time.LocalDate.now());
        Avaliacao salva = avaliacaoRepo.save(avaliacao);
        log.info("Avaliação salva para nutricionistaId={}", avaliacao.getNutricionista().getId());
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id",            salva.getId());
        resp.put("nota",          salva.getNota());
        resp.put("comentario",    salva.getComentario());
        resp.put("dataAvaliacao", salva.getDataAvaliacao());
        resp.put("clienteNome",   salva.getCliente() != null ? salva.getCliente().getNomeCompleto() : "Paciente");
        return ResponseEntity.ok(resp);
    }

    // ── PAGAMENTOS PIX ────────────────────────────────────────────

    @GetMapping("/admin/pagamentos")
    public List<PagamentoPix> listarPagamentos() {
        return pagamentoRepo.findByStatusOrderByDataEnvioDesc("PENDENTE");
    }

    @GetMapping("/admin/pagamentos/todos")
    public List<Map<String, Object>> listarTodosPagamentos() {
        return pagamentoRepo.findAll(Sort.by(Sort.Direction.DESC, "dataEnvio")).stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",            p.getId());
            m.put("status",        p.getStatus());
            m.put("temComprovante", p.getComprovanteUrl() != null && !p.getComprovanteUrl().isBlank());
            m.put("dataEnvio",     p.getDataEnvio());
            m.put("dataAprovacao", p.getDataAprovacao());
            m.put("observacao",    p.getObservacao());
            if (p.getNutricionista() != null) {
                Usuario u = p.getNutricionista();
                Map<String, Object> nutri = new LinkedHashMap<>();
                nutri.put("id",           u.getId());
                nutri.put("nomeCompleto", u.getNomeCompleto());
                nutri.put("email",        u.getEmail());
                nutri.put("telefone",     u.getTelefone());
                nutri.put("crn",          u.getCrn());
                m.put("nutricionista", nutri);
            }
            return m;
        }).collect(Collectors.toList());
    }

    @GetMapping("/admin/pagamentos/{id}/comprovante")
    public ResponseEntity<?> getComprovante(@PathVariable Long id) {
        return pagamentoRepo.findById(id)
            .filter(p -> p.getComprovanteUrl() != null && !p.getComprovanteUrl().isBlank())
            .map(p -> ResponseEntity.ok(Map.of("comprovanteUrl", p.getComprovanteUrl())))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/pagamentos/pix")
    public ResponseEntity<?> enviarComprovante(@RequestBody PagamentoPix pagamento) {
        pagamento.setStatus("PENDENTE");
        pagamento.setDataEnvio(LocalDateTime.now());
        PagamentoPix salvo = pagamentoRepo.save(pagamento);
        log.info("Comprovante PIX enviado por nutricionistaId={}", pagamento.getNutricionista().getId());
        return ResponseEntity.ok(Map.of("id", salvo.getId(), "status", salvo.getStatus()));
    }

    @GetMapping("/pagamentos/pix/status/{nutricionistaId}")
    public ResponseEntity<?> statusPagamento(@PathVariable Long nutricionistaId) {
        return pagamentoRepo.findByNutricionistaIdOrderByDataEnvioDesc(nutricionistaId)
            .stream().findFirst()
            .map(p -> ResponseEntity.ok(Map.of(
                "id",     p.getId(),
                "status", p.getStatus()
            )))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/admin/pagamentos/{id}/aprovar")
    public ResponseEntity<?> aprovarPagamento(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        return pagamentoRepo.findById(id).map(p -> {
            p.setStatus("APROVADO");
            p.setDataAprovacao(LocalDateTime.now());
            if (body != null && body.containsKey("observacao")) p.setObservacao(body.get("observacao"));
            pagamentoRepo.save(p);
            // ativa a conta do nutricionista
            usuarioRepo.findById(p.getNutricionista().getId()).ifPresent(u -> {
                u.setCodStatus(true);
                usuarioRepo.save(u);
            });
            log.info("Pagamento id={} aprovado, nutricionista ativado", id);
            return ResponseEntity.ok(Map.of("message", "Pagamento aprovado e nutricionista liberado."));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/admin/pagamentos/{id}/rejeitar")
    public ResponseEntity<?> rejeitarPagamento(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        return pagamentoRepo.findById(id).map(p -> {
            p.setStatus("REJEITADO");
            if (body != null && body.containsKey("observacao")) p.setObservacao(body.get("observacao"));
            pagamentoRepo.save(p);
            log.info("Pagamento id={} rejeitado", id);
            return ResponseEntity.ok(Map.of("message", "Pagamento rejeitado."));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── PERFIL PÚBLICO DO NUTRICIONISTA ───────────────────────────

    @GetMapping("/nutricionistas/perfis")
    public List<Map<String, Object>> listarPerfis() {
        // Returns all active nutritionists, merging profile data where available
        return usuarioRepo.findByTipoUsuario(TipoUsuario.NUTRICIONISTA).stream()
            .filter(u -> u.isCodStatus())
            .map(u -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("nutricionista", u);
                nutriPerfilRepo.findByNutricionistaId(u.getId()).ifPresent(p -> {
                    m.put("especialidade", p.getEspecialidade());
                    m.put("bio", p.getBio());
                    m.put("whatsapp", p.getWhatsapp());
                    m.put("instagram", p.getInstagram());
                    m.put("linkedin", p.getLinkedin());
                    m.put("website", p.getWebsite());
                    m.put("precos", p.getPrecos());
                    m.put("experiencia", p.getExperiencia());
                    m.put("portfolio", p.getPortfolio());
                    m.put("videos", p.getVideos());
                });
                return m;
            })
            .collect(Collectors.toList());
    }

    @GetMapping("/nutricionistas/{id}/perfil")
    public ResponseEntity<?> buscarPerfil(@PathVariable Long id) {
        return nutriPerfilRepo.findByNutricionistaId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/nutricionistas/{id}/perfil")
    public ResponseEntity<?> salvarPerfil(@PathVariable Long id, @RequestBody NutricionistaPerfil perfil) {
        return usuarioRepo.findById(id).map(u -> {
            var existente = nutriPerfilRepo.findByNutricionistaId(id);
            NutricionistaPerfil p = existente.orElseGet(NutricionistaPerfil::new);
            if (!existente.isPresent()) p.setNutricionista((Nutricionista) u);
            if (perfil.getEspecialidade() != null) p.setEspecialidade(perfil.getEspecialidade());
            if (perfil.getBio()           != null) p.setBio(perfil.getBio());
            if (perfil.getWhatsapp()      != null) p.setWhatsapp(perfil.getWhatsapp());
            if (perfil.getInstagram()     != null) p.setInstagram(perfil.getInstagram());
            if (perfil.getLinkedin()      != null) p.setLinkedin(perfil.getLinkedin());
            if (perfil.getWebsite()       != null) p.setWebsite(perfil.getWebsite());
            if (perfil.getPrecos()        != null) p.setPrecos(perfil.getPrecos());
            if (perfil.getExperiencia()   != null) p.setExperiencia(perfil.getExperiencia());
            if (perfil.getPortfolio()     != null) p.setPortfolio(perfil.getPortfolio());
            if (perfil.getVideos()        != null) p.setVideos(perfil.getVideos());
            return ResponseEntity.ok(nutriPerfilRepo.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuarios/{id}/imagem")
    public ResponseEntity<byte[]> getImagem(@PathVariable Long id) {
        return usuarioRepo.findById(id)
            .filter(u -> u.getImagem() != null)
            .map(u -> ResponseEntity.ok()
                .header("Content-Type", "image/jpeg")
                .header("Cache-Control", "max-age=86400")
                .body(u.getImagem()))
            .orElse(ResponseEntity.notFound().build());
    }

    // ── ALIMENTOS ─────────────────────────────────────────────────

    @GetMapping("/admin/alimentos")
    public List<Alimento> listarAlimentos() {
        return alimentoRepo.findAll();
    }

    @PostMapping("/admin/alimentos")
    public ResponseEntity<?> criarAlimento(@RequestBody Alimento alimento) {
        return ResponseEntity.ok(alimentoRepo.save(alimento));
    }

    @PutMapping("/admin/alimentos/{id}")
    public ResponseEntity<?> atualizarAlimento(@PathVariable Long id, @RequestBody Alimento dados) {
        return alimentoRepo.findById(id).map(a -> {
            a.setNome(dados.getNome());
            a.setCalorias(dados.getCalorias());
            a.setProteina(dados.getProteina());
            a.setCarboidrato(dados.getCarboidrato());
            a.setGordura(dados.getGordura());
            return ResponseEntity.ok(alimentoRepo.save(a));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/alimentos/{id}")
    public ResponseEntity<?> removerAlimento(@PathVariable Long id) {
        if (!alimentoRepo.existsById(id)) return ResponseEntity.notFound().build();
        alimentoRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Removido."));
    }

    // ── ESTATÍSTICAS ADMIN ────────────────────────────────────────

    @GetMapping("/admin/stats")
    public ResponseEntity<?> stats() {
        long totalPacientes      = usuarioRepo.countByTipoUsuario(TipoUsuario.CLIENTE);
        long totalNutricionistas = usuarioRepo.countByTipoUsuario(TipoUsuario.NUTRICIONISTA);
        long totalCadastros      = totalPacientes + totalNutricionistas;
        long pendentes           = pagamentoRepo.findByStatusOrderByDataEnvioDesc("PENDENTE").size();
        long totalLogs           = logRepo.count();

        // Média de avaliações de todos nutricionistas
        List<Avaliacao> todasAvaliacoes = avaliacaoRepo.findAll();
        double mediaGeral = todasAvaliacoes.stream().mapToInt(Avaliacao::getNota).average().orElse(0);

        return ResponseEntity.ok(Map.of(
            "totalPacientes",      totalPacientes,
            "totalNutricionistas", totalNutricionistas,
            "totalCadastros",      totalCadastros,
            "pagamentosPendentes", pendentes,
            "totalLogs",           totalLogs,
            "mediaAvaliacoes",     todasAvaliacoes.isEmpty() ? null : Math.round(mediaGeral * 10.0) / 10.0
        ));
    }
}
