package com.casadelosol.inventario.model;

public class MateriaPrima {
    private int id;
    private String nombre;
    private String unidadMedida;
    private double stockActual;
    private int categoriaId;

    public MateriaPrima() {
    }

    public MateriaPrima(String nombre, String unidadMedida, int categoriaId) {
        this.nombre = nombre;
        this.unidadMedida = unidadMedida;
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

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
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
        return nombre + " (" + stockActual + " " + unidadMedida + ")";
    }
}
