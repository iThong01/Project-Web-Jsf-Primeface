package com.greenmarket.repository;

import com.greenmarket.entity.Product;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ProductRepository extends GenericRepository<Product, Integer> {

    public ProductRepository() {
        super(Product.class);
    }

    public List<Product> searchByName(String name) {
        return em.createQuery("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(:query)", Product.class)
                 .setParameter("query", "%" + name.trim() + "%")
                 .getResultList();
    }
}
