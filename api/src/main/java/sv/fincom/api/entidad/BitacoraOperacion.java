
package sv.fincom.api.entidad;

import jakarta.persistence.*;
import sv.fincom.api.entidad.enums.AccionBitacora;
import sv.fincom.api.entidad.enums.ResultadoOperacion;

import java.time.LocalDateTime;

@Entity
@Table(name = "bitacora_operacion")
public class BitacoraOperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liquidacion_id")
    private Liquidacion liquidacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", length = 32, nullable = false)
    private AccionBitacora accion;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", length = 16, nullable = false)
    private ResultadoOperacion resultado;

    @Column(name = "detalle", length = 255, nullable = false)
    private String detalle;

    @Column(
            name = "creado_en",
            nullable = false,
            updatable = false
    )
    private LocalDateTime creadoEn = LocalDateTime.now();

    protected BitacoraOperacion() {
    }

    public BitacoraOperacion(
            Usuario usuario,
            Liquidacion liquidacion,
            AccionBitacora accion,
            ResultadoOperacion resultado,
            String detalle
    ) {
        if (usuario == null || accion == null || resultado == null
                || detalle == null || detalle.isBlank()
                || detalle.length() > 255) {
            throw new IllegalArgumentException(
                    "Datos invalidos para la bitacora"
            );
        }

        this.usuario = usuario;
        this.liquidacion = liquidacion;
        this.accion = accion;
        this.resultado = resultado;
        this.detalle = detalle;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Liquidacion getLiquidacion() {
        return liquidacion;
    }

    public AccionBitacora getAccion() {
        return accion;
    }

    public ResultadoOperacion getResultado() {
        return resultado;
    }

    public String getDetalle() {
        return detalle;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }
}
