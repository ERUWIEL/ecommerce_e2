package org.itson.ecommerce_e2.services;

import org.itson.ecommerce_e2.models.Cart;

public interface ICartService {

    /**
     * Agrega un producto al carrito del usuario. Si el producto ya existe en el
     * carrito, incrementa la cantidad. Lanza IllegalStateException si no hay
     * stock suficiente.
     * @param userId
     * @param productId
     * @param quantity
     * @return 
     */
    Cart addItem(Long userId, Long productId, int quantity);

    /**
     * Cambia la cantidad de un ítem. Si quantity <= 0, elimina el ítem del
     * carrito.
     * @param cartItemId
     * @param quantity
     * @return 
     */
    Cart updateQuantity(Long cartItemId, int quantity);

    Cart removeItem(Long cartItemId);

    Cart getCartByUserId(Long userId);

    /**
     * Vacía el carrito — se llama internamente después del checkout.
     * @param userId
     */
    void clear(Long userId);
}
