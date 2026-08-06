package com.itb.inf3an.nutrilife.repository;

import com.itb.inf3an.nutrilife.model.entity.Usuario;
import com.itb.inf3an.nutrilife.model.enums.TipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Usuario findByEmail(String email);

    long countByTipoUsuario(TipoUsuario tipoUsuario);

    List<Usuario> findByTipoUsuario(TipoUsuario tipoUsuario);

    // Retorna pacientes com dados da anamnese para o painel admin
    @Query("SELECT u FROM Usuario u LEFT JOIN Anamnese a ON a.cliente.id = u.id WHERE u.tipoUsuario = 'CLIENTE'")
    List<Usuario> findAllClientes();
}
