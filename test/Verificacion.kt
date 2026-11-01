import modelo.*
import servicio.Pizzeria
import java.math.BigDecimal

private inline fun <reified T : Throwable> fallaCon(accion: () -> Unit) {
    try { accion() } catch (error: Throwable) {
        check(error is T) { "Se esperaba ${T::class.simpleName}, se obtuvo ${error::class.simpleName}." }
        return
    }
    error("Se esperaba rechazar la operación.")
}
private fun falla(accion: () -> Unit) {
    fallaCon<IllegalArgumentException>(accion)
}
fun main() {
    val cliente = Cliente("Ana", "Local", "abc", "incorrecto")
    check(cliente.telefono == "(000) 000-0000" && cliente.correo == "correo@invalido.com")
    check(Cliente.validarCorreo("ana@example.com") == "ana@example.com")
    check(Cliente.validarTelefono("+56912345678") == "+56912345678")
    val pizza = Pizza("Prueba", 1000.0, mapOf("Masa" to 1, "Queso" to 2))
    falla { Pizza("Mala", Double.NaN, mapOf("Masa" to 1)) }
    falla { Pizzeria(mapOf("Masa" to -1), emptyList()) }
    val lejos = Repartidor(1, "Lejos", "1", 5.0)
    val cerca = Repartidor(2, "Cerca", "2", 1.0)
    val sistema = Pizzeria(mapOf("Masa" to 10, "Queso" to 6), listOf(lejos, cerca))
    val pedido = Pedido(1, cliente, List(4) { pizza })
    check(pedido.descuento == BigDecimal("400"))
    check(pedido.impuestos == BigDecimal("684"))
    check(pedido.calcularTotal() == BigDecimal("4284"))
    check(Pedido(9, cliente, List(3) { pizza }).descuento == BigDecimal.ZERO)
    sistema.registrarPedido(pedido)
    val antes = sistema.consultarInventario()
    falla { sistema.procesarPedido(pedido) }
    check(sistema.consultarInventario() == antes && pedido.estado == EstadoPedido.PENDIENTE)
    falla { sistema.reabastecer("Queso", -2) }
    sistema.reabastecer("Queso", 2)
    sistema.procesarPedido(pedido)
    check(sistema.consultarInventario() == mapOf("Masa" to 6, "Queso" to 0))
    fallaCon<IllegalStateException> { sistema.procesarPedido(pedido) }
    falla { pedido.cambiarEstado(EstadoPedido.ENTREGADO) }
    pedido.cambiarEstado(EstadoPedido.LISTO)
    sistema.despachar(pedido)
    check(pedido.repartidorAsignado === cerca && !cerca.disponible)
    falla { lejos.entregarPedido(pedido) }
    cerca.entregarPedido(pedido)
    fallaCon<IllegalStateException> { cerca.entregarPedido(pedido) }
    check(cerca.disponible && pedido.estado == EstadoPedido.ENTREGADO)
    check("Sin notas" in pedido.resumen() && "Completados (1)" in sistema.reporteDiario())
    val sinRepartidor = Pizzeria(mapOf("Masa" to 1, "Queso" to 2), emptyList())
    val otro = Pedido(2, cliente, listOf(pizza), "Sin aceitunas")
    sinRepartidor.registrarPedido(otro)
    sinRepartidor.procesarPedido(otro)
    otro.cambiarEstado(EstadoPedido.LISTO)
    check("No hay repartidores" in sinRepartidor.despachar(otro))
    check(otro.estado == EstadoPedido.LISTO)
    falla { sinRepartidor.registrarPedido(otro) }
    falla { Pedido(10, cliente, List(101) { pizza }) }
    falla { Cliente("Ana", "", "", "") }
    falla { Repartidor(0, "Ana", "1", 1.0) }
    falla { Pizzeria(emptyMap(), listOf(cerca, cerca)) }
    val stockAntes = sistema.consultarInventario()
    falla { sistema.reabastecer("Quesoo", 5) }
    check(sistema.consultarInventario() == stockAntes)
    sistema.reabastecer(" queso ", 3)
    check(sistema.consultarInventario()["Queso"] == 3)
    val limite = Pizzeria(mapOf("Masa" to Int.MAX_VALUE), emptyList())
    fallaCon<ArithmeticException> { limite.reabastecer("Masa", 1) }
    check(limite.consultarInventario()["Masa"] == Int.MAX_VALUE)
    val recetaGrande = Pizza("Grande", 1.0, mapOf("Masa" to Int.MAX_VALUE))
    fallaCon<ArithmeticException> { limite.prepararLote(listOf(recetaGrande, recetaGrande)) }
    check(limite.consultarInventario()["Masa"] == Int.MAX_VALUE)

    val respuestas = ArrayDeque(listOf("abc", "999999999999999999999", "-1", "2.5", "7", "3"))
    val mensajes = mutableListOf<String>()
    val entrada = EntradaConsola({ respuestas.removeFirstOrNull() }, { mensajes.add(it) })
    check(entrada.entero("Opción", 0, 0, 6) == 3)
    check(mensajes.any { "sin decimales" in it } && mensajes.any { "entre 0 y 6" in it })
    val confirmaciones = ArrayDeque(listOf("quizás", "sí"))
    check(EntradaConsola({ confirmaciones.removeFirstOrNull() }, {}).confirmar("Continuar"))
    fallaCon<FinDeEntrada> { EntradaConsola({ null }, {}).entero("Cantidad", 1, 1) }
    fallaCon<FinDeEntrada> { EntradaConsola({ null }, {}).texto("Nombre", "Ana") }
    fallaCon<OperacionCancelada> { EntradaConsola({ "/cancelar" }, {}).confirmar("Continuar") }
    val textos = ArrayDeque(listOf("x".repeat(201), "Ana"))
    check(EntradaConsola({ textos.removeFirstOrNull() }, {}).texto("Nombre", "") == "Ana")
    println("Verificaciones correctas: contactos, cálculos, inventario, errores, estados, reparto y reporte.")
}
