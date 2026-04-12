/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import com.greenmarket.entity.Product;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import jakarta.annotation.PostConstruct;
import org.primefaces.model.file.UploadedFile;
import java.io.Serializable;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

@Named(value = "productBean")
@ViewScoped
public class ProductBean implements Serializable {

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
        try {
            if (uploadedImage != null && uploadedImage.getSize() > 0) {
                newProduct.setImage(uploadedImage.getContent());
            }
            if (newProduct.getId() == null) {
                em.persist(newProduct);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("เพิ่มสินค้าเรียบร้อย"));
            } else {
                em.merge(newProduct);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("แก้ไขสินค้าเรียบร้อย"));
            }
            newProduct = new Product();
            uploadedImage = null;
            loadProducts();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "เกิดข้อผิดพลาด", e.getMessage()));
            System.err.println("Error saving product : " + e.getMessage());
        }
    }

    public void editProduct(Product product) {
        this.newProduct = product;
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "แก้ไข", "กำลังแก้ไขสินค้า: " + product.getName()));
    }

    @Transactional
    public void deleteProduct(Product product) {
        try {
            Product p = em.find(Product.class, product.getId());
            if (p != null) {
                em.remove(p);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("ลบสินค้าเรียบร้อย"));
                loadProducts();
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "เกิดข้อผิดพลาดในการลบ", e.getMessage()));
            System.err.println("Error deleting product : " + e.getMessage());
        }
    }
}
