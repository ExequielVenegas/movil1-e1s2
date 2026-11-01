package servicio
import modelo.*

class Pizzeria(inventarioInicial: Map<String, Int>, repartidores: List<Repartidor>) {
    private val repartidores = repartidores.toList()
    private val inventario = inventarioInicial.toMutableMap()
    private var pedidosDelDia: Array<Pedido> = emptyArray()
    init {
        require(inventario.all { it.key.isNotBlank() && it.key == it.key.trim() && it.value >= 0 }) { "Inventario inválido: usa nombres sin espacios en los extremos y cantidades no negativas." }
        require(inventario.keys.map { it.lowercase() }.distinct().size == inventario.size) { "Hay ingredientes duplicados que solo difieren en mayúsculas." }
        require(this.repartidores.map { it.idRepartidor }.distinct().size == this.repartidores.size) { "Hay identificadores de repartidor repetidos." }
    }
    fun consultarInventario(): Map<String, Int> = inventario.toMap()
    fun consultarPedidos(): Array<Pedido> = pedidosDelDia.copyOf()
    fun consultarRepartidores(): List<Repartidor> = repartidores.toList()
    fun calcularIngredientes(productos: List<Producto>): Map<String, Int> {
        val necesarios = mutableMapOf<String, Int>()
        for (producto in productos) for ((ingrediente, cantidad) in producto.ingredientes)
            necesarios[ingrediente] = Math.addExact(necesarios[ingrediente] ?: 0, cantidad)
        return necesarios.toMap()
    }
    fun prepararLote(productos: List<Producto>) {
        require(productos.isNotEmpty()) { "El lote debe contener pizzas." }
        val necesarios = calcularIngredientes(productos)
        val faltantes = necesarios.filter { (nombre, cantidad) -> (inventario[nombre] ?: 0) < cantidad }
        when {
            faltantes.isNotEmpty() -> throw IllegalArgumentException(faltantes.entries.joinToString("\n") { (nombre, cantidad) ->
                "$nombre insuficiente: disponible ${inventario[nombre] ?: 0}, necesario $cantidad. Comprar ${cantidad - (inventario[nombre] ?: 0)} unidades, registrar reabastecimiento y reintentar."
            })
            else -> necesarios.forEach { (nombre, cantidad) -> inventario[nombre] = inventario.getValue(nombre) - cantidad }
        }
    }
    fun reabastecer(ingrediente: String, cantidad: Int) {
        require(cantidad > 0) { "La cantidad de reabastecimiento debe ser mayor que cero." }
        val nombre = inventario.keys.find { it.equals(ingrediente.trim(), ignoreCase = true) }
            ?: throw IllegalArgumentException("Ingrediente desconocido: '$ingrediente'. Usa uno de: ${inventario.keys.joinToString()}.")
        val actualizado = Math.addExact(inventario.getValue(nombre), cantidad)
        inventario[nombre] = actualizado
    }
    fun registrarPedido(pedido: Pedido) {
        require(pedidosDelDia.none { it.idPedido == pedido.idPedido }) { "Identificador repetido." }
        require(pedido.estado == EstadoPedido.PENDIENTE) { "Pedido ya procesado." }
        pedidosDelDia += pedido
    }
    fun procesarPedido(pedido: Pedido): String {
        require(pedidosDelDia.any { it === pedido }) { "Registra el pedido primero." }
        check(pedido.estado == EstadoPedido.PENDIENTE) { "Este pedido ya fue procesado." }
        prepararLote(pedido.productos)
        pedido.cambiarEstado(EstadoPedido.EN_PREPARACION)
        return "Pedido confirmado. Inventario actualizado."
    }
    fun despachar(pedido: Pedido): String {
        require(pedidosDelDia.any { it === pedido }) { "Pedido no registrado." }
        check(pedido.estado == EstadoPedido.LISTO) { "El pedido debe estar listo." }
        val repartidor = repartidores.sortedBy { it.distanciaKm }.find { it.disponible }
            ?: return "No hay repartidores disponibles. El pedido permanece listo."
        pedido.asignar(repartidor)
        return "Pedido en camino con ${repartidor.nombre} (${repartidor.distanciaKm} km)."
    }
    fun reporteDiario(): String = buildString {
        appendLine("=== Reporte de pedidos del día ===")
        for (pedido in pedidosDelDia) appendLine(pedido.resumen())
        val completados = pedidosDelDia.filter { it.estado == EstadoPedido.ENTREGADO }
        val pendientes = pedidosDelDia.filter { it.estado != EstadoPedido.ENTREGADO }
        val estados = pedidosDelDia.map { it.estado.etiqueta }.toSet()
        appendLine("Completados (${completados.size}): ${completados.joinToString { "#${it.idPedido}" }}")
        appendLine("Pendientes (${pendientes.size}): ${pendientes.joinToString { "#${it.idPedido}" }}")
        appendLine("Estados presentes: ${estados.joinToString()}")
    }
}
