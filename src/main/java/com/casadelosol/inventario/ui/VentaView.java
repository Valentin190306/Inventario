package com.casadelosol.inventario.ui;

import com.casadelosol.inventario.dao.ProductoTerminadoDAO;
import com.casadelosol.inventario.dao.VentaDAO;
import com.casadelosol.inventario.model.ProductoTerminado;
import com.casadelosol.inventario.model.Venta;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class VentaView implements Refreshable {

    private final ComboBox<ProductoTerminado> cbProducto = new ComboBox<>();
    private final TextField tfCantidad = new TextField();
    private final DatePicker dpFecha = new DatePicker(LocalDate.now());
    private final Label lblStockActual = new Label();
    private final Label lblTotal = new Label();
    private final TableView<Venta> historialTable = new TableView<>();
    private final ObservableList<Venta> historialData = FXCollections.observableArrayList();

    private final VentaDAO ventaDAO = new VentaDAO();
    private final ProductoTerminadoDAO ptDAO = new ProductoTerminadoDAO();

    public Node getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Registrar Venta");
        title.getStyleClass().add("section-title");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(15));
        form.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        cbProducto.setPrefWidth(350);
        cbProducto.setPromptText("Seleccionar producto...");
        cbProducto.setOnAction(e -> actualizarInfo());

        tfCantidad.setPromptText("Ej: 3");
        tfCantidad.textProperty().addListener((obs, o, n) -> actualizarTotal());

        Button btnVender = new Button("Registrar Venta");
        btnVender.getStyleClass().add("btn-success");
        btnVender.setOnAction(e -> registrarVenta());

        form.add(new Label("Producto Terminado*:"), 0, 0);
        form.add(cbProducto, 1, 0);
        form.add(lblStockActual, 1, 1);
        form.add(new Label("Cantidad a vender*:"), 0, 2);
        form.add(tfCantidad, 1, 2);
        form.add(lblTotal, 1, 3);
        form.add(new Label("Fecha*:"), 0, 4);
        form.add(dpFecha, 1, 4);
        form.add(btnVender, 1, 5);

        Label historyTitle = new Label("Historial de Ventas");
        historyTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 15 0 5 0;");

        TableColumn<Venta, String> colPT = new TableColumn<>("Producto");
        colPT.setCellValueFactory(d -> {
            ProductoTerminado pt = ptDAO.findById(d.getValue().getProductoTerminadoId());
            return new SimpleStringProperty(pt != null ? pt.getNombre() : "");
        });
        colPT.setPrefWidth(250);

        TableColumn<Venta, Number> colCant = new TableColumn<>("Cantidad");
        colCant.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getCantidad()));
        colCant.setPrefWidth(100);

        TableColumn<Venta, Number> colTotal = new TableColumn<>("Total ($)");
        colTotal.setCellValueFactory(d -> {
            ProductoTerminado pt = ptDAO.findById(d.getValue().getProductoTerminadoId());
            double total = pt != null ? d.getValue().getCantidad() * pt.getPrecioVenta() : 0;
            return new SimpleDoubleProperty(total);
        });
        colTotal.setPrefWidth(120);

        TableColumn<Venta, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha().toString()));
        colFecha.setPrefWidth(120);

        historialTable.getColumns().addAll(colPT, colCant, colTotal, colFecha);
        historialTable.setItems(historialData);
        historialTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        root.getChildren().addAll(title, form, historyTitle, historialTable);

        refresh();
        return root;
    }

    private void actualizarInfo() {
        ProductoTerminado pt = cbProducto.getValue();
        if (pt != null) {
            lblStockActual.setText("Stock disponible: " + pt.getStockActual());
            lblStockActual.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
            actualizarTotal();
        } else {
            lblStockActual.setText("");
            lblTotal.setText("");
        }
    }

    private void actualizarTotal() {
        ProductoTerminado pt = cbProducto.getValue();
        if (pt == null) return;

        try {
            double cantidad = Double.parseDouble(tfCantidad.getText().trim());
            double total = cantidad * pt.getPrecioVenta();
            lblTotal.setText("Total venta: $" + String.format("%.2f", total));
            lblTotal.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } catch (NumberFormatException e) {
            lblTotal.setText("");
        }
    }

    private void registrarVenta() {
        ProductoTerminado pt = cbProducto.getValue();
        if (pt == null) {
            showAlert("Seleccione un producto");
            return;
        }
        if (tfCantidad.getText().trim().isEmpty()) {
            showAlert("Ingrese la cantidad a vender");
            return;
        }

        try {
            double cantidad = Double.parseDouble(tfCantidad.getText().trim());
            if (cantidad <= 0) {
                showAlert("La cantidad debe ser mayor a cero");
                return;
            }

            Venta venta = new Venta(pt.getId(), cantidad, dpFecha.getValue());
            ventaDAO.save(venta);

            tfCantidad.clear();
            dpFecha.setValue(LocalDate.now());

            showInfo("Venta registrada correctamente");
            refresh();
        } catch (NumberFormatException e) {
            showAlert("La cantidad debe ser un número válido");
        } catch (RuntimeException e) {
            showAlert("Error: " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }

    @Override
    public void refresh() {
        cbProducto.getItems().setAll(ptDAO.findAll());
        historialData.setAll(ventaDAO.findAll());
        lblStockActual.setText("");
        lblTotal.setText("");
    }
}
