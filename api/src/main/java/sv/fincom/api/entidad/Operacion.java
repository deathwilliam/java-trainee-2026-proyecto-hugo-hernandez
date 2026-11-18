
package sv.fincom.api.entidad;

import jakarta.persistence.*;
import sv.fincom.api.entidad.enums.EstadoOperacion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "operacion")
public class Operacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "referencia",
            length = 40,
            nullable = false,
            unique = true
    )
    private String referencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agente_id", nullable = false)
    private Agente agente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tarifa_comision_id", nullable = false)
    private TarifaComision tarifaComision;

    @Column(
            name = "monto",
            precision = 15,
            scale = 2,
            nullable = false
    )
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOperacion estado;

    @Column(name = "fecha_operacion", nullable = false)
    private LocalDateTime fechaOperacion;

    protected Operacion() {
    }

    public Operacion(
            String referencia,
            Agente agente,
            Cliente cliente,
            TarifaComision tarifaComision,
            BigDecimal monto,
            EstadoOperacion estado,
            LocalDateTime fechaOperacion
    ) {
        if (referencia == null || referencia.isBlank()
                || referencia.length() > 40) {
            throw new IllegalArgumentException(
                    "La referencia de la operacion es invalida"
            );
        }

        if (agente == null || cliente == null
                || tarifaComision == null) {
            throw new IllegalArgumentException(
                    "El agente, cliente y tarifa son obligatorios"
            );
        }

        if (monto == null
                || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser mayor que cero"
            );
        }

        if (estado == null || fechaOperacion == null) {
            throw new IllegalArgumentException(
                    "El estado y la fecha son obligatorios"
            );
        }

        this.referencia = referencia;
        this.agente = agente;
        this.cliente = cliente;
        this.tarifaComision = tarifaComision;
        this.monto = monto;
        this.estado = estado;
        this.fechaOperacion = fechaOperacion;
    }


    public void cambiarEstado(EstadoOperacion nuevo) {

        if (nuevo == null || nuevo == EstadoOperacion.LIQUIDADA) {
            throw new IllegalArgumentException(
                    "No es posible asignar ese estado manualmente"
            );
        }

        if (this.estado == EstadoOperacion.LIQUIDADA) {
            throw new IllegalArgumentException(
                    "Una operacion liquidada no puede modificarse"
            );
        }

        if (this.estado == nuevo) {
            return;
        }

        boolean permitido =
                (this.estado == EstadoOperacion.PENDIENTE
                        && (nuevo == EstadoOperacion.CONFIRMADA
                        || nuevo == EstadoOperacion.ANULADA))
                        ||
                        (this.estado == EstadoOperacion.CONFIRMADA
                                && nuevo == EstadoOperacion.ANULADA);

        if (!permitido) {
            throw new IllegalArgumentException(
                    "Transicion de estado no permitida"
            );
        }

        this.estado = nuevo;
    }


    public void marcarLiquidada() {
        if (this.estado != EstadoOperacion.CONFIRMADA) {
            throw new IllegalStateException(
                    "Solo se pueden liquidar operaciones CONFIRMADAS"
            );
        }

        this.estado = EstadoOperacion.LIQUIDADA;
    }

    public Long getId() {
        return id;
    }

    public String getReferencia() {
        return referencia;
    }

    public Agente getAgente() {
        return agente;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public TarifaComision getTarifaComision() {
        return tarifaComision;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public EstadoOperacion getEstado() {
        return estado;
    }

    public LocalDateTime getFechaOperacion() {
        return fechaOperacion;
    }
}
