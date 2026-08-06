package com.itb.inf3an.nutrilife.service;

import com.itb.inf3an.nutrilife.model.entity.Cliente;
import com.itb.inf3an.nutrilife.model.enums.TipoUsuario;
import com.itb.inf3an.nutrilife.repository.PlanoAlimentarRepository;
import com.itb.inf3an.nutrilife.repository.RegistroRefeicaoRepository;
import com.itb.inf3an.nutrilife.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SchedulerService {

    private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);

    @Autowired private RegistroRefeicaoRepository registroRepo;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private PlanoAlimentarRepository planoRepo;
    @Autowired private PlanoAlimentarService planoService;

    // Reset food tracking every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void resetarRastreamentoAlimentar() {
        // Delete all records before today for every client
        List<?> clientes = usuarioRepo.findByTipoUsuario(TipoUsuario.CLIENTE);
        for (var u : clientes) {
            Cliente c = (Cliente) u;
            registroRepo.deleteByClienteIdAndDataBefore(c.getId(), LocalDate.now());
        }
        log.info("Rastreamento alimentar resetado em {}", LocalDate.now());
    }

    // Refresh meal plans weekly — every Monday at 02:00
    @Scheduled(cron = "0 0 2 * * MON")
    @Transactional
    public void atualizarPlanosSemanais() {
        List<?> clientes = usuarioRepo.findByTipoUsuario(TipoUsuario.CLIENTE);
        int renovados = 0;
        for (var u : clientes) {
            Cliente c = (Cliente) u;
            var plano = planoRepo.findTopByClienteIdOrderByDataInicioDesc(c.getId());
            if (plano.isPresent()) {
                LocalDate inicio = plano.get().getDataInicio();
                // Regenerate if plan is 7+ days old
                if (inicio != null && inicio.plusDays(7).isBefore(LocalDate.now())) {
                    try {
                        planoService.regenerarPlano(c.getId());
                        renovados++;
                    } catch (Exception e) {
                        log.warn("Falha ao regenerar plano para clienteId={}: {}", c.getId(), e.getMessage());
                    }
                }
            }
        }
        log.info("Renovação semanal de planos concluída: {} planos atualizados", renovados);
    }
}
