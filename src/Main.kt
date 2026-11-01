import modelo.*
import servicio.Pizzeria

private val entrada = EntradaConsola()

fun main() {
    try { ejecutarAplicacion() }
    catch (error: FinDeEntrada) { println("\nEntrada finalizada. La operación en curso se canceló; no se crean pedidos incompletos.") }
}

private fun ejecutarAplicacion() {
    val catalogo = listOf(
        Pizza("Pepperoni", 8500.0, mapOf("Masa" to 1, "Queso" to 2, "Pepperoni" to 1)),
        Pizza("Margarita", 7000.0, mapOf("Masa" to 1, "Queso" to 2, "Tomate" to 1)),
        Pizza("Vegetariana", 8000.0, mapOf("Masa" to 1, "Queso" to 1, "Tomate" to 2))
    )
    val pizzeria = Pizzeria(mapOf("Masa" to 20, "Queso" to 30, "Pepperoni" to 10, "Tomate" to 15),
        listOf(Repartidor(1, "Camila", "+56911111111", 2.5), Repartidor(2, "Diego", "+56922222222", 1.0)))
    var siguienteId = 1
    println("=== Pizzería Duoc | Semana 2 ===")
    println("Precios netos en CLP; impuesto del ejercicio: 19%. Enter usa el valor predeterminado. /cancelar vuelve al menú.")
    while (true) {
        println("\n1. Nuevo pedido  2. Avanzar pedido  3. Inventario  4. Reabastecer  5. Reporte diario  6. Repartidores  0. Salir")
        val opcion = try { leerEntero("Opción", 0, 0, 6) }
        catch (error: OperacionCancelada) { continue }
        when (opcion) {
            0 -> { println(pizzeria.reporteDiario()); return }
            1 -> ejecutarSeguro {
                val nombre = leerTexto("Nombre", "Cliente sin nombre")
                val direccion = leerTexto("Dirección", "Retiro en local")
                val telefono = leerTexto("Teléfono", "")
                val correo = leerTexto("Correo", "")
                val cliente = Cliente(nombre, direccion, telefono, correo)
                if (cliente.telefono != telefono) println("Teléfono vacío o inválido. Se usará: ${cliente.telefono}")
                if (cliente.correo != correo) println("Correo vacío o inválido. Se usará: ${cliente.correo}")
                println(cliente.descripcion())
                val productos = mutableListOf<Producto>()
                do {
                    for ((indice, producto) in catalogo.withIndex()) println("${indice + 1}. ${producto.detallarProducto()}")
                    val producto = catalogo[leerEntero("Producto", 1, 1, catalogo.size) - 1]
                    val cantidad = leerEntero("Cantidad", 1, 1, Pedido.MAXIMO_PIZZAS - productos.size)
                    repeat(cantidad) { productos.add(producto) }
                } while (productos.size < Pedido.MAXIMO_PIZZAS && entrada.confirmar("¿Agregar otra variedad?"))
                val notas = leerTexto("Notas opcionales", "").takeIf { it.isNotBlank() }
                val pedido = Pedido(siguienteId, cliente, productos, notas)
                pizzeria.registrarPedido(pedido)
                siguienteId++
                ejecutarSeguro { println(pizzeria.procesarPedido(pedido)) }
                val clienteRegistrado: Cliente? = cliente.takeIf { it.correo != "correo@invalido.com" }
                clienteRegistrado?.let { println("Notificación local a ${it.correo}: pedido #${pedido.idPedido}, ${pedido.estado.etiqueta}.") }
                println(pedido.resumen())
            }
            2 -> ejecutarSeguro {
                val id = leerEntero("Número de pedido", 1, 1)
                val pedido = pizzeria.consultarPedidos().find { it.idPedido == id }
                    ?: throw IllegalArgumentException("No existe el pedido #$id. Consulta los números en la opción 5.")
                when (pedido.estado) {
                    EstadoPedido.PENDIENTE -> println(pizzeria.procesarPedido(pedido))
                    EstadoPedido.EN_PREPARACION -> pedido.cambiarEstado(EstadoPedido.LISTO)
                    EstadoPedido.LISTO -> println(pizzeria.despachar(pedido))
                    EstadoPedido.EN_CAMINO -> println((pedido.repartidorAsignado ?: error("El pedido no tiene repartidor asignado.")).entregarPedido(pedido))
                    EstadoPedido.ENTREGADO -> println("El pedido ya fue entregado.")
                }
                println(pedido.resumen())
            }
            3 -> pizzeria.consultarInventario().forEach { (nombre, cantidad) -> println("$nombre: $cantidad unidades") }
            4 -> ejecutarSeguro {
                val ingrediente = leerTexto("Ingrediente del inventario", "Masa")
                pizzeria.reabastecer(ingrediente, leerEntero("Unidades a agregar", 1, 1))
                println("Inventario actualizado.")
            }
            5 -> println(pizzeria.reporteDiario())
            6 -> pizzeria.consultarRepartidores().forEach { println(it.descripcion()) }
        }
    }
}

private fun ejecutarSeguro(accion: () -> Unit) {
    try { accion() }
    catch (error: OperacionCancelada) { println("Operación cancelada. No se guardaron datos incompletos.") }
    catch (error: IllegalArgumentException) { println("No se pudo completar: ${error.message}") }
    catch (error: IllegalStateException) { println("Operación inválida: ${error.message}") }
    catch (error: ArithmeticException) { println("Cantidad demasiado grande. Ingresa un valor menor.") }
}
private fun leerTexto(mensaje: String, predeterminado: String): String =
    entrada.texto(mensaje, predeterminado)

private fun leerEntero(mensaje: String, predeterminado: Int, minimo: Int, maximo: Int = 1_000_000): Int =
    entrada.entero(mensaje, predeterminado, minimo, maximo)

