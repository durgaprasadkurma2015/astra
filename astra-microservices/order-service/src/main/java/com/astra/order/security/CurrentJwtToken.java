
package com.astra.order.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CurrentJwtToken {

    public String getToken() {
        var authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new IllegalStateException(
                    "JWT authentication is required");
        }

        return jwtAuth.getToken().getTokenValue();
    }
}
