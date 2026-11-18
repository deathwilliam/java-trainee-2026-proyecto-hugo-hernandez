
package sv.fincom.api.entidad;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tarifa_comision")
public class TarifaComision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_servicio_id", nullable = false)
    private TipoServicio tipoServicio;

    @Column(
            name = "monto_fijo",
            precision = 12,
            scale = 2,
            nullable = false
    )
    private BigDecimal montoFijo;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDate vigenteDesde;

    @Column(name = "vigente_hasta")
    private LocalDate vigenteHasta;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(
            name = "creado_en",
            nullable = false,
            updatable = false
    )
    private LocalDateTime creadoEn = LocalDateTime.now();

    protected TarifaComision() {
    }

    public TarifaComision(
            TipoServicio tipoServicio,
            BigDecimal montoFijo,
            LocalDate vigenteDesde,
            LocalDate vigenteHasta
    ) {
        if (tipoServicio == null) {
            throw new IllegalArgumentException(
                    "El tipo de servicio es obligatorio"
            );
        }

        if (montoFijo == null
                || montoFijo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "La comision debe ser mayor que cero"
            );
        }

        if (montoFijo.scale() > 2
                || montoFijo.precision() - montoFijo.scale() > 10) {
            throw new IllegalArgumentException(
                    "El monto de comision excede la precision permitida"
            );
        }

        if (vigenteDesde == null) {
            throw new IllegalArgumentException(
                    "La fecha de inicio es obligatoria"
            );
        }

        if (vigenteHasta != null
                && vigenteHasta.isBefore(vigenteDesde)) {
            throw new IllegalArgumentException(
                    "La fecha final no puede ser anterior a la inicial"
            );
        }

        this.tipoServicio = tipoServicio;
        this.montoFijo = montoFijo;
        this.vigenteDesde = vigenteDesde;
        this.vigenteHasta = vigenteHasta;
    }

    public boolean estaVigente(LocalDate fecha) {
        if (fecha == null || !Boolean.TRUE.equals(activo)) {
            return false;
        }

        boolean inicioValido =
                !fecha.isBefore(vigenteDesde);

        boolean finValido =
                vigenteHasta == null
                        || !fecha.isAfter(vigenteHasta);

        return inicioValido && finValido;
    }

    public void desactivar() {
        this.activo = false;
    }

    public Long getId() {
        return id;
    }

    public TipoServicio getTipoServicio() {
        return tipoServicio;
    }

    public BigDecimal getMontoFijo() {
        return montoFijo;
    }

    public LocalDate getVigenteDesde() {
        return vigenteDesde;
    }

    public LocalDate getVigenteHasta() {
        return vigenteHasta;
    }

    public Boolean getActivo() {
        return activo;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }




}
