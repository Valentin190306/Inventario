package com.casadelosol.inventario.dao;

import com.casadelosol.inventario.DatabaseManager;
import com.casadelosol.inventario.model.CategoriaMP;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaMPDAO {

    public List<CategoriaMP> findAll() {
        List<CategoriaMP> list = new ArrayList<>();
        String sql = "SELECT id, nombre, categoria_padre_id FROM categoria_mp ORDER BY nombre";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar categorías de MP", e);
        }
        return list;
    }

    public CategoriaMP findById(int id) {
        String sql = "SELECT id, nombre, categoria_padre_id FROM categoria_mp WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar categoría de MP", e);
        }
        return null;
    }

    public List<CategoriaMP> findSubcategorias(int parentId) {
        List<CategoriaMP> list = new ArrayList<>();
        String sql = "SELECT id, nombre, categoria_padre_id FROM categoria_mp WHERE categoria_padre_id = ? ORDER BY nombre";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, parentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar subcategorías de MP", e);
        }
        return list;
    }

    public int save(CategoriaMP categoria) {
        String sql = "INSERT INTO categoria_mp (nombre, categoria_padre_id) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, categoria.getNombre());
            if (categoria.getCategoriaPadreId() != null) {
                stmt.setInt(2, categoria.getCategoriaPadreId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    categoria.setId(keys.getInt(1));
                    return categoria.getId();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar categoría de MP", e);
        }
        return -1;
    }

    public void update(CategoriaMP categoria) {
        String sql = "UPDATE categoria_mp SET nombre = ?, categoria_padre_id = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoria.getNombre());
            if (categoria.getCategoriaPadreId() != null) {
                stmt.setInt(2, categoria.getCategoriaPadreId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setInt(3, categoria.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar categoría de MP", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM categoria_mp WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar categoría de MP", e);
        }
    }

    private CategoriaMP mapRow(ResultSet rs) throws SQLException {
        CategoriaMP c = new CategoriaMP();
        c.setId(rs.getInt("id"));
        c.setNombre(rs.getString("nombre"));
        int padreId = rs.getInt("categoria_padre_id");
        if (!rs.wasNull()) {
            c.setCategoriaPadreId(padreId);
        }
        return c;
    }
}
