package com.itb.inf3an.nutrilife.model.repository;

import com.itb.inf3an.nutrilife.model.entity.PlanoAlimentar;
import com.itb.inf3an.nutrilife.model.enums.OrigemPlano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PlanoAlimentarRepository extends JpaRepository<PlanoAlimentar, Long> {
    Optional<PlanoAlimentar> findTopByClienteIdOrderByDataInicioDesc(Long clienteId);
    Optional<PlanoAlimentar> findTopByClienteIdAndOrigemAndCodStatusTrue(Long clienteId, OrigemPlano origem);
}
