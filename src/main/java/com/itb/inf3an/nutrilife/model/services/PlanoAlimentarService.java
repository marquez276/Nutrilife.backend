package com.itb.inf3an.nutrilife.model.services;

import com.itb.inf3an.nutrilife.model.entity.*;
import com.itb.inf3an.nutrilife.model.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlanoAlimentarService {

    private static final Logger log = LoggerFactory.getLogger(PlanoAlimentarService.class);

    @Autowired private AnamneseRepository anamneseRepository;
    @Autowired private PlanoAlimentarRepository planoRepository;
    @Autowired private RefeicaoRepository refeicaoRepository;
    @Autowired private AlimentoRepository alimentoRepository;

    private final SecureRandom random = new SecureRandom();


    public PlanoAlimentar gerarPlano(Long usuarioId) {
        return gerarPlanoInterno(usuarioId, false);
    }

    public PlanoAlimentar regenerarPlano(Long usuarioId) {
        return gerarPlanoInterno(usuarioId, true);
    }

    private PlanoAlimentar gerarPlanoInterno(Long usuarioId, boolean forcar) {
        Anamnese anamnese = anamneseRepository.findByClienteId(usuarioId);
        if (anamnese == null) throw new RuntimeException("Anamnese nÃ£o encontrada para o usuÃ¡rio " + usuarioId);

        Cliente cliente = anamnese.getCliente();
        if (cliente == null) throw new RuntimeException("Cliente nÃ£o encontrado");

        String hashAtual = gerarHashAnamnese(anamnese);

        var planoExistente = planoRepository.findTopByClienteIdOrderByDataInicioDesc(usuarioId);
        // Reutiliza plano se: mesma anamnese, menos de 7 dias e nÃ£o Ã© forÃ§ado
        if (!forcar && planoExistente.isPresent()) {
            PlanoAlimentar p = planoExistente.get();
            boolean mesmaAnamnese = hashAtual.equals(p.getAnamneseHash());
            boolean dentroDosPrazo = p.getDataInicio() != null &&
                !p.getDataInicio().plusDays(7).isBefore(LocalDate.now());
            if (mesmaAnamnese && dentroDosPrazo) {
                log.info("Plano existente retornado para usuarioId={}", usuarioId);
                return p;
            }
        }

        PlanoAlimentar plano = new PlanoAlimentar();
        plano.setCliente(cliente);
        plano.setDataInicio(LocalDate.now());
        plano.setAnamneseHash(hashAtual);
        plano = planoRepository.save(plano);

        Perfil perfil = new Perfil(anamnese);
        log.info("Gerando plano para usuarioId={} | calorias={} | objetivo={} | restricoes={}",
            usuarioId, String.format("%.0f", perfil.calorias), perfil.objetivo, perfil.restricoes);

        montarRefeicoes(plano, perfil);

        return planoRepository.findById(plano.getId()).orElse(plano);
    }

    private String gerarHashAnamnese(Anamnese a) {
        return String.format("%s|%s|%s|%s|%s|%s",
            a.getPeso(), a.getAltura(), a.getObjetivo(),
            a.getAtividade(), a.getRestricoes(), a.getComorbidades());
    }

    // â”€â”€ PERFIL: centraliza todas as flags do paciente â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private static class Perfil {
        final double calorias;
        final String objetivo;
        final String restricoes;
        final boolean semLactose;
        final boolean semGluten;
        final boolean vegano;
        final boolean vegetariano;
        final boolean emagrecimento;
        final boolean hipertrofia;
        final boolean diabetes;
        final boolean hipertensao;
        final boolean colesterol;

        Perfil(Anamnese a) {
            this.calorias   = calcularCalorias(a);
            this.objetivo   = a.getObjetivo() != null ? a.getObjetivo().toUpperCase() : "";
            this.restricoes = a.getRestricoes() != null ? a.getRestricoes().toLowerCase() : "";
            String comorbidades = a.getComorbidades() != null ? a.getComorbidades().toLowerCase() : "";

            this.semLactose   = restricoes.contains("lactose");
            this.semGluten    = restricoes.contains("glÃºten") || restricoes.contains("gluten");
            this.vegano       = restricoes.contains("vegano");
            this.vegetariano  = restricoes.contains("vegetariano") || vegano;
            this.emagrecimento = objetivo.contains("EMAGRECIMENTO");
            this.hipertrofia  = objetivo.contains("HIPERTROFIA");
            this.diabetes     = comorbidades.contains("diabetes");
            this.hipertensao  = comorbidades.contains("hipertensÃ£o") || comorbidades.contains("hipertensao");
            this.colesterol   = comorbidades.contains("colesterol");
        }

        private static double calcularCalorias(Anamnese a) {
            if (a.getPeso() == null || a.getAltura() == null || a.getIdade() == null) return 1800.0;
            double h = a.getAltura() > 3 ? a.getAltura() / 100.0 : a.getAltura();
            double tmb = "M".equalsIgnoreCase(a.getSexo())
                ? 10 * a.getPeso() + 6.25 * (h * 100) - 5 * a.getIdade() + 5
                : 10 * a.getPeso() + 6.25 * (h * 100) - 5 * a.getIdade() - 161;

            String ativ = a.getAtividade() != null ? a.getAtividade().toLowerCase() : "";
            double fator = ativ.startsWith("leve") ? 1.37
                : ativ.startsWith("moderado") ? 1.55
                : ativ.startsWith("intenso")  ? 1.72 : 1.2;

            double cal = tmb * fator;
            double imc = (a.getAltura() > 0) ? a.getPeso() / (h * h) : 22.0;
            String obj = a.getObjetivo() != null ? a.getObjetivo().toUpperCase() : "";

            if (obj.contains("EMAGRECIMENTO")) cal -= 500;
            else if (obj.contains("HIPERTROFIA")) cal += 500;
            if (imc > 30) cal -= 300;
            else if (imc < 18.5) cal += 300;

            return Math.max(cal, 1200); // mÃ­nimo seguro de 1200 kcal
        }
    }

    // â”€â”€ MONTA AS 4 REFEIÃ‡Ã•ES â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private void montarRefeicoes(PlanoAlimentar plano, Perfil p) {
        // distribuiÃ§Ã£o calÃ³rica: cafÃ© 25%, almoÃ§o 35%, lanche 15%, jantar 25%
        criarRefeicao("CafÃ© da ManhÃ£", p.calorias * 0.25, plano, p, "cafe");
        criarRefeicao("AlmoÃ§o",        p.calorias * 0.35, plano, p, "almoco");
        criarRefeicao("Lanche",        p.calorias * 0.15, plano, p, "lanche");
        criarRefeicao("Jantar",        p.calorias * 0.25, plano, p, "jantar");
    }

    private void criarRefeicao(String nome, double caloriasAlvo, PlanoAlimentar plano, Perfil p, String tipo) {
        Refeicao r = new Refeicao();
        r.setNome(nome);
        r.setPlano(plano);
        r = refeicaoRepository.save(r);

        List<Alimento> itens = montarItens(tipo, caloriasAlvo, p);
        for (Alimento item : itens) {
            item.setRefeicao(r);
            alimentoRepository.save(item);
        }
    }

    // â”€â”€ MONTAGEM DE ITENS POR REFEIÃ‡ÃƒO â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private List<Alimento> montarItens(String tipo, double caloriasAlvo, Perfil p) {
        return switch (tipo) {
            case "cafe"   -> montarCafe(p);
            case "almoco" -> montarAlmoco(p);
            case "lanche" -> montarLanche(p);
            case "jantar" -> montarJantar(p);
            default       -> new ArrayList<>();
        };
    }

    // â”€â”€ CAFÃ‰ DA MANHÃƒ â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Estrutura: proteÃ­na + carboidrato + fruta + bebida
    // Regras: sem carbo pesado para diabÃ©ticos, sem lactose se restrito

    private List<Alimento> montarCafe(Perfil p) {
        List<Alimento> itens = new ArrayList<>();

        // 1. PROTEÃNA DO CAFÃ‰ (ovos ou alternativa vegana)
        if (!p.vegano) {
            itens.add(escolher(filtrar(List.of(
                alimento("Ovo mexido (2 unidades)",        140, 12, 1,  10),
                alimento("Ovo cozido (2 unidades)",        140, 12, 1,  10),
                alimento("Omelete simples (2 ovos)",       150, 13, 1,  11)
            ), p)));
        } else {
            // proteÃ­na vegana para cafÃ©
            itens.add(escolher(List.of(
                alimento("Pasta de amendoim (1 col. sopa)", 95,  4, 3,  8),
                alimento("Tofu firme (80g)",                64,  7, 1,  4),
                alimento("GrÃ£o-de-bico cozido (50g)",       82,  4, 14, 1)
            )));
        }

        // 2. CARBOIDRATO DO CAFÃ‰
        // diabÃ©ticos: sem pÃ£o branco, preferir aveia (baixo Ã­ndice glicÃªmico)
        // emagrecimento: porÃ§Ã£o menor
        if (!p.diabetes) {
            List<Alimento> carbos = filtrar(List.of(
                alimento("PÃ£o integral (2 fatias)",  160, 6, 28, 2, false, true),
                alimento("Tapioca (1 unidade)",      110, 1, 27, 0, false, false),
                alimento("Aveia (4 col. sopa)",      150, 5, 27, 3, false, true),
                alimento("Cuscuz (1 fatia mÃ©dia)",   120, 3, 25, 1, false, false)
            ), p);
            if (!carbos.isEmpty()) itens.add(p.emagrecimento
                ? alimento("Aveia (2 col. sopa)", 75, 3, 14, 2) // porÃ§Ã£o menor
                : escolher(carbos));
        } else {
            // diabÃ©tico: aveia com baixo IG, sem tapioca/pÃ£o branco
            List<Alimento> carbosDiabetes = filtrar(List.of(
                alimento("Aveia (4 col. sopa)",          150, 5, 27, 3, false, true),
                alimento("PÃ£o integral (1 fatia)",        80, 3, 14, 1, false, true)
            ), p);
            if (!carbosDiabetes.isEmpty()) itens.add(escolher(carbosDiabetes));
        }

        // 3. FRUTA â€” diabÃ©ticos evitam frutas de alto IG (banana, mamÃ£o)
        if (!p.diabetes) {
            itens.add(escolher(List.of(
                alimento("MamÃ£o (1 fatia mÃ©dia)",  45, 0, 11, 0),
                alimento("Banana (1 unidade)",     90, 1, 23, 0),
                alimento("MaÃ§Ã£ (1 unidade)",       80, 0, 21, 0),
                alimento("Morango (1 xÃ­cara)",     50, 1, 12, 0)
            )));
        } else {
            // frutas de baixo IG para diabÃ©ticos
            itens.add(escolher(List.of(
                alimento("MaÃ§Ã£ (1 unidade)",       80, 0, 21, 0),
                alimento("Pera (1 unidade)",        60, 0, 15, 0),
                alimento("Morango (1 xÃ­cara)",      50, 1, 12, 0)
            )));
        }

        // 4. BEBIDA â€” hipertensos: sem cafÃ© (cafeÃ­na eleva pressÃ£o)
        if (p.hipertensao) {
            itens.add(p.semLactose
                ? alimento("ChÃ¡ de ervas (200ml)",          0, 0, 0, 0)
                : alimento("Leite desnatado (200ml)",       70, 7, 10, 0, true, false));
        } else {
            itens.add(p.semLactose
                ? alimento("CafÃ© preto sem aÃ§Ãºcar (200ml)", 5, 0, 1, 0)
                : alimento("CafÃ© com leite desnatado (200ml)", 60, 4, 6, 1, true, false));
        }

        return itens;
    }

    // â”€â”€ ALMOÃ‡O â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Estrutura: proteÃ­na + carboidrato + leguminosa + salada + gordura boa
    // Regras: hipertensos sem sal extra, colesterol sem carne vermelha

    private List<Alimento> montarAlmoco(Perfil p) {
        List<Alimento> itens = new ArrayList<>();

        // 1. PROTEÃNA PRINCIPAL
        if (!p.vegetariano) {
            List<Alimento> proteinas = new ArrayList<>();
            // colesterol alto: sem carne vermelha
            if (!p.colesterol) {
                proteinas.add(alimento("Frango grelhado (150g)",    248, 46, 0,  5, false, false, true, true));
                proteinas.add(alimento("Carne bovina magra (150g)", 250, 38, 0, 10, false, false, true, true));
            }
            proteinas.add(alimento("Peixe grelhado (150g)",         180, 35, 0,  4, false, false, true, false));
            proteinas.add(alimento("Frango grelhado (150g)",        248, 46, 0,  5, false, false, true, true));
            itens.add(escolher(proteinas));
        } else {
            itens.add(escolher(List.of(
                alimento("Tofu grelhado (150g)",   120, 13, 3,  7),
                alimento("GrÃ£o-de-bico (100g)",    164,  9, 27, 3),
                alimento("Lentilha cozida (100g)", 116,  9, 20, 0),
                alimento("FeijÃ£o preto (1 concha)",  80,  5, 15, 0)
            )));
        }

        // 2. CARBOIDRATO â€” diabÃ©ticos: sem arroz branco, preferir integral
        if (!p.emagrecimento) {
            if (!p.diabetes) {
                List<Alimento> carbos = filtrar(List.of(
                    alimento("Arroz integral (4 col. sopa)", 168, 4, 36, 1),
                    alimento("Arroz branco (4 col. sopa)",   160, 3, 35, 0),
                    alimento("Batata-doce cozida (130g)",    112, 2, 26, 0),
                    alimento("MacarrÃ£o integral (100g)",     160, 6, 31, 1, false, true)
                ), p);
                if (!carbos.isEmpty()) itens.add(escolher(carbos));
            } else {
                // diabÃ©tico: sÃ³ opÃ§Ãµes de baixo IG
                itens.add(escolher(List.of(
                    alimento("Arroz integral (3 col. sopa)", 126, 3, 27, 1),
                    alimento("Batata-doce cozida (100g)",     86, 2, 20, 0)
                )));
            }
        } else {
            // emagrecimento: porÃ§Ã£o reduzida de carboidrato
            itens.add(alimento("Batata-doce cozida (80g)", 69, 1, 16, 0));
        }

        // 3. LEGUMINOSA â€” fonte de fibra e proteÃ­na vegetal
        if (!p.vegetariano) {
            itens.add(escolher(List.of(
                alimento("FeijÃ£o carioca (1 concha)", 76, 5, 14, 0),
                alimento("FeijÃ£o preto (1 concha)",   80, 5, 15, 0),
                alimento("Lentilha (1 concha)",       90, 7, 16, 0)
            )));
        }

        // 4. SALADA â€” sempre presente, boa para todos os perfis
        itens.add(escolher(List.of(
            alimento("Salada verde mista (Ã  vontade)",    20, 2, 3, 0),
            alimento("Tomate + alface + pepino",          25, 1, 5, 0),
            alimento("RÃºcula com tomate cereja",          20, 2, 3, 0)
        )));

        // 5. GORDURA BOA â€” colesterol: azeite em vez de manteiga
        itens.add(alimento("Azeite de oliva extravirgem (1 col. sopa)", 88, 0, 0, 10));

        return itens;
    }

    // â”€â”€ LANCHE â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Estrutura: proteÃ­na leve + fruta ou carboidrato simples
    // Regras: NUNCA refeiÃ§Ã£o pesada, sem aÃ§Ãºcar para diabÃ©ticos

    private List<Alimento> montarLanche(Perfil p) {
        List<Alimento> itens = new ArrayList<>();

        if (p.hipertrofia) {
            // lanche reforÃ§ado: proteÃ­na + carboidrato + gordura boa
            if (!p.semLactose) {
                itens.add(alimento("Iogurte grego natural (170g)", 130, 12, 6, 6, true, false));
            } else {
                itens.add(alimento("Amendoim torrado sem sal (30g)", 170, 8, 5, 14));
            }
            // fruta para energia
            if (!p.diabetes) {
                itens.add(escolher(List.of(
                    alimento("Banana (1 unidade)",  90, 1, 23, 0),
                    alimento("Manga (1 fatia)",     65, 1, 17, 0)
                )));
            } else {
                itens.add(alimento("MaÃ§Ã£ (1 unidade)", 80, 0, 21, 0));
            }
            // carboidrato extra para hipertrofia (sem glÃºten se necessÃ¡rio)
            if (!p.semGluten) {
                itens.add(alimento("PÃ£o integral (1 fatia)", 80, 3, 14, 1, false, true));
            } else {
                itens.add(alimento("Batata-doce cozida (80g)", 69, 1, 16, 0));
            }

        } else if (p.emagrecimento) {
            // lanche leve: fruta + gordura boa (sem carboidrato extra)
            if (!p.diabetes) {
                itens.add(escolher(List.of(
                    alimento("MaÃ§Ã£ (1 unidade)",     80, 0, 21, 0),
                    alimento("Pera (1 unidade)",      60, 0, 15, 0),
                    alimento("Cenoura baby (100g)",   41, 1,  9, 0),
                    alimento("Pepino fatiado (100g)", 16, 1,  4, 0)
                )));
            } else {
                // diabÃ©tico emagrecendo: fruta de baixo IG + proteÃ­na
                itens.add(alimento("MaÃ§Ã£ (1 unidade)", 80, 0, 21, 0));
            }
            itens.add(alimento("Castanha-do-parÃ¡ (2 unidades)", 60, 1, 1, 6));

        } else {
            // lanche padrÃ£o: proteÃ­na leve + fruta
            if (!p.semLactose) {
                itens.add(alimento("Iogurte natural desnatado (170g)", 100, 6, 8, 3, true, false));
            } else if (!p.vegano) {
                // substituto proteico sem lactose
                itens.add(alimento("Ovo cozido (1 unidade)", 70, 6, 1, 5));
            } else {
                itens.add(alimento("Amendoim torrado sem sal (20g)", 113, 5, 3, 10));
            }
            if (!p.diabetes) {
                itens.add(escolher(List.of(
                    alimento("Banana (1 unidade)", 90, 1, 23, 0),
                    alimento("MaÃ§Ã£ (1 unidade)",   80, 0, 21, 0),
                    alimento("Pera (1 unidade)",    60, 0, 15, 0)
                )));
            } else {
                itens.add(alimento("MaÃ§Ã£ (1 unidade)", 80, 0, 21, 0));
            }
        }

        return itens;
    }

    // â”€â”€ JANTAR â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Estrutura: proteÃ­na leve + legume + carboidrato moderado (sem para emagrecimento)
    // Regras: mais leve que almoÃ§o, sem frituras, colesterol sem carne vermelha

    private List<Alimento> montarJantar(Perfil p) {
        List<Alimento> itens = new ArrayList<>();

        // 1. PROTEÃNA LEVE â€” jantar deve ser mais leve que almoÃ§o
        if (!p.vegetariano) {
            List<Alimento> proteinas = new ArrayList<>();
            proteinas.add(alimento("Peixe grelhado (120g)",   144, 28, 0, 3, false, false, true, false));
            if (!p.colesterol) {
                proteinas.add(alimento("Frango grelhado (120g)", 200, 37, 0, 4, false, false, true, true));
            }
            proteinas.add(alimento("Omelete (2 ovos)",         140, 12, 1, 10, false, false, true, false));
            itens.add(escolher(proteinas));
        } else {
            itens.add(escolher(List.of(
                alimento("Tofu grelhado (120g)",          96, 10, 2, 6),
                alimento("Omelete de legumes (2 ovos)",  140, 12, 4, 9, false, false, true, false),
                alimento("GrÃ£o-de-bico temperado (80g)", 131,  7, 22, 2)
            )));
        }

        // 2. LEGUMES â€” sempre presentes no jantar
        itens.add(escolher(List.of(
            alimento("BrÃ³colis no vapor (90g)",    31, 3, 6, 0),
            alimento("Abobrinha refogada (100g)",  17, 1, 3, 0),
            alimento("Cenoura cozida (80g)",       33, 1, 8, 0),
            alimento("Espinafre refogado (80g)",   18, 2, 3, 0),
            alimento("Couve-flor no vapor (90g)",  23, 2, 5, 0)
        )));

        // 3. CARBOIDRATO â€” apenas para manutenÃ§Ã£o e hipertrofia, nÃ£o para emagrecimento
        if (!p.emagrecimento) {
            if (!p.diabetes) {
                itens.add(escolher(List.of(
                    alimento("Batata-doce cozida (100g)",    86, 2, 20, 0),
                    alimento("Arroz integral (3 col. sopa)", 126, 3, 27, 1),
                    alimento("Mandioca cozida (100g)",       125, 1, 30, 0),
                    alimento("Inhame cozido (100g)",          97, 2, 22, 0)
                )));
            } else {
                // diabÃ©tico: sÃ³ batata-doce (baixo IG)
                itens.add(alimento("Batata-doce cozida (80g)", 69, 1, 16, 0));
            }
        }

        // 4. GORDURA BOA â€” sempre presente
        itens.add(alimento("Azeite de oliva (1 col. chÃ¡)", 44, 0, 0, 5));

        return itens;
    }

    // â”€â”€ HELPERS â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private List<Alimento> filtrar(List<Alimento> lista, Perfil p) {
        return lista.stream()
            .filter(a -> !(p.semLactose  && a.isTemLactose()))
            .filter(a -> !(p.semGluten   && a.isTemGluten()))
            .filter(a -> !(p.vegano      && a.isTemAnimal()))
            .filter(a -> !(p.vegetariano && a.isTemCarne()))
            .collect(Collectors.toList());
    }

    private Alimento escolher(List<Alimento> lista) {
        if (lista.isEmpty()) return alimento("Fruta da estaÃ§Ã£o", 60, 1, 15, 0);
        return lista.get(random.nextInt(lista.size()));
    }

    // factories de Alimento

    private Alimento alimento(String nome, double cal, double prot, double carbo, double gord) {
        Alimento a = new Alimento();
        a.setNome(nome); a.setCalorias(cal); a.setProteina(prot);
        a.setCarboidrato(carbo); a.setGordura(gord);
        return a;
    }

    private Alimento alimento(String nome, double cal, double prot, double carbo, double gord,
                               boolean temLactose, boolean temGluten) {
        Alimento a = alimento(nome, cal, prot, carbo, gord);
        a.setTemLactose(temLactose); a.setTemGluten(temGluten);
        return a;
    }

    private Alimento alimento(String nome, double cal, double prot, double carbo, double gord,
                               boolean temLactose, boolean temGluten, boolean temAnimal, boolean temCarne) {
        Alimento a = alimento(nome, cal, prot, carbo, gord, temLactose, temGluten);
        a.setTemAnimal(temAnimal); a.setTemCarne(temCarne);
        return a;
    }
}

