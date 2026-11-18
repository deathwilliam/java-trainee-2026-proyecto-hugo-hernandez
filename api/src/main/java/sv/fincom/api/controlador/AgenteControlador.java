
package sv.fincom.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sv.fincom.api.dto.AgenteSolicitud;
import sv.fincom.api.dto.AgenteEditarSolicitud;
import sv.fincom.api.dto.AgenteRespuesta;
import sv.fincom.api.servicio.AgenteService;

import java.util.List;

@RestController
@RequestMapping("/api/agentes")
@Tag(
        name = "Agentes",
        description = "Gestion de agentes financieros"
)
public class AgenteControlador {

    private final AgenteService servicio;

    public AgenteControlador(AgenteService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Listar agentes activos")
    public List<AgenteRespuesta> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar agente por ID")
    public AgenteRespuesta obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Registrar agente")
    public ResponseEntity<AgenteRespuesta> crear(
            @Valid @RequestBody AgenteSolicitud solicitud) {

        AgenteRespuesta creado = servicio.crear(solicitud);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar agente")
    public AgenteRespuesta actualizar(
            @PathVariable Long id,
            @Valid @RequestBody AgenteEditarSolicitud solicitud) {

        return servicio.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar agente")
    public ResponseEntity<Void> desactivar(
            @PathVariable Long id) {

        servicio.desactivar(id);

        return ResponseEntity.noContent().build();
    }
}
