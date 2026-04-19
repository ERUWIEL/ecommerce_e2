package org.itson.ecommerce_e2.services.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.itson.ecommerce_e2.dao.AddressDAO;
import org.itson.ecommerce_e2.dao.UserDAO;
import org.itson.ecommerce_e2.models.Address;
import org.itson.ecommerce_e2.models.User;
import org.itson.ecommerce_e2.services.IAddressService;

@ApplicationScoped
public class AddressService implements IAddressService {

    @Inject
    private EntityManager em;
    @Inject
    private AddressDAO addressDAO;
    @Inject
    private UserDAO userDAO;

    @Override
    public Address add(Long userId, String street, String number, String neighborhood,
            String city, String state, String country, String zipCode) {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado: " + userId);
        }

        em.getTransaction().begin();
        try {
            Address address = new Address();
            address.setUser(user);
            address.setStreet(street);
            address.setNumber(number);
            address.setNeighborhood(neighborhood);
            address.setCity(city);
            address.setState(state);
            address.setCountry(country);
            address.setZipCode(zipCode);

            // Si es la primera dirección del usuario, marcarla como default
            List<Address> existing = addressDAO.findByUserId(userId);
            address.setDefaultAddress(existing.isEmpty());

            addressDAO.create(address);
            em.getTransaction().commit();
            return address;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public Address findById(Long id) {
        return addressDAO.findById(id);
    }

    @Override
    public List<Address> findByUser(Long userId) {
        return addressDAO.findByUserId(userId);
    }

    @Override
    public void setDefault(Long addressId, Long userId) {
        em.getTransaction().begin();
        try {
            // Quitar default a todas las direcciones del usuario
            List<Address> addresses = addressDAO.findByUserId(userId);
            for (Address a : addresses) {
                if (a.isDefaultAddress()) {
                    a.setDefaultAddress(false);
                    addressDAO.update(a);
                }
            }
            // Marcar la nueva default
            Address target = addressDAO.findById(addressId);
            if (target == null || !target.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("Dirección no válida para este usuario.");
            }
            target.setDefaultAddress(true);
            addressDAO.update(target);

            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public Address update(Address address) {
        em.getTransaction().begin();
        try {
            Address updated = addressDAO.update(address);
            em.getTransaction().commit();
            return updated;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public void delete(Long id) {
        em.getTransaction().begin();
        try {
            addressDAO.delete(id);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
