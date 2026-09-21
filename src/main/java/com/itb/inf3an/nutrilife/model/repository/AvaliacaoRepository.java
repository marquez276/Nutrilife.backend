package com.itb.inf3an.nutrilife.model.repository;

import com.itb.inf3an.nutrilife.model.entity.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByNutricionistaId(Long nutricionistaId);
}

