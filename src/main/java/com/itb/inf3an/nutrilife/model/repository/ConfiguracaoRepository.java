package com.itb.inf3an.nutrilife.model.repository;

import com.itb.inf3an.nutrilife.model.entity.Configuracao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracaoRepository extends JpaRepository<Configuracao, String> {
}
