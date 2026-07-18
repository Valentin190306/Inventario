package com.casadelosol.inventario.ui;

import com.casadelosol.inventario.dao.ProduccionDAO;
import com.casadelosol.inventario.dao.ProductoTerminadoDAO;
import com.casadelosol.inventario.dao.RecetaDAO;
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

    private final ComboBox<ProductoTerminado> cbProducto = new ComboBox<>();
    private final TextField tfCantidad = new TextField();
    private final DatePicker dpFecha = new DatePicker(LocalDate.now());
    private final TableView<RecetaDetalle> recetaPreview = new TableView<>();
    private final ObservableList<RecetaDetalle> recetaData = FXCollections.observableArrayList();
    private final TableView<Produccion> historialTable = new TableView<>();
    private final ObservableList<Produccion> historialData = FXCollections.observableArrayList();
    private final Button btnProducir = new Button("Registrar Producción");

    private final ProduccionDAO produccionDAO = new ProduccionDAO();
    private final ProductoTerminadoDAO ptDAO = new ProductoTerminadoDAO();
    private final RecetaDAO recetaDAO = new RecetaDAO();

    public Node getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Registrar Producción");
        title.getStyleClass().add("section-title");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(15));
        form.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        cbProducto.setPrefWidth(350);
        cbProducto.setPromptText("Seleccionar producto...");
        cbProducto.setOnAction(e -> actualizarVistaPrevia());

        tfCantidad.setPromptText("Ej: 10");

        btnProducir.getStyleClass().add("btn-success");
        btnProducir.setOnAction(e -> registrarProduccion());

        form.add(new Label("Producto Terminado*:"), 0, 0);
        form.add(cbProducto, 1, 0);
        form.add(new Label("Cantidad a fabricar*:"), 0, 1);
        form.add(tfCantidad, 1, 1);
        form.add(new Label("Fecha*:"), 0, 2);
        form.add(dpFecha, 1, 2);
        form.add(btnProducir, 1, 3);

        Label previewTitle = new Label("Insumos requeridos (según receta)");
        previewTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");

        TableColumn<RecetaDetalle, String> colMP = new TableColumn<>("Materia Prima");
        colMP.setCellValueFactory(d -> {
            var mp = new com.casadelosol.inventario.dao.MateriaPrimaDAO().findById(d.getValue().getMateriaPrimaId());
            return new SimpleStringProperty(mp != null ? mp.getNombre() : "");
        });
        colMP.setPrefWidth(200);

        TableColumn<RecetaDetalle, Number> colCantReq = new TableColumn<>("Cant. Requerida");
        colCantReq.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getCantidad()));
        colCantReq.setPrefWidth(120);

        TableColumn<RecetaDetalle, String> colUnidad = new TableColumn<>("Unidad");
        colUnidad.setCellValueFactory(d -> {
            var mp = new com.casadelosol.inventario.dao.MateriaPrimaDAO().findById(d.getValue().getMateriaPrimaId());
            return new SimpleStringProperty(mp != null ? mp.getUnidadMedida() : "");
        });
        colUnidad.setPrefWidth(80);

        recetaPreview.getColumns().addAll(colMP, colCantReq, colUnidad);
        recetaPreview.setItems(recetaData);
        recetaPreview.setPrefHeight(150);

        Label historyTitle = new Label("Historial de Producción");
        historyTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 15 0 5 0;");

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

        root.getChildren().addAll(title, form, previewTitle, recetaPreview, historyTitle, historialTable);

        refresh();
        return root;
    }

    private void actualizarVistaPrevia() {
        ProductoTerminado pt = cbProducto.getValue();
        if (pt == null) {
            recetaData.clear();
            return;
        }
        Receta receta = recetaDAO.findByProducto(pt.getId());
        if (receta != null) {
            recetaData.setAll(receta.getDetalles());
        } else {
            recetaData.clear();
        }
    }

    private void registrarProduccion() {
        ProductoTerminado pt = cbProducto.getValue();
        if (pt == null) {
            showAlert("Seleccione un producto");
            return;
        }
        if (tfCantidad.getText().trim().isEmpty()) {
            showAlert("Ingrese la cantidad a fabricar");
            return;
        }

        try {
            double cantidad = Double.parseDouble(tfCantidad.getText().trim());
            if (cantidad <= 0) {
                showAlert("La cantidad debe ser mayor a cero");
                return;
            }

            Produccion produccion = new Produccion(pt.getId(), cantidad, dpFecha.getValue());
            produccionDAO.save(produccion);

            tfCantidad.clear();
            dpFecha.setValue(LocalDate.now());

            showInfo("Producción registrada correctamente");
            refresh();
            actualizarVistaPrevia();
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
        historialData.setAll(produccionDAO.findAll());
        recetaData.clear();
    }
}
