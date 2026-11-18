
package sv.fincom.api.controlador;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import sv.fincom.api.dto.LiquidacionSolicitud;
import sv.fincom.api.dto.LiquidacionRespuesta;

import sv.fincom.api.entidad.Liquidacion;

import sv.fincom.api.servicio.EjecutorLiquidacionService;
import sv.fincom.api.servicio.LiquidacionConsultaService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
@Tag(
        name = "Liquidaciones",
        description = "Gestion de liquidaciones de comisiones FINCOM"
)
@RestController
@RequestMapping("/api/liquidaciones")
public class LiquidacionControlador {

    private final EjecutorLiquidacionService ejecutor;
    private final LiquidacionConsultaService consultas;

    public LiquidacionControlador(
            EjecutorLiquidacionService ejecutor,
            LiquidacionConsultaService consultas) {

        this.ejecutor = ejecutor;
        this.consultas = consultas;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Liquidar comisiones",
            description = "Genera una liquidacion mensual para un agente"
    )
    @PostMapping
    public ResponseEntity<LiquidacionRespuesta> liquidar(
            @Valid @RequestBody LiquidacionSolicitud solicitud,
            @AuthenticationPrincipal Jwt jwt) {

        Long usuarioId = ((Number) jwt.getClaim("usuarioId"))
                .longValue();

        Liquidacion creada = ejecutor.ejecutarLiquidacion(
                solicitud.agenteId(),
                solicitud.anio(),
                solicitud.mes(),
                usuarioId
        );

        LiquidacionRespuesta respuesta =
                consultas.obtener(creada.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar liquidaciones")
    @GetMapping
    public List<LiquidacionRespuesta> listar() {
        return consultas.listar();
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
    @Operation(summary = "Consultar liquidacion por ID")
    @GetMapping("/{id}")
    public LiquidacionRespuesta obtener(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        boolean esAdmin = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        if (esAdmin) {
            return consultas.obtener(id);
        }

        Long usuarioId = ((Number) jwt.getClaim("usuarioId"))
                .longValue();

        return consultas.obtenerParaAsesor(id, usuarioId);
    }

}
