package com.casadelosol.inventario.dao;

import com.casadelosol.inventario.DatabaseManager;
import com.casadelosol.inventario.model.Receta;
import com.casadelosol.inventario.model.RecetaDetalle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecetaDAO {

    public Receta findByProducto(int productoTerminadoId) {
        String sql = "SELECT id, producto_terminado_id, notas FROM receta WHERE producto_terminado_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productoTerminadoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Receta receta = mapRow(rs);
                    receta.setDetalles(findDetallesByReceta(receta.getId(), conn));
                    return receta;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar receta por producto", e);
        }
        return null;
    }

    Receta findByProducto(int productoTerminadoId, Connection conn) throws SQLException {
        String sql = "SELECT id, producto_terminado_id, notas FROM receta WHERE producto_terminado_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productoTerminadoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Receta receta = mapRow(rs);
                    receta.setDetalles(findDetallesByReceta(receta.getId(), conn));
                    return receta;
                }
            }
        }
        return null;
    }

    public Receta findById(int id) {
        String sql = "SELECT id, producto_terminado_id, notas FROM receta WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Receta receta = mapRow(rs);
                    receta.setDetalles(findDetallesByReceta(receta.getId(), conn));
                    return receta;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar receta", e);
        }
        return null;
    }

    public int save(Receta receta) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            String sql = "INSERT INTO receta (producto_terminado_id, notas) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, receta.getProductoTerminadoId());
                if (receta.getNotas() != null) {
                    stmt.setString(2, receta.getNotas());
                } else {
                    stmt.setNull(2, Types.VARCHAR);
                }
                stmt.executeUpdate();
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        receta.setId(keys.getInt(1));
                    }
                }
            }

            saveDetalles(receta.getId(), receta.getDetalles(), conn);

            conn.commit();
            return receta.getId();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw new RuntimeException("Error al guardar receta", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public void update(Receta receta) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            String sql = "UPDATE receta SET producto_terminado_id = ?, notas = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, receta.getProductoTerminadoId());
                if (receta.getNotas() != null) {
                    stmt.setString(2, receta.getNotas());
                } else {
                    stmt.setNull(2, Types.VARCHAR);
                }
                stmt.setInt(3, receta.getId());
                stmt.executeUpdate();
            }

            deleteDetallesByReceta(receta.getId(), conn);
            saveDetalles(receta.getId(), receta.getDetalles(), conn);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw new RuntimeException("Error al actualizar receta", e);
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
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            deleteDetallesByReceta(id, conn);

            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM receta WHERE id = ?")) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw new RuntimeException("Error al eliminar receta", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public List<RecetaDetalle> findDetallesByReceta(int recetaId, Connection conn) throws SQLException {
        List<RecetaDetalle> list = new ArrayList<>();
        String sql = "SELECT id, receta_id, materia_prima_id, cantidad FROM receta_detalle WHERE receta_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recetaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RecetaDetalle d = new RecetaDetalle();
                    d.setId(rs.getInt("id"));
                    d.setRecetaId(rs.getInt("receta_id"));
                    d.setMateriaPrimaId(rs.getInt("materia_prima_id"));
                    d.setCantidad(rs.getDouble("cantidad"));
                    list.add(d);
                }
            }
        }
        return list;
    }

    private void saveDetalles(int recetaId, List<RecetaDetalle> detalles, Connection conn) throws SQLException {
        String sql = "INSERT INTO receta_detalle (receta_id, materia_prima_id, cantidad) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (RecetaDetalle d : detalles) {
                stmt.setInt(1, recetaId);
                stmt.setInt(2, d.getMateriaPrimaId());
                stmt.setDouble(3, d.getCantidad());
                stmt.executeUpdate();
            }
        }
    }

    private void deleteDetallesByReceta(int recetaId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM receta_detalle WHERE receta_id = ?")) {
            stmt.setInt(1, recetaId);
            stmt.executeUpdate();
        }
    }

    private Receta mapRow(ResultSet rs) throws SQLException {
        Receta r = new Receta();
        r.setId(rs.getInt("id"));
        r.setProductoTerminadoId(rs.getInt("producto_terminado_id"));
        String notas = rs.getString("notas");
        if (!rs.wasNull()) {
            r.setNotas(notas);
        }
        return r;
    }
}
