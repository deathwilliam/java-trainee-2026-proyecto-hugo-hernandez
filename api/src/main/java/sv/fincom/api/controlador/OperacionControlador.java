
package sv.fincom.api.controlador;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.*;

import sv.fincom.api.dto.OperacionRespuesta;
import sv.fincom.api.servicio.OperacionConsultaService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sv.fincom.api.dto.OperacionSolicitud;
import sv.fincom.api.servicio.OperacionRegistroService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import sv.fincom.api.dto.OperacionEstadoSolicitud;
import sv.fincom.api.servicio.OperacionEstadoService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;




@RestController
@RequestMapping("/api/operaciones")
@Tag(
        name = "Operaciones",
        description = "Consulta de operaciones financieras FINCOM"
)
public class OperacionControlador {

    private final OperacionConsultaService consultas;

    private final OperacionRegistroService registros;
    private final OperacionEstadoService estados;




    public OperacionControlador(
            OperacionConsultaService consultas,
            OperacionRegistroService registros,
            OperacionEstadoService estados) {

        this.consultas = consultas;
        this.registros = registros;
        this.estados = estados;
    }




    @PostMapping
    @Operation(summary = "Registrar una operacion financiera")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
    public ResponseEntity<OperacionRespuesta> crear(
            @Valid @RequestBody OperacionSolicitud solicitud,
            Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();

        Long usuarioId = ((Number) jwt.getClaim("usuarioId"))
                .longValue();

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        OperacionRespuesta creada = registros.crear(
                solicitud,
                usuarioId,
                esAdmin
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creada);
    }




    @GetMapping
    @Operation(summary = "Listar operaciones")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR', 'CLIENTE')")
    public List<OperacionRespuesta> listar(Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long usuarioId = ((Number) jwt.getClaim("usuarioId"))
                .longValue();

        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return consultas.listar();
        }

        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ASESOR"))) {
            return consultas.listarPorAsesor(usuarioId);
        }

        return consultas.listarPorCliente(usuarioId);
    }



    @GetMapping("/{id}")
    @Operation(summary = "Consultar operacion por ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR', 'CLIENTE')")
    public OperacionRespuesta obtener(
            @PathVariable Long id,
            Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long usuarioId = ((Number) jwt.getClaim("usuarioId"))
                .longValue();

        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return consultas.obtener(id);
        }

        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ASESOR"))) {
            return consultas.obtenerPorAsesor(id, usuarioId);
        }

        return consultas.obtenerPorCliente(id, usuarioId);
    }




    @PatchMapping("/{id}")
    @Operation(summary = "Cambiar estado de una operacion")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
    public OperacionRespuesta cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody OperacionEstadoSolicitud solicitud,
            Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long usuarioId = ((Number) jwt.getClaim("usuarioId")).longValue();

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        estados.cambiarEstado(
                id,
                solicitud.estado(),
                usuarioId,
                esAdmin
        );

        return consultas.obtener(id);
    }



    @DeleteMapping("/{id}")
    @Operation(summary = "Anular una operacion sin borrarla")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
    public ResponseEntity<Void> anular(
            @PathVariable Long id,
            Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long usuarioId = ((Number) jwt.getClaim("usuarioId")).longValue();

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        estados.anular(id, usuarioId, esAdmin);

        return ResponseEntity.noContent().build();
    }


}
