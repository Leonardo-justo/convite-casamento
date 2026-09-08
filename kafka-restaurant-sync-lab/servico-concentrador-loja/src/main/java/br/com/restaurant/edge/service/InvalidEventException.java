/* Erro de contrato: permite ao handler aplicar retry e depois enviar à DLT. */
package br.com.restaurant.edge.service;

public class InvalidEventException extends RuntimeException {

    public InvalidEventException(String message) {
        super(message);
    }

    public InvalidEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
