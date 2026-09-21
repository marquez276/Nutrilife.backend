package com.itb.inf3an.nutrilife.dto.auth;

import com.itb.inf3an.nutrilife.model.enums.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    private String nome;
    private String email;
    private String password;
    private TipoUsuario tipoUsuario;
    private String cpf;
    private String telefone;
    private LocalDate dataNascimento;
    private String crn;
    private String especialidade;
}

