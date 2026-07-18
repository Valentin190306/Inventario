package com.casadelosol.inventario.dao;

import com.casadelosol.inventario.DatabaseManager;
import com.casadelosol.inventario.model.Compra;
import com.casadelosol.inventario.model.MateriaPrima;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CompraDAO {

    public List<Compra> findAll() {
        List<Compra> list = new ArrayList<>();
        String sql = "SELECT id, materia_prima_id, fecha, cantidad, precio, lugar FROM compra ORDER BY fecha DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar compras", e);
        }
        return list;
    }

    public Compra findById(int id) {
        String sql = "SELECT id, materia_prima_id, fecha, cantidad, precio, lugar FROM compra WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar compra", e);
        }
        return null;
    }

    public List<Compra> findByMateriaPrima(int materiaPrimaId) {
        List<Compra> list = new ArrayList<>();
        String sql = "SELECT id, materia_prima_id, fecha, cantidad, precio, lugar FROM compra WHERE materia_prima_id = ? ORDER BY fecha DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, materiaPrimaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar compras por materia prima", e);
        }
        return list;
    }

    public List<Compra> findByFechaRange(LocalDate desde, LocalDate hasta) {
        List<Compra> list = new ArrayList<>();
        String sql = "SELECT id, materia_prima_id, fecha, cantidad, precio, lugar FROM compra WHERE fecha BETWEEN ? AND ? ORDER BY fecha DESC";
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
            throw new RuntimeException("Error al buscar compras por rango de fecha", e);
        }
        return list;
    }

    public int save(Compra compra) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            String sql = "INSERT INTO compra (materia_prima_id, fecha, cantidad, precio, lugar) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, compra.getMateriaPrimaId());
                stmt.setString(2, compra.getFecha().toString());
                stmt.setDouble(3, compra.getCantidad());
                stmt.setDouble(4, compra.getPrecio());
                if (compra.getLugar() != null) {
                    stmt.setString(5, compra.getLugar());
                } else {
                    stmt.setNull(5, Types.VARCHAR);
                }
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        compra.setId(keys.getInt(1));
                    }
                }
            }

            MateriaPrimaDAO mpDAO = new MateriaPrimaDAO();
            MateriaPrima mp = mpDAO.findById(compra.getMateriaPrimaId());
            double nuevoStock = mp.getStockActual() + compra.getCantidad();
            mpDAO.updateStock(compra.getMateriaPrimaId(), nuevoStock);

            conn.commit();
            return compra.getId();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw new RuntimeException("Error al registrar compra", e);
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
        String sql = "DELETE FROM compra WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar compra", e);
        }
    }

    private Compra mapRow(ResultSet rs) throws SQLException {
        Compra c = new Compra();
        c.setId(rs.getInt("id"));
        c.setMateriaPrimaId(rs.getInt("materia_prima_id"));
        c.setFecha(LocalDate.parse(rs.getString("fecha")));
        c.setCantidad(rs.getDouble("cantidad"));
        c.setPrecio(rs.getDouble("precio"));
        String lugar = rs.getString("lugar");
        if (!rs.wasNull()) {
            c.setLugar(lugar);
        }
        return c;
    }
}
