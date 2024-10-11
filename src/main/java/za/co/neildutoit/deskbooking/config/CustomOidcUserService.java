package za.co.neildutoit.deskbooking.config;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import za.co.neildutoit.deskbooking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final CustomConfig config;
    private final UserService userService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        Collection<? extends GrantedAuthority> authorities = oidcUser.getAuthorities();
        List<GrantedAuthority> updatedAuthorities = new ArrayList<>(authorities);
        String email = (String) oidcUser.getAttributes().get("preferred_username");
        if (email == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token"), "No email found in OIDC");
        }
        String domain = email.substring(email.lastIndexOf("@") + 1);
        if (!config.systemConfig().getAllowedDomains().contains(domain.toLowerCase())) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token"), "Invalid email domain");
        }
         userService.createOrUpdateLocalUser(oidcUser);
        return new DefaultOidcUser(updatedAuthorities, oidcUser.getIdToken());
    }
}