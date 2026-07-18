package com.casadelosol.inventario.ui;

import com.casadelosol.inventario.dao.MateriaPrimaDAO;
import com.casadelosol.inventario.dao.ProductoTerminadoDAO;
import com.casadelosol.inventario.dao.RecetaDAO;
import com.casadelosol.inventario.model.MateriaPrima;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class RecetaView implements Refreshable {

    private final ComboBox<ProductoTerminado> cbProducto = new ComboBox<>();
    private final TextArea taNotas = new TextArea();
    private final TableView<RecetaDetalle> table = new TableView<>();
    private final ObservableList<RecetaDetalle> detalles = FXCollections.observableArrayList();
    private final Label lblStatus = new Label();

    private final RecetaDAO recetaDAO = new RecetaDAO();
    private final ProductoTerminadoDAO ptDAO = new ProductoTerminadoDAO();
    private final MateriaPrimaDAO mpDAO = new MateriaPrimaDAO();

    private Receta recetaActual;

    public Node getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Recetas");
        title.getStyleClass().add("section-title");

        GridPane selectorPanel = new GridPane();
        selectorPanel.setHgap(10);
        selectorPanel.setVgap(10);
        selectorPanel.setPadding(new Insets(15));
        selectorPanel.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        cbProducto.setPrefWidth(350);
        cbProducto.setPromptText("Seleccionar producto...");
        cbProducto.setOnAction(e -> cargarReceta());

        taNotas.setPromptText("Notas de la receta (procedimiento, observaciones...)");
        taNotas.setPrefRowCount(3);

        Button btnGuardar = new Button("Guardar Receta");
        btnGuardar.getStyleClass().add("btn-success");
        btnGuardar.setOnAction(e -> guardarReceta());

        selectorPanel.add(new Label("Producto Terminado:"), 0, 0);
        selectorPanel.add(cbProducto, 1, 0);
        selectorPanel.add(lblStatus, 1, 1);

        Label ingredientsTitle = new Label("Ingredientes (Materias Primas)");
        ingredientsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");

        HBox ingredientButtons = new HBox(10);
        Button btnAddIngredient = new Button("Agregar Insumo");
        btnAddIngredient.getStyleClass().add("btn-primary");
        btnAddIngredient.setOnAction(e -> showAddIngredientDialog());

        Button btnRemoveIngredient = new Button("Quitar");
        btnRemoveIngredient.getStyleClass().add("btn-danger");
        btnRemoveIngredient.setOnAction(e -> {
            RecetaDetalle selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                detalles.remove(selected);
            }
        });

        ingredientButtons.getChildren().addAll(btnAddIngredient, btnRemoveIngredient);

        TableColumn<RecetaDetalle, String> colMP = new TableColumn<>("Materia Prima");
        colMP.setCellValueFactory(d -> {
            MateriaPrima mp = mpDAO.findById(d.getValue().getMateriaPrimaId());
            return new SimpleStringProperty(mp != null ? mp.getNombre() : "");
        });
        colMP.setPrefWidth(250);

        TableColumn<RecetaDetalle, Number> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getCantidad()));
        colCantidad.setPrefWidth(100);

        TableColumn<RecetaDetalle, String> colUnidad = new TableColumn<>("Unidad");
        colUnidad.setCellValueFactory(d -> {
            MateriaPrima mp = mpDAO.findById(d.getValue().getMateriaPrimaId());
            return new SimpleStringProperty(mp != null ? mp.getUnidadMedida() : "");
        });
        colUnidad.setPrefWidth(80);

        table.getColumns().addAll(colMP, colCantidad, colUnidad);
        table.setItems(detalles);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(250);

        HBox notasPanel = new HBox(10);
        notasPanel.getChildren().addAll(new Label("Notas:"), taNotas);

        root.getChildren().addAll(title, selectorPanel, ingredientsTitle, ingredientButtons, table, notasPanel, btnGuardar);

        refresh();
        return root;
    }

    private void cargarReceta() {
        ProductoTerminado pt = cbProducto.getValue();
        if (pt == null) {
            detalles.clear();
            taNotas.clear();
            lblStatus.setText("");
            return;
        }

        recetaActual = recetaDAO.findByProducto(pt.getId());
        if (recetaActual != null) {
            detalles.setAll(recetaActual.getDetalles());
            taNotas.setText(recetaActual.getNotas() != null ? recetaActual.getNotas() : "");
            lblStatus.setText("✔ Receta cargada - " + recetaActual.getId());
            lblStatus.setStyle("-fx-text-fill: #27ae60;");
        } else {
            detalles.clear();
            taNotas.clear();
            recetaActual = new Receta(pt.getId());
            lblStatus.setText("Nueva receta - sin ingredientes aún");
            lblStatus.setStyle("-fx-text-fill: #7f8c8d;");
        }
    }

    private void showAddIngredientDialog() {
        if (cbProducto.getValue() == null) {
            showAlert("Seleccione un producto primero");
            return;
        }

        Dialog<RecetaDetalle> dialog = new Dialog<>();
        dialog.setTitle("Agregar Insumo");
        dialog.setHeaderText("Agregar materia prima a la receta");

        ButtonType btnAgregar = new ButtonType("Agregar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAgregar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        ComboBox<MateriaPrima> cbMP = new ComboBox<>();
        cbMP.getItems().addAll(mpDAO.findAll());
        cbMP.setPrefWidth(300);
        cbMP.setPromptText("Seleccionar materia prima...");

        TextField tfCantidad = new TextField();
        tfCantidad.setPromptText("Cantidad requerida");

        grid.add(new Label("Materia Prima:"), 0, 0);
        grid.add(cbMP, 1, 0);
        grid.add(new Label("Cantidad:"), 0, 1);
        grid.add(tfCantidad, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnAgregar) {
                if (cbMP.getValue() == null || tfCantidad.getText().trim().isEmpty()) {
                    return null;
                }
                try {
                    double cantidad = Double.parseDouble(tfCantidad.getText().trim());
                    return new RecetaDetalle(cbMP.getValue().getId(), cantidad);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(detalle -> {
            boolean existe = detalles.stream()
                    .anyMatch(d -> d.getMateriaPrimaId() == detalle.getMateriaPrimaId());
            if (existe) {
                showAlert("Esa materia prima ya está en la receta");
                return;
            }
            detalles.add(detalle);
        });
    }

    private void guardarReceta() {
        if (cbProducto.getValue() == null) {
            showAlert("Seleccione un producto");
            return;
        }
        if (detalles.isEmpty()) {
            showAlert("Agregue al menos un ingrediente");
            return;
        }

        recetaActual.setNotas(taNotas.getText().trim());
        recetaActual.setDetalles(detalles.stream()
                .map(d -> new RecetaDetalle(d.getMateriaPrimaId(), d.getCantidad()))
                .toList());

        try {
            if (recetaActual.getId() > 0) {
                recetaDAO.update(recetaActual);
            } else {
                recetaDAO.save(recetaActual);
            }
            cargarReceta();
            showInfo("Receta guardada correctamente");
        } catch (Exception e) {
            showAlert("Error al guardar: " + e.getMessage());
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
        detalles.clear();
        taNotas.clear();
        recetaActual = null;
        lblStatus.setText("");
    }
}
