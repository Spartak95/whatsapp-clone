package com.xcoder.whatsapp.clone.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt source) {
        return new JwtAuthenticationToken(source, getGrantedAuthorities(source));
    }

    private Set<GrantedAuthority> getGrantedAuthorities(Jwt source) {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        Collection<GrantedAuthority> grantedAuthorities = jwtGrantedAuthoritiesConverter.convert(source);
        Stream<GrantedAuthority> grantedAuthorityStream = grantedAuthorities.stream();
        Stream<? extends GrantedAuthority> resourceRolesStream = extractResourceRoles(source).stream();

        return Stream.concat(grantedAuthorityStream, resourceRolesStream).collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<Object, Object> resourceAccess = new HashMap<>(jwt.getClaim("resource_access"));
        Map<String, List<String>> eternal = (Map<String, List<String>>) resourceAccess.get("account");
        List<String> roles = eternal.get("roles");

        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.replace("-", "_")))
            .collect(Collectors.toSet());
    }
}
