package modelo
open class Producto(val nombre: String, val precio: Double, ingredientes: Map<String, Int>) {
    val ingredientes = ingredientes.toMap()
    init {
        require(nombre.isNotBlank()) { "Nombre obligatorio." }
        require(precio.isFinite() && precio > 0 && precio <= 1_000_000_000) { "Precio inválido." }
        require(this.ingredientes.isNotEmpty() && this.ingredientes.all { it.key.isNotBlank() && it.value > 0 }) { "Receta inválida." }
    }
    fun detallarProducto(): String = "$nombre | Precio: $precio | Ingredientes: ${ingredientes.entries.joinToString { "${it.key}: ${it.value}" }}"
}
