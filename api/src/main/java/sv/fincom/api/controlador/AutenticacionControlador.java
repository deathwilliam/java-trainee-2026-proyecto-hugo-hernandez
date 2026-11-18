
package sv.fincom.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sv.fincom.api.dto.LoginSolicitud;
import sv.fincom.api.dto.LoginRespuesta;
import sv.fincom.api.servicio.AutenticacionService;

@Tag(
        name = "Autenticacion",
        description = "Acceso seguro a FINCOM"
)
@RestController
@RequestMapping("/api/auth")
public class AutenticacionControlador {

    private final AutenticacionService autenticacionService;

    public AutenticacionControlador(
            AutenticacionService autenticacionService) {

        this.autenticacionService = autenticacionService;
    }

    @Operation(
            summary = "Iniciar sesion",
            description = "Valida las credenciales y devuelve un JWT"
    )
    @PostMapping("/login")
    public LoginRespuesta login(
            @Valid @RequestBody LoginSolicitud solicitud) {

        return autenticacionService.iniciarSesion(solicitud);
    }
}
