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
import java.util.Date;
import jakarta.transaction.Transactional;

import com.greenmarket.util.CookieUtil;
import com.greenmarket.util.JwtUtil;

@Named(value = "authBean")
@SessionScoped
public class AuthBean implements Serializable {
    @PersistenceContext(unitName = "GreenMarketDB")
    private EntityManager em;
    private boolean rememberMe;
    private User currentUser;
    private String firstName;
    private String lastName;
    private Date birthDay;
    private String email;
    private String user;
    private String password;

    
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(Date birthDay) {
        this.birthDay = birthDay;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
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

    public String login() {
        try {
            User userObj = em
                    .createQuery("SELECT u FROM User u WHERE u.user = :user AND u.password = :password", User.class)
                    .setParameter("user", user)
                    .setParameter("password", password)
                    .getSingleResult();

            String token = JwtUtil.generateToken(userObj.getUser(), userObj.getRole());
            HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            CookieUtil.addCookie(response, "AUTH_TOKEN", token, 60*60*24, true);

            this.currentUser = userObj;
            if (rememberMe) CookieUtil.addCookie(response, "remember_user", user, 60*60*24*10, false);
            else CookieUtil.removeCookie(response, "remember_user");
           
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
        this.currentUser = null;
        this.user = null;
        this.password = null;
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();

        CookieUtil.removeCookie(response, "AUTH_TOKEN");
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/index.xhtml?faces-redirect=true";
    }

    @jakarta.transaction.Transactional
    public String register(){
        try{
            User newUser = new User();
            newUser.setName(this.firstName);
            newUser.setLname(this.lastName);
            newUser.setEmail(this.email);
            newUser.setUser(this.user);
            newUser.setPassword(this.password);
            newUser.setRole("customer");

            em.persist(newUser);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Success","สมัครเรียบร้อย"));
            return "/login/login.xhtml?faces-redirect=true";
        }catch(Exception e){
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Register Fail","สมัครไม่สำเร็จ"));
            return null;
        }
    }
}
