/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import com.greenmarket.entity.Product;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.annotation.PostConstruct;
import org.primefaces.model.file.UploadedFile;

@Named(value = "productBean")
@RequestScoped
public class ProductBean {

    @PersistenceContext(unitName = "GreenMarketDB")
    private EntityManager em;
    private List<Product> productList;
    private Product newProduct = new Product();
    private UploadedFile uploadedImage;

    public List<Product> getProductList() {
        return productList;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }

    public Product getNewProduct() {
        return newProduct;
    }

    public void setNewProduct(Product newProduct) {
        this.newProduct = newProduct;
    }

    public UploadedFile getUploadedImage() {
        return uploadedImage;
    }

    public void setUploadedImage(UploadedFile uploadedImage) {
        this.uploadedImage = uploadedImage;
    }
    
    @PostConstruct
    public void init() {
        loadProducts();
    }

    public void loadProducts() {
        productList = em.createQuery("SELECT p FROM Product p", Product.class).getResultList();
    }

    @Transactional
    public void saveProduct() {
        // if (currentUser != null && "admin".equals(currentUser.getRole())) {
        try {
            if (uploadedImage != null && uploadedImage.getSize() > 0) {
                     newProduct.setImage(uploadedImage.getContent());
                 }
            em.persist(newProduct);
            newProduct = new Product();
            uploadedImage = null;
            loadProducts();
        } catch (Exception e) {
            System.err.println("Error saving product : " + e.getMessage());
        }
        
    }
}
