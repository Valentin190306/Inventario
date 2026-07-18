package com.casadelosol.inventario.ui;

import com.casadelosol.inventario.dao.CompraDAO;
import com.casadelosol.inventario.dao.MateriaPrimaDAO;
import com.casadelosol.inventario.model.Compra;
import com.casadelosol.inventario.model.MateriaPrima;
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

public class CompraView implements Refreshable {

    private final TableView<Compra> table = new TableView<>();
    private final ObservableList<Compra> data = FXCollections.observableArrayList();

    private final CompraDAO compraDAO = new CompraDAO();
    private final MateriaPrimaDAO mpDAO = new MateriaPrimaDAO();

    public Node getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Compras de Materia Prima");
        title.getStyleClass().add("section-title");

        Button btnNueva = new Button("+ Nueva Compra");
        btnNueva.getStyleClass().add("btn-success");
        btnNueva.setOnAction(e -> showNuevaCompraDialog());

        TableColumn<Compra, String> colMP = new TableColumn<>("Materia Prima");
        colMP.setCellValueFactory(d -> {
            MateriaPrima mp = mpDAO.findById(d.getValue().getMateriaPrimaId());
            return new SimpleStringProperty(mp != null ? mp.getNombre() : "");
        });
        colMP.setPrefWidth(200);

        TableColumn<Compra, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha().toString()));
        colFecha.setPrefWidth(100);

        TableColumn<Compra, Number> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getCantidad()));
        colCantidad.setPrefWidth(80);

        TableColumn<Compra, Number> colPrecio = new TableColumn<>("Precio Total");
        colPrecio.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPrecio()));
        colPrecio.setPrefWidth(100);

        TableColumn<Compra, Number> colPrecioUnit = new TableColumn<>("Precio Unit.");
        colPrecioUnit.setCellValueFactory(d -> {
            double unitario = d.getValue().getCantidad() > 0 ? d.getValue().getPrecio() / d.getValue().getCantidad() : 0;
            return new SimpleDoubleProperty(unitario);
        });
        colPrecioUnit.setPrefWidth(100);

        TableColumn<Compra, String> colLugar = new TableColumn<>("Lugar");
        colLugar.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLugar() != null ? d.getValue().getLugar() : ""));
        colLugar.setPrefWidth(150);

        table.getColumns().addAll(colMP, colFecha, colCantidad, colPrecio, colPrecioUnit, colLugar);
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        root.getChildren().addAll(title, btnNueva, table);

        refresh();
        return root;
    }

    private void showNuevaCompraDialog() {
        Dialog<Compra> dialog = new Dialog<>();
        dialog.setTitle("Nueva Compra");
        dialog.setHeaderText("Registrar compra de materia prima");

        ButtonType btnSave = new ButtonType("Registrar Compra", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        ComboBox<MateriaPrima> cbMP = new ComboBox<>();
        cbMP.getItems().setAll(mpDAO.findAll());
        cbMP.setPrefWidth(300);
        cbMP.setPromptText("Seleccionar materia prima...");

        DatePicker dpFecha = new DatePicker(LocalDate.now());
        dpFecha.setPrefWidth(150);

        TextField tfCantidad = new TextField();
        tfCantidad.setPromptText("Ej: 500");

        TextField tfPrecio = new TextField();
        tfPrecio.setPromptText("Ej: 2500.00");

        TextField tfLugar = new TextField();
        tfLugar.setPromptText("Ej: Mercado Central (opcional)");

        Label lblPrecioUnitario = new Label();

        tfCantidad.textProperty().addListener((obs, o, n) -> {
            try {
                double cant = Double.parseDouble(n.trim());
                double prec = Double.parseDouble(tfPrecio.getText().trim());
                if (cant > 0)
                    lblPrecioUnitario.setText("Precio unitario: $" + String.format("%.2f", prec / cant));
            } catch (NumberFormatException e) {
                lblPrecioUnitario.setText("");
            }
        });
        tfPrecio.textProperty().addListener((obs, o, n) -> {
            try {
                double cant = Double.parseDouble(tfCantidad.getText().trim());
                double prec = Double.parseDouble(n.trim());
                if (cant > 0)
                    lblPrecioUnitario.setText("Precio unitario: $" + String.format("%.2f", prec / cant));
            } catch (NumberFormatException e) {
                lblPrecioUnitario.setText("");
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.add(new Label("Materia Prima*:"), 0, 0);
        grid.add(cbMP, 1, 0);
        grid.add(new Label("Fecha*:"), 0, 1);
        grid.add(dpFecha, 1, 1);
        grid.add(new Label("Cantidad*:"), 0, 2);
        grid.add(tfCantidad, 1, 2);
        grid.add(new Label("Precio total*:"), 0, 3);
        grid.add(tfPrecio, 1, 3);
        grid.add(new Label("Lugar:"), 0, 4);
        grid.add(tfLugar, 1, 4);
        grid.add(lblPrecioUnitario, 1, 5);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(btnSave);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String error = null;
            if (cbMP.getValue() == null) error = "Seleccione una materia prima";
            else if (tfCantidad.getText().trim().isEmpty() || tfPrecio.getText().trim().isEmpty())
                error = "Complete cantidad y precio";
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
                    double precio = Double.parseDouble(tfPrecio.getText().trim());
                    Compra compra = new Compra(cbMP.getValue().getId(), dpFecha.getValue(), cantidad, precio);
                    String lugar = tfLugar.getText().trim();
                    if (!lugar.isEmpty()) compra.setLugar(lugar);
                    compraDAO.save(compra);
                    return compra;
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

        dialog.showAndWait().ifPresent(compra -> refresh());
    }

    @Override
    public void refresh() {
        data.setAll(compraDAO.findAll());
    }
}
