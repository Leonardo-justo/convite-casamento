package br.com.restaurant.edge.service;

import br.com.restaurant.contracts.ProductPayload;
import br.com.restaurant.contracts.ProductSnapshot;
import br.com.restaurant.edge.domain.LocalProduct;
import br.com.restaurant.edge.repository.LocalProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SnapshotImportService {

    /*
     * Importa a carga inicial sem destruir uma atualização em tempo real.
     * A comparação por version torna snapshot e eventos comutativos: o mais
     * novo vence, independentemente de qual caminho chegou primeiro.
     */

    private final LocalProductRepository repository;

    public SnapshotImportService(LocalProductRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SnapshotImportResult importar(ProductSnapshot snapshot) {
        int inserted = 0;
        int updated = 0;
        int ignoredAsNewerLocally = 0;

        for (ProductPayload incoming : snapshot.products()) {
            LocalProduct local = repository.findById(incoming.productId()).orElse(null);
            if (local == null) {
                repository.save(new LocalProduct(
                        snapshot.tenantId(),
                        snapshot.storeId(),
                        incoming,
                        snapshot.generatedAt()
                ));
                inserted++;
            } else if (incoming.version() > local.getSourceVersion()) {
                // Atualiza somente quando a fonte conhece uma versão superior.
                local.apply(incoming, snapshot.generatedAt());
                updated++;
            } else {
                ignoredAsNewerLocally++;
            }
        }

        return new SnapshotImportResult(
                snapshot.generatedAt(),
                snapshot.products().size(),
                inserted,
                updated,
                ignoredAsNewerLocally
        );
    }

    @Deprecated public SnapshotImportResult apply(ProductSnapshot snapshot) { return importar(snapshot); }
}
