
package sv.fincom.api.servicio;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sv.fincom.api.dto.LoginSolicitud;
import sv.fincom.api.dto.LoginRespuesta;
import sv.fincom.api.entidad.Usuario;
import sv.fincom.api.excepcion.CredencialesInvalidasException;
import sv.fincom.api.repositorio.UsuarioRepository;
import sv.fincom.api.repositorio.UsuarioRolRepository;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;

@Service
public class AutenticacionService {

    private final UsuarioRepository usuarios;
    private final UsuarioRolRepository usuariosRoles;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AutenticacionService(
            UsuarioRepository usuarios,
            UsuarioRolRepository usuariosRoles,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder) {

        this.usuarios = usuarios;
        this.usuariosRoles = usuariosRoles;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    @Transactional(readOnly = true)
    public LoginRespuesta iniciarSesion(
            LoginSolicitud solicitud) {

        String correo = solicitud.correo()
                .trim()
                .toLowerCase(Locale.ROOT);

        Usuario usuario = usuarios.findByCorreo(correo)
                .orElseThrow(CredencialesInvalidasException::new);

        if (!usuario.estaActivo()
                || !passwordEncoder.matches(
                solicitud.clave(),
                usuario.getClaveHash()
        )) {
            throw new CredencialesInvalidasException();
        }

        List<String> roles = usuariosRoles.buscarRolesActivos(
                usuario.getId()
        );

        if (roles.isEmpty()) {
            throw new CredencialesInvalidasException();
        }

        Instant ahora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("fincom")
                .subject(usuario.getCorreo())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(2, ChronoUnit.HOURS))
                .claim("usuarioId", usuario.getId())
                .claim("roles", roles)
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(),
                        claims
                )
        ).getTokenValue();

        return new LoginRespuesta(
                token,
                "Bearer",
                usuario.getId(),
                roles
        );
    }
}
