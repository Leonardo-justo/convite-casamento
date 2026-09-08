/* Queries usadas pelos endpoints do PDV e pelo importador de eventos. */
package br.com.restaurant.edge.repository;

import br.com.restaurant.edge.domain.LocalProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LocalProductRepository extends JpaRepository<LocalProduct, UUID> {

    List<LocalProduct> findAllByTenantIdAndStoreIdOrderByName(UUID tenantId, UUID storeId);

    List<LocalProduct> findAllByTenantIdAndStoreIdAndActiveTrueOrderByName(UUID tenantId, UUID storeId);

    long countByTenantIdAndStoreId(UUID tenantId, UUID storeId);
}
