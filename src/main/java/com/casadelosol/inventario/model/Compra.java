package com.casadelosol.inventario.model;

import java.time.LocalDate;

public class Compra {
    private int id;
    private int materiaPrimaId;
    private LocalDate fecha;
    private double cantidad;
    private double precio;
    private String lugar;

    public Compra() {
    }

    public Compra(int materiaPrimaId, LocalDate fecha, double cantidad, double precio) {
        this.materiaPrimaId = materiaPrimaId;
        this.fecha = fecha;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMateriaPrimaId() {
        return materiaPrimaId;
    }

    public void setMateriaPrimaId(int materiaPrimaId) {
        this.materiaPrimaId = materiaPrimaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }
}
