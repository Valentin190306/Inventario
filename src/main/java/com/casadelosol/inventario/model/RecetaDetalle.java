package com.casadelosol.inventario.model;

public class RecetaDetalle {
    private int id;
    private int recetaId;
    private int materiaPrimaId;
    private double cantidad;

    public RecetaDetalle() {
    }

    public RecetaDetalle(int materiaPrimaId, double cantidad) {
        this.materiaPrimaId = materiaPrimaId;
        this.cantidad = cantidad;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecetaId() {
        return recetaId;
    }

    public void setRecetaId(int recetaId) {
        this.recetaId = recetaId;
    }

    public int getMateriaPrimaId() {
        return materiaPrimaId;
    }

    public void setMateriaPrimaId(int materiaPrimaId) {
        this.materiaPrimaId = materiaPrimaId;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }
}
