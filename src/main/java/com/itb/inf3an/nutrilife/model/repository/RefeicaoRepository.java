package com.itb.inf3an.nutrilife.model.repository;

import com.itb.inf3an.nutrilife.model.entity.Refeicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefeicaoRepository extends JpaRepository<Refeicao, Long> {
}

