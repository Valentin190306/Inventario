package com.casadelosol.inventario.model;

import java.time.LocalDate;

public class Venta {
    private int id;
    private int productoTerminadoId;
    private double cantidad;
    private LocalDate fecha;

    public Venta() {
    }

    public Venta(int productoTerminadoId, double cantidad, LocalDate fecha) {
        this.productoTerminadoId = productoTerminadoId;
        this.cantidad = cantidad;
        this.fecha = fecha;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductoTerminadoId() {
        return productoTerminadoId;
    }

    public void setProductoTerminadoId(int productoTerminadoId) {
        this.productoTerminadoId = productoTerminadoId;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}
