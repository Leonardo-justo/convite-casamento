/* Cliente HTTP interno: busca snapshot, enquanto Kafka trata o incremental. */
package br.com.restaurant.edge.service;

import br.com.restaurant.contracts.ProductSnapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class CatalogSnapshotClient {

    private final RestClient restClient;

    public CatalogSnapshotClient(
            RestClient.Builder builder,
            @Value("${app.catalog.base-url}") String catalogBaseUrl
    ) {
        this.restClient = builder.baseUrl(catalogBaseUrl).build();
    }

    public ProductSnapshot buscarCargaInicial(UUID tenantId, UUID storeId) {
        ProductSnapshot snapshot = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/interno/cargas-iniciais/produtos")
                        .queryParam("tenantId", tenantId)
                        .queryParam("storeId", storeId)
                        .build())
                .retrieve()
                .body(ProductSnapshot.class);

        if (snapshot == null) {
            throw new IllegalStateException("A API central retornou uma carga inicial vazia");
        }
        return snapshot;
    }
}
