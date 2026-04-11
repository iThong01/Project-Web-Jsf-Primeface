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

@DataSourceDefinition(
        name = "java:app/jdbc/GreenMarketDS2",
        className = "com.mysql.cj.jdbc.MysqlDataSource",
        url = "jdbc:mysql://localhost:3306/greenmarket?serverTimezone=UTC&sslMode=DISABLED&allowPublicKeyRetrieval=true",
        user = "root",
        password = "T12345678"
)
@Named(value = "productBean")
@RequestScoped
public class ProductBean {

    @PersistenceContext(unitName = "GreenMarketDB")
    private EntityManager em;
    private List<Product> productList;
    private Product newProduct = new Product();

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

    @PostConstruct
    public void init() {
        loadProducts();
    }

    public void loadProducts() {
        productList = em.createQuery("SELECT p FROM Product p", Product.class).getResultList();
    }

    @Transactional
    public void saveProduct() {
        try {
            em.persist(newProduct);
            newProduct = new Product();
            loadProducts();
        } catch (Exception e) {
            System.err.println("Error saving product : " + e.getMessage());
        }
    }
}
