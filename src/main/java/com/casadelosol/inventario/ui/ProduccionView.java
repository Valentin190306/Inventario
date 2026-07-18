package com.casadelosol.inventario.ui;

import com.casadelosol.inventario.dao.MateriaPrimaDAO;
import com.casadelosol.inventario.dao.ProduccionDAO;
import com.casadelosol.inventario.dao.ProductoTerminadoDAO;
import com.casadelosol.inventario.dao.RecetaDAO;
import com.casadelosol.inventario.model.MateriaPrima;
import com.casadelosol.inventario.model.Produccion;
import com.casadelosol.inventario.model.ProductoTerminado;
import com.casadelosol.inventario.model.Receta;
import com.casadelosol.inventario.model.RecetaDetalle;
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

public class ProduccionView implements Refreshable {

    private final TableView<Produccion> historialTable = new TableView<>();
    private final ObservableList<Produccion> historialData = FXCollections.observableArrayList();

    private final ProduccionDAO produccionDAO = new ProduccionDAO();
    private final ProductoTerminadoDAO ptDAO = new ProductoTerminadoDAO();
    private final RecetaDAO recetaDAO = new RecetaDAO();
    private final MateriaPrimaDAO mpDAO = new MateriaPrimaDAO();

    public Node getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Registrar Producción");
        title.getStyleClass().add("section-title");

        Button btnNueva = new Button("+ Nueva Producción");
        btnNueva.getStyleClass().add("btn-success");
        btnNueva.setOnAction(e -> showNuevaProduccionDialog());

        TableColumn<Produccion, String> colPT = new TableColumn<>("Producto");
        colPT.setCellValueFactory(d -> {
            ProductoTerminado pt = ptDAO.findById(d.getValue().getProductoTerminadoId());
            return new SimpleStringProperty(pt != null ? pt.getNombre() : "");
        });
        colPT.setPrefWidth(250);

        TableColumn<Produccion, Number> colCant = new TableColumn<>("Cantidad");
        colCant.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getCantidad()));
        colCant.setPrefWidth(100);

        TableColumn<Produccion, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha().toString()));
        colFecha.setPrefWidth(120);

        historialTable.getColumns().addAll(colPT, colCant, colFecha);
        historialTable.setItems(historialData);
        historialTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        root.getChildren().addAll(title, btnNueva, historialTable);

        refresh();
        return root;
    }

    private void showNuevaProduccionDialog() {
        Dialog<Produccion> dialog = new Dialog<>();
        dialog.setTitle("Nueva Producción");
        dialog.setHeaderText("Registrar producción de productos terminados");

        ButtonType btnSave = new ButtonType("Registrar Producción", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        ComboBox<ProductoTerminado> cbProducto = new ComboBox<>();
        cbProducto.getItems().setAll(ptDAO.findAll());
        cbProducto.setPrefWidth(350);
        cbProducto.setPromptText("Seleccionar producto...");

        TextField tfCantidad = new TextField();
        tfCantidad.setPromptText("Ej: 10");

        DatePicker dpFecha = new DatePicker(LocalDate.now());

        Label lblInsumos = new Label("Insumos requeridos:");
        lblInsumos.setStyle("-fx-font-weight: bold;");

        TableView<RecetaDetalle> recetaPreview = new TableView<>();
        ObservableList<RecetaDetalle> recetaData = FXCollections.observableArrayList();
        recetaPreview.setPrefHeight(150);

        TableColumn<RecetaDetalle, String> colMP = new TableColumn<>("Materia Prima");
        colMP.setCellValueFactory(d -> {
            MateriaPrima mp = mpDAO.findById(d.getValue().getMateriaPrimaId());
            return new SimpleStringProperty(mp != null ? mp.getNombre() : "");
        });
        colMP.setPrefWidth(200);

        TableColumn<RecetaDetalle, Number> colCantReq = new TableColumn<>("Cant. Requerida");
        colCantReq.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getCantidad()));
        colCantReq.setPrefWidth(120);

        TableColumn<RecetaDetalle, String> colUnidad = new TableColumn<>("Unidad");
        colUnidad.setCellValueFactory(d -> {
            MateriaPrima mp = mpDAO.findById(d.getValue().getMateriaPrimaId());
            return new SimpleStringProperty(mp != null ? mp.getUnidadMedida() : "");
        });
        colUnidad.setPrefWidth(80);

        recetaPreview.getColumns().addAll(colMP, colCantReq, colUnidad);
        recetaPreview.setItems(recetaData);

        cbProducto.setOnAction(e -> {
            ProductoTerminado pt = cbProducto.getValue();
            if (pt == null) { recetaData.clear(); return; }
            Receta receta = recetaDAO.findByProducto(pt.getId());
            recetaData.setAll(receta != null ? receta.getDetalles() : FXCollections.observableArrayList());
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.add(new Label("Producto Terminado*:"), 0, 0);
        grid.add(cbProducto, 1, 0);
        grid.add(new Label("Cantidad a fabricar*:"), 0, 1);
        grid.add(tfCantidad, 1, 1);
        grid.add(new Label("Fecha*:"), 0, 2);
        grid.add(dpFecha, 1, 2);
        grid.add(lblInsumos, 0, 3, 2, 1);
        grid.add(recetaPreview, 0, 4, 2, 1);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(btnSave);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String error = null;
            if (cbProducto.getValue() == null) error = "Seleccione un producto";
            else if (tfCantidad.getText().trim().isEmpty()) error = "Ingrese la cantidad a fabricar";
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
                    Produccion produccion = new Produccion(cbProducto.getValue().getId(), cantidad, dpFecha.getValue());
                    produccionDAO.save(produccion);
                    return produccion;
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

        dialog.showAndWait().ifPresent(p -> refresh());
    }

    @Override
    public void refresh() {
        historialData.setAll(produccionDAO.findAll());
    }
}
