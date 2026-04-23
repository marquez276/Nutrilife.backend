package com.itb.inf3an.nutrilife.service;

import java.util.ArrayList;
import java.util.List;

public class BancoAlimentos {

    private static AlimentoDB a(String nome, double kcal, double prot, double carbo, double gord,
                                 boolean veg, boolean vegano, boolean semLac, boolean semGlut,
                                 String ig, boolean diab, boolean hiper, boolean colest,
                                 String... ref) {
        return AlimentoDB.de(nome, kcal, prot, carbo, gord, veg, vegano, semLac, semGlut,
                             ig, diab, hiper, colest, ref);
    }

    // ── CARBOIDRATOS CAFÉ DA MANHÃ ─────────────────────────────────

    public static final List<AlimentoDB> CARBOS_CAFE = List.of(
        a("Pão integral (2 fatias)",             160, 6, 28, 2,  true, false, true,  false, "medio", false, true,  true,  "cafe"),
        a("Tapioca (1 unidade)",                 110, 1, 27, 0,  true, true,  true,  true,  "alto",  false, true,  true,  "cafe"),
        a("Aveia em flocos (4 col. sopa)",       150, 5, 27, 3,  true, true,  true,  false, "baixo", true,  true,  true,  "cafe"),
        a("Cuscuz de milho (1 fatia)",           120, 3, 25, 1,  true, true,  true,  true,  "medio", false, true,  true,  "cafe"),
        a("Beiju de tapioca (1 unidade)",         90, 1, 22, 0,  true, true,  true,  true,  "alto",  false, true,  true,  "cafe"),
        a("Mingau de aveia (200ml)",             180, 6, 32, 3,  true, false, false, false, "baixo", true,  true,  true,  "cafe"),
        a("Pão de queijo (2 unidades)",          200, 5, 28, 8,  true, false, false, true,  "alto",  false, false, false, "cafe"),
        a("Biscoito de arroz (4 unidades)",       80, 2, 18, 0,  true, true,  true,  true,  "medio", false, true,  true,  "cafe"),
        a("Granola sem açúcar (3 col. sopa)",    130, 3, 22, 4,  true, true,  true,  false, "medio", false, true,  true,  "cafe"),
        a("Torrada integral (3 unidades)",       120, 4, 22, 2,  true, false, true,  false, "medio", false, true,  true,  "cafe"),
        a("Panqueca de aveia (2 unidades)",      180, 8, 24, 5,  true, false, false, false, "medio", false, true,  true,  "cafe"),
        a("Crepioca (1 unidade)",                130, 8, 18, 3,  true, false, true,  true,  "medio", false, true,  true,  "cafe"),
        a("Pão de batata-doce (1 fatia)",        110, 3, 22, 1,  true, true,  true,  true,  "baixo", true,  true,  true,  "cafe"),
        a("Muffin de aveia e banana (1 un.)",    170, 4, 30, 4,  true, true,  true,  false, "medio", false, true,  true,  "cafe"),
        a("Pão sem glúten (2 fatias)",           140, 2, 30, 2,  true, true,  true,  true,  "medio", false, true,  true,  "cafe"),
        a("Bolo de banana sem açúcar (1 fatia)", 140, 3, 28, 2,  true, true,  true,  false, "medio", false, true,  true,  "cafe"),
        a("Waffle integral (1 unidade)",         160, 5, 26, 4,  true, false, false, false, "medio", false, true,  true,  "cafe"),
        a("Canjica de milho (200ml)",            180, 4, 36, 2,  true, false, false, true,  "medio", false, true,  true,  "cafe"),
        a("Pão francês (1 unidade)",             135, 4, 27, 1,  true, true,  true,  false, "alto",  false, true,  true,  "cafe"),
        a("Batata-doce cozida (80g) - café",      69, 1, 16, 0,  true, true,  true,  true,  "baixo", true,  true,  true,  "cafe")
    );

    // ── BEBIDAS CAFÉ DA MANHÃ ──────────────────────────────────────

    public static final List<AlimentoDB> BEBIDAS_CAFE = List.of(
        a("Café preto sem açúcar (200ml)",           5, 0,  1, 0,  true, true,  true,  true,  "baixo", true,  false, true,  "cafe"),
        a("Café com leite desnatado (200ml)",        60, 4,  6, 1,  true, false, false, true,  "baixo", true,  false, true,  "cafe"),
        a("Leite desnatado (200ml)",                 70, 7, 10, 0,  true, false, false, true,  "baixo", true,  true,  true,  "cafe"),
        a("Leite de aveia (200ml)",                  80, 2, 14, 2,  true, true,  true,  false, "medio", false, true,  true,  "cafe"),
        a("Leite de amêndoas sem açúcar (200ml)",    30, 1,  1, 3,  true, true,  true,  true,  "baixo", true,  true,  true,  "cafe"),
        a("Leite de coco light (200ml)",             50, 1,  3, 4,  true, true,  true,  true,  "baixo", true,  true,  false, "cafe"),
        a("Chá verde sem açúcar (200ml)",             2, 0,  0, 0,  true, true,  true,  true,  "baixo", true,  true,  true,  "cafe"),
        a("Chá de camomila (200ml)",                  2, 0,  0, 0,  true, true,  true,  true,  "baixo", true,  true,  true,  "cafe"),
        a("Suco de laranja natural (150ml)",         70, 1, 17, 0,  true, true,  true,  true,  "alto",  false, true,  true,  "cafe"),
        a("Iogurte líquido desnatado (200ml)",       90, 8, 12, 0,  true, false, false, true,  "baixo", true,  true,  true,  "cafe"),
        a("Vitamina de banana com leite (200ml)",   160, 6, 30, 2,  true, false, false, true,  "medio", false, true,  true,  "cafe"),
        a("Leite semidesnatado (200ml)",             90, 6,  9, 3,  true, false, false, true,  "baixo", false, true,  false, "cafe")
    );

    // ── CARBOIDRATOS ALMOÇO/JANTAR ─────────────────────────────────

    public static final List<AlimentoDB> CARBOS_REFEICAO = List.of(
        a("Arroz integral (4 col. sopa)",   168, 4, 36, 1,  true, true,  true, true,  "baixo", true,  true,  true,  "almoco", "jantar"),
        a("Arroz branco (4 col. sopa)",     160, 3, 35, 0,  true, true,  true, true,  "alto",  false, true,  true,  "almoco", "jantar"),
        a("Batata-doce cozida (130g)",      112, 2, 26, 0,  true, true,  true, true,  "baixo", true,  true,  true,  "almoco", "jantar"),
        a("Mandioca cozida (100g)",         160, 1, 38, 0,  true, true,  true, true,  "alto",  false, true,  true,  "almoco", "jantar"),
        a("Inhame cozido (100g)",           118, 2, 28, 0,  true, true,  true, true,  "baixo", true,  true,  true,  "almoco", "jantar"),
        a("Macarrão integral (100g coz.)",  124, 5, 26, 1,  true, true,  true, false, "medio", false, true,  true,  "almoco", "jantar"),
        a("Macarrão comum (100g coz.)",     157, 5, 31, 1,  true, true,  true, false, "alto",  false, true,  true,  "almoco", "jantar"),
        a("Batata inglesa cozida (100g)",    77, 2, 17, 0,  true, true,  true, true,  "alto",  false, true,  true,  "almoco", "jantar"),
        a("Cará cozido (100g)",              97, 2, 23, 0,  true, true,  true, true,  "baixo", true,  true,  true,  "almoco", "jantar"),
        a("Polenta (100g)",                  70, 2, 15, 1,  true, true,  true, true,  "medio", false, true,  true,  "almoco", "jantar"),
        a("Quinoa cozida (100g)",           120, 4, 21, 2,  true, true,  true, true,  "baixo", true,  true,  true,  "almoco", "jantar")
    );

    // ── PROTEÍNAS ANIMAIS ──────────────────────────────────────────

    public static final List<AlimentoDB> PROTEINAS_ANIMAIS = List.of(
        a("Frango grelhado (150g)",       248, 46,  0,  5, false, false, true, true,  "baixo", true,  true,  true,  "almoco", "jantar"),
        a("Carne bovina magra (150g)",    375, 39,  0, 23, false, false, true, true,  "baixo", true,  false, false, "almoco", "jantar"),
        a("Tilápia grelhada (150g)",      144, 30,  0,  3, false, false, true, true,  "baixo", true,  true,  true,  "almoco", "jantar"),
        a("Salmão grelhado (120g)",       250, 24,  0, 16, false, false, true, true,  "baixo", true,  true,  true,  "almoco", "jantar"),
        a("Atum em água (100g)",          132, 28,  0,  1, false, false, true, true,  "baixo", true,  true,  true,  "almoco", "jantar", "lanche"),
        a("Sardinha grelhada (100g)",     208, 25,  0, 11, false, false, true, true,  "baixo", true,  true,  true,  "almoco", "jantar"),
        a("Camarão grelhado (100g)",       99, 24,  0,  1, false, false, true, true,  "baixo", true,  false, true,  "almoco", "jantar"),
        a("Ovo cozido (2 unidades)",      140, 12,  0, 10, true,  false, true, true,  "baixo", true,  true,  false, "cafe", "lanche", "jantar"),
        a("Peito de peru (100g)",         135, 29,  0,  1, false, false, true, true,  "baixo", true,  true,  true,  "almoco", "jantar", "lanche"),
        a("Lombo suíno grelhado (100g)",  242, 27,  0, 14, false, false, true, true,  "baixo", true,  false, false, "almoco", "jantar"),
        a("Omelete simples (2 ovos)",     150, 12,  1, 11, true,  false, true, true,  "baixo", true,  true,  false, "cafe", "jantar"),
        a("Frango desfiado (100g)",       165, 31,  0,  4, false, false, true, true,  "baixo", true,  true,  true,  "almoco", "jantar", "lanche"),
        a("Bacalhau cozido (120g)",       160, 35,  0,  1, false, false, true, true,  "baixo", true,  false, true,  "almoco", "jantar"),
        a("Peito de peru fatiado (50g)",   68, 15,  0,  1, false, false, true, true,  "baixo", true,  false, true,  "cafe", "lanche")
    );

    // ── PROTEÍNAS VEGETAIS ─────────────────────────────────────────

    public static final List<AlimentoDB> PROTEINAS_VEGETAIS = List.of(
        a("Tofu grelhado (150g)",          114, 12,  3,  6, true, true,  true, true,  "baixo", true,  true,  true,  "almoco", "jantar"),
        a("Grão-de-bico cozido (100g)",    164,  9, 27,  3, true, true,  true, true,  "baixo", true,  true,  true,  "almoco", "jantar", "lanche"),
        a("Lentilha cozida (100g)",        116,  9, 20,  0, true, true,  true, true,  "baixo", true,  true,  true,  "almoco", "jantar"),
        a("Feijão preto (1 concha)",        77,  5, 14,  0, true, true,  true, true,  "baixo", true,  true,  true,  "almoco", "jantar"),
        a("Feijão carioca (1 concha)",      76,  5, 13,  0, true, true,  true, true,  "baixo", true,  true,  true,  "almoco", "jantar"),
        a("Ervilha cozida (100g)",          81,  5, 14,  0, true, true,  true, true,  "baixo", true,  true,  true,  "almoco", "jantar"),
        a("Edamame (100g)",                121, 11, 10,  5, true, true,  true, true,  "baixo", true,  true,  true,  "almoco", "jantar", "lanche"),
        a("Fava cozida (100g)",            110,  8, 19,  0, true, true,  true, true,  "baixo", true,  true,  true,  "almoco", "jantar"),
        a("Tempeh (100g)",                 193, 20,  9, 11, true, true,  true, true,  "baixo", true,  true,  true,  "almoco", "jantar"),
        a("Pasta de amendoim (30g)",       190,  8,  6, 16, true, true,  true, true,  "baixo", true,  true,  false, "cafe", "lanche"),
        a("Amendoim torrado sem sal (30g)",170,  7,  6, 14, true, true,  true, true,  "baixo", true,  true,  false, "lanche"),
        a("Quinoa cozida (100g)",          120,  4, 21,  2, true, true,  true, true,  "baixo", true,  true,  true,  "almoco", "jantar")
    );

    // ── LEGUMES E VERDURAS ─────────────────────────────────────────

    public static final List<AlimentoDB> LEGUMES = List.of(
        a("Brócolis no vapor (100g)",       35, 3,  7, 0, true, true, true, true, "baixo", true, true, true, "almoco", "jantar"),
        a("Couve-flor no vapor (100g)",     25, 2,  5, 0, true, true, true, true, "baixo", true, true, true, "almoco", "jantar"),
        a("Abobrinha refogada (100g)",      17, 1,  3, 0, true, true, true, true, "baixo", true, true, true, "almoco", "jantar"),
        a("Cenoura cozida (100g)",          41, 1, 10, 0, true, true, true, true, "baixo", true, true, true, "almoco", "jantar", "lanche"),
        a("Espinafre refogado (100g)",      23, 3,  4, 0, true, true, true, true, "baixo", true, true, true, "almoco", "jantar"),
        a("Couve refogada (100g)",          49, 4,  9, 1, true, true, true, true, "baixo", true, true, true, "almoco", "jantar"),
        a("Beterraba cozida (100g)",        43, 2, 10, 0, true, true, true, true, "medio", false, true, true, "almoco", "jantar"),
        a("Chuchu cozido (100g)",           19, 1,  4, 0, true, true, true, true, "baixo", true, true, true, "almoco", "jantar"),
        a("Vagem cozida (100g)",            31, 2,  7, 0, true, true, true, true, "baixo", true, true, true, "almoco", "jantar"),
        a("Berinjela grelhada (100g)",      25, 1,  6, 0, true, true, true, true, "baixo", true, true, true, "almoco", "jantar"),
        a("Tomate (100g)",                  18, 1,  4, 0, true, true, true, true, "baixo", true, true, true, "almoco", "jantar", "lanche"),
        a("Pepino fatiado (100g)",          16, 1,  4, 0, true, true, true, true, "baixo", true, true, true, "almoco", "jantar", "lanche"),
        a("Alface (100g)",                  15, 1,  3, 0, true, true, true, true, "baixo", true, true, true, "almoco", "jantar"),
        a("Rúcula (100g)",                  25, 3,  4, 1, true, true, true, true, "baixo", true, true, true, "almoco", "jantar"),
        a("Repolho cozido (100g)",          25, 1,  6, 0, true, true, true, true, "baixo", true, true, true, "almoco", "jantar"),
        a("Cenoura baby (100g)",            35, 1,  8, 0, true, true, true, true, "baixo", true, true, true, "lanche"),
        a("Salada verde mista (à vontade)", 20, 2,  3, 0, true, true, true, true, "baixo", true, true, true, "almoco", "jantar")
    );

    // ── GORDURAS BOAS ──────────────────────────────────────────────

    public static final List<AlimentoDB> GORDURAS_BOAS = List.of(
        a("Azeite de oliva (1 col. sopa)",    120, 0, 0, 14, true, true, true, true, "baixo", true, true, true, "almoco", "jantar"),
        a("Azeite de oliva (1 col. chá)",      40, 0, 0,  5, true, true, true, true, "baixo", true, true, true, "almoco", "jantar"),
        a("Abacate (100g)",                   160, 2, 9, 15, true, true, true, true, "baixo", true, true, true, "cafe", "lanche", "almoco"),
        a("Castanha-do-pará (30g)",           200, 4, 4, 20, true, true, true, true, "baixo", true, true, true, "lanche"),
        a("Castanha de caju (30g)",           165, 5, 9, 13, true, true, true, true, "baixo", true, true, false, "lanche"),
        a("Nozes (30g)",                      196, 5, 4, 20, true, true, true, true, "baixo", true, true, true, "lanche"),
        a("Amêndoas (30g)",                   174, 6, 6, 15, true, true, true, true, "baixo", true, true, true, "lanche"),
        a("Linhaça (1 col. sopa)",             55, 2, 3,  4, true, true, true, true, "baixo", true, true, true, "cafe", "almoco", "jantar"),
        a("Chia (1 col. sopa)",                58, 2, 5,  4, true, true, true, true, "baixo", true, true, true, "cafe", "almoco", "jantar")
    );

    // ── FRUTAS ─────────────────────────────────────────────────────

    public static final List<AlimentoDB> FRUTAS = List.of(
        a("Maçã (1 unidade)",       95, 0, 25, 0, true, true, true, true, "baixo", true,  true, true, "cafe", "lanche"),
        a("Banana (1 unidade)",    105, 1, 27, 0, true, true, true, true, "alto",  false, true, true, "cafe", "lanche"),
        a("Pera (1 unidade)",       96, 1, 26, 0, true, true, true, true, "baixo", true,  true, true, "cafe", "lanche"),
        a("Morango (100g)",         32, 1,  8, 0, true, true, true, true, "baixo", true,  true, true, "cafe", "lanche"),
        a("Uva (100g)",             69, 1, 18, 0, true, true, true, true, "medio", false, true, true, "lanche"),
        a("Melão (100g)",           34, 1,  8, 0, true, true, true, true, "medio", false, true, true, "lanche"),
        a("Melancia (100g)",        30, 1,  8, 0, true, true, true, true, "alto",  false, true, true, "lanche"),
        a("Mamão (100g)",           43, 1, 11, 0, true, true, true, true, "medio", false, true, true, "cafe", "lanche"),
        a("Manga (100g)",           60, 1, 15, 0, true, true, true, true, "medio", false, true, true, "lanche"),
        a("Abacaxi (100g)",         50, 0, 13, 0, true, true, true, true, "medio", false, true, true, "lanche"),
        a("Kiwi (100g)",            61, 1, 15, 0, true, true, true, true, "baixo", true,  true, true, "cafe", "lanche"),
        a("Laranja (1 unidade)",    62, 1, 15, 0, true, true, true, true, "baixo", true,  true, true, "cafe", "lanche"),
        a("Tangerina (1 unidade)",  47, 1, 12, 0, true, true, true, true, "baixo", true,  true, true, "lanche"),
        a("Ameixa (1 unidade)",     30, 0,  8, 0, true, true, true, true, "baixo", true,  true, true, "lanche"),
        a("Pêssego (1 unidade)",    58, 1, 14, 0, true, true, true, true, "baixo", true,  true, true, "lanche"),
        a("Goiaba (100g)",          68, 3, 14, 1, true, true, true, true, "baixo", true,  true, true, "lanche"),
        a("Caju (100g)",            43, 1, 10, 0, true, true, true, true, "baixo", true,  true, true, "lanche"),
        a("Acerola (100g)",         32, 1,  8, 0, true, true, true, true, "baixo", true,  true, true, "lanche"),
        a("Maracujá (100g)",        97, 2, 23, 1, true, true, true, true, "baixo", true,  true, true, "lanche")
    );

    // ── LANCHES PRONTOS ────────────────────────────────────────────

    public static final List<AlimentoDB> LANCHES = List.of(
        a("Iogurte natural (170g)",           100,  6, 12, 3, true,  false, false, true,  "baixo", true,  true,  true,  "lanche"),
        a("Iogurte grego natural (170g)",     130, 10, 10, 5, true,  false, false, true,  "baixo", true,  true,  true,  "lanche"),
        a("Queijo cottage (100g)",             98, 11,  3, 4, true,  false, false, true,  "baixo", true,  true,  true,  "lanche"),
        a("Ricota (100g)",                    174, 11,  3,13, true,  false, false, true,  "baixo", true,  true,  false, "lanche"),
        a("Amendoim torrado sem sal (30g)",   170,  7,  6,14, true,  true,  true,  true,  "baixo", true,  true,  false, "lanche"),
        a("Mix de castanhas (30g)",           180,  5,  6,15, true,  true,  true,  true,  "baixo", true,  true,  true,  "lanche"),
        a("Barra de cereal integral (1 un.)",  90,  2, 17, 2, true,  true,  true,  false, "medio", false, true,  true,  "lanche"),
        a("Tapioca pequena (50g)",            130,  0, 32, 0, true,  true,  true,  true,  "alto",  false, true,  true,  "lanche"),
        a("Ovo cozido (1 unidade)",            70,  6,  0, 5, true,  false, true,  true,  "baixo", true,  true,  false, "lanche"),
        a("Pepino fatiado (100g)",             16,  1,  4, 0, true,  true,  true,  true,  "baixo", true,  true,  true,  "lanche"),
        a("Atum em água (100g)",              132, 28,  0, 1, false, false, true,  true,  "baixo", true,  true,  true,  "lanche"),
        a("Grão-de-bico temperado (80g)",     131,  7, 22, 2, true,  true,  true,  true,  "baixo", true,  true,  true,  "lanche")
    );

    // ── MÉTODO PRINCIPAL ───────────────────────────────────────────

    public static List<AlimentoDB> todos() {
        List<AlimentoDB> lista = new ArrayList<>();
        lista.addAll(CARBOS_CAFE);
        lista.addAll(BEBIDAS_CAFE);
        lista.addAll(CARBOS_REFEICAO);
        lista.addAll(PROTEINAS_ANIMAIS);
        lista.addAll(PROTEINAS_VEGETAIS);
        lista.addAll(LEGUMES);
        lista.addAll(GORDURAS_BOAS);
        lista.addAll(FRUTAS);
        lista.addAll(LANCHES);
        return lista;
    }

    // filtra por refeição e perfil do paciente
    public static List<AlimentoDB> filtrar(String tipoRefeicao, boolean semLactose, boolean semGluten,
                                            boolean vegano, boolean vegetariano,
                                            boolean diabetes, boolean hipertensao, boolean colesterol) {
        return todos().stream()
            .filter(a -> a.podeNaRefeicao(tipoRefeicao))
            .filter(a -> !semLactose  || a.semLactose)
            .filter(a -> !semGluten   || a.semGluten)
            .filter(a -> !vegano      || a.vegano)
            .filter(a -> !vegetariano || a.vegetariano)
            .filter(a -> !diabetes    || a.okDiabetes)
            .filter(a -> !hipertensao || a.okHipertensao)
            .filter(a -> !colesterol  || a.okColesterol)
            .collect(java.util.stream.Collectors.toList());
    }
}
