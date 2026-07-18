package com.casadelosol.inventario.dao;

import com.casadelosol.inventario.DatabaseManager;
import com.casadelosol.inventario.model.MateriaPrima;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MateriaPrimaDAO {

    public List<MateriaPrima> findAll() {
        List<MateriaPrima> list = new ArrayList<>();
        String sql = "SELECT id, nombre, unidad_medida, stock_actual, categoria_id FROM materia_prima ORDER BY nombre";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar materias primas", e);
        }
        return list;
    }

    public MateriaPrima findById(int id) {
        String sql = "SELECT id, nombre, unidad_medida, stock_actual, categoria_id FROM materia_prima WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar materia prima", e);
        }
        return null;
    }

    MateriaPrima findById(int id, Connection conn) throws SQLException {
        String sql = "SELECT id, nombre, unidad_medida, stock_actual, categoria_id FROM materia_prima WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<MateriaPrima> findByCategoria(int categoriaId) {
        List<MateriaPrima> list = new ArrayList<>();
        String sql = "SELECT id, nombre, unidad_medida, stock_actual, categoria_id FROM materia_prima WHERE categoria_id = ? ORDER BY nombre";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoriaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al filtrar materias primas por categoría", e);
        }
        return list;
    }

    public List<MateriaPrima> searchByNombre(String texto) {
        List<MateriaPrima> list = new ArrayList<>();
        String sql = "SELECT id, nombre, unidad_medida, stock_actual, categoria_id FROM materia_prima WHERE nombre LIKE ? ORDER BY nombre";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + texto + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar materias primas por nombre", e);
        }
        return list;
    }

    public int save(MateriaPrima mp) {
        String sql = "INSERT INTO materia_prima (nombre, unidad_medida, stock_actual, categoria_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, mp.getNombre());
            stmt.setString(2, mp.getUnidadMedida());
            stmt.setDouble(3, mp.getStockActual());
            stmt.setInt(4, mp.getCategoriaId());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    mp.setId(keys.getInt(1));
                    return mp.getId();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar materia prima", e);
        }
        return -1;
    }

    public void update(MateriaPrima mp) {
        String sql = "UPDATE materia_prima SET nombre = ?, unidad_medida = ?, stock_actual = ?, categoria_id = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mp.getNombre());
            stmt.setString(2, mp.getUnidadMedida());
            stmt.setDouble(3, mp.getStockActual());
            stmt.setInt(4, mp.getCategoriaId());
            stmt.setInt(5, mp.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar materia prima", e);
        }
    }

    public void updateStock(int id, double nuevoStock) {
        String sql = "UPDATE materia_prima SET stock_actual = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, nuevoStock);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar stock de materia prima", e);
        }
    }

    void updateStock(int id, double nuevoStock, Connection conn) throws SQLException {
        String sql = "UPDATE materia_prima SET stock_actual = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, nuevoStock);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM materia_prima WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar materia prima", e);
        }
    }

    private MateriaPrima mapRow(ResultSet rs) throws SQLException {
        MateriaPrima mp = new MateriaPrima();
        mp.setId(rs.getInt("id"));
        mp.setNombre(rs.getString("nombre"));
        mp.setUnidadMedida(rs.getString("unidad_medida"));
        mp.setStockActual(rs.getDouble("stock_actual"));
        mp.setCategoriaId(rs.getInt("categoria_id"));
        return mp;
    }
}
