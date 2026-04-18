package org.itson.ecommerce_e2.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Ítem dentro de una orden (snapshot del producto al momento de compra).
 *
 * Por qué guardamos price aquí y no solo la FK al producto: Si el precio del
 * producto cambia después de la compra, el historial de la orden debe reflejar
 * lo que el cliente pagó en ese momento. Este es el "snapshot pattern".
 *
 * La dirección NO va aquí — va en la Order, que aplica a todos los ítems.
 */
@Entity
@Table(name = "order_items")
public class OrderItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    /**
     * Precio unitario al momento del checkout (snapshot).
     */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Subtotal = quantity × unitPrice, calculado al crear el ítem.
     */
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    /**
     * Nombre del producto al momento de la compra (snapshot).
     */
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    // ── Relaciones ────────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // ── Constructores ─────────────────────────────────────────────────────────
    public OrderItem() {
    }

    public OrderItem(Order order, Product product, int quantity) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
        this.productName = product.getName();
        this.subtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    // ── Getters y Setters ─────────────────────────────────────────────────────
    public Long getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
