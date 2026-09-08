/* Abstração de persistência do catálogo central, com filtros por tenant/loja. */
package br.com.restaurant.catalog.repository;

import br.com.restaurant.catalog.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByIdAndTenantIdAndStoreId(UUID id, UUID tenantId, UUID storeId);

    List<Product> findAllByTenantIdAndStoreIdOrderByName(UUID tenantId, UUID storeId);
}
