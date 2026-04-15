package controller;

import com.greenmarket.dto.CartItem;
import com.greenmarket.entity.Product;
import com.greenmarket.util.CookieUtil;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

@Named(value = "cartBean")
@RequestScoped
public class CartBean implements Serializable {

    @PersistenceContext(unitName = "GreenMarketDB")
    private EntityManager em;

    @Inject
    private AuthBean authBean;

    private Map<Integer, Integer> quantityMap = new HashMap<Integer, Integer>() {
        @Override
        public Integer get(Object key) {
            return super.containsKey(key) ? super.get(key) : 1;
        }
    };

    public Map<Integer, Integer> getQuantityMap() {
        return quantityMap;
    }

    public void setQuantityMap(Map<Integer, Integer> quantityMap) {
        this.quantityMap = quantityMap;
    }

    private static final String BASE_CART_COOKIE_NAME = "SHOPPING_CART";

    private String getCartCookieName() {
        if (authBean != null && authBean.getCurrentUser() != null) {
            String rawUser = authBean.getCurrentUser().getUser();
            if (rawUser != null) {
                return BASE_CART_COOKIE_NAME + "_" + rawUser.replaceAll("[^a-zA-Z0-9]", "");
            }
        }
        return BASE_CART_COOKIE_NAME;
    }
public void addToCart(Integer productId) throws IOException {
    try {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

        String cookieName = getCartCookieName();
        String cartCookie = CookieUtil.getCookieValue(request, cookieName);
        Map<Integer, Integer> cartMap = parseCartCookie(cartCookie);

        if (quantityMap == null) {
            quantityMap = new HashMap<>();
        }

        int qtyToAdd = 1;
        for (Map.Entry<?, Integer> entry : quantityMap.entrySet()) {
            if (entry.getKey() != null && String.valueOf(entry.getKey()).equals(String.valueOf(productId))) {
                Object val = entry.getValue();
                if (val instanceof Number) {
                    qtyToAdd = ((Number) val).intValue();
                } else if (val instanceof String) {
                    try {
                        qtyToAdd = Integer.parseInt((String) val);
                    } catch (NumberFormatException e) {
                    }
                }
                break;
            }
        }

        cartMap.put(productId, cartMap.getOrDefault(productId, 0) + qtyToAdd);

        String newCartStr = buildCartCookieString(cartMap);

        CookieUtil.addCookie(response, cookieName, newCartStr, 60 * 60 * 24 * 7, false);

        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Success", "Added " + qtyToAdd + " item(s) to cart"));
    } catch (Throwable t) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error in AddToCart", t.toString()));
        t.printStackTrace();
    }
}

    public String removeFromCart(Integer productId) {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

        String cookieName = getCartCookieName();
        String cartCookie = CookieUtil.getCookieValue(request, cookieName);
        Map<Integer, Integer> cartMap = parseCartCookie(cartCookie);

        Integer keyToRemove = null;
        for (Integer key : cartMap.keySet()) {
            if (String.valueOf(key).equals(String.valueOf(productId))) {
                keyToRemove = key;
                break;
            }
        }

        if (keyToRemove != null) {
            cartMap.remove(keyToRemove);
            String newCartStr = buildCartCookieString(cartMap);
            CookieUtil.addCookie(response, cookieName, newCartStr, 60 * 60 * 24 * 7, false);
        }
        
        return "/shop/basket.xhtml?faces-redirect=true";
    }

    public List<CartItem> getCartItems() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();

        String cookieName = getCartCookieName();
        String cartCookie = CookieUtil.getCookieValue(request, cookieName);

        System.out.println(">>> Cart cookie in getCartItems: " + cartCookie);

        Map<Integer, Integer> cartMap = parseCartCookie(cartCookie);
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

    private Map<Integer, Integer> parseCartCookie(String cookieValue) {
        Map<Integer, Integer> map = new HashMap<>();
        if (cookieValue == null || cookieValue.isEmpty())
            return map;

        try {
            cookieValue = java.net.URLDecoder.decode(cookieValue, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
        }
        String[] pairs = cookieValue.split("_");
        for (String pair : pairs) {
            String[] kv = pair.split("-");
            if (kv.length == 2) {
                try {
                    map.put(Integer.parseInt(kv[0].trim()),
                            Integer.parseInt(kv[1].trim()));
                } catch (NumberFormatException e) {
                }
            }
        }
        return map;
    }

    private String buildCartCookieString(Map<Integer, Integer> map) {
        if (map.isEmpty())
            return "";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (sb.length() > 0)
                sb.append("_");
            sb.append(entry.getKey()).append("-").append(entry.getValue());
        }
        return sb.toString();
    }

    public void clearCart() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
        String cookieName = getCartCookieName();
        CookieUtil.removeCookie(response, cookieName);
    }
}