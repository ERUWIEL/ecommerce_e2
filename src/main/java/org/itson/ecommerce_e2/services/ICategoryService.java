package org.itson.ecommerce_e2.services;

import java.util.List;
import org.itson.ecommerce_e2.models.Category;

public interface ICategoryService {

    Category create(String name, String description);

    Category findById(Long id);

    List<Category> findAll();

    Category update(Category category);

    void delete(Long id);
}
