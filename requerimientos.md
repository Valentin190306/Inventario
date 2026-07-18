# Requerimientos del Sistema de Gestión de Inventario

## Contexto del proyecto

Sistema de gestión de inventario para un emprendimiento de fabricación y venta de productos de higiene personal libres de sustancias tóxicas (shampoo en barra, pasta dental, desodorante, entre otros).

**Usuario:** una sola persona, que fabrica y vende los productos, opera desde su propia notebook con Windows 11.

**Tipo de solución:** aplicación de escritorio local.

---

## Entidades principales

### Categoría de Materia Prima
- Nombre
- Categoría padre (opcional, para subcategorías)

### Categoría de Producto Terminado
- Nombre
- Categoría padre (opcional, para subcategorías)

> Las categorías de materias primas y productos terminados son **independientes entre sí**, ya que responden a clasificaciones de negocio distintas.

### Materia Prima
- Nombre
- Unidad de medida (gramos, ml, unidades, etc.)
- Stock actual
- Categoría (referencia a Categoría de Materia Prima)

### Compra de Materia Prima
- Materia prima asociada
- Fecha de compra
- Cantidad comprada
- Precio pagado
- Lugar de compra (opcional, sin gestión formal de proveedores)

> Permite mantener un **historial completo de precios** por materia prima, no solo el último precio pagado, para que la usuaria pueda comparar y decidir dónde reabastecerse.

### Producto Terminado
- Nombre / variante (ej. "Shampoo en barra - cabello graso")
- Precio de venta
- Stock actual
- Categoría (referencia a Categoría de Producto Terminado)

> Cada variante de un producto (ej. distintos tipos de cabello) se modela como un **producto terminado independiente**, ya que la receta puede diferir entre variantes.

### Receta
- Producto terminado asociado
- Lista de materias primas requeridas, cada una con su cantidad necesaria
- Notas (texto libre para observaciones, detalles o procedimientos de fabricación)

### Producción
- Producto terminado fabricado
- Cantidad fabricada
- Fecha
- Efecto: descuenta automáticamente el stock de materias primas según la receta, y suma al stock del producto terminado.

### Venta
- Producto terminado vendido
- Cantidad vendida
- Fecha
- Efecto: descuenta automáticamente el stock del producto terminado.

---

## Requisitos Funcionales

| ID | Descripción | Prioridad |
|---|---|---|
| RF-001 | El sistema debe permitir registrar una nueva materia prima, indicando nombre y unidad de medida. | Alta |
| RF-002 | El sistema debe permitir modificar y eliminar materias primas existentes. | Alta |
| RF-003 | El sistema debe permitir consultar el stock actual de cada materia prima. | Alta |
| RF-004 | El sistema debe permitir registrar una compra de materia prima, indicando cantidad, precio pagado, fecha y lugar de compra (opcional). | Alta |
| RF-005 | El sistema debe actualizar automáticamente el stock de la materia prima al registrar una compra. | Alta |
| RF-006 | El sistema debe mantener un historial completo de compras por materia prima, incluyendo precios históricos. | Alta |
| RF-007 | El sistema debe permitir registrar un nuevo producto terminado, indicando nombre/variante y precio de venta. | Alta |
| RF-008 | El sistema debe permitir modificar y eliminar productos terminados existentes. | Alta |
| RF-009 | El sistema debe permitir consultar el stock actual de cada producto terminado. | Alta |
| RF-010 | El sistema debe permitir definir una receta para cada producto terminado, asociando una o más materias primas con su cantidad necesaria. | Alta |
| RF-011 | El sistema debe permitir modificar la receta de un producto terminado existente. | Media |
| RF-012 | El sistema debe permitir registrar un evento de producción, indicando producto terminado y cantidad fabricada. | Alta |
| RF-013 | Al registrar una producción, el sistema debe descontar automáticamente el stock de cada materia prima involucrada, según la receta correspondiente. | Alta |
| RF-014 | Al registrar una producción, el sistema debe incrementar automáticamente el stock del producto terminado fabricado. | Alta |
| RF-015 | El sistema debe impedir registrar una producción si no hay stock suficiente de alguna materia prima requerida. | Media |
| RF-016 | El sistema debe permitir registrar una venta, indicando producto terminado y cantidad vendida. | Alta |
| RF-017 | Al registrar una venta, el sistema debe descontar automáticamente el stock del producto terminado correspondiente. | Alta |
| RF-018 | El sistema debe impedir registrar una venta si no hay stock suficiente del producto terminado. | Media |
| RF-019 | El sistema debe mostrar un listado del stock actual de todas las materias primas y productos terminados. | Alta |
| RF-020 | El sistema debe alertar o destacar visualmente las materias primas o productos terminados con stock por debajo de un umbral definido. | Media |
| RF-021 | El sistema debe permitir consultar el historial de precios de compra de una materia prima específica. | Alta |
| RF-022 | El sistema debe permitir generar un reporte de totales de producción por período de tiempo. | Baja |
| RF-023 | El sistema debe permitir generar un reporte de totales de ventas por período de tiempo. | Baja |
| RF-024 | El sistema debe permitir gestionar categorías y subcategorías independientes para materias primas. | Alta |
| RF-025 | El sistema debe permitir gestionar categorías y subcategorías independientes para productos terminados. | Alta |
| RF-026 | El sistema debe permitir filtrar materias primas por categoría y por nombre, y ordenar por stock actual, fecha y precio de última compra. | Alta |
| RF-027 | El sistema debe permitir filtrar productos terminados por categoría y por nombre, y ordenar por stock actual y precio de venta. | Alta |
| RF-028 | El sistema debe permitir agregar notas de texto libre a cada receta, para registrar observaciones, detalles o procedimientos de fabricación. | Media |
| RF-029 | El sistema debe permitir ordenar el historial de compras por fecha, precio pagado y cantidad. | Alta |
| RF-030 | El sistema debe permitir ordenar el historial de producción por fecha y cantidad fabricada. | Media |
| RF-031 | El sistema debe permitir ordenar el historial de ventas por fecha y cantidad vendida. | Media |

> **Nota:** RF-022 y RF-023 quedan pendientes de mayor especificación (granularidad temporal, filtros, formato de salida) antes de pasar a diseño.

---

## Requisitos No Funcionales

| ID | Descripción |
|---|---|
| RNF-001 | El sistema debe ejecutarse como aplicación de escritorio nativa en Windows 11. |
| RNF-002 | El sistema debe funcionar íntegramente de forma local, sin depender de conexión a internet. |
| RNF-003 | El sistema debe estar pensado para un único usuario concurrente. |
| RNF-004 | El sistema debe poder iniciarse de forma simple, sin intervención técnica del usuario final. |
| RNF-005 | El sistema debe minimizar el consumo de recursos (memoria y CPU), considerando las limitaciones de la notebook donde opera. |

---

## Fuera de alcance (por ahora)

- Gestión formal de proveedores (no se requiere, la usuaria reabastece de manera informal).
- Multiusuario o roles de acceso (un solo usuario en notebook propia).
- Facturación o integración con sistemas fiscales.

---

## Stack tecnológico

| Capa | Tecnología | Justificación |
|---|---|---|
| **UI** | JavaFX | Framework de escritorio moderno para Java; superior a Swing en capacidades visuales y arquitectura. |
| **Lógica de negocio** | Java | Lenguaje ya conocido por el desarrollador; ecosistema maduro y multiplataforma. |
| **Acceso a datos** | JDBC directo | Modelo de datos acotado (6 entidades); evita el overhead de un ORM; el desarrollador ya domina SQL. |
| **Base de datos** | SQLite | Un solo usuario concurrente; no requiere servidor separado; backup trivial (un archivo). |
| **Build** | Maven o Gradle | Gestión de dependencias y empaquetado del ejecutable. |

**Arquitectura:** aplicación de escritorio monolítica. La UI en JavaFX accede directamente a una base de datos SQLite local mediante JDBC, sin servidor HTTP ni proceso adicional.
