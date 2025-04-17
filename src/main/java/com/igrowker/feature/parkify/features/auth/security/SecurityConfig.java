package com.igrowker.feature.parkify.features.auth.security;

import com.igrowker.feature.parkify.features.auth.entities.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthTokenFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private static final String OWNER_ROLE = Role.OWNER.name();
    private static final String DRIVER_ROLE = Role.DRIVER.name();
    private static final String API_BASE_PATH_V1 = "/api/v1";
    private static final String AUTH_PATH = API_BASE_PATH_V1 + "/auth";
    private static final String CONFIG_PATH = API_BASE_PATH_V1 + "/config";
    private static final String CONTENT_PATH = API_BASE_PATH_V1 + "/content";
    private static final String USERS_PATH = API_BASE_PATH_V1 + "/users";
    private static final String PARKINGS_PATH = API_BASE_PATH_V1 + "/parkings";
    private static final String BOOKINGS_PATH = API_BASE_PATH_V1 + "/bookings";
    private static final String RECOMMENDATIONS_PATH = API_BASE_PATH_V1 + "/recommendations";
    private static final String OPERATIONS_PATH = API_BASE_PATH_V1 + "/operations";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                antMatcher(HttpMethod.POST, PARKINGS_PATH + "/my"),
                                antMatcher(HttpMethod.GET, PARKINGS_PATH + "/my"), // <-- Наше правило
                                antMatcher(HttpMethod.PATCH, PARKINGS_PATH + "/my/availability"),
                                antMatcher(HttpMethod.PUT, PARKINGS_PATH + "/{parkingId}/features/{featureSlug}"),
                                antMatcher(HttpMethod.DELETE, PARKINGS_PATH + "/{parkingId}/features/{featureSlug}"),
                                antMatcher(HttpMethod.GET, PARKINGS_PATH + "/owner/parking") // Если остается
                        ).hasRole(OWNER_ROLE)
                        .requestMatchers(
                                antMatcher(HttpMethod.POST, BOOKINGS_PATH)
                        ).hasRole(DRIVER_ROLE)
                        .requestMatchers(
                                antMatcher(HttpMethod.GET, AUTH_PATH + "/me"),
                                antMatcher(HttpMethod.PUT, USERS_PATH + "/me/location"),
                                antMatcher(HttpMethod.GET, RECOMMENDATIONS_PATH + "/zones"),
                                antMatcher(HttpMethod.GET, RECOMMENDATIONS_PATH + "/parkings"),
                                antMatcher(HttpMethod.PATCH, BOOKINGS_PATH + "/{bookingRequestId}"),
                                antMatcher(HttpMethod.GET, OPERATIONS_PATH + "/{operationId}/status")
                        ).authenticated()
                        .requestMatchers(
                                antMatcher(HttpMethod.POST, AUTH_PATH + "/register"),
                                antMatcher(HttpMethod.POST, AUTH_PATH + "/login"),
                                antMatcher(HttpMethod.GET, CONTENT_PATH + "/home"),
                                antMatcher(HttpMethod.GET, CONTENT_PATH + "/footer"),
                                antMatcher(HttpMethod.GET, PARKINGS_PATH),
                                antMatcher(HttpMethod.GET, PARKINGS_PATH + "/{parkingId}"),
                                antMatcher(HttpMethod.GET, PARKINGS_PATH + "/{parkingId}/availability"),
                                antMatcher(HttpMethod.GET, CONFIG_PATH + "/initial"),
                                antMatcher(HttpMethod.GET, "/api/v1/features"),
                                antMatcher(HttpMethod.GET, "/api/v1/features/**"),
                                antMatcher("/v3/api-docs/**"),
                                antMatcher("/swagger-ui/**"),
                                antMatcher("/swagger-ui.html")
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .userDetailsService(userDetailsService)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}