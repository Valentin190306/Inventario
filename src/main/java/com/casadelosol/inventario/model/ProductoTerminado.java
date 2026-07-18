package com.casadelosol.inventario.model;

public class ProductoTerminado {
    private int id;
    private String nombre;
    private double precioVenta;
    private double stockActual;
    private int categoriaId;

    public ProductoTerminado() {
    }

    public ProductoTerminado(String nombre, double precioVenta, int categoriaId) {
        this.nombre = nombre;
        this.precioVenta = precioVenta;
        this.categoriaId = categoriaId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public double getStockActual() {
        return stockActual;
    }

    public void setStockActual(double stockActual) {
        this.stockActual = stockActual;
    }

    public int getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }

    @Override
    public String toString() {
        return nombre + " ($" + precioVenta + ")";
    }
}
