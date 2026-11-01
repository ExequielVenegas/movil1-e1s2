package modelo
class Repartidor(val idRepartidor: Int, nombre: String, contacto: String, val distanciaKm: Double) : Usuario(nombre, contacto) {
    var disponible = true
        private set
    init {
        require(idRepartidor > 0) { "El identificador del repartidor debe ser positivo." }
        require(distanciaKm.isFinite() && distanciaKm >= 0) { "La distancia debe ser un número finito mayor o igual a cero." }
    }
    internal fun reservar() { check(disponible) { "Repartidor ocupado." }; disponible = false }
    fun entregarPedido(pedido: Pedido): String {
        require(pedido.repartidorAsignado === this) { "El pedido pertenece a otro repartidor." }
        pedido.confirmarEntrega(this)
        disponible = true
        return "$nombre entregó el pedido #${pedido.idPedido}."
    }
    override fun descripcion(): String = "Repartidor: ${super.descripcion()} | $distanciaKm km | ${if (disponible) "Disponible" else "Ocupado"}"
}
