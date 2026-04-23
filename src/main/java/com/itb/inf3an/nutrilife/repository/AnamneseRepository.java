package com.itb.inf3an.nutrilife.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.itb.inf3an.nutrilife.model.entity.Anamnese;

@Repository
public interface AnamneseRepository extends JpaRepository<Anamnese, Long> {

    Anamnese findByClienteId(Long clienteId);

}