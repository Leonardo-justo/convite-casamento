/* Endpoint que dispara/reinicia a carga inicial do central para esta loja. */
package br.com.restaurant.edge.api;

import br.com.restaurant.edge.service.InitialLoadService;
import br.com.restaurant.edge.service.SnapshotImportResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/interno/sincronizacao")
public class InitialLoadController {

    private final InitialLoadService initialLoadService;

    public InitialLoadController(InitialLoadService initialLoadService) {
        this.initialLoadService = initialLoadService;
    }

    @PostMapping("/carga-inicial")
    SnapshotImportResult executarCargaInicial() {
        return initialLoadService.sincronizar();
    }
}
