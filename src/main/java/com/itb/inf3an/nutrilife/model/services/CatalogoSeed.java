package com.itb.inf3an.nutrilife.model.services;

import com.itb.inf3an.nutrilife.model.entity.Alimento;
import com.itb.inf3an.nutrilife.model.repository.AlimentoRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Catálogo de alimentos que o gerador de planos (PlanoAlimentarService) usa. Populado uma vez, quando o
 * catálogo está vazio; a partir daí o admin controla valores e disponibilidade e o gerador lê do banco.
 */
@Component
public class CatalogoSeed implements ApplicationRunner {

    public static final String CATEGORIA_PLANO = "Plano alimentar";

    // nome, kcal, proteína, carboidrato, gordura
    private static final Object[][] ALIMENTOS = {
        {"Tofu grelhado (120g)", 96.0, 10.0, 2.0, 6.0},
        {"Grão-de-bico temperado (80g)", 131.0, 7.0, 22.0, 2.0},
        {"Lentilha cozida (100g)", 116.0, 9.0, 20.0, 0.0},
        {"Frango grelhado (120g)", 200.0, 37.0, 0.0, 4.0},
        {"Peixe grelhado (120g)", 144.0, 28.0, 0.0, 3.0},
        {"Ovo cozido (2 unidades)", 140.0, 12.0, 1.0, 10.0},
        {"Ovo mexido (2 unidades)", 140.0, 12.0, 1.0, 10.0},
        {"Omelete simples (2 ovos)", 150.0, 13.0, 1.0, 11.0},
        {"Pasta de amendoim (1 col. sopa)", 95.0, 4.0, 3.0, 8.0},
        {"Tofu firme (80g)", 64.0, 7.0, 1.0, 4.0},
        {"Grão-de-bico cozido (50g)", 82.0, 4.0, 14.0, 1.0},
        {"Pão integral (2 fatias)", 160.0, 6.0, 28.0, 2.0},
        {"Tapioca (1 unidade)", 110.0, 1.0, 27.0, 0.0},
        {"Aveia (4 col. sopa)", 150.0, 5.0, 27.0, 3.0},
        {"Cuscuz (1 fatia média)", 120.0, 3.0, 25.0, 1.0},
        {"Aveia (2 col. sopa)", 75.0, 3.0, 14.0, 2.0},
        {"Pão integral (1 fatia)", 80.0, 3.0, 14.0, 1.0},
        {"Mamão (1 fatia média)", 45.0, 0.0, 11.0, 0.0},
        {"Banana (1 unidade)", 90.0, 1.0, 23.0, 0.0},
        {"Maçã (1 unidade)", 80.0, 0.0, 21.0, 0.0},
        {"Morango (1 xícara)", 50.0, 1.0, 12.0, 0.0},
        {"Pera (1 unidade)", 60.0, 0.0, 15.0, 0.0},
        {"Chá de ervas (200ml)", 0.0, 0.0, 0.0, 0.0},
        {"Leite desnatado (200ml)", 70.0, 7.0, 10.0, 0.0},
        {"Café preto sem açúcar (200ml)", 5.0, 0.0, 1.0, 0.0},
        {"Café com leite desnatado (200ml)", 60.0, 4.0, 6.0, 1.0},
        {"Frango grelhado (150g)", 248.0, 46.0, 0.0, 5.0},
        {"Carne bovina magra (150g)", 250.0, 38.0, 0.0, 10.0},
        {"Peixe grelhado (150g)", 180.0, 35.0, 0.0, 4.0},
        {"Tofu grelhado (150g)", 120.0, 13.0, 3.0, 7.0},
        {"Grão-de-bico (100g)", 164.0, 9.0, 27.0, 3.0},
        {"Feijão preto (1 concha)", 80.0, 5.0, 15.0, 0.0},
        {"Arroz integral (4 col. sopa)", 168.0, 4.0, 36.0, 1.0},
        {"Arroz branco (4 col. sopa)", 160.0, 3.0, 35.0, 0.0},
        {"Batata-doce cozida (130g)", 112.0, 2.0, 26.0, 0.0},
        {"Macarrão integral (100g)", 160.0, 6.0, 31.0, 1.0},
        {"Arroz integral (3 col. sopa)", 126.0, 3.0, 27.0, 1.0},
        {"Batata-doce cozida (100g)", 86.0, 2.0, 20.0, 0.0},
        {"Batata-doce cozida (80g)", 69.0, 1.0, 16.0, 0.0},
        {"Feijão carioca (1 concha)", 76.0, 5.0, 14.0, 0.0},
        {"Lentilha (1 concha)", 90.0, 7.0, 16.0, 0.0},
        {"Salada verde mista (à vontade)", 20.0, 2.0, 3.0, 0.0},
        {"Tomate + alface + pepino", 25.0, 1.0, 5.0, 0.0},
        {"Rúcula com tomate cereja", 20.0, 2.0, 3.0, 0.0},
        {"Azeite de oliva extravirgem (1 col. sopa)", 88.0, 0.0, 0.0, 10.0},
        {"Iogurte grego natural (170g)", 130.0, 12.0, 6.0, 6.0},
        {"Amendoim torrado sem sal (30g)", 170.0, 8.0, 5.0, 14.0},
        {"Manga (1 fatia)", 65.0, 1.0, 17.0, 0.0},
        {"Cenoura baby (100g)", 41.0, 1.0, 9.0, 0.0},
        {"Pepino fatiado (100g)", 16.0, 1.0, 4.0, 0.0},
        {"Castanha-do-pará (2 unidades)", 60.0, 1.0, 1.0, 6.0},
        {"Iogurte natural desnatado (170g)", 100.0, 6.0, 8.0, 3.0},
        {"Ovo cozido (1 unidade)", 70.0, 6.0, 1.0, 5.0},
        {"Amendoim torrado sem sal (20g)", 113.0, 5.0, 3.0, 10.0},
        {"Omelete (2 ovos)", 140.0, 12.0, 1.0, 10.0},
        {"Omelete de legumes (2 ovos)", 140.0, 12.0, 4.0, 9.0},
        {"Brócolis no vapor (90g)", 31.0, 3.0, 6.0, 0.0},
        {"Abobrinha refogada (100g)", 17.0, 1.0, 3.0, 0.0},
        {"Cenoura cozida (80g)", 33.0, 1.0, 8.0, 0.0},
        {"Espinafre refogado (80g)", 18.0, 2.0, 3.0, 0.0},
        {"Couve-flor no vapor (90g)", 23.0, 2.0, 5.0, 0.0},
        {"Mandioca cozida (100g)", 125.0, 1.0, 30.0, 0.0},
        {"Inhame cozido (100g)", 97.0, 2.0, 22.0, 0.0},
        {"Azeite de oliva (1 col. chá)", 44.0, 0.0, 0.0, 5.0},
        {"Fruta da estação", 60.0, 1.0, 15.0, 0.0}
    };

    private final AlimentoRepository repo;

    public CatalogoSeed(AlimentoRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!repo.findByCustomizadoFalseAndRefeicaoIsNull().isEmpty()) return;
        for (Object[] r : ALIMENTOS) {
            Alimento a = new Alimento();
            a.setNome((String) r[0]);
            a.setCalorias((Double) r[1]);
            a.setProteina((Double) r[2]);
            a.setCarboidrato((Double) r[3]);
            a.setGordura((Double) r[4]);
            a.setCategoria(CATEGORIA_PLANO);
            repo.save(a);
        }
    }
}
