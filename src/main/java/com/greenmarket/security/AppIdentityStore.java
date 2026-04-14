package com.greenmarket.security;

import com.greenmarket.entity.User;
import com.greenmarket.service.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import java.util.Collections;
import java.util.HashSet;

@ApplicationScoped
public class AppIdentityStore implements IdentityStore {

    @Inject
    private UserService userService;

    @Override
    public CredentialValidationResult validate(Credential credential) {
        if (credential instanceof UsernamePasswordCredential) {
            UsernamePasswordCredential userCred = (UsernamePasswordCredential) credential;
            User user = userService.login(userCred.getCaller(), userCred.getPasswordAsString());

            if (user != null) {
                return new CredentialValidationResult(user.getUser(), new HashSet<>(Collections.singletonList(user.getRole())));
            }
        }
        return CredentialValidationResult.INVALID_RESULT;
    }
}
