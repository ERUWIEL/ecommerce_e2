package models.enums;

/**
 * Ciclo de vida de una orden.
 *
 *  PENDING    → orden creada, esperando confirmación de pago.
 *  PAID       → pago confirmado, pendiente de preparación.
 *  PROCESSING → en bodega: armando el paquete.
 *  SHIPPED    → paquete entregado al servicio de mensajería.
 *  DELIVERED  → entregado al cliente.
 *  CANCELLED  → cancelado antes de ser enviado.
 *  REFUNDED   → devuelto y reembolsado.
 */
public enum OrderStatus {
    PENDING,
    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}