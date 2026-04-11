package controller;

import com.greenmarket.entity.User;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.Serializable;

@Named(value = "authBean")
@SessionScoped
public class AuthBean implements Serializable {
    @PersistenceContext(unitName = "GreenMarketDB")
    private EntityManager em;

    private String user;
    private String password;
    private boolean rememberMe;
    private User currentUser;

    public String login() {
        try {
            User userObj = em
                    .createQuery("SELECT u FROM User u WHERE u.user = :user AND u.password = :password", User.class)
                    .setParameter("user", user)
                    .setParameter("password", password)
                    .getSingleResult();
            currentUser = userObj;

            handleRememberMeCookie();
            return "/index.xhtml?faces-redirect=true";

        } catch (NoResultException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login Failed", "Username หรือรหัสผ่านไม่ถูกต้อง"));

            return null;
        }
    }

    private void handleRememberMeCookie() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

        if (rememberMe) {
            Cookie userCookie = new Cookie("remember_user", currentUser.getUser());
            userCookie.setMaxAge(60 * 60 * 24 * 10);
            userCookie.setPath("/");
            response.addCookie(userCookie);
        } else {
            Cookie userCookie = new Cookie("remember_user", "");
            userCookie.setMaxAge(0);
            userCookie.setPath("/");
            response.addCookie(userCookie);
        }
    }

    public String logout() {
        currentUser = null;
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

        return "/login/login.xhtml?faces-redirect=true";
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}
