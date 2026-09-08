/* Erro de domínio traduzido para HTTP 404 pelo advice. */
package br.com.restaurant.catalog.service;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(UUID productId) {
        super("Produto não encontrado: " + productId);
    }
}
