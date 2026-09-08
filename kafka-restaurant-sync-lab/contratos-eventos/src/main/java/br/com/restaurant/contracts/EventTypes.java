/* Vocabulário permitido; consumidores rejeitam tipos desconhecidos. */
package br.com.restaurant.contracts;

public final class EventTypes {

    public static final String PRODUCT_UPSERTED = "ProductUpserted";
    public static final String PRODUCT_DEACTIVATED = "ProductDeactivated";

    private EventTypes() {
    }
}
