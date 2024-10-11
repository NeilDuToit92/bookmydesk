package za.co.neildutoit.deskbooking.config;

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

//    public CustomOidcUserService(CustomConfig config) {
//        this.config = config;
//    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        Collection<? extends GrantedAuthority> authorities = oidcUser.getAuthorities();
        List<GrantedAuthority> updatedAuthorities = new ArrayList<>(authorities);
        //TODO: Is this always the email??
        String email = (String) oidcUser.getAttributes().get("preferred_username");
        if (email == null) {
            throw new SecurityException("Access Denied: No email found in OIDC");
        }
        String domain = email.substring(email.lastIndexOf("@") + 1);
        if (!config.systemConfig().getAllowedDomains().contains(domain.toLowerCase())) {
            //TODO: This needs to show an error page, it currently just reprompts auth
            throw new SecurityException("Access Denied: Invalid email domain");
        }
        userService.createLocalUser(oidcUser);
        return new DefaultOidcUser(updatedAuthorities, oidcUser.getIdToken());
    }
}