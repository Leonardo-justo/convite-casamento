/* Orquestra snapshot remoto + importação transacional na base local. */
package br.com.restaurant.edge.service;

import br.com.restaurant.contracts.ProductSnapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InitialLoadService {

    private final CatalogSnapshotClient client;
    private final SnapshotImportService importer;
    private final UUID tenantId;
    private final UUID storeId;

    public InitialLoadService(
            CatalogSnapshotClient client,
            SnapshotImportService importer,
            @Value("${app.identity.tenant-id}") UUID tenantId,
            @Value("${app.identity.store-id}") UUID storeId
    ) {
        this.client = client;
        this.importer = importer;
        this.tenantId = tenantId;
        this.storeId = storeId;
    }

    public SnapshotImportResult sincronizar() {
        ProductSnapshot snapshot = client.buscarCargaInicial(tenantId, storeId);
        if (!tenantId.equals(snapshot.tenantId()) || !storeId.equals(snapshot.storeId())) {
            throw new IllegalStateException("A carga inicial retornou dados de outra loja");
        }
        return importer.importar(snapshot);
    }

    @Deprecated public SnapshotImportResult synchronize() { return sincronizar(); }
}
