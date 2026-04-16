package controller;

import io.jsonwebtoken.Claims;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import model.OrderItem;
import model.Product;
import model.Transaction;
import model.User;
import util.CookieUtil;
import util.JwtUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import dto.CartItem;

@Named(value = "paymentBean")
@RequestScoped
public class PaymentBean implements Serializable {

    @PersistenceContext(unitName = "GreenMarketDB")
    private EntityManager em;

    @Inject
    private CartBean cartBean;

    @Inject
    private AuthBean authBean;

    @Transactional
    public String processPayment() {
        List<CartItem> cartItems = cartBean.getCartItems();

        if (cartItems == null || cartItems.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "ตะกร้าว่าง", "กรุณาเพิ่มสินค้าก่อน");
            return null;
        }

        User currentUser = getCurrentOrRecoveredUser();

        if (currentUser == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "การเข้าสู่ระบบหมดอายุ", "กรุณาเข้าสู่ระบบใหม่");
            return "/login/login.xhtml?faces-redirect=true";
        }

        try {
            Transaction tx = createTransaction(currentUser, cartBean.getTotalPrice(), calculateTotalItems(cartItems));
            em.persist(tx);

            for (CartItem item : cartItems) {
                Product product = em.find(Product.class, item.getProduct().getId());

                if (product.getCount() < item.getQuantity()) {
                    addMessage(FacesMessage.SEVERITY_ERROR, "สินค้าไม่เพียงพอ", "สินค้า " + product.getName() + " มีจำนวนไม่พอ (เหลือ " + product.getCount() + " ชิ้น)");
                    return null;
                }

                product.setCount(product.getCount() - item.getQuantity());
                em.merge(product);

                OrderItem orderItem = createOrderItem(tx, product, item);
                em.persist(orderItem);
            }

            cartBean.clearCart();
            
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            addMessage(FacesMessage.SEVERITY_INFO, "ชำระเงินสำเร็จ", "ขอบคุณสำหรับการสั่งซื้อ");

            return "/shop/shop.xhtml?faces-redirect=true";

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "เกิดข้อผิดพลาดในการชำระเงิน", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private User getCurrentOrRecoveredUser() {
        User user = authBean.getCurrentUser();
        if (user != null) {
            return user;
        }

        try {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            String token = CookieUtil.getCookieValue(request, "AUTH_TOKEN");

            if (token != null && !token.trim().isEmpty()) {
                Claims claims = JwtUtil.validateToken(token);
                String username = claims.getSubject();

                if (username != null) {
                    user = em.createQuery("SELECT u FROM User u WHERE u.user = :user", User.class)
                            .setParameter("user", username)
                            .getSingleResult();
                    authBean.setCurrentUser(user); // Restore session
                }
            }
        } catch (Exception e) {
            System.err.println("Session recovery failed: " + e.getMessage());
        }

        return user;
    }

    private Transaction createTransaction(User user, double totalPrice, int totalItems) {
        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setCreateAt(new Date());
        tx.setStatus("SUCCESS");
        tx.setTotalprice(totalPrice);
        tx.setTotalitem(totalItems);
        return tx;
    }

    private OrderItem createOrderItem(Transaction tx, Product product, CartItem item) {
        OrderItem orderItem = new OrderItem();
        orderItem.setTransaction(tx);
        orderItem.setProduct(product);
        orderItem.setQuantity(item.getQuantity());
        orderItem.setPriceAtPurchase(product.getPrice());
        orderItem.setTotalprice(item.getTotal());
        return orderItem;
    }

    private int calculateTotalItems(List<CartItem> cartItems) {
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getQuantity();
        }
        return total;
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
}