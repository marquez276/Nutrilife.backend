package com.itb.inf3an.nutrilife.controller;

import com.itb.inf3an.nutrilife.model.entity.Nutricionista;
import com.itb.inf3an.nutrilife.model.entity.NutricionistaPerfil;
import com.itb.inf3an.nutrilife.repository.NutriPerfilRepository;
import com.itb.inf3an.nutrilife.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class NutricionistaController {

    @Autowired private NutriPerfilRepository perfilRepo;
    @Autowired private UsuarioRepository usuarioRepo;

    /** POST /nutricionistas/{id}/portfolio — appends one image (Base64 data URL) to the portfolio list */
    @PostMapping("/nutricionistas/{id}/portfolio")
    public ResponseEntity<?> uploadPortfolio(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return usuarioRepo.findById(id).map(u -> {
            if (!(u instanceof Nutricionista nutri)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Usuário não é nutricionista."));
            }
            try {
                String mime = file.getContentType() != null ? file.getContentType() : "image/jpeg";
                String dataUrl = "data:" + mime + ";base64," +
                    java.util.Base64.getEncoder().encodeToString(file.getBytes());
                NutricionistaPerfil perfil = perfilRepo.findByNutricionistaId(id).orElse(new NutricionistaPerfil());
                perfil.setNutricionista(nutri);
                String existing = perfil.getPortfolio();
                perfil.setPortfolio(existing != null && !existing.isBlank() ? existing + "," + dataUrl : dataUrl);
                perfilRepo.save(perfil);
                return ResponseEntity.ok(Map.of("url", dataUrl));
            } catch (IOException e) {
                return ResponseEntity.internalServerError().body(Map.of("message", "Erro ao salvar imagem."));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

}
