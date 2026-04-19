package org.itson.ecommerce_e2.services;

import java.util.List;
import org.itson.ecommerce_e2.models.Order;
import org.itson.ecommerce_e2.models.enums.OrderStatus;

public interface IOrderService {

    /**
     * Proceso de checkout — operación atómica que: 1. Valida que el carrito no
     * esté vacío. 2. Valida stock de cada producto. 3. Crea la Order con sus
     * OrderItems (snapshot de precios). 4. Reduce el stock de cada producto. 5.
     * Vacía el carrito.
     *
     * Si cualquier paso falla, toda la operación hace rollback.
     *
     * @param userId usuario que compra
     * @param addressId dirección de entrega seleccionada
     * @param paymentMethodId método de pago seleccionado
     * @return la Order creada con status PENDING
     */
    Order checkout(Long userId, Long addressId, Long paymentMethodId);

    Order findById(Long id);

    List<Order> findByUser(Long userId);

    /**
     * Para el panel de administración.
     * @return 
     */
    List<Order> findAll();

    List<Order> findByStatus(OrderStatus status);

    /**
     * Cambia el estado de la orden (PAID, SHIPPED, DELIVERED, etc.)
     * @param orderId
     * @param newStatus
     * @return 
     */
    Order updateStatus(Long orderId, OrderStatus newStatus);
}
