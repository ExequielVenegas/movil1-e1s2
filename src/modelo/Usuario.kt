package modelo
open class Usuario(val nombre: String, val contacto: String) {
    init { require(nombre.isNotBlank() && nombre.length <= 200 && nombre.none { it.isISOControl() }) { "El nombre es obligatorio y debe tener hasta 200 caracteres sin controles." } }
    open fun descripcion(): String = "$nombre | $contacto"
}
