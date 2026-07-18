package com.casadelosol.inventario.dao;

import com.casadelosol.inventario.DatabaseManager;
import com.casadelosol.inventario.model.ProductoTerminado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoTerminadoDAO {

    public List<ProductoTerminado> findAll() {
        List<ProductoTerminado> list = new ArrayList<>();
        String sql = "SELECT id, nombre, precio_venta, stock_actual, categoria_id FROM producto_terminado ORDER BY nombre";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos terminados", e);
        }
        return list;
    }

    public ProductoTerminado findById(int id) {
        String sql = "SELECT id, nombre, precio_venta, stock_actual, categoria_id FROM producto_terminado WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar producto terminado", e);
        }
        return null;
    }

    public List<ProductoTerminado> findByCategoria(int categoriaId) {
        List<ProductoTerminado> list = new ArrayList<>();
        String sql = "SELECT id, nombre, precio_venta, stock_actual, categoria_id FROM producto_terminado WHERE categoria_id = ? ORDER BY nombre";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoriaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al filtrar productos por categoría", e);
        }
        return list;
    }

    public List<ProductoTerminado> searchByNombre(String texto) {
        List<ProductoTerminado> list = new ArrayList<>();
        String sql = "SELECT id, nombre, precio_venta, stock_actual, categoria_id FROM producto_terminado WHERE nombre LIKE ? ORDER BY nombre";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + texto + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar productos por nombre", e);
        }
        return list;
    }

    public int save(ProductoTerminado pt) {
        String sql = "INSERT INTO producto_terminado (nombre, precio_venta, stock_actual, categoria_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, pt.getNombre());
            stmt.setDouble(2, pt.getPrecioVenta());
            stmt.setDouble(3, pt.getStockActual());
            stmt.setInt(4, pt.getCategoriaId());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    pt.setId(keys.getInt(1));
                    return pt.getId();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar producto terminado", e);
        }
        return -1;
    }

    public void update(ProductoTerminado pt) {
        String sql = "UPDATE producto_terminado SET nombre = ?, precio_venta = ?, stock_actual = ?, categoria_id = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pt.getNombre());
            stmt.setDouble(2, pt.getPrecioVenta());
            stmt.setDouble(3, pt.getStockActual());
            stmt.setInt(4, pt.getCategoriaId());
            stmt.setInt(5, pt.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar producto terminado", e);
        }
    }

    public void updateStock(int id, double nuevoStock) {
        String sql = "UPDATE producto_terminado SET stock_actual = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, nuevoStock);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar stock de producto terminado", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM producto_terminado WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar producto terminado", e);
        }
    }

    private ProductoTerminado mapRow(ResultSet rs) throws SQLException {
        ProductoTerminado pt = new ProductoTerminado();
        pt.setId(rs.getInt("id"));
        pt.setNombre(rs.getString("nombre"));
        pt.setPrecioVenta(rs.getDouble("precio_venta"));
        pt.setStockActual(rs.getDouble("stock_actual"));
        pt.setCategoriaId(rs.getInt("categoria_id"));
        return pt;
    }
}
