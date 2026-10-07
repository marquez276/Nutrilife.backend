package com.itb.inf3an.nutrilife.model.repository;

import com.itb.inf3an.nutrilife.model.entity.Vinculo;
import com.itb.inf3an.nutrilife.model.enums.StatusVinculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VinculoRepository extends JpaRepository<Vinculo, Long> {
    List<Vinculo> findByNutricionistaId(Long nutricionistaId);
    List<Vinculo> findByClienteId(Long clienteId);
    Optional<Vinculo> findByCodigoConvite(String codigoConvite);
    boolean existsByClienteIdAndNutricionistaIdAndStatusIn(Long clienteId, Long nutricionistaId, List<StatusVinculo> status);
    boolean existsByNutricionistaIdAndClienteIdAndStatus(Long nutricionistaId, Long clienteId, StatusVinculo status);
}
