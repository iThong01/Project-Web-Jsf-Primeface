package com.greenmarket.dao;

import com.greenmarket.model.Product;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ProductDAO extends GenericDAO<Product, Integer> {

    public ProductDAO() {
        super(Product.class);
    }

    public List<Product> searchByName(String name) {
        return em.createQuery("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(:query)", Product.class)
                 .setParameter("query", "%" + name.trim() + "%")
                 .getResultList();
    }
}
