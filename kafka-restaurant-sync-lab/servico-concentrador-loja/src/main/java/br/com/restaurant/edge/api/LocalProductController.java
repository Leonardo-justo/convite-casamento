/* API local consumida pelo PDV; não deve depender do Kafka diretamente. */
package br.com.restaurant.edge.api;

import br.com.restaurant.edge.domain.LocalProduct;
import br.com.restaurant.edge.repository.LocalProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/local/produtos")
public class LocalProductController {

    private final LocalProductRepository repository;
    private final UUID tenantId;
    private final UUID storeId;

    public LocalProductController(
            LocalProductRepository repository,
            @Value("${app.identity.tenant-id}") UUID tenantId,
            @Value("${app.identity.store-id}") UUID storeId
    ) {
        this.repository = repository;
        this.tenantId = tenantId;
        this.storeId = storeId;
    }

    @GetMapping
    List<LocalProductResponse> listar(@RequestParam(defaultValue = "true") boolean activeOnly) {
        List<LocalProduct> products = activeOnly
                ? repository.findAllByTenantIdAndStoreIdAndActiveTrueOrderByName(tenantId, storeId)
                : repository.findAllByTenantIdAndStoreIdOrderByName(tenantId, storeId);
        return products.stream().map(LocalProductResponse::from).toList();
    }
}
