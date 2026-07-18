package com.casadelosol.inventario.dao;

import com.casadelosol.inventario.DatabaseManager;
import com.casadelosol.inventario.model.ProductoTerminado;
import com.casadelosol.inventario.model.Venta;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO {

    public List<Venta> findAll() {
        List<Venta> list = new ArrayList<>();
        String sql = "SELECT id, producto_terminado_id, cantidad, fecha FROM venta ORDER BY fecha DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar ventas", e);
        }
        return list;
    }

    public Venta findById(int id) {
        String sql = "SELECT id, producto_terminado_id, cantidad, fecha FROM venta WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar venta", e);
        }
        return null;
    }

    public List<Venta> findByFechaRange(LocalDate desde, LocalDate hasta) {
        List<Venta> list = new ArrayList<>();
        String sql = "SELECT id, producto_terminado_id, cantidad, fecha FROM venta WHERE fecha BETWEEN ? AND ? ORDER BY fecha DESC";
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
            throw new RuntimeException("Error al buscar ventas por rango de fecha", e);
        }
        return list;
    }

    public int save(Venta venta) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            ProductoTerminadoDAO ptDAO = new ProductoTerminadoDAO();
            ProductoTerminado pt = ptDAO.findById(venta.getProductoTerminadoId(), conn);

            if (pt.getStockActual() < venta.getCantidad()) {
                throw new RuntimeException("Stock insuficiente de " + pt.getNombre()
                        + " (disponible: " + pt.getStockActual()
                        + ", requerido: " + venta.getCantidad() + ")");
            }

            String sql = "INSERT INTO venta (producto_terminado_id, cantidad, fecha) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, venta.getProductoTerminadoId());
                stmt.setDouble(2, venta.getCantidad());
                stmt.setString(3, venta.getFecha().toString());
                stmt.executeUpdate();
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        venta.setId(keys.getInt(1));
                    }
                }
            }

            double nuevoStock = pt.getStockActual() - venta.getCantidad();
            ptDAO.updateStock(venta.getProductoTerminadoId(), nuevoStock, conn);

            conn.commit();
            return venta.getId();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw new RuntimeException("Error al registrar venta", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public List<ResumenVenta> getResumenPorPeriodo(LocalDate desde, LocalDate hasta) {
        List<ResumenVenta> list = new ArrayList<>();
        String sql = """
            SELECT v.producto_terminado_id, pt.nombre,
                   SUM(v.cantidad) as total_cantidad,
                   SUM(v.cantidad * pt.precio_venta) as total_importe,
                   COUNT(*) as total_ventas
            FROM venta v
            JOIN producto_terminado pt ON v.producto_terminado_id = pt.id
            WHERE v.fecha BETWEEN ? AND ?
            GROUP BY v.producto_terminado_id
            ORDER BY total_cantidad DESC
            """;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, desde.toString());
            stmt.setString(2, hasta.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new ResumenVenta(
                            rs.getInt("producto_terminado_id"),
                            rs.getString("nombre"),
                            rs.getDouble("total_cantidad"),
                            rs.getDouble("total_importe"),
                            rs.getInt("total_ventas")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al generar resumen de ventas", e);
        }
        return list;
    }

    public void delete(int id) {
        String sql = "DELETE FROM venta WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar venta", e);
        }
    }

    public record ResumenVenta(int productoId, String productoNombre, double totalCantidad, double totalImporte, int totalVentas) {
    }

    private Venta mapRow(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setId(rs.getInt("id"));
        v.setProductoTerminadoId(rs.getInt("producto_terminado_id"));
        v.setCantidad(rs.getDouble("cantidad"));
        v.setFecha(LocalDate.parse(rs.getString("fecha")));
        return v;
    }
}
