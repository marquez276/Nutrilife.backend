package com.itb.inf3an.nutrilife.model.repository;

import com.itb.inf3an.nutrilife.model.entity.PacienteExterno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PacienteExternoRepository extends JpaRepository<PacienteExterno, Long> {
    List<PacienteExterno> findByNutricionistaId(Long nutricionistaId);
}
