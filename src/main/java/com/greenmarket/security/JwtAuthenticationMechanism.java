package com.greenmarket.security;

import com.greenmarket.util.CookieUtil;
import com.greenmarket.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashSet;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import java.util.logging.Logger;
import java.util.Set;
import io.jsonwebtoken.ExpiredJwtException;
import java.io.IOException;

@ApplicationScoped
public class JwtAuthenticationMechanism implements HttpAuthenticationMechanism {

    private static final Logger log = Logger.getLogger(JwtAuthenticationMechanism.class.getName());

    @Inject
    private IdentityStoreHandler identityStoreHandler;

    @Override
    public AuthenticationStatus validateRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpMessageContext httpMessageContext) {

        String uri = request.getRequestURI();
        if (uri.contains("/javax.faces.resource/")) {
            return httpMessageContext.doNothing();
        }

        if (httpMessageContext.getAuthParameters().getCredential() 
                instanceof UsernamePasswordCredential) {

            UsernamePasswordCredential credential =
                (UsernamePasswordCredential) httpMessageContext.getAuthParameters().getCredential();

            CredentialValidationResult result = identityStoreHandler.validate(credential);

            if (result.getStatus() == CredentialValidationResult.Status.VALID) {
                Set<String> groups = result.getCallerGroups();
                if (groups == null || groups.isEmpty()) {
                    return httpMessageContext.responseUnauthorized();
                }

                String username = result.getCallerPrincipal().getName();
                String role = groups.iterator().next();
                String token = JwtUtil.generateToken(username, role);
                CookieUtil.addCookie(response, "AUTH_TOKEN", token, 60 * 60 * 24,true);

                return httpMessageContext.notifyContainerAboutLogin(
                    result.getCallerPrincipal(), groups
                );
            }
            return httpMessageContext.responseUnauthorized();
        }

        String token = CookieUtil.getCookieValue(request, "AUTH_TOKEN");
        if (token != null && !token.trim().isEmpty()) {
            try {
                Claims claims = JwtUtil.validateToken(token);
                String username = claims.getSubject();
                String role = claims.get("role", String.class);

                if (username != null && role != null) {
                    return httpMessageContext.notifyContainerAboutLogin(
                        username,
                        new HashSet<>(Collections.singletonList(role))
                    );
                }
            } catch (ExpiredJwtException e) {
                log.info("Token expired: " + request.getRequestURI());
                CookieUtil.removeCookie(response, "AUTH_TOKEN");
            } catch (Exception e) {
                log.warning("Token invalid: " + e.getMessage());
                CookieUtil.removeCookie(response, "AUTH_TOKEN");
            }
        }

        if (httpMessageContext.isProtected()) {
            try {
                response.sendRedirect(request.getContextPath() + "/login/login.xhtml");
                return AuthenticationStatus.SEND_CONTINUE;
            } catch (IOException e) {
                log.severe("Redirect failed: " + e.getMessage());
                return AuthenticationStatus.SEND_FAILURE;
            }
        }

        return httpMessageContext.doNothing();
    }
}