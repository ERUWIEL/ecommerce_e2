package org.itson.ecommerce_e2.services;

import java.util.List;
import org.itson.ecommerce_e2.models.Address;

public interface IAddressService {

    Address add(Long userId, String street, String number,
            String neighborhood, String city, String state,
            String country, String zipCode);

    Address findById(Long id);

    List<Address> findByUser(Long userId);

    /**
     * Marca esta dirección como predeterminada y quita el flag a las demás.
     * @param addressId
     * @param userId
     */
    void setDefault(Long addressId, Long userId);

    Address update(Address address);

    void delete(Long id);
}
