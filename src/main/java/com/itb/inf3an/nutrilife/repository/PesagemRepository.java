package com.itb.inf3an.nutrilife.repository;

import com.itb.inf3an.nutrilife.model.entity.Pesagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PesagemRepository extends JpaRepository<Pesagem, Long> {
    List<Pesagem> findByClienteId(Long clienteId);
}
