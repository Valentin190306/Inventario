CREATE TABLE IF NOT EXISTS categoria_mp (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    categoria_padre_id INTEGER REFERENCES categoria_mp(id)
);

CREATE TABLE IF NOT EXISTS categoria_pt (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    categoria_padre_id INTEGER REFERENCES categoria_pt(id)
);

CREATE TABLE IF NOT EXISTS materia_prima (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    unidad_medida TEXT NOT NULL,
    stock_actual REAL NOT NULL DEFAULT 0,
    categoria_id INTEGER REFERENCES categoria_mp(id)
);

CREATE TABLE IF NOT EXISTS compra (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    materia_prima_id INTEGER NOT NULL REFERENCES materia_prima(id),
    fecha TEXT NOT NULL,
    cantidad REAL NOT NULL,
    precio REAL NOT NULL,
    lugar TEXT
);

CREATE TABLE IF NOT EXISTS producto_terminado (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    precio_venta REAL NOT NULL,
    stock_actual REAL NOT NULL DEFAULT 0,
    categoria_id INTEGER REFERENCES categoria_pt(id)
);

CREATE TABLE IF NOT EXISTS receta (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    producto_terminado_id INTEGER NOT NULL REFERENCES producto_terminado(id),
    notas TEXT
);

CREATE TABLE IF NOT EXISTS receta_detalle (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    receta_id INTEGER NOT NULL REFERENCES receta(id),
    materia_prima_id INTEGER NOT NULL REFERENCES materia_prima(id),
    cantidad REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS produccion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    producto_terminado_id INTEGER NOT NULL REFERENCES producto_terminado(id),
    cantidad REAL NOT NULL,
    fecha TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS venta (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    producto_terminado_id INTEGER NOT NULL REFERENCES producto_terminado(id),
    cantidad REAL NOT NULL,
    fecha TEXT NOT NULL
);
