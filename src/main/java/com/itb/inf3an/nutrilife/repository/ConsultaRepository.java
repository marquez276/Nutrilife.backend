package com.itb.inf3an.nutrilife.repository;

import com.itb.inf3an.nutrilife.model.entity.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findByClienteId(Long clienteId);
    List<Consulta> findByNutricionistaId(Long nutricionistaId);
}
