package com.hng.nameprocessing.configuration;


import com.hng.nameprocessing.configuration.components.ApiVersionFilter;
import com.hng.nameprocessing.security.CookieDebugFilter;
import com.hng.nameprocessing.security.KeyUtils;
import com.hng.nameprocessing.security.RateLimitingFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.ObjectMapper;
import java.util.concurrent.Executor;

@Slf4j
@EnableAsync
@Configuration
public class ConfigurationFile {

    public ObjectMapper objectMapper;
    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final Auth2LoginSuccessHandler successHandler;
    private final CustomLogoutSuccessHandler logoutSuccessHandler;
    private final CookieDebugFilter cookieDebugFilter;
    private final RateLimitingFilter rateLimitingFilter;


    public ConfigurationFile(ObjectMapper objectMapper, Auth2LoginSuccessHandler successHandler, CustomLogoutSuccessHandler logoutSuccessHandler, CookieDebugFilter cookieDebugFilter, RateLimitingFilter rateLimitingFilter) {
        this.objectMapper = objectMapper;
        this.successHandler = successHandler;
        this.logoutSuccessHandler = logoutSuccessHandler;
        this.cookieDebugFilter = cookieDebugFilter;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(40);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("AsynchThread-");
        executor.initialize();
        return executor;
    }


    @Bean
    @Order(1)
    public SecurityFilterChain authApiChain(HttpSecurity http) throws Exception {

        http
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .securityMatcher("/auth/token", "/auth/refresh")

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )

                .cors(cors -> Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain oauthChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/auth/**", "/login/**")

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )


                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(endpoint ->
                                endpoint.baseUri("/auth")
                        )
                        .redirectionEndpoint(endpoint ->
                                endpoint.baseUri("/auth/*/callback")
                        )
                        .successHandler(successHandler)
                )

                .logout(logout ->
                        logout.logoutUrl("/auth/logout")
                                .logoutSuccessHandler(logoutSuccessHandler)
                )

                .cors(cors -> Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/api/**")

                .addFilterAfter(cookieDebugFilter,
                        BearerTokenAuthenticationFilter.class)

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login", "/api/debug").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/profiles")
                        .hasAnyRole("ADMIN", "ANALYST")

                        .requestMatchers(HttpMethod.POST, "/api/profiles")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/profiles/**")
                        .hasRole("ADMIN")

                        .anyRequest().hasAnyRole("ANALYST", "ADMIN")
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )

                .cors(cors -> Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ApiVersionFilter apiVersionFilter) throws Exception {

        http
                .addFilterBefore(apiVersionFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        return NimbusJwtDecoder.withPublicKey(KeyUtils.loadPublicKey("/keys/local-only/public_key.pem")).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();

        converter.setAuthoritiesClaimName("roles");
        converter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);

        return jwtConverter;
    }

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> loggingFilter() {
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>();

        registration.setFilter(new RequestLoggingFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE); // 🔥 absolute first
        registration.addUrlPatterns("/*"); // apply to all endpoints

        return registration;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin(frontendUrl);
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }



}
