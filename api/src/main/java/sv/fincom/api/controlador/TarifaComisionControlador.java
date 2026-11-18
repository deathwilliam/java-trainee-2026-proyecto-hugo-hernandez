
package sv.fincom.api.controlador;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sv.fincom.api.dto.TarifaRespuesta;
import sv.fincom.api.servicio.TarifaComisionService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import sv.fincom.api.dto.TarifaSolicitud;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.ResponseEntity;
import sv.fincom.api.servicio.TarifaEstadoService;

@RestController
@RequestMapping("/api/tarifas")
@Tag(
        name = "Tarifas",
        description = "Consulta de tarifas de comision FINCOM"
)
public class TarifaComisionControlador {

    private final TarifaComisionService servicio;

    private final TarifaEstadoService estados;



    public TarifaComisionControlador(
            TarifaComisionService servicio,
            TarifaEstadoService estados) {

        this.servicio = servicio;
        this.estados = estados;
    }



    @PostMapping
    @Operation(summary = "Registrar nueva tarifa de comision")
    public ResponseEntity<TarifaRespuesta> crear(
            @Valid @RequestBody TarifaSolicitud solicitud) {

        TarifaRespuesta creada = servicio.crear(solicitud);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creada);
    }

    @GetMapping
    @Operation(summary = "Listar tarifas de comision")
    public List<TarifaRespuesta> listar() {

        return servicio.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar tarifa por ID")
    public TarifaRespuesta obtener(
            @PathVariable Long id) {

        return servicio.obtener(id);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar una tarifa sin eliminarla")
    public ResponseEntity<Void> desactivar(
            @PathVariable Long id) {

        estados.desactivar(id);

        return ResponseEntity.noContent().build();
    }

}
