package org.itson.ecommerce_e2.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.itson.ecommerce_e2.models.enums.PaymentCategory;

/**
 * Método de pago (tarjeta) registrado por un usuario.
 *
 * IMPORTANTE DE SEGURIDAD: - Nunca guardes el número completo de tarjeta. -
 * Guarda solo los últimos 4 dígitos para mostrar al usuario. - El CVV JAMÁS
 * debe persistirse (ni encriptado). - En producción usa un token del proveedor
 * de pagos (Stripe, Conekta, etc.)
 */
@Entity                          // ← estaba faltando en la versión original
@Table(name = "payment_methods")
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Alias o apodo dado por el usuario: "Mi Visa Personal", "Tarjeta de
     * trabajo".
     */
    @Column(name = "alias", length = 100)
    private String alias;

    /**
     * Solo los últimos 4 dígitos de la tarjeta.
     */
    @Column(name = "last_four_digits", nullable = false, length = 4)
    private String lastFourDigits;

    /**
     * Nombre del titular tal como aparece en la tarjeta.
     */
    @Column(name = "cardholder_name", nullable = false, length = 150)
    private String cardholderName;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_category", nullable = false, length = 10)
    private PaymentCategory paymentCategory;

    @Column(name = "is_default", nullable = false)
    private boolean defaultMethod = false;

    // ── Relaciones ────────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Órdenes pagadas con este método.
     */
    @OneToMany(mappedBy = "paymentMethod", fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    // ── Constructores ─────────────────────────────────────────────────────────
    public PaymentMethod() {
    }

    // ── Getters y Setters ─────────────────────────────────────────────────────
    public Long getId() {
        return id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public PaymentCategory getPaymentCategory() {
        return paymentCategory;
    }

    public void setPaymentCategory(PaymentCategory paymentCategory) {
        this.paymentCategory = paymentCategory;
    }

    public boolean isDefaultMethod() {
        return defaultMethod;
    }

    public void setDefaultMethod(boolean defaultMethod) {
        this.defaultMethod = defaultMethod;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
