package com.itb.inf3an.nutrilife.model.repository;

import com.itb.inf3an.nutrilife.model.entity.MetasDiarias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetasDiariasRepository extends JpaRepository<MetasDiarias, Long> {
    MetasDiarias findByClienteId(Long clienteId);
}

