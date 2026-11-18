
package sv.fincom.api.entidad;

import jakarta.persistence.*;
import sv.fincom.api.entidad.enums.EstadoOrdenPago;

import java.time.LocalDateTime;

@Entity
@Table(name = "orden_pago")
public class OrdenPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "liquidacion_id",
            nullable = false,
            unique = true
    )
    private Liquidacion liquidacion;

    @Column(
            name = "numero_orden",
            length = 40,
            nullable = false,
            unique = true
    )
    private String numeroOrden;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EstadoOrdenPago estado = EstadoOrdenPago.PENDIENTE;

    @Column(
            name = "fecha_emision",
            nullable = false,
            updatable = false
    )
    private LocalDateTime fechaEmision = LocalDateTime.now();

    protected OrdenPago() {
    }

    public OrdenPago(
            Liquidacion liquidacion,
            String numeroOrden
    ) {
        if (liquidacion == null) {
            throw new IllegalArgumentException(
                    "La liquidacion es obligatoria"
            );
        }

        if (numeroOrden == null || numeroOrden.isBlank()) {
            throw new IllegalArgumentException(
                    "El numero de orden es obligatorio"
            );
        }

        this.liquidacion = liquidacion;
        this.numeroOrden = numeroOrden;
    }

    public void autorizar() {
        if (this.estado != EstadoOrdenPago.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo se puede autorizar una orden pendiente"
            );
        }

        this.estado = EstadoOrdenPago.AUTORIZADA;
    }

    public Long getId() {
        return id;
    }

    public Liquidacion getLiquidacion() {
        return liquidacion;
    }

    public String getNumeroOrden() {
        return numeroOrden;
    }

    public EstadoOrdenPago getEstado() {
        return estado;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }
}
