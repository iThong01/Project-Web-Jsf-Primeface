package controller;

import com.greenmarket.entity.Product;
import com.greenmarket.service.ProductService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import jakarta.annotation.PostConstruct;
import org.primefaces.model.file.UploadedFile;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

@Named(value = "productBean")
@ViewScoped
public class ProductBean implements Serializable {

    @Inject
    private ProductService productService;

    private List<Product> productList;
    private Product newProduct = new Product();
    private UploadedFile uploadedImage;
    private String searchKeyword;
    private String searchQuery;
    private List<Product> searchResultList;

    @PostConstruct
    public void init() {
        loadProducts();
    }

    public void loadProducts() {
        productList = productService.getAllProducts();
    }

    public void loadSearchResults() {
        searchResultList = productService.searchProducts(searchQuery);
    }

    public void saveProduct() {
        try {
            byte[] imageBytes = (uploadedImage != null) ? uploadedImage.getContent() : null;
            productService.saveOrUpdateProduct(newProduct, imageBytes);
            
            String msg = (newProduct.getId() == null) ? "เพิ่มสินค้าเรียบร้อย" : "แก้ไขสินค้าเรียบร้อย";
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(msg));
            
            newProduct = new Product();
            uploadedImage = null;
            loadProducts();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "เกิดข้อผิดพลาด", e.getMessage()));
        }
    }

    public void deleteProduct(Product product) {
        try {
            productService.deleteProduct(product.getId());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("ลบสินค้าเรียบร้อย"));
            loadProducts();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "เกิดข้อผิดพลาดในการลบ", e.getMessage()));
        }
    }

    public void editProduct(Product product) {
        this.newProduct = product;
    }

    public List<String> completeProduct(String query) {
        String queryLowerCase = query.toLowerCase();
        List<String> suggestions = new java.util.ArrayList<>();
        if (productList != null) {
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(queryLowerCase)) {
                    suggestions.add(product.getName());
                }
            }
        }
        return suggestions;
    }

    public void onProductSelect(org.primefaces.event.SelectEvent<String> event) {
        try {
            String selectedName = event.getObject();
            FacesContext.getCurrentInstance().getExternalContext().redirect(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/shop/search.xhtml?query=" + java.net.URLEncoder.encode(selectedName, "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Getters and Setters
    public List<Product> getProductList() { return productList; }
    public void setProductList(List<Product> productList) { this.productList = productList; }
    public Product getNewProduct() { return newProduct; }
    public void setNewProduct(Product newProduct) { this.newProduct = newProduct; }
    public UploadedFile getUploadedImage() { return uploadedImage; }
    public void setUploadedImage(UploadedFile uploadedImage) { this.uploadedImage = uploadedImage; }
    public String getSearchKeyword() { return searchKeyword; }
    public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }
    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
    public List<Product> getSearchResultList() { return searchResultList; }
    public void setSearchResultList(List<Product> searchResultList) { this.searchResultList = searchResultList; }
}
