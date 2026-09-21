package com.itb.inf3an.nutrilife.controller;

import com.itb.inf3an.nutrilife.model.entity.Alimento;
import com.itb.inf3an.nutrilife.model.repository.AlimentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class AlimentoController {

    @Autowired
    private AlimentoRepository alimentoRepo;

    @GetMapping("/alimentos/cliente/{clienteId}")
    public List<Alimento> listarCustomizados(@PathVariable Long clienteId) {
        return alimentoRepo.findByClienteIdAndCustomizadoTrue(clienteId);
    }

    @PostMapping("/alimentos/customizado")
    public ResponseEntity<?> criarCustomizado(@RequestBody Alimento alimento) {
        alimento.setCustomizado(true);
        alimento.setRefeicao(null);
        return ResponseEntity.ok(alimentoRepo.save(alimento));
    }

    @DeleteMapping("/alimentos/customizado/{id}")
    public ResponseEntity<?> removerCustomizado(@PathVariable Long id) {
        return alimentoRepo.findById(id)
            .filter(Alimento::isCustomizado)
            .map(a -> {
                alimentoRepo.deleteById(id);
                return ResponseEntity.ok(Map.of("message", "Alimento removido."));
            })
            .orElse(ResponseEntity.notFound().build());
    }
}

