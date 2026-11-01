# Pizzería Duoc
Aplicación de consola Kotlin/JVM para la actividad formativa individual de la semana 2.

## Ejecutar en IntelliJ IDEA
1. Abrir la carpeta PizzeriaDuoc como proyecto.
2. Usar el JDK 25 y la biblioteca Kotlin del proyecto.
3. Abrir src/Main.kt y ejecutar su función main.
4. Para comprobar los casos de negocio, ejecutar main en test/Verificacion.kt.

## Uso
- Opción 1: registrar cliente, elegir variedades y cantidades y agregar notas opcionales.
- Opción 2: indicar el número del pedido para avanzar un paso:
  Pendiente → En preparación → Listo → En camino → Entregado.
- Si faltan ingredientes, el pedido queda pendiente y muestra cuánto comprar.
  Reabastecer con la opción 4 y reintentar con la opción 2.
- Opciones 3 y 6: consultar inventario y repartidores.
- Opción 5: reporte completo y agrupación de pedidos completados y pendientes.
- Opción 0: mostrar el reporte final y salir. Fin de entrada termina sin crear pedidos incompletos.
- Escribir /cancelar en cualquier entrada cancela la operación y vuelve al menú.
- Las respuestas s/n se vuelven a solicitar si son inválidas. Cada pedido admite hasta 100 pizzas en total.
- Los nombres y notas tienen un límite de 200 caracteres.
- Reabastecer admite mayúsculas y espacios en los extremos, pero rechaza ingredientes desconocidos.
- Los desbordamientos numéricos y las operaciones rechazadas no alteran el inventario.
- Las entradas numéricas inválidas se vuelven a solicitar. Enter acepta el valor predeterminado.
