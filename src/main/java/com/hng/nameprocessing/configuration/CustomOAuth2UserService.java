package com.hng.nameprocessing.configuration;

import com.hng.nameprocessing.dtos.User;
import com.hng.nameprocessing.dtos.UserDto;
import com.hng.nameprocessing.repositories.UserRepository;
import com.hng.nameprocessing.services.UserService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;
  private final UserService userService;

  public CustomOAuth2UserService(UserRepository userRepository, UserService userService) {
    this.userRepository = userRepository;
    this.userService = userService;
  }

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

    OAuth2User oauthUser = super.loadUser(userRequest);

    String githubId = oauthUser.getAttribute("id").toString();
    String username = oauthUser.getAttribute("login");
    String avatarUrl = oauthUser.getAttribute("avatar_url");
    String email = oauthUser.getAttribute("email");

    User user = userService.findOrCreateUser(githubId, username, email, avatarUrl);
    UserDto userDto = new UserDto(githubId, username, email, user.getRole().name());

    List<GrantedAuthority> authorities = new ArrayList<>();

    authorities.addAll(oauthUser.getAuthorities());

    authorities.add(new SimpleGrantedAuthority(user.getRole().name()));

    return new CustomPrincipal(userDto, oauthUser, authorities);
  }
}
