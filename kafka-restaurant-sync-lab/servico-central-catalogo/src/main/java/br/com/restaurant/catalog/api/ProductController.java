/* Camada HTTP central: converte requests REST em casos de uso do ProductService. */
package br.com.restaurant.catalog.api;

import br.com.restaurant.catalog.service.ProductService;
import br.com.restaurant.contracts.ProductSnapshot;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/api/catalogo/produtos")
    ResponseEntity<ProductResponse> salvar(@Valid @RequestBody ProductUpsertRequest request) {
        ProductResponse created = productService.salvar(request);
        return ResponseEntity
                .created(URI.create("/api/catalogo/produtos/" + created.id()))
                .body(created);
    }

    @PutMapping("/api/catalogo/produtos/{productId}")
    ProductResponse atualizar(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductUpsertRequest request
    ) {
        return productService.atualizar(productId, request);
    }

    @GetMapping("/api/catalogo/produtos")
    List<ProductResponse> listar(
            @RequestParam UUID tenantId,
            @RequestParam UUID storeId
    ) {
        return productService.listar(tenantId, storeId);
    }

    @GetMapping("/interno/cargas-iniciais/produtos")
    ProductSnapshot cargaInicial(
            @RequestParam UUID tenantId,
            @RequestParam UUID storeId
    ) {
        return productService.cargaInicial(tenantId, storeId);
    }
}
