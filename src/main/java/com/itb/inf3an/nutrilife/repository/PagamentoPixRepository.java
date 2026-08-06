package com.itb.inf3an.nutrilife.repository;

import com.itb.inf3an.nutrilife.model.entity.PagamentoPix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PagamentoPixRepository extends JpaRepository<PagamentoPix, Long> {
    List<PagamentoPix> findByStatusOrderByDataEnvioDesc(String status);
    List<PagamentoPix> findByNutricionistaIdOrderByDataEnvioDesc(Long nutricionistaId);
}
