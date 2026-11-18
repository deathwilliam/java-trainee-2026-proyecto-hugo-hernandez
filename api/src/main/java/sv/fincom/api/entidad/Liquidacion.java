
package sv.fincom.api.entidad;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "liquidacion",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_liquidacion_agente_periodo",
                        columnNames = {"agente_id", "anio", "mes"}
                )
        }
)
public class Liquidacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agente_id", nullable = false)
    private Agente agente;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false)
    private Integer mes;

    @Column(
            name = "total_comisiones",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal totalComisiones;

    @Column(
            name = "fecha_generacion",
            nullable = false,
            updatable = false
    )
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    protected Liquidacion() {
    }

    public Liquidacion(
            Agente agente,
            Integer anio,
            Integer mes,
            BigDecimal totalComisiones
    ) {
        if (agente == null) {
            throw new IllegalArgumentException(
                    "El agente es obligatorio"
            );
        }

        if (anio == null || anio < 1) {
            throw new IllegalArgumentException(
                    "El año es invalido"
            );
        }

        if (mes == null || mes < 1 || mes > 12) {
            throw new IllegalArgumentException(
                    "El mes debe estar entre 1 y 12"
            );
        }

        if (totalComisiones == null
                || totalComisiones.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El total de comisiones debe ser mayor que cero"
            );
        }

        this.agente = agente;
        this.anio = anio;
        this.mes = mes;
        this.totalComisiones = totalComisiones;
    }

    public Long getId() {
        return id;
    }

    public Agente getAgente() {
        return agente;
    }

    public Integer getAnio() {
        return anio;
    }

    public Integer getMes() {
        return mes;
    }

    public BigDecimal getTotalComisiones() {
        return totalComisiones;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }
}
