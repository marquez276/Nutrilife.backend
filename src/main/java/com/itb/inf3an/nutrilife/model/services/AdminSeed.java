package com.itb.inf3an.nutrilife.model.services;

import com.itb.inf3an.nutrilife.model.entity.Admin;
import com.itb.inf3an.nutrilife.model.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria o admin padrão no primeiro start, se nenhum admin existir ainda. Substitui o INSERT
 * manual no script de schema, que guardava a senha em texto puro até o primeiro login.
 */
@Component
public class AdminSeed implements ApplicationRunner {

    private final AdminRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String senha;

    public AdminSeed(AdminRepository repo, PasswordEncoder passwordEncoder,
                      @Value("${app.admin.email:admin@nutrilife.com}") String email,
                      @Value("${app.admin.senha:23122008}") String senha) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repo.count() > 0) return;
        Admin admin = new Admin();
        admin.setNomeCompleto("Administrador");
        admin.setEmail(email);
        admin.setSenha(passwordEncoder.encode(senha));
        admin.setCodStatus(true);
        admin.setAprovado(true);
        admin.setPrincipal(true);
        repo.save(admin);
    }
}
