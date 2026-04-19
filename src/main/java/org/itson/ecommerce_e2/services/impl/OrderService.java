package org.itson.ecommerce_e2.services.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.itson.ecommerce_e2.dao.AddressDAO;
import org.itson.ecommerce_e2.dao.CartDAO;
import org.itson.ecommerce_e2.dao.CartItemDAO;
import org.itson.ecommerce_e2.dao.OrderDAO;
import org.itson.ecommerce_e2.dao.OrderItemDAO;
import org.itson.ecommerce_e2.dao.PaymentMethodDAO;
import org.itson.ecommerce_e2.dao.ProductDAO;
import org.itson.ecommerce_e2.dao.UserDAO;
import org.itson.ecommerce_e2.models.Address;
import org.itson.ecommerce_e2.models.Cart;
import org.itson.ecommerce_e2.models.CartItem;
import org.itson.ecommerce_e2.models.Order;
import org.itson.ecommerce_e2.models.OrderItem;
import org.itson.ecommerce_e2.models.PaymentMethod;
import org.itson.ecommerce_e2.models.Product;
import org.itson.ecommerce_e2.models.User;
import org.itson.ecommerce_e2.models.enums.OrderStatus;
import org.itson.ecommerce_e2.services.IOrderService;

/**
 * OrderServiceImpl actúa como Facade del checkout.
 *
 * Todo el proceso de checkout ocurre en UNA sola transacción: validar carrito →
 * validar stock → crear Order → crear OrderItems → reducir stock → vaciar
 * carrito → commit
 *
 * Si cualquier paso falla → rollback total. Esto garantiza que nunca quede una
 * orden creada sin stock descontado o un carrito lleno después de un pago
 * exitoso.
 */
@ApplicationScoped
public class OrderService implements IOrderService {

    @Inject
    private EntityManager em;
    @Inject
    private UserDAO userDAO;
    @Inject
    private CartDAO cartDAO;
    @Inject
    private CartItemDAO cartItemDAO;
    @Inject
    private OrderDAO orderDAO;
    @Inject
    private OrderItemDAO orderItemDAO;
    @Inject
    private ProductDAO productDAO;
    @Inject
    private AddressDAO addressDAO;
    @Inject
    private PaymentMethodDAO paymentMethodDAO;

    @Override
    public Order checkout(Long userId, Long addressId, Long paymentMethodId) {

        // ── Validaciones previas (fuera de transacción) ───────────────────────
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado: " + userId);
        }

        Address address = addressDAO.findById(addressId);
        if (address == null) {
            throw new IllegalArgumentException("Dirección no encontrada: " + addressId);
        }
        if (!address.getUser().getId().equals(userId)) {
            throw new IllegalStateException("La dirección no pertenece al usuario.");
        }

        PaymentMethod payment = paymentMethodDAO.findById(paymentMethodId);
        if (payment == null) {
            throw new IllegalArgumentException("Método de pago no encontrado: " + paymentMethodId);
        }
        if (!payment.getUser().getId().equals(userId)) {
            throw new IllegalStateException("El método de pago no pertenece al usuario.");
        }

        Cart cart = cartDAO.findByUserId(userId);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("El carrito está vacío.");
        }

        // Validar stock de todos los productos ANTES de abrir la transacción
        for (CartItem item : cart.getItems()) {
            Product p = item.getProduct();
            if (p.getStock() < item.getQuantity()) {
                throw new IllegalStateException(
                        "Stock insuficiente para '" + p.getName() + "'. "
                        + "Disponible: " + p.getStock() + ", en carrito: " + item.getQuantity());
            }
        }

        // ── Transacción atómica del checkout ──────────────────────────────────
        em.getTransaction().begin();
        try {
            // 1. Crear la orden
            Order order = new Order(user, address, payment);
            order.setCreatedAt(LocalDateTime.now());
            order.setStatus(OrderStatus.PENDING);
            orderDAO.create(order);

            // 2. Crear los OrderItems y calcular el total
            BigDecimal total = BigDecimal.ZERO;
            for (CartItem cartItem : cart.getItems()) {
                Product product = cartItem.getProduct();

                OrderItem orderItem = new OrderItem(order, product, cartItem.getQuantity());
                orderItemDAO.create(orderItem);
                total = total.add(orderItem.getSubtotal());

                // 3. Reducir el stock de cada producto
                product.setStock(product.getStock() - cartItem.getQuantity());
                productDAO.update(product);
            }

            // 4. Guardar el total en la orden
            order.setTotal(total);
            orderDAO.update(order);

            // 5. Vaciar el carrito
            cartItemDAO.deleteAllByCartId(cart.getId());

            em.getTransaction().commit();
            return order;

        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public Order findById(Long id) {
        return orderDAO.findById(id);
    }

    @Override
    public List<Order> findByUser(Long userId) {
        return orderDAO.findByUserId(userId);
    }

    @Override
    public List<Order> findAll() {
        return orderDAO.findAll();
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return orderDAO.findByStatus(status);
    }

    @Override
    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderDAO.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Orden no encontrada: " + orderId);
        }

        em.getTransaction().begin();
        try {
            order.setStatus(newStatus);
            Order updated = orderDAO.update(order);
            em.getTransaction().commit();
            return updated;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
