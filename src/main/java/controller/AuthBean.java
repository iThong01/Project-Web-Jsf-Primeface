package controller;

import com.greenmarket.model.User;
import com.greenmarket.service.UserService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Date;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;

import com.greenmarket.util.CookieUtil;
import com.greenmarket.util.JwtUtil;

@Named(value = "authBean")
@SessionScoped
public class AuthBean implements Serializable {

    @Inject
    private UserService userService;

    @Inject
    private SecurityContext securityContext;

    private boolean rememberMe;
    private User currentUser;
    private String firstName;
    private String lastName;
    private Date birthDay;
    private String email;
    private String user;
    private String password;

    private String emailForForgot;
    private User foundUser;
    private String newPassword;

    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public User getCurrentUser() {
        if (currentUser == null) {
            String username = securityContext.getCallerPrincipal() != null
                    ? securityContext.getCallerPrincipal().getName()
                    : null;
            if (username != null) {
                currentUser = userService.getUserByUsername(username);
            }
        }
        return currentUser;
    }

    public String login() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        try {
            AuthenticationStatus status = securityContext.authenticate(
                    request, response,
                    AuthenticationParameters.withParams().credential(new UsernamePasswordCredential(user, password)));

            if (status == AuthenticationStatus.SUCCESS) {
                User userObj = userService.getUserByUsername(user);
                if (userObj != null) {
                    String token = JwtUtil.generateToken(userObj.getUser(), userObj.getRole());
                    CookieUtil.addCookie(response, "AUTH_TOKEN", token, 60 * 60 * 24, true);

                    this.currentUser = userObj;
                    if (rememberMe) {
                        CookieUtil.addCookie(response, "remember_user", user, 60 * 60 * 24 * 10, true);
                    } else {
                        CookieUtil.removeCookie(response, "remember_user");
                    }
                    if ("admin".equals(userObj.getRole())) {
                        return "/shop/manage.xhtml?faces-redirect=true";
                    }
                    return "/index.xhtml?faces-redirect=true";
                }
            } else if (status == AuthenticationStatus.SEND_FAILURE || status == AuthenticationStatus.NOT_DONE) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login Failed",
                        "Username หรือรหัสผ่านไม่ถูกต้อง"));
                return null;
            }
        } catch (Exception e) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "เกิดข้อผิดพลาด: " + e.getMessage()));
            e.printStackTrace();
        }
        return null;
    }

    public String logout() {
        this.currentUser = null;
        this.user = null;
        this.password = null;
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
                .getResponse();
        CookieUtil.removeCookie(response, "AUTH_TOKEN");
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/index.xhtml?faces-redirect=true";
    }

    public String register() {
        try {
            User newUser = new User();
            newUser.setName(this.firstName);
            newUser.setLname(this.lastName);
            newUser.setEmail(this.email);
            newUser.setUser(this.user);
            newUser.setPassword(this.password);

            userService.register(newUser);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "สมัครเรียบร้อย"));
            return "/login/login.xhtml?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Register Fail", "สมัครไม่สำเร็จ"));
            return null;
        }
    }

    public void findUserByEmail() {
        foundUser = userService.getUserByEmail(emailForForgot);
        if (foundUser != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "พบข้อมูลผู้ใช้", "กรุณาตั้งรหัสผ่านใหม่"));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "ไม่พบข้อมูล", "ไม่พบผู้ใช้ที่ใช้ Email นี้"));
        }
    }

    public String updatePassword() {
        try {
            if (foundUser != null && newPassword != null && !newPassword.isEmpty()) {
                userService.updatePassword(foundUser.getId(), newPassword);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "สำเร็จ", "เปลี่ยนรหัสผ่านเรียบร้อยแล้ว"));
                foundUser = null;
                emailForForgot = null;
                newPassword = null;
                return "/login/login.xhtml?faces-redirect=true";
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "ผิดพลาด", "ไม่สามารถเปลี่ยนรหัสผ่านได้"));
        }
        return null;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
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

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public String getEmailForForgot() {
        return emailForForgot;
    }

    public void setEmailForForgot(String emailForForgot) {
        this.emailForForgot = emailForForgot;
    }

    public User getFoundUser() {
        return foundUser;
    }

    public void setFoundUser(User foundUser) {
        this.foundUser = foundUser;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
