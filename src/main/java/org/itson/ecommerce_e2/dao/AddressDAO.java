package org.itson.ecommerce_e2.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;
import org.itson.ecommerce_e2.models.Address;

@ApplicationScoped
public class AddressDAO {

    @Inject
    private EntityManager em;

    public void create(Address address) {
        em.persist(address);
    }

    public Address findById(Long id) {
        return em.find(Address.class, id);
    }

    public List<Address> findByUserId(Long userId) {
        return em.createQuery(
                "SELECT a FROM Address a WHERE a.user.id = :userId",
                Address.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /**
     * Obtiene la dirección marcada como predeterminada del usuario. Se usa en
     * el checkout para pre-seleccionar la dirección de envío.
     */
    public Address findDefaultByUserId(Long userId) {
        try {
            return em.createQuery(
                    "SELECT a FROM Address a "
                    + "WHERE a.user.id = :userId AND a.defaultAddress = true",
                    Address.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Address update(Address address) {
        return em.merge(address);
    }

    public void delete(Long id) {
        Address a = findById(id);
        if (a != null) {
            em.remove(a);
        }
    }
}
