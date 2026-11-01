package modelo
class Cliente(nombre: String, val direccion: String, telefono: String, correo: String) :
    Usuario(nombre, "${validarTelefono(telefono)} | ${validarCorreo(correo)}") {
    val telefono = validarTelefono(telefono)
    val correo = validarCorreo(correo)
    init { require(direccion.isNotBlank() && direccion.length <= 200) { "Indica una dirección de hasta 200 caracteres." } }
    override fun descripcion(): String = "Cliente: ${super.descripcion()} | Dirección: $direccion"
    companion object {
        fun validarTelefono(valor: String): String {
            val texto = valor.trim()
            return if (texto.matches(Regex("\\+?[0-9 ()-]+")) && texto.count { it in '0'..'9' } in 7..15)
                texto else "(000) 000-0000"
        }
        fun validarCorreo(valor: String): String = valor.trim().takeIf {
            it.matches(Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+"))
        } ?: "correo@invalido.com"
    }
}
