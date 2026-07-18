package com.casadelosol.inventario.ui;

import com.casadelosol.inventario.dao.MateriaPrimaDAO;
import com.casadelosol.inventario.dao.ProductoTerminadoDAO;
import com.casadelosol.inventario.dao.RecetaDAO;
import com.casadelosol.inventario.model.MateriaPrima;
import com.casadelosol.inventario.model.ProductoTerminado;
import com.casadelosol.inventario.model.Receta;
import com.casadelosol.inventario.model.RecetaDetalle;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class RecetaView implements Refreshable {

    private final TableView<RecetaResumen> table = new TableView<>();
    private final ObservableList<RecetaResumen> data = FXCollections.observableArrayList();

    private final RecetaDAO recetaDAO = new RecetaDAO();
    private final ProductoTerminadoDAO ptDAO = new ProductoTerminadoDAO();
    private final MateriaPrimaDAO mpDAO = new MateriaPrimaDAO();

    public Node getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Recetas");
        title.getStyleClass().add("section-title");

        Button btnGestionar = new Button("Administrar Recetas");
        btnGestionar.getStyleClass().add("btn-primary");
        btnGestionar.setOnAction(e -> showRecetaDialog(null));

        TableColumn<RecetaResumen, String> colProducto = new TableColumn<>("Producto");
        colProducto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().producto()));
        colProducto.setPrefWidth(250);

        TableColumn<RecetaResumen, Number> colIngredientes = new TableColumn<>("Ingredientes");
        colIngredientes.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().ingredientes()));
        colIngredientes.setPrefWidth(100);

        TableColumn<RecetaResumen, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().estado()));
        colEstado.setPrefWidth(120);

        table.getColumns().addAll(colProducto, colIngredientes, colEstado);
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setRowFactory(tv -> {
            TableRow<RecetaResumen> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    showRecetaDialog(row.getItem().recetaId());
                }
            });
            return row;
        });

        root.getChildren().addAll(title, btnGestionar, table);

        refresh();
        return root;
    }

    private void showRecetaDialog(Integer recetaId) {
        Receta receta = recetaId != null ? recetaDAO.findById(recetaId) : null;

        Dialog<Receta> dialog = new Dialog<>();
        dialog.setTitle(receta != null ? "Editar Receta" : "Nueva Receta");
        dialog.setHeaderText(receta != null ? "Editar receta existente" : "Crear nueva receta");

        ButtonType btnSave = new ButtonType("Guardar Receta", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        ComboBox<ProductoTerminado> cbProducto = new ComboBox<>();
        cbProducto.getItems().addAll(ptDAO.findAll());
        cbProducto.setPrefWidth(350);
        cbProducto.setPromptText("Seleccionar producto...");
        if (receta != null) {
            ProductoTerminado pt = ptDAO.findById(receta.getProductoTerminadoId());
            if (pt != null) cbProducto.setValue(pt);
            cbProducto.setDisable(true);
        }
        cbProducto.setOnAction(e -> {
            if (cbProducto.getValue() != null && receta == null) {
                Receta existente = recetaDAO.findByProducto(cbProducto.getValue().getId());
                if (existente != null) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                            "Ya existe una receta para este producto. ¿Desea editarla?",
                            ButtonType.YES, ButtonType.NO);
                    alert.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.YES) {
                            dialog.close();
                            showRecetaDialog(existente.getId());
                        }
                    });
                }
            }
        });

        TableView<RecetaDetalle> tblIngredientes = new TableView<>();
        ObservableList<RecetaDetalle> detalles = FXCollections.observableArrayList();
        if (receta != null) detalles.setAll(receta.getDetalles());

        TableColumn<RecetaDetalle, String> colMP = new TableColumn<>("Materia Prima");
        colMP.setCellValueFactory(d -> {
            MateriaPrima mp = mpDAO.findById(d.getValue().getMateriaPrimaId());
            return new SimpleStringProperty(mp != null ? mp.getNombre() : "");
        });
        colMP.setPrefWidth(200);

        TableColumn<RecetaDetalle, Number> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getCantidad()));
        colCantidad.setPrefWidth(100);

        TableColumn<RecetaDetalle, String> colUnidad = new TableColumn<>("Unidad");
        colUnidad.setCellValueFactory(d -> {
            MateriaPrima mp = mpDAO.findById(d.getValue().getMateriaPrimaId());
            return new SimpleStringProperty(mp != null ? mp.getUnidadMedida() : "");
        });
        colUnidad.setPrefWidth(80);

        tblIngredientes.getColumns().addAll(colMP, colCantidad, colUnidad);
        tblIngredientes.setItems(detalles);
        tblIngredientes.setPrefHeight(200);
        tblIngredientes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        Button btnAdd = new Button("Agregar Insumo");
        btnAdd.getStyleClass().add("btn-primary");
        Button btnRemove = new Button("Quitar");
        btnRemove.getStyleClass().add("btn-danger");

        btnRemove.setOnAction(e -> {
            RecetaDetalle sel = tblIngredientes.getSelectionModel().getSelectedItem();
            if (sel != null) detalles.remove(sel);
        });

        btnAdd.setOnAction(e -> {
            Dialog<RecetaDetalle> dlg = new Dialog<>();
            dlg.setTitle("Agregar Insumo");
            dlg.setHeaderText("Agregar materia prima a la receta");

            ButtonType btnAgregar = new ButtonType("Agregar", ButtonBar.ButtonData.OK_DONE);
            dlg.getDialogPane().getButtonTypes().addAll(btnAgregar, ButtonType.CANCEL);

            ComboBox<MateriaPrima> cbMP = new ComboBox<>();
            cbMP.getItems().addAll(mpDAO.findAll());
            cbMP.setPrefWidth(300);
            cbMP.setPromptText("Seleccionar materia prima...");

            TextField tfCant = new TextField();
            tfCant.setPromptText("Cantidad requerida");

            GridPane g = new GridPane();
            g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(15));
            g.add(new Label("Materia Prima:"), 0, 0);
            g.add(cbMP, 1, 0);
            g.add(new Label("Cantidad:"), 0, 1);
            g.add(tfCant, 1, 1);
            dlg.getDialogPane().setContent(g);

            dlg.setResultConverter(btn -> {
                if (btn == btnAgregar) {
                    if (cbMP.getValue() == null || tfCant.getText().trim().isEmpty()) return null;
                    try {
                        return new RecetaDetalle(cbMP.getValue().getId(), Double.parseDouble(tfCant.getText().trim()));
                    } catch (NumberFormatException ex) { return null; }
                }
                return null;
            });

            dlg.showAndWait().ifPresent(det -> {
                boolean existe = detalles.stream().anyMatch(d -> d.getMateriaPrimaId() == det.getMateriaPrimaId());
                if (existe) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Esa materia prima ya está en la receta", ButtonType.OK);
                    alert.showAndWait();
                    return;
                }
                detalles.add(det);
            });
        });

        TextArea taNotas = new TextArea();
        taNotas.setPromptText("Notas de la receta (procedimiento, observaciones...)");
        taNotas.setPrefRowCount(3);
        if (receta != null) taNotas.setText(receta.getNotas() != null ? receta.getNotas() : "");

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.getChildren().addAll(
                new Label("Producto Terminado:"), cbProducto,
                new Label("Ingredientes:"),
                new HBox(10, btnAdd, btnRemove),
                tblIngredientes,
                new Label("Notas:"), taNotas
        );
        dialog.getDialogPane().setContent(content);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(btnSave);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String error = null;
            if (cbProducto.getValue() == null) error = "Seleccione un producto";
            else if (detalles.isEmpty()) error = "Agregue al menos un ingrediente";
            if (error != null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, error, ButtonType.OK);
                alert.showAndWait();
                event.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == btnSave) {
                try {
                    Receta r;
                    if (receta != null) {
                        r = receta;
                    } else {
                        r = new Receta(cbProducto.getValue().getId());
                    }
                    r.setNotas(taNotas.getText().trim());
                    r.setDetalles(detalles.stream()
                            .map(d -> new RecetaDetalle(d.getMateriaPrimaId(), d.getCantidad()))
                            .toList());
                    if (r.getId() > 0) recetaDAO.update(r);
                    else recetaDAO.save(r);
                    return r;
                } catch (RuntimeException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Error al guardar: " + e.getMessage(), ButtonType.OK);
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(r -> refresh());
    }

    @Override
    public void refresh() {
        List<Receta> recetas = recetaDAO.findAll();
        List<ProductoTerminado> productos = ptDAO.findAll();
        data.clear();
        for (ProductoTerminado pt : productos) {
            Receta r = recetas.stream()
                    .filter(rec -> rec.getProductoTerminadoId() == pt.getId())
                    .findFirst().orElse(null);
            int count = 0;
            if (r != null) {
                Receta full = recetaDAO.findById(r.getId());
                count = full != null ? full.getDetalles().size() : 0;
            }
            data.add(new RecetaResumen(
                    pt.getNombre(),
                    count,
                    r != null ? "Tiene receta" : "Sin receta",
                    r != null ? r.getId() : null
            ));
        }
    }

    private record RecetaResumen(String producto, int ingredientes, String estado, Integer recetaId) {}
}
