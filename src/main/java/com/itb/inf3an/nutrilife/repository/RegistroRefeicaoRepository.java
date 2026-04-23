package com.itb.inf3an.nutrilife.repository;

import com.itb.inf3an.nutrilife.model.entity.RegistroRefeicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegistroRefeicaoRepository extends JpaRepository<RegistroRefeicao, Long> {
    List<RegistroRefeicao> findByClienteId(Long clienteId);
}
