
package sv.fincom.api.seguridad;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;

import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableMethodSecurity
public class ConfiguracionSeguridad {

    // Cifrado de contrasenas

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Clave secreta para JWT

    @Bean
    public SecretKey claveJwt(
            @Value("${jwt.clave}") String clave) {

        byte[] bytes = clave.getBytes(StandardCharsets.UTF_8);

        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_CLAVE debe tener al menos 32 bytes"
            );
        }

        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    // Generacion de tokens JWT

    @Bean
    public JwtEncoder jwtEncoder(SecretKey clave) {

        return new NimbusJwtEncoder(
                new ImmutableSecret<>(clave)
        );
    }

    // Validacion de tokens JWT

    @Bean
    public JwtDecoder jwtDecoder(SecretKey clave) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(clave)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer("fincom")
        );

        return decoder;
    }

    // Conversion de roles del JWT a autoridades

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter roles =
                new JwtGrantedAuthoritiesConverter();

        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(roles);

        return converter;
    }

    // Configuracion principal de seguridad

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter converter,
            JsonMapper jsonMapper) throws Exception {

        // Error 401: no autenticado

        AuthenticationEntryPoint error401 =
                (request, response, ex) -> {

                    new BearerTokenAuthenticationEntryPoint()
                            .commence(request, response, ex);

                    escribirError(
                            request,
                            response,
                            HttpStatus.UNAUTHORIZED,
                            "No autorizado",
                            "Se requiere un token JWT valido",
                            jsonMapper
                    );
                };

        // Error 403: permisos insuficientes

        AccessDeniedHandler error403 =
                (request, response, ex) -> {

                    new BearerTokenAccessDeniedHandler()
                            .handle(request, response, ex);

                    escribirError(
                            request,
                            response,
                            HttpStatus.FORBIDDEN,
                            "Acceso prohibido",
                            "No tiene permisos para esta operacion",
                            jsonMapper
                    );
                };

        http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Acceso publico

                        .requestMatchers(
                                "/api/auth/login",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()



                        // Consultas: ADMIN, ASESOR y CLIENTE
                        // Las restricciones sobre datos propios se validan en los servicios.
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/**"
                                ).hasAnyRole("ADMIN", "ASESOR", "CLIENTE")

                        // Operaciones de escritura: ADMIN y ASESOR
                        // Los permisos específicos se validan en los controladores.
                                .requestMatchers("/api/**")
                                .hasAnyRole("ADMIN", "ASESOR")


                                // Resto de solicitudes

                        .anyRequest().authenticated()
                )

                // Respuestas de seguridad personalizadas

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(error401)
                        .accessDeniedHandler(error403)
                )

                // Autenticacion mediante JWT

                .oauth2ResourceServer(oauth2 -> oauth2

                        .authenticationEntryPoint(error401)
                        .accessDeniedHandler(error403)

                        .jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        converter
                                )
                        )
                );

        return http.build();
    }

    // Generacion de errores JSON con ProblemDetail

    private static void escribirError(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus estado,
            String titulo,
            String detalle,
            JsonMapper jsonMapper) throws IOException {

        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(
                        estado,
                        detalle
                );

        problema.setTitle(titulo);

        problema.setInstance(
                URI.create(request.getRequestURI())
        );

        response.setStatus(estado.value());

        response.setContentType(
                MediaType.APPLICATION_PROBLEM_JSON_VALUE
        );

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        response.getWriter().write(
                jsonMapper.writeValueAsString(problema)
        );
    }
}
