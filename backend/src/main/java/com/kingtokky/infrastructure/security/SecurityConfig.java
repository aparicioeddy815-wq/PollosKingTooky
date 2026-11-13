package com.kingtokky.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import com.kingtokky.infrastructure.config.KingTokkyProperties;
import com.kingtokky.presentation.exception.ErrorApi;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] RUTAS_ABIERTAS = {
            "/api/publico/**", "/api/archivos/fotos/**",
            "/api/auth/login", "/api/auth/register", "/api/auth/refresh", "/api/auth/logout",
            "/api/auth/olvide-contrasena", "/api/auth/restablecer-contrasena", "/api/auth/google",
            "/actuator/health", "/actuator/info", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain cadenaDeSeguridad(HttpSecurity http, JwtService jwtService, ObjectMapper mapper,
                                                 @Qualifier("configuracionCors") CorsConfigurationSource cors) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(c -> c.configurationSource(cors))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        // Las conexiones SSE vuelven a pasar por aquí en el despacho asíncrono; la autenticación ya se validó al abrirlas
                        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(RUTAS_ABIERTAS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> escribirError(res, mapper, HttpStatus.UNAUTHORIZED,
                                "Debes iniciar sesión para continuar", req.getRequestURI()))
                        .accessDeniedHandler((req, res, ex) -> escribirError(res, mapper, HttpStatus.FORBIDDEN,
                                "No tienes permiso para realizar esta acción", req.getRequestURI())))
                .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource configuracionCors(KingTokkyProperties propiedades) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(propiedades.cors().origenesPermitidos());
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        cors.setExposedHeaders(List.of("Content-Disposition"));
        cors.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", cors);
        return fuente;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** La autenticación se resuelve con JWT; este bean evita que Spring cree un usuario por defecto. */
    @Bean
    public UserDetailsService sinUsuariosEnMemoria() {
        return nombre -> {
            throw new UsernameNotFoundException(nombre);
        };
    }

    private void escribirError(jakarta.servlet.http.HttpServletResponse res, ObjectMapper mapper, HttpStatus estado,
                               String mensaje, String ruta) throws java.io.IOException {
        res.setStatus(estado.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        mapper.writeValue(res.getWriter(), ErrorApi.de(estado.value(), estado.getReasonPhrase(), mensaje, ruta, List.of()));
    }
}