package com.casadelosol.inventario.model;

public class CategoriaMP {
    private int id;
    private String nombre;
    private Integer categoriaPadreId;

    public CategoriaMP() {
    }

    public CategoriaMP(String nombre) {
        this.nombre = nombre;
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

    public Integer getCategoriaPadreId() {
        return categoriaPadreId;
    }

    public void setCategoriaPadreId(Integer categoriaPadreId) {
        this.categoriaPadreId = categoriaPadreId;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
