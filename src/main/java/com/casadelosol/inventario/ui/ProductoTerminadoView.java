package com.casadelosol.inventario.ui;

import com.casadelosol.inventario.dao.CategoriaPTDAO;
import com.casadelosol.inventario.dao.ProductoTerminadoDAO;
import com.casadelosol.inventario.model.CategoriaPT;
import com.casadelosol.inventario.model.ProductoTerminado;
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

public class ProductoTerminadoView implements Refreshable {

    private final TableView<ProductoTerminado> table = new TableView<>();
    private final ObservableList<ProductoTerminado> data = FXCollections.observableArrayList();
    private final TextField searchField = new TextField();
    private final ComboBox<CategoriaPT> filterCategoria = new ComboBox<>();

    private final ProductoTerminadoDAO dao = new ProductoTerminadoDAO();
    private final CategoriaPTDAO catDao = new CategoriaPTDAO();

    public Node getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Productos Terminados");
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

        TableColumn<ProductoTerminado, String> colNombre = new TableColumn<>("Nombre / Variante");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colNombre.setPrefWidth(250);

        TableColumn<ProductoTerminado, Number> colPrecio = new TableColumn<>("Precio Venta ($)");
        colPrecio.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPrecioVenta()));
        colPrecio.setPrefWidth(120);

        TableColumn<ProductoTerminado, Number> colStock = new TableColumn<>("Stock Actual");
        colStock.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getStockActual()));
        colStock.setPrefWidth(100);

        TableColumn<ProductoTerminado, String> colCategoria = new TableColumn<>("Categoría");
        colCategoria.setCellValueFactory(d -> {
            CategoriaPT cat = catDao.findById(d.getValue().getCategoriaId());
            return new SimpleStringProperty(cat != null ? cat.getNombre() : "");
        });
        colCategoria.setPrefWidth(150);

        table.getColumns().addAll(colNombre, colPrecio, colStock, colCategoria);
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        root.getChildren().addAll(title, toolbar, buttons, table);

        refresh();
        return root;
    }

    private void filtrar() {
        String texto = searchField.getText().trim().toLowerCase();
        CategoriaPT cat = filterCategoria.getValue();

        data.setAll(dao.findAll().stream()
                .filter(p -> texto.isEmpty() || p.getNombre().toLowerCase().contains(texto))
                .filter(p -> cat == null || p.getCategoriaId() == cat.getId())
                .toList());
    }

    private void showAddDialog() {
        Dialog<ProductoTerminado> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Producto Terminado");
        dialog.setHeaderText("Registrar producto terminado");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        TextField tfNombre = new TextField();
        tfNombre.setPromptText("Ej: Shampoo en barra - cabello graso");

        TextField tfPrecio = new TextField();
        tfPrecio.setPromptText("Ej: 250.00");

        ComboBox<CategoriaPT> cbCategoria = new ComboBox<>();
        cbCategoria.getItems().addAll(catDao.findAll());
        cbCategoria.setPromptText("Seleccionar categoría");

        grid.add(new Label("Nombre*:"), 0, 0);
        grid.add(tfNombre, 1, 0);
        grid.add(new Label("Precio de venta*:"), 0, 1);
        grid.add(tfPrecio, 1, 1);
        grid.add(new Label("Categoría*:"), 0, 2);
        grid.add(cbCategoria, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                if (tfNombre.getText().trim().isEmpty() || tfPrecio.getText().trim().isEmpty() || cbCategoria.getValue() == null) {
                    return null;
                }
                try {
                    double precio = Double.parseDouble(tfPrecio.getText().trim());
                    ProductoTerminado pt = new ProductoTerminado(tfNombre.getText().trim(), precio, cbCategoria.getValue().getId());
                    return pt;
                } catch (NumberFormatException e) {
                    showAlert("El precio debe ser un número válido");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(pt -> {
            dao.save(pt);
            refresh();
        });
    }

    private void showEditDialog() {
        ProductoTerminado selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Seleccione un producto para editar");
            return;
        }

        Dialog<ProductoTerminado> dialog = new Dialog<>();
        dialog.setTitle("Editar Producto Terminado");
        dialog.setHeaderText("Modificar: " + selected.getNombre());

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        TextField tfNombre = new TextField(selected.getNombre());
        TextField tfPrecio = new TextField(String.valueOf(selected.getPrecioVenta()));

        ComboBox<CategoriaPT> cbCategoria = new ComboBox<>();
        cbCategoria.getItems().addAll(catDao.findAll());
        CategoriaPT catActual = catDao.findById(selected.getCategoriaId());
        if (catActual != null) {
            cbCategoria.setValue(catActual);
        }

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(tfNombre, 1, 0);
        grid.add(new Label("Precio de venta:"), 0, 1);
        grid.add(tfPrecio, 1, 1);
        grid.add(new Label("Categoría:"), 0, 2);
        grid.add(cbCategoria, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                selected.setNombre(tfNombre.getText().trim());
                try {
                    selected.setPrecioVenta(Double.parseDouble(tfPrecio.getText().trim()));
                } catch (NumberFormatException e) {
                    return null;
                }
                if (cbCategoria.getValue() != null) {
                    selected.setCategoriaId(cbCategoria.getValue().getId());
                }
                return selected;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(pt -> {
            if (!pt.getNombre().isEmpty()) {
                dao.update(pt);
                refresh();
            }
        });
    }

    private void showDeleteDialog() {
        ProductoTerminado selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Seleccione un producto para eliminar");
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
