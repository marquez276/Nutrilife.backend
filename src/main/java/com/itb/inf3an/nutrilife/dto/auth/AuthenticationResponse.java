package com.itb.inf3an.nutrilife.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.itb.inf3an.nutrilife.model.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    @JsonProperty("access_token")
    private String accessToken;
    private Usuario usuario;
}
