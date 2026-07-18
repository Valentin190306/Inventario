package com.casadelosol.inventario.model;

import java.util.ArrayList;
import java.util.List;

public class Receta {
    private int id;
    private int productoTerminadoId;
    private String notas;
    private List<RecetaDetalle> detalles;

    public Receta() {
        this.detalles = new ArrayList<>();
    }

    public Receta(int productoTerminadoId) {
        this.productoTerminadoId = productoTerminadoId;
        this.detalles = new ArrayList<>();
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

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public List<RecetaDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<RecetaDetalle> detalles) {
        this.detalles = detalles;
    }
}
