package com.hng.nameprocessing.configuration;

import com.hng.nameprocessing.dtos.UserDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomPrincipal implements OAuth2User {

    private final UserDto user;
    private final OAuth2User oauth2User;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomPrincipal(UserDto user, OAuth2User oauth2User, Collection<? extends GrantedAuthority> authorities) {
        this.user = user;
        this.oauth2User = oauth2User;
        this.authorities = authorities;
    }

    public UserDto getUser() {
        return user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return oauth2User.getAttribute("id").toString();
    }
}
