package com.itb.inf3an.nutrilife.repository;

import com.itb.inf3an.nutrilife.model.entity.PlanoAlimentar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PlanoAlimentarRepository extends JpaRepository<PlanoAlimentar, Long> {
    Optional<PlanoAlimentar> findTopByClienteIdOrderByDataInicioDesc(Long clienteId);
}