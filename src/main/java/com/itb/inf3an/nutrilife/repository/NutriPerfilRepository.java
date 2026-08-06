package com.itb.inf3an.nutrilife.repository;

import com.itb.inf3an.nutrilife.model.entity.NutricionistaPerfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NutriPerfilRepository extends JpaRepository<NutricionistaPerfil, Long> {
    Optional<NutricionistaPerfil> findByNutricionistaId(Long nutricionistaId);
}
