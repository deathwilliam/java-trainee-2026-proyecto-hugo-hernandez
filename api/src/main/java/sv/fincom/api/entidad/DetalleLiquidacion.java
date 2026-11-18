
package sv.fincom.api.entidad;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "detalle_liquidacion")
@Check(constraints = "monto_comision > 0")
public class DetalleLiquidacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "liquidacion_id", nullable = false)
    private Liquidacion liquidacion;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "operacion_id",
            nullable = false,
            unique = true
    )
    private Operacion operacion;

    @Column(
            name = "monto_comision",
            precision = 12,
            scale = 2,
            nullable = false
    )
    private BigDecimal montoComision;

    @Column(
            name = "creado_en",
            nullable = false,
            updatable = false
    )
    private LocalDateTime creadoEn = LocalDateTime.now();

    protected DetalleLiquidacion() {
    }

    public DetalleLiquidacion(
            Liquidacion liquidacion,
            Operacion operacion,
            BigDecimal montoComision
    ) {
        if (liquidacion == null || operacion == null
                || montoComision == null
                || montoComision.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Datos invalidos para el detalle de liquidacion"
            );
        }

        this.liquidacion = liquidacion;
        this.operacion = operacion;
        this.montoComision = montoComision;
    }

    public Long getId() {
        return id;
    }

    public Liquidacion getLiquidacion() {
        return liquidacion;
    }

    public Operacion getOperacion() {
        return operacion;
    }

    public BigDecimal getMontoComision() {
        return montoComision;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }
}
