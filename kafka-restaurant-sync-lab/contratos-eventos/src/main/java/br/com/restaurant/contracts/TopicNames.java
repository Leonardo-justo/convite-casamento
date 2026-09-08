package br.com.restaurant.contracts;

public final class TopicNames {

    // Centralizar nomes evita strings diferentes entre produtor, consumidor e operação.
    public static final String PRODUCT_EVENTS = "restaurant.catalog.product.v1";
    public static final String PRODUCT_EVENTS_DLT = PRODUCT_EVENTS + ".dlt";

    private TopicNames() {
    }
}
