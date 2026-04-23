package com.itb.inf3an.nutrilife.repository;

import com.itb.inf3an.nutrilife.model.entity.Alimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlimentoRepository extends JpaRepository<Alimento, Long> {
}
