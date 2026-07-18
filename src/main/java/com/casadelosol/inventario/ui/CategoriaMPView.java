package com.casadelosol.inventario.ui;

import com.casadelosol.inventario.dao.CategoriaMPDAO;
import com.casadelosol.inventario.model.CategoriaMP;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class CategoriaMPView implements Refreshable {

    private final TreeView<CategoriaMP> treeView = new TreeView<>();
    private final CategoriaMPDAO dao = new CategoriaMPDAO();
    private final ListView<CategoriaMP> parentList = new ListView<>();

    private TreeItem<CategoriaMP> rootItem;

    public Node getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Categorías - Materias Primas");
        title.getStyleClass().add("section-title");

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

        treeView.setShowRoot(false);
        treeView.setPrefHeight(500);
        treeView.setCellFactory(tv -> new TextFieldTreeCell<>(new CategoriaMPStringConverter()));

        root.getChildren().addAll(title, buttons, treeView);

        refresh();
        return root;
    }

    private void showAddDialog() {
        Dialog<CategoriaMP> dialog = new Dialog<>();
        dialog.setTitle("Nueva Categoría");
        dialog.setHeaderText("Agregar categoría de materia prima");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        TextField tfNombre = new TextField();
        tfNombre.setPromptText("Nombre de la categoría");

        ComboBox<CategoriaMP> cbParent = new ComboBox<>();
        cbParent.getItems().addAll(dao.findAll());
        cbParent.setPromptText("Categoría padre (opcional)");

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Nombre:"), tfNombre,
                new Label("Categoría padre:"), cbParent
        );
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                CategoriaMP cat = new CategoriaMP(tfNombre.getText().trim());
                if (cbParent.getValue() != null) {
                    cat.setCategoriaPadreId(cbParent.getValue().getId());
                }
                return cat;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(cat -> {
            if (!cat.getNombre().isEmpty()) {
                dao.save(cat);
                refresh();
            }
        });
    }

    private void showEditDialog() {
        TreeItem<CategoriaMP> selected = treeView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Seleccione una categoría para editar");
            return;
        }

        CategoriaMP cat = selected.getValue();
        Dialog<CategoriaMP> dialog = new Dialog<>();
        dialog.setTitle("Editar Categoría");
        dialog.setHeaderText("Modificar categoría: " + cat.getNombre());

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        TextField tfNombre = new TextField(cat.getNombre());

        ComboBox<CategoriaMP> cbParent = new ComboBox<>();
        ObservableList<CategoriaMP> cats = FXCollections.observableArrayList(dao.findAll());
        cats.remove(cat);
        cbParent.setItems(cats);
        if (cat.getCategoriaPadreId() != null) {
            cbParent.getSelectionModel().select(dao.findById(cat.getCategoriaPadreId()));
        }

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Nombre:"), tfNombre,
                new Label("Categoría padre:"), cbParent
        );
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                cat.setNombre(tfNombre.getText().trim());
                cat.setCategoriaPadreId(cbParent.getValue() != null ? cbParent.getValue().getId() : null);
                return cat;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            if (!c.getNombre().isEmpty()) {
                dao.update(c);
                refresh();
            }
        });
    }

    private void showDeleteDialog() {
        TreeItem<CategoriaMP> selected = treeView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Seleccione una categoría para eliminar");
            return;
        }

        CategoriaMP cat = selected.getValue();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar categoría: " + cat.getNombre() + "?");
        confirm.setContentText("Las subcategorías quedarán sin padre.");

        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                dao.delete(cat.getId());
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
        rootItem = new TreeItem<>(new CategoriaMP("Raíz"));
        rootItem.setExpanded(true);

        Map<Integer, TreeItem<CategoriaMP>> itemMap = new HashMap<>();
        for (CategoriaMP cat : dao.findAll()) {
            itemMap.put(cat.getId(), new TreeItem<>(cat));
        }

        for (CategoriaMP cat : dao.findAll()) {
            TreeItem<CategoriaMP> item = itemMap.get(cat.getId());
            if (cat.getCategoriaPadreId() != null && itemMap.containsKey(cat.getCategoriaPadreId())) {
                itemMap.get(cat.getCategoriaPadreId()).getChildren().add(item);
            } else {
                rootItem.getChildren().add(item);
            }
        }

        treeView.setRoot(rootItem);
    }

    private static class CategoriaMPStringConverter extends javafx.util.StringConverter<CategoriaMP> {
        @Override
        public String toString(CategoriaMP object) {
            return object != null ? object.getNombre() : "";
        }

        @Override
        public CategoriaMP fromString(String string) {
            return new CategoriaMP(string);
        }
    }
}
