class FinDeEntrada : RuntimeException()
class OperacionCancelada : RuntimeException()

class EntradaConsola(
    private val leer: () -> String? = { readlnOrNull() },
    private val escribir: (String) -> Unit = { println(it) }
) {
    private fun entrada(mensaje: String): String {
        escribir(mensaje)
        val valor = leer()?.trim() ?: throw FinDeEntrada()
        if (valor.equals("/cancelar", true)) throw OperacionCancelada()
        return valor
    }
    fun texto(mensaje: String, predeterminado: String, maximo: Int = 200): String {
        while (true) {
            val valor = entrada("$mensaje [$predeterminado]: ").ifEmpty { predeterminado }
            if (valor.length <= maximo && valor.none { it.isISOControl() }) return valor
            escribir("Usa hasta $maximo caracteres sin caracteres de control.")
        }
    }
    fun entero(mensaje: String, predeterminado: Int, minimo: Int, maximo: Int = 1_000_000): Int {
        require(minimo <= maximo && predeterminado in minimo..maximo)
        while (true) {
            val valor = entrada("$mensaje [$predeterminado]: ")
            if (valor.isEmpty()) return predeterminado
            try {
                val numero = valor.toInt()
                if (numero in minimo..maximo) return numero
                escribir("El valor debe estar entre $minimo y $maximo.")
            } catch (error: NumberFormatException) {
                escribir("Ingresa un número entero sin decimales, letras ni separadores de miles (entre $minimo y $maximo).")
            }
        }
    }
    fun confirmar(mensaje: String): Boolean {
        while (true) {
            when (entrada("$mensaje (s/n) [n]: ").lowercase()) {
                "s", "si", "sí" -> return true
                "", "n", "no" -> return false
                else -> escribir("Respuesta inválida. Escribe s o n.")
            }
        }
    }
}
