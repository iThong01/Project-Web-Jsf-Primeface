package controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Product;
import util.CookieUtil;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dto.CartItem;
import jakarta.inject.Inject;

@Named(value = "cartBean")
@RequestScoped
public class CartBean implements Serializable {

    @PersistenceContext(unitName = "GreenMarketDB")
    private EntityManager em;

    @Inject
    private AuthBean authBean;

    private static final String BASE_CART_COOKIE_NAME = "SHOPPING_CART";

    private Map<Object, Object> quantityMap = new HashMap<Object, Object>() {
        @Override
        public Object get(Object key) {
            Object val = super.get(key);
            return val != null ? val : 1;
        }
    };

    public Map<Object, Object> getQuantityMap() {
        if (quantityMap == null) {
            quantityMap = new HashMap<Object, Object>() {
                @Override
                public Object get(Object key) {
                    Object val = super.get(key);
                    return val != null ? val : 1;
                }
            };
        }
        return quantityMap;
    }

    public void setQuantityMap(Map<Object, Object> quantityMap) {
        this.quantityMap = quantityMap;
    }

    private HttpServletRequest getRequest() {
        return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    }

    private HttpServletResponse getResponse() {
        return (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
    }

    private String getCartCookieName() {
        if (authBean != null && authBean.getCurrentUser() != null) {
            String rawUser = authBean.getCurrentUser().getUser();
            if (rawUser != null && !rawUser.isEmpty()) {
                return BASE_CART_COOKIE_NAME + "_" + rawUser.replaceAll("[^a-zA-Z0-9]", "");
            }
        }
        return BASE_CART_COOKIE_NAME;
    }

    private Map<Integer, Integer> currentCartMap;

    private Map<Integer, Integer> getCartMap() {
        if (currentCartMap == null) {
            String cookieName = getCartCookieName();
            String cartCookie = CookieUtil.getCookieValue(getRequest(), cookieName);
            currentCartMap = parseCartCookie(cartCookie);
        }
        return currentCartMap;
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public void addToCart(Integer productId) throws IOException {
        try {
            Map<Integer, Integer> cartMap = getCartMap();

            getQuantityMap(); 

            int qtyToAdd = getQuantityToAdd(productId);

            cartMap.put(productId, cartMap.getOrDefault(productId, 0) + qtyToAdd);

            String cookieName = getCartCookieName();
            String newCartStr = buildCartCookieString(cartMap);
            CookieUtil.addCookie(getResponse(), cookieName, newCartStr, 60 * 60 * 24 * 7, false);

            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Added " + qtyToAdd + " item(s) to cart");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error in AddToCart", e.getMessage());
            e.printStackTrace();
        }
    }

    public String removeFromCart(Integer productId) {
        Map<Integer, Integer> cartMap = getCartMap();

        if (cartMap.containsKey(productId)) {
            cartMap.remove(productId);
            String cookieName = getCartCookieName();
            String newCartStr = buildCartCookieString(cartMap);
            CookieUtil.addCookie(getResponse(), cookieName, newCartStr, 60 * 60 * 24 * 7, false);
        }
        
        return "/shop/basket.xhtml?faces-redirect=true";
    }

    public void clearCart() {
        currentCartMap = new HashMap<>();
        CookieUtil.removeCookie(getResponse(), getCartCookieName());
    }

    public List<CartItem> getCartItems() {
        Map<Integer, Integer> cartMap = getCartMap();
        List<CartItem> items = new ArrayList<>();

        if (!cartMap.isEmpty()) {
            for (Map.Entry<Integer, Integer> entry : cartMap.entrySet()) {
                Product p = em.find(Product.class, entry.getKey());
                if (p != null) {
                    items.add(new CartItem(p, entry.getValue()));
                }
            }
        }
        return items;
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : getCartItems()) {
            total += item.getTotal();
        }
        return total;
    }

    public int getAvailableStock(Product p) {
        if (p == null) return 0;
        Map<Integer, Integer> cartMap = getCartMap();
        int inCart = cartMap.getOrDefault(p.getId(), 0);
        int available = p.getCount() - inCart;
        return available > 0 ? available : 0;
    }

    private int getQuantityToAdd(Integer productId) {
        for (Map.Entry<Object, Object> entry : quantityMap.entrySet()) {
            if (entry.getKey() != null && String.valueOf(entry.getKey()).equals(String.valueOf(productId))) {
                Object val = entry.getValue();
                if (val instanceof Number) {
                    return ((Number) val).intValue();
                } else if (val != null) {
                    try {
                        return Integer.parseInt(val.toString());
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return 1;
    }

    private Map<Integer, Integer> parseCartCookie(String cookieValue) {
        Map<Integer, Integer> map = new HashMap<>();
        if (cookieValue == null || cookieValue.trim().isEmpty()) {
            return map;
        }

        try {
            cookieValue = URLDecoder.decode(cookieValue, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            System.err.println("Failed to decode cart cookie: " + e.getMessage());
        }

        String[] pairs = cookieValue.split("_");
        for (String pair : pairs) {
            String[] kv = pair.split("-");
            if (kv.length == 2) {
                try {
                    map.put(Integer.parseInt(kv[0].trim()), Integer.parseInt(kv[1].trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return map;
    }

    private String buildCartCookieString(Map<Integer, Integer> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("_");
            }
            sb.append(entry.getKey()).append("-").append(entry.getValue());
        }
        return sb.toString();
    }
}