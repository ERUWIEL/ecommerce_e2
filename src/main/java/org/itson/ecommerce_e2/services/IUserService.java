package org.itson.ecommerce_e2.services;

import java.util.List;
import org.itson.ecommerce_e2.models.User;
import org.itson.ecommerce_e2.models.enums.Role;

public interface IUserService {

    /**
     * Registra un nuevo usuario y crea su carrito en la misma transacción.
     * @param name
     * @param email
     * @param password
     * @param role
     * @return 
     */
    User register(String name, String email, String password, Role role);

    /**
     * Autentica al usuario.
     *
     * @param email
     * @param password
     * @return el User si las credenciales son correctas, null si no.
     */
    User login(String email, String password);

    User findById(Long id);

    User findByEmail(String email);

    List<User> findAll();

    User updateProfile(User user);

    /**
     * Desactiva la cuenta sin borrarla — preserva historial de órdenes.
     * @param id
     */
    void deactivate(Long id);

    boolean existsByEmail(String email);
}
