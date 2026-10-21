package com.itb.inf3an.nutrilife.controller;

import com.itb.inf3an.nutrilife.model.entity.*;
import com.itb.inf3an.nutrilife.model.enums.TipoUsuario;
import com.itb.inf3an.nutrilife.model.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import com.itb.inf3an.nutrilife.exceptions.BadRequest;
import com.itb.inf3an.nutrilife.security.config.Authz;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import com.itb.inf3an.nutrilife.model.services.Auditoria;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired private LogAtividadeRepository logRepo;
    @Autowired private AvaliacaoRepository avaliacaoRepo;
    @Autowired private NutriPerfilRepository nutriPerfilRepo;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private AdminRepository adminRepo;
    @Autowired private AlimentoRepository alimentoRepo;
    @Autowired private AnamneseRepository anamneseRepo;
    @Autowired private Authz authz;
    @Autowired private ConfiguracaoRepository configRepo;
    @Autowired private ConsultaRepository consultaRepo;
    @Autowired private Auditoria auditoria;

    private static final String CFG_DIAS_INATIVIDADE = "diasInatividade";

    private int diasInatividade() {
        return configRepo.findById(CFG_DIAS_INATIVIDADE).map(c -> Integer.parseInt(c.getValor())).orElse(30);
    }

    private Usuario atual() {
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof Usuario u ? u : null;
    }

    // Sem acesso há mais de N dias (se nunca acessou, conta a partir do cadastro)
    private boolean inativo(Usuario u, int dias) {
        LocalDateTime ref = u.getUltimoAcesso() != null ? u.getUltimoAcesso() : u.getDataCadastro();
        return ref != null && ref.isBefore(LocalDateTime.now().minusDays(dias));
    }

    private Map<String, Object> resumo(Usuario u, int dias) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("nome", u.getNomeCompleto());
        m.put("email", u.getEmail());
        m.put("telefone", u.getTelefone());
        m.put("ativo", u.isCodStatus());
        m.put("dataCadastro", u.getDataCadastro());
        m.put("ultimoAcesso", u.getUltimoAcesso());
        m.put("inativo", inativo(u, dias));
        return m;
    }

    @GetMapping("/admin/usuarios")
    public ResponseEntity<?> listarUsuarios() {
        int dias = diasInatividade();
        List<Map<String, Object>> result = usuarioRepo.findByTipoUsuario(TipoUsuario.CLIENTE).stream().map(u -> {
            Map<String, Object> m = resumo(u, dias);
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
        int dias = diasInatividade();
        List<Map<String, Object>> result = usuarioRepo.findByTipoUsuario(TipoUsuario.NUTRICIONISTA).stream().map(u -> {
            Map<String, Object> m = resumo(u, dias);
            m.put("crn", u.getCrn());
            List<Avaliacao> avs = avaliacaoRepo.findByNutricionistaId(u.getId());
            double media = avs.stream().mapToInt(Avaliacao::getNota).average().orElse(0);
            m.put("mediaAvaliacoes", avs.isEmpty() ? null : Math.round(media * 10.0) / 10.0);
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/admin/crescimento")
    public ResponseEntity<?> crescimento() {
        // Total acumulado de pacientes e nutricionistas (separados) ao fim de cada um dos últimos 6 meses
        List<Usuario> pacientes = usuarioRepo.findByTipoUsuario(TipoUsuario.CLIENTE);
        List<Usuario> nutris = usuarioRepo.findByTipoUsuario(TipoUsuario.NUTRICIONISTA);
        YearMonth now = YearMonth.now();
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            LocalDateTime fim = ym.plusMonths(1).atDay(1).atStartOfDay();
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("month", ym.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, new java.util.Locale("pt", "BR")));
            entry.put("pacientes", pacientes.stream().filter(u -> u.getDataCadastro() != null && u.getDataCadastro().isBefore(fim)).count());
            entry.put("nutricionistas", nutris.stream().filter(u -> u.getDataCadastro() != null && u.getDataCadastro().isBefore(fim)).count());
            data.add(entry);
        }
        return ResponseEntity.ok(data);
    }

    @GetMapping("/admin/verificar-crn")
    public ResponseEntity<?> verificarCrn(@RequestParam String crn) {
        boolean existe = usuarioRepo.findByTipoUsuario(TipoUsuario.NUTRICIONISTA)
            .stream().anyMatch(u -> crn.equalsIgnoreCase(u.getCrn()));
        return ResponseEntity.ok(Map.of("disponivel", !existe));
    }

    @GetMapping("/admin/configuracoes")
    public ResponseEntity<?> getConfiguracoes() {
        return ResponseEntity.ok(Map.of("diasInatividade", diasInatividade()));
    }

    @PutMapping("/admin/configuracoes")
    public ResponseEntity<?> salvarConfiguracoes(@RequestBody Map<String, Object> body) {
        int dias;
        try {
            dias = Integer.parseInt(String.valueOf(body.get("diasInatividade")));
        } catch (NumberFormatException e) {
            throw new BadRequest("Informe um número de dias válido.");
        }
        if (dias < 1 || dias > 3650) throw new BadRequest("Dias de inatividade deve ficar entre 1 e 3650.");
        Configuracao c = new Configuracao();
        c.setChave(CFG_DIAS_INATIVIDADE);
        c.setValor(String.valueOf(dias));
        configRepo.save(c);
        auditoria.registrar("Configuração alterada: inatividade = " + dias + " dias");
        return ResponseEntity.ok(Map.of("diasInatividade", dias, "message", "Configurações salvas."));
    }

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

    /** Edita o próprio perfil do admin logado. Trocar a senha exige a senha atual. */
    @PutMapping("/admin/perfil")
    public ResponseEntity<?> atualizarPerfil(@RequestBody Map<String, String> body) {
        Admin admin = adminRepo.findById(atual().getId()).orElseThrow(() -> new BadRequest("Admin não encontrado."));
        String nome = body.get("nome") == null ? "" : body.get("nome").trim();
        if (nome.isEmpty() || nome.length() > 100) throw new BadRequest("Nome é obrigatório (máx. 100 caracteres).");
        admin.setNomeCompleto(nome);
        admin.setTelefone(body.get("telefone"));
        String nova = body.get("novaSenha");
        if (nova != null && !nova.isEmpty()) {
            if (nova.length() < 6) throw new BadRequest("A nova senha deve ter ao menos 6 caracteres.");
            if (!encoder.matches(String.valueOf(body.get("senhaAtual")), admin.getSenha()))
                throw new BadRequest("Senha atual incorreta.");
            admin.setSenha(encoder.encode(nova));
        }
        adminRepo.save(admin);
        auditoria.registrar("Perfil de administrador atualizado");
        return ResponseEntity.ok(Map.of("nome", admin.getNomeCompleto(), "telefone", admin.getTelefone() == null ? "" : admin.getTelefone()));
    }

    // ── Funcionários (admins) ─────────────────────────────────────────
    @GetMapping("/admin/funcionarios")
    public ResponseEntity<?> listarFuncionarios() {
        int dias = diasInatividade();
        return ResponseEntity.ok(adminRepo.findAll().stream().map(a -> {
            Map<String, Object> m = resumo(a, dias);
            m.put("principal", a.isPrincipal());
            m.put("aprovado", a.isAprovado());
            return m;
        }).collect(Collectors.toList()));
    }

    /** Admin cadastra novo funcionário/admin: já nasce ativo e aprovado. */
    @PostMapping("/admin/funcionarios")
    public ResponseEntity<?> cadastrarFuncionario(@RequestBody Map<String, String> body) {
        String nome = body.getOrDefault("nome", "").trim();
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        String senha = body.getOrDefault("senha", "");
        if (nome.isEmpty() || nome.length() > 100) throw new BadRequest("Nome é obrigatório (máx. 100 caracteres).");
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") || email.length() > 100) throw new BadRequest("E-mail inválido.");
        if (senha.length() < 6) throw new BadRequest("A senha deve ter ao menos 6 caracteres.");
        if (usuarioRepo.findByEmail(email).isPresent()) throw new BadRequest("E-mail já cadastrado.");

        Admin novo = new Admin();
        novo.setNomeCompleto(nome);
        novo.setEmail(email);
        novo.setTelefone(body.get("telefone"));
        novo.setSenha(encoder.encode(senha));
        novo.setCodStatus(true);
        novo.setAprovado(true);
        Admin salvo = adminRepo.save(novo);
        auditoria.registrar("Cadastro de funcionário/admin: " + nome);
        return ResponseEntity.ok(Map.of("id", salvo.getId(), "nome", salvo.getNomeCompleto(), "email", salvo.getEmail()));
    }

    /** Ativa/desativa qualquer conta (paciente, nutricionista ou admin), exceto a própria e o admin principal. */
    @PutMapping("/admin/usuarios/{id}/status")
    public ResponseEntity<?> alterarStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Usuario u = usuarioRepo.findById(id).orElse(null);
        if (u == null) return ResponseEntity.notFound().build();
        boolean ativo = Boolean.TRUE.equals(body.get("ativo"));
        if (u.getId().equals(atual().getId())) throw new BadRequest("Você não pode alterar o status da própria conta.");
        if (u instanceof Admin a && a.isPrincipal()) throw new BadRequest("O administrador principal não pode ser desativado.");
        u.setCodStatus(ativo);
        usuarioRepo.save(u);
        auditoria.registrar((ativo ? "Ativou" : "Desativou") + " a conta de " + u.getNomeCompleto());
        return ResponseEntity.ok(Map.of("id", u.getId(), "ativo", ativo));
    }

    @GetMapping("/admin/pendentes")
    public List<Admin> listarAdminsPendentes() {
        return adminRepo.findByAprovadoFalse();
    }

    @PutMapping("/admin/{id}/aprovar")
    public ResponseEntity<?> aprovarAdmin(@PathVariable Long id) {
        return adminRepo.findById(id).map(a -> {
            a.setAprovado(true);
            adminRepo.save(a);
            auditoria.registrar("Aprovou o admin " + a.getNomeCompleto());
            return ResponseEntity.ok(Map.of("message", "Admin aprovado."));
        }).orElse(ResponseEntity.notFound().build());
    }

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
        authz.check(avaliacao.getCliente() != null ? avaliacao.getCliente().getId() : null);
        if (avaliacao.getNutricionista() == null || avaliacao.getNutricionista().getId() == null
                || avaliacao.getNota() == null || avaliacao.getNota() < 1 || avaliacao.getNota() > 5) {
            throw new BadRequest("Nutricionista e nota (1 a 5) são obrigatórios.");
        }
        avaliacao.setId(null);
        avaliacao.setDataAvaliacao(java.time.LocalDate.now());
        Avaliacao salva = avaliacaoRepo.save(avaliacao);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id",            salva.getId());
        resp.put("nota",          salva.getNota());
        resp.put("comentario",    salva.getComentario());
        resp.put("dataAvaliacao", salva.getDataAvaliacao());
        resp.put("clienteNome",   salva.getCliente() != null ? salva.getCliente().getNomeCompleto() : "Paciente");
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/nutricionistas/perfis")
    public List<Map<String, Object>> listarPerfis() {
        return usuarioRepo.findByTipoUsuario(TipoUsuario.NUTRICIONISTA).stream()
            .map(u -> {
                Map<String, Object> nutri = new LinkedHashMap<>();
                nutri.put("id", u.getId());
                nutri.put("nomeCompleto", u.getNomeCompleto());
                nutri.put("email", u.getEmail());
                nutri.put("telefone", u.getTelefone());
                nutri.put("crn", u.getCrn());

                Map<String, Object> m = new LinkedHashMap<>();
                m.put("nutricionista", nutri);
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

    @PreAuthorize("@authz.self(#id)")
    @PostMapping("/nutricionistas/{id}/perfil")
    public ResponseEntity<?> salvarPerfil(@PathVariable Long id, @RequestBody NutricionistaPerfil perfil) {
        return usuarioRepo.findById(id).map(u -> {
            var existente = nutriPerfilRepo.findByNutricionistaId(id);
            NutricionistaPerfil p = existente.orElseGet(NutricionistaPerfil::new);
            if (!(u instanceof Nutricionista nutri)) return ResponseEntity.badRequest().body((Object) Map.of("message", "Usuário não é nutricionista."));
            if (!existente.isPresent()) p.setNutricionista(nutri);
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

    @GetMapping("/admin/alimentos")
    public List<Alimento> listarAlimentos() {
        // Catálogo: exclui itens de planos alimentares (refeicao) e alimentos customizados de pacientes
        return alimentoRepo.findByCustomizadoFalseAndRefeicaoIsNull();
    }

    private void validarAlimento(Alimento a) {
        if (a.getNome() == null || a.getNome().isBlank()) throw new BadRequest("Nome do alimento é obrigatório.");
        for (Double v : new Double[]{a.getCalorias(), a.getProteina(), a.getCarboidrato(), a.getGordura()}) {
            if (v == null || v < 0) throw new BadRequest("Valores nutricionais devem ser números maiores ou iguais a zero.");
        }
    }

    @PostMapping("/admin/alimentos")
    public ResponseEntity<?> criarAlimento(@RequestBody Alimento alimento) {
        validarAlimento(alimento);
        alimento.setId(null);
        alimento.setRefeicao(null);
        alimento.setClienteId(null);
        alimento.setCustomizado(false);
        Alimento salvo = alimentoRepo.save(alimento);
        auditoria.registrar("Adicionou o alimento " + salvo.getNome());
        return ResponseEntity.ok(salvo);
    }

    @PutMapping("/admin/alimentos/{id}")
    public ResponseEntity<?> atualizarAlimento(@PathVariable Long id, @RequestBody Alimento dados) {
        validarAlimento(dados);
        return alimentoRepo.findById(id).filter(a -> a.getRefeicao() == null && !a.isCustomizado()).map(a -> {
            a.setNome(dados.getNome());
            a.setCalorias(dados.getCalorias());
            a.setProteina(dados.getProteina());
            a.setCarboidrato(dados.getCarboidrato());
            a.setGordura(dados.getGordura());
            a.setAtivo(dados.isAtivo());
            auditoria.registrar("Editou o alimento " + a.getNome());
            return ResponseEntity.ok(alimentoRepo.save(a));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/alimentos/{id}")
    public ResponseEntity<?> removerAlimento(@PathVariable Long id) {
        return alimentoRepo.findById(id).filter(a -> a.getRefeicao() == null && !a.isCustomizado()).map(a -> {
            alimentoRepo.delete(a);
            auditoria.registrar("Removeu o alimento " + a.getNome());
            return ResponseEntity.ok((Object) Map.of("message", "Removido."));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<?> stats() {
        List<Usuario> nutris = usuarioRepo.findByTipoUsuario(TipoUsuario.NUTRICIONISTA);
        long totalPacientes      = usuarioRepo.countByTipoUsuario(TipoUsuario.CLIENTE);
        long totalNutricionistas = nutris.size();
        long nutricionistasAtivos = nutris.stream().filter(Usuario::isCodStatus).count();
        int dias = diasInatividade();
        long inativos = usuarioRepo.findAll().stream()
            .filter(u -> u.getTipoUsuario() != TipoUsuario.ADMIN && inativo(u, dias)).count();

        List<Avaliacao> todasAvaliacoes = avaliacaoRepo.findAll();
        double mediaGeral = todasAvaliacoes.stream().mapToInt(Avaliacao::getNota).average().orElse(0);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalPacientes",        totalPacientes);
        m.put("totalNutricionistas",   totalNutricionistas);
        m.put("nutricionistasAtivos",  nutricionistasAtivos);
        m.put("nutricionistasPendentes", totalNutricionistas - nutricionistasAtivos);
        m.put("totalCadastros",        totalPacientes + totalNutricionistas);
        m.put("usuariosInativos",      inativos);
        m.put("diasInatividade",       dias);
        m.put("totalConsultas",        consultaRepo.count());
        m.put("totalAlimentos",        alimentoRepo.findByCustomizadoFalseAndRefeicaoIsNull().size());
        m.put("totalAdmins",           adminRepo.count());
        m.put("totalLogs",             logRepo.count());
        m.put("mediaAvaliacoes",       todasAvaliacoes.isEmpty() ? null : Math.round(mediaGeral * 10.0) / 10.0);
        return ResponseEntity.ok(m);
    }
}

