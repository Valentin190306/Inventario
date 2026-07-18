package com.casadelosol.inventario.ui;

import com.casadelosol.inventario.dao.CategoriaMPDAO;
import com.casadelosol.inventario.dao.CompraDAO;
import com.casadelosol.inventario.dao.MateriaPrimaDAO;
import com.casadelosol.inventario.model.CategoriaMP;
import com.casadelosol.inventario.model.MateriaPrima;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MateriaPrimaView implements Refreshable {

    private final TableView<MateriaPrima> table = new TableView<>();
    private final ObservableList<MateriaPrima> data = FXCollections.observableArrayList();
    private final TextField searchField = new TextField();
    private final ComboBox<CategoriaMP> filterCategoria = new ComboBox<>();

    private final MateriaPrimaDAO dao = new MateriaPrimaDAO();
    private final CategoriaMPDAO catDao = new CategoriaMPDAO();
    private final CompraDAO compraDAO = new CompraDAO();

    public Node getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Materias Primas");
        title.getStyleClass().add("section-title");

        HBox toolbar = new HBox(10);
        searchField.setPromptText("Buscar por nombre...");
        searchField.getStyleClass().add("filter-field");
        searchField.textProperty().addListener((obs, o, n) -> filtrar());

        filterCategoria.setPromptText("Todas las categorías");
        filterCategoria.getItems().addAll(catDao.findAll());
        filterCategoria.setOnAction(e -> filtrar());

        Button btnClear = new Button("Limpiar filtros");
        btnClear.setOnAction(e -> {
            searchField.clear();
            filterCategoria.getSelectionModel().clearSelection();
            refresh();
        });

        toolbar.getChildren().addAll(searchField, filterCategoria, btnClear);

        HBox buttons = new HBox(10);
        Button btnAdd = new Button("Agregar");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> showAddDialog());

        Button btnEdit = new Button("Editar");
        btnEdit.getStyleClass().add("btn-warning");
        btnEdit.setOnAction(e -> showEditDialog());

        Button btnDelete = new Button("Eliminar");
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.setOnAction(e -> showDeleteDialog());

        buttons.getChildren().addAll(btnAdd, btnEdit, btnDelete);

        TableColumn<MateriaPrima, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colNombre.setPrefWidth(200);

        TableColumn<MateriaPrima, String> colUnidad = new TableColumn<>("Unidad");
        colUnidad.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidadMedida()));
        colUnidad.setPrefWidth(100);

        TableColumn<MateriaPrima, Number> colStock = new TableColumn<>("Stock Actual");
        colStock.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getStockActual()));
        colStock.setPrefWidth(100);

        TableColumn<MateriaPrima, String> colUltPrecio = new TableColumn<>("Último Precio");
        colUltPrecio.setCellValueFactory(d -> {
            Double precio = compraDAO.getUltimoPrecioUnitario(d.getValue().getId());
            if (precio != null) {
                return new SimpleStringProperty("$" + String.format("%.2f", precio) + "/" + d.getValue().getUnidadMedida());
            }
            return new SimpleStringProperty("-");
        });
        colUltPrecio.setPrefWidth(130);

        TableColumn<MateriaPrima, String> colCategoria = new TableColumn<>("Categoría");
        colCategoria.setCellValueFactory(d -> {
            CategoriaMP cat = catDao.findById(d.getValue().getCategoriaId());
            return new SimpleStringProperty(cat != null ? cat.getNombre() : "");
        });
        colCategoria.setPrefWidth(150);

        table.getColumns().addAll(colNombre, colUnidad, colStock, colUltPrecio, colCategoria);
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        root.getChildren().addAll(title, toolbar, buttons, table);

        refresh();
        return root;
    }

    private void filtrar() {
        String texto = searchField.getText().trim().toLowerCase();
        CategoriaMP cat = filterCategoria.getValue();

        data.setAll(dao.findAll().stream()
                .filter(m -> texto.isEmpty() || m.getNombre().toLowerCase().contains(texto))
                .filter(m -> cat == null || m.getCategoriaId() == cat.getId())
                .toList());
    }

    private void showAddDialog() {
        Dialog<MateriaPrima> dialog = new Dialog<>();
        dialog.setTitle("Nueva Materia Prima");
        dialog.setHeaderText("Registrar materia prima");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        TextField tfNombre = new TextField();
        tfNombre.setPromptText("Ej: Manteca de Karité");

        TextField tfUnidad = new TextField();
        tfUnidad.setPromptText("Ej: gramos, ml, unidades");

        ComboBox<CategoriaMP> cbCategoria = new ComboBox<>();
        cbCategoria.getItems().addAll(catDao.findAll());
        cbCategoria.setPromptText("Seleccionar categoría");

        grid.add(new Label("Nombre*:"), 0, 0);
        grid.add(tfNombre, 1, 0);
        grid.add(new Label("Unidad de medida*:"), 0, 1);
        grid.add(tfUnidad, 1, 1);
        grid.add(new Label("Categoría*:"), 0, 2);
        grid.add(cbCategoria, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                if (tfNombre.getText().trim().isEmpty() || tfUnidad.getText().trim().isEmpty() || cbCategoria.getValue() == null) {
                    return null;
                }
                MateriaPrima mp = new MateriaPrima(tfNombre.getText().trim(), tfUnidad.getText().trim(), cbCategoria.getValue().getId());
                return mp;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(mp -> {
            dao.save(mp);
            refresh();
        });
    }

    private void showEditDialog() {
        MateriaPrima selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Seleccione una materia prima para editar");
            return;
        }

        Dialog<MateriaPrima> dialog = new Dialog<>();
        dialog.setTitle("Editar Materia Prima");
        dialog.setHeaderText("Modificar: " + selected.getNombre());

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        TextField tfNombre = new TextField(selected.getNombre());
        TextField tfUnidad = new TextField(selected.getUnidadMedida());

        ComboBox<CategoriaMP> cbCategoria = new ComboBox<>();
        cbCategoria.getItems().addAll(catDao.findAll());
        CategoriaMP catActual = catDao.findById(selected.getCategoriaId());
        if (catActual != null) {
            cbCategoria.setValue(catActual);
        }

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(tfNombre, 1, 0);
        grid.add(new Label("Unidad de medida:"), 0, 1);
        grid.add(tfUnidad, 1, 1);
        grid.add(new Label("Categoría:"), 0, 2);
        grid.add(cbCategoria, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                selected.setNombre(tfNombre.getText().trim());
                selected.setUnidadMedida(tfUnidad.getText().trim());
                if (cbCategoria.getValue() != null) {
                    selected.setCategoriaId(cbCategoria.getValue().getId());
                }
                return selected;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(mp -> {
            if (!mp.getNombre().isEmpty()) {
                dao.update(mp);
                refresh();
            }
        });
    }

    private void showDeleteDialog() {
        MateriaPrima selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Seleccione una materia prima para eliminar");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar: " + selected.getNombre() + "?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                dao.delete(selected.getId());
                refresh();
            }
        });
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.showAndWait();
    }

    @Override
    public void refresh() {
        filterCategoria.getItems().setAll(catDao.findAll());
        data.setAll(dao.findAll());
    }
}
