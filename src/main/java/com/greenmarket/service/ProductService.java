package com.greenmarket.service;

import com.greenmarket.model.Product;
import com.greenmarket.dao.ProductDAO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class ProductService {

    @Inject
    private ProductDAO productRepo;

    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return productRepo.searchByName(query);
    }

    @Transactional
    public void saveOrUpdateProduct(Product product, byte[] imageContent) {
        if (imageContent != null && imageContent.length > 0) {
            product.setImage(imageContent);
        }
        
        if (product.getId() == null) {
            productRepo.save(product);
        } else {
            productRepo.update(product);
        }
    }

    @Transactional
    public void deleteProduct(Integer id) {
        productRepo.delete(id);
    }
}
