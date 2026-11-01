package modelo
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

enum class EstadoPedido(val etiqueta: String) {
    PENDIENTE("Pendiente"), EN_PREPARACION("En preparación"), LISTO("Listo"), EN_CAMINO("En camino"), ENTREGADO("Entregado")
}
class Pedido(val idPedido: Int, val cliente: Cliente, productos: List<Producto>, val notas: String? = null,
             val tasaImpuesto: Double = 0.19) {
    // Una aparición representa una pizza; se permiten distintas variedades.
    val productos = productos.toList()
    var estado = EstadoPedido.PENDIENTE
        private set
    var repartidorAsignado: Repartidor? = null
        private set
    val cantidadPizzas get() = productos.size
    private fun dinero(valor: BigDecimal) = valor.setScale(0, RoundingMode.HALF_UP)
    val subtotal: BigDecimal get() = dinero(productos.fold(BigDecimal.ZERO) { suma, p -> suma + BigDecimal.valueOf(p.precio) })
    val descuento: BigDecimal get() = if (cantidadPizzas > 3) dinero(subtotal * BigDecimal("0.10")) else BigDecimal.ZERO
    val impuestos: BigDecimal get() = dinero((subtotal - descuento) * BigDecimal.valueOf(tasaImpuesto))
    fun calcularTotal(): BigDecimal = subtotal - descuento + impuestos
    init {
        require(idPedido > 0) { "El número de pedido debe ser positivo." }
        require(this.productos.size in 1..MAXIMO_PIZZAS) { "El pedido debe contener entre 1 y $MAXIMO_PIZZAS pizzas." }
        require(notas == null || notas.length <= 200) { "Las notas no pueden superar 200 caracteres." }
        require(tasaImpuesto.isFinite() && tasaImpuesto in 0.0..1.0) { "Tasa inválida." }
    }
    fun cambiarEstado(nuevo: EstadoPedido) {
        require(nuevo != EstadoPedido.ENTREGADO) { "Confirma la entrega mediante el repartidor asignado." }
        val siguiente = when (estado) {
            EstadoPedido.PENDIENTE -> EstadoPedido.EN_PREPARACION
            EstadoPedido.EN_PREPARACION -> EstadoPedido.LISTO
            EstadoPedido.LISTO -> EstadoPedido.EN_CAMINO
            EstadoPedido.EN_CAMINO -> EstadoPedido.ENTREGADO
            EstadoPedido.ENTREGADO -> null
        }
        require(nuevo == siguiente) { "Transición inválida: ${estado.etiqueta} a ${nuevo.etiqueta}." }
        require(nuevo != EstadoPedido.EN_CAMINO || repartidorAsignado != null) { "Falta asignar repartidor." }
        estado = nuevo
    }
    internal fun confirmarEntrega(repartidor: Repartidor) {
        check(estado == EstadoPedido.EN_CAMINO) { "Solo se puede entregar un pedido en camino." }
        require(repartidorAsignado === repartidor) { "El repartidor no corresponde a este pedido." }
        estado = EstadoPedido.ENTREGADO
    }
    companion object { const val MAXIMO_PIZZAS = 100 }
    internal fun asignar(repartidor: Repartidor) {
        check(estado == EstadoPedido.LISTO && repartidorAsignado == null) { "El pedido debe estar listo y sin repartidor." }
        repartidor.reservar()
        repartidorAsignado = repartidor
        cambiarEstado(EstadoPedido.EN_CAMINO)
    }
    fun resumen(): String {
        val moneda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL"))
        return buildString {
            appendLine("=== Pedido #$idPedido ===")
            appendLine(cliente.descripcion())
            for ((producto, cantidad) in productos.groupingBy { it }.eachCount())
                appendLine("${producto.nombre} | ${moneda.format(producto.precio)} x $cantidad")
            appendLine("Subtotal: ${moneda.format(subtotal)}")
            appendLine("Descuento (${if (cantidadPizzas > 3) 10 else 0}%): ${moneda.format(descuento)}")
            appendLine("Impuestos (${tasaImpuesto * 100}%): ${moneda.format(impuestos)}")
            appendLine("Total final: ${moneda.format(calcularTotal())}")
            appendLine("Estado: ${estado.etiqueta}")
            appendLine("Notas: ${notas?.trim()?.takeIf { it.isNotEmpty() } ?: "Sin notas"}")
            appendLine("Repartidor: ${repartidorAsignado?.nombre ?: "Sin asignar"}")
            if (estado == EstadoPedido.PENDIENTE) appendLine("Sin confirmar: total referencial.")
        }
    }
}
