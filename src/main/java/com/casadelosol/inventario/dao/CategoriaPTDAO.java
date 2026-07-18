package com.casadelosol.inventario.dao;

import com.casadelosol.inventario.DatabaseManager;
import com.casadelosol.inventario.model.CategoriaPT;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaPTDAO {

    public List<CategoriaPT> findAll() {
        List<CategoriaPT> list = new ArrayList<>();
        String sql = "SELECT id, nombre, categoria_padre_id FROM categoria_pt ORDER BY nombre";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar categorías de PT", e);
        }
        return list;
    }

    public CategoriaPT findById(int id) {
        String sql = "SELECT id, nombre, categoria_padre_id FROM categoria_pt WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar categoría de PT", e);
        }
        return null;
    }

    public List<CategoriaPT> findSubcategorias(int parentId) {
        List<CategoriaPT> list = new ArrayList<>();
        String sql = "SELECT id, nombre, categoria_padre_id FROM categoria_pt WHERE categoria_padre_id = ? ORDER BY nombre";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, parentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar subcategorías de PT", e);
        }
        return list;
    }

    public int save(CategoriaPT categoria) {
        String sql = "INSERT INTO categoria_pt (nombre, categoria_padre_id) VALUES (?, ?)";
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
            throw new RuntimeException("Error al guardar categoría de PT", e);
        }
        return -1;
    }

    public void update(CategoriaPT categoria) {
        String sql = "UPDATE categoria_pt SET nombre = ?, categoria_padre_id = ? WHERE id = ?";
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
            throw new RuntimeException("Error al actualizar categoría de PT", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM categoria_pt WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar categoría de PT", e);
        }
    }

    private CategoriaPT mapRow(ResultSet rs) throws SQLException {
        CategoriaPT c = new CategoriaPT();
        c.setId(rs.getInt("id"));
        c.setNombre(rs.getString("nombre"));
        int padreId = rs.getInt("categoria_padre_id");
        if (!rs.wasNull()) {
            c.setCategoriaPadreId(padreId);
        }
        return c;
    }
}
