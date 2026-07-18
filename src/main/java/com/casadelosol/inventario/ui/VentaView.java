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

    private final TableView<Venta> historialTable = new TableView<>();
    private final ObservableList<Venta> historialData = FXCollections.observableArrayList();

    private final VentaDAO ventaDAO = new VentaDAO();
    private final ProductoTerminadoDAO ptDAO = new ProductoTerminadoDAO();

    public Node getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Ventas");
        title.getStyleClass().add("section-title");

        Button btnNueva = new Button("+ Nueva Venta");
        btnNueva.getStyleClass().add("btn-success");
        btnNueva.setOnAction(e -> showNuevaVentaDialog());

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

        root.getChildren().addAll(title, btnNueva, historialTable);

        refresh();
        return root;
    }

    private void showNuevaVentaDialog() {
        Dialog<Venta> dialog = new Dialog<>();
        dialog.setTitle("Nueva Venta");
        dialog.setHeaderText("Registrar venta de producto terminado");

        ButtonType btnSave = new ButtonType("Registrar Venta", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        ComboBox<ProductoTerminado> cbProducto = new ComboBox<>();
        cbProducto.getItems().setAll(ptDAO.findAll());
        cbProducto.setPrefWidth(350);
        cbProducto.setPromptText("Seleccionar producto...");

        TextField tfCantidad = new TextField();
        tfCantidad.setPromptText("Ej: 3");

        DatePicker dpFecha = new DatePicker(LocalDate.now());

        Label lblStockActual = new Label();
        Label lblTotal = new Label();

        cbProducto.setOnAction(e -> {
            ProductoTerminado pt = cbProducto.getValue();
            if (pt != null) {
                lblStockActual.setText("Stock disponible: " + pt.getStockActual());
                lblStockActual.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
                actualizarTotalVenta(pt, tfCantidad, lblTotal);
            } else {
                lblStockActual.setText("");
                lblTotal.setText("");
            }
        });

        tfCantidad.textProperty().addListener((obs, o, n) -> {
            ProductoTerminado pt = cbProducto.getValue();
            if (pt != null) actualizarTotalVenta(pt, tfCantidad, lblTotal);
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.add(new Label("Producto Terminado*:"), 0, 0);
        grid.add(cbProducto, 1, 0);
        grid.add(lblStockActual, 1, 1);
        grid.add(new Label("Cantidad a vender*:"), 0, 2);
        grid.add(tfCantidad, 1, 2);
        grid.add(lblTotal, 1, 3);
        grid.add(new Label("Fecha*:"), 0, 4);
        grid.add(dpFecha, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(btnSave);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String error = null;
            if (cbProducto.getValue() == null) error = "Seleccione un producto";
            else if (tfCantidad.getText().trim().isEmpty()) error = "Ingrese la cantidad a vender";
            if (error != null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, error, ButtonType.OK);
                alert.showAndWait();
                event.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == btnSave) {
                try {
                    double cantidad = Double.parseDouble(tfCantidad.getText().trim());
                    if (cantidad <= 0) {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "La cantidad debe ser mayor a cero", ButtonType.OK);
                        alert.showAndWait();
                        return null;
                    }
                    Venta venta = new Venta(cbProducto.getValue().getId(), cantidad, dpFecha.getValue());
                    ventaDAO.save(venta);
                    return venta;
                } catch (NumberFormatException e) {
                    return null;
                } catch (RuntimeException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage(), ButtonType.OK);
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(v -> refresh());
    }

    private void actualizarTotalVenta(ProductoTerminado pt, TextField tfCantidad, Label lblTotal) {
        try {
            double cantidad = Double.parseDouble(tfCantidad.getText().trim());
            double total = cantidad * pt.getPrecioVenta();
            lblTotal.setText("Total venta: $" + String.format("%.2f", total));
            lblTotal.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } catch (NumberFormatException e) {
            lblTotal.setText("");
        }
    }

    @Override
    public void refresh() {
        historialData.setAll(ventaDAO.findAll());
    }
}
