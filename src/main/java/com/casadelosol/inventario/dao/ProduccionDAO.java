package com.casadelosol.inventario.dao;

import com.casadelosol.inventario.DatabaseManager;
import com.casadelosol.inventario.model.MateriaPrima;
import com.casadelosol.inventario.model.Produccion;
import com.casadelosol.inventario.model.Receta;
import com.casadelosol.inventario.model.RecetaDetalle;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProduccionDAO {

    public List<Produccion> findAll() {
        List<Produccion> list = new ArrayList<>();
        String sql = "SELECT id, producto_terminado_id, cantidad, fecha FROM produccion ORDER BY fecha DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar producciones", e);
        }
        return list;
    }

    public Produccion findById(int id) {
        String sql = "SELECT id, producto_terminado_id, cantidad, fecha FROM produccion WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar producción", e);
        }
        return null;
    }

    public List<Produccion> findByFechaRange(LocalDate desde, LocalDate hasta) {
        List<Produccion> list = new ArrayList<>();
        String sql = "SELECT id, producto_terminado_id, cantidad, fecha FROM produccion WHERE fecha BETWEEN ? AND ? ORDER BY fecha DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, desde.toString());
            stmt.setString(2, hasta.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar producciones por rango de fecha", e);
        }
        return list;
    }

    public int save(Produccion produccion) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            RecetaDAO recetaDAO = new RecetaDAO();
            Receta receta = recetaDAO.findByProducto(produccion.getProductoTerminadoId());

            if (receta == null) {
                throw new RuntimeException("El producto no tiene una receta definida");
            }

            MateriaPrimaDAO mpDAO = new MateriaPrimaDAO();

            for (RecetaDetalle detalle : receta.getDetalles()) {
                MateriaPrima mp = mpDAO.findById(detalle.getMateriaPrimaId());
                double cantidadRequerida = detalle.getCantidad() * produccion.getCantidad();
                if (mp.getStockActual() < cantidadRequerida) {
                    throw new RuntimeException("Stock insuficiente de " + mp.getNombre()
                            + " (requerido: " + cantidadRequerida + " " + mp.getUnidadMedida()
                            + ", disponible: " + mp.getStockActual() + ")");
                }
            }

            for (RecetaDetalle detalle : receta.getDetalles()) {
                MateriaPrima mp = mpDAO.findById(detalle.getMateriaPrimaId());
                double nuevaCantidad = mp.getStockActual() - (detalle.getCantidad() * produccion.getCantidad());
                mpDAO.updateStock(detalle.getMateriaPrimaId(), nuevaCantidad);
            }

            String sql = "INSERT INTO produccion (producto_terminado_id, cantidad, fecha) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, produccion.getProductoTerminadoId());
                stmt.setDouble(2, produccion.getCantidad());
                stmt.setString(3, produccion.getFecha().toString());
                stmt.executeUpdate();
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        produccion.setId(keys.getInt(1));
                    }
                }
            }

            ProductoTerminadoDAO ptDAO = new ProductoTerminadoDAO();
            var pt = ptDAO.findById(produccion.getProductoTerminadoId());
            double nuevoStockPT = pt.getStockActual() + produccion.getCantidad();
            ptDAO.updateStock(produccion.getProductoTerminadoId(), nuevoStockPT);

            conn.commit();
            return produccion.getId();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw new RuntimeException("Error al registrar producción", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM produccion WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar producción", e);
        }
    }

    private Produccion mapRow(ResultSet rs) throws SQLException {
        Produccion p = new Produccion();
        p.setId(rs.getInt("id"));
        p.setProductoTerminadoId(rs.getInt("producto_terminado_id"));
        p.setCantidad(rs.getDouble("cantidad"));
        p.setFecha(LocalDate.parse(rs.getString("fecha")));
        return p;
    }
}
