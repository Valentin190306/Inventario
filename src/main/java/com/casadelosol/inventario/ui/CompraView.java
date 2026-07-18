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

    private final ComboBox<MateriaPrima> cbMP = new ComboBox<>();
    private final DatePicker dpFecha = new DatePicker(LocalDate.now());
    private final TextField tfCantidad = new TextField();
    private final TextField tfPrecio = new TextField();
    private final TextField tfLugar = new TextField();
    private final TableView<Compra> table = new TableView<>();
    private final ObservableList<Compra> data = FXCollections.observableArrayList();
    private final Label lblPrecioUnitario = new Label();

    private final CompraDAO compraDAO = new CompraDAO();
    private final MateriaPrimaDAO mpDAO = new MateriaPrimaDAO();

    public Node getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Registrar Compra de Materia Prima");
        title.getStyleClass().add("section-title");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(15));
        form.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        cbMP.setPrefWidth(300);
        cbMP.setPromptText("Seleccionar materia prima...");

        dpFecha.setPrefWidth(150);

        tfCantidad.setPromptText("Ej: 500");
        tfPrecio.setPromptText("Ej: 2500.00");
        tfLugar.setPromptText("Ej: Mercado Central (opcional)");

        tfCantidad.textProperty().addListener((obs, o, n) -> actualizarPrecioUnitario());
        tfPrecio.textProperty().addListener((obs, o, n) -> actualizarPrecioUnitario());

        Button btnGuardar = new Button("Registrar Compra");
        btnGuardar.getStyleClass().add("btn-success");
        btnGuardar.setOnAction(e -> registrarCompra());

        form.add(new Label("Materia Prima*:"), 0, 0);
        form.add(cbMP, 1, 0);
        form.add(new Label("Fecha*:"), 0, 1);
        form.add(dpFecha, 1, 1);
        form.add(new Label("Cantidad*:"), 0, 2);
        form.add(tfCantidad, 1, 2);
        form.add(new Label("Precio total*:"), 0, 3);
        form.add(tfPrecio, 1, 3);
        form.add(new Label("Lugar:"), 0, 4);
        form.add(tfLugar, 1, 4);
        form.add(lblPrecioUnitario, 1, 5);
        form.add(btnGuardar, 1, 6);

        Label historyTitle = new Label("Historial de Compras");
        historyTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");

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

        cbMP.setOnAction(e -> {
            MateriaPrima selected = cbMP.getValue();
            if (selected != null) {
                data.setAll(compraDAO.findByMateriaPrima(selected.getId()));
            } else {
                data.clear();
            }
        });

        root.getChildren().addAll(title, form, historyTitle, table);

        refresh();
        return root;
    }

    private void actualizarPrecioUnitario() {
        try {
            double cantidad = Double.parseDouble(tfCantidad.getText().trim());
            double precio = Double.parseDouble(tfPrecio.getText().trim());
            if (cantidad > 0) {
                lblPrecioUnitario.setText("Precio unitario: $" + String.format("%.2f", precio / cantidad));
            }
        } catch (NumberFormatException e) {
            lblPrecioUnitario.setText("");
        }
    }

    private void registrarCompra() {
        MateriaPrima mp = cbMP.getValue();
        if (mp == null) {
            showAlert("Seleccione una materia prima");
            return;
        }
        if (tfCantidad.getText().trim().isEmpty() || tfPrecio.getText().trim().isEmpty()) {
            showAlert("Complete cantidad y precio");
            return;
        }

        try {
            double cantidad = Double.parseDouble(tfCantidad.getText().trim());
            double precio = Double.parseDouble(tfPrecio.getText().trim());
            LocalDate fecha = dpFecha.getValue();

            Compra compra = new Compra(mp.getId(), fecha, cantidad, precio);
            String lugar = tfLugar.getText().trim();
            if (!lugar.isEmpty()) {
                compra.setLugar(lugar);
            }

            compraDAO.save(compra);

            tfCantidad.clear();
            tfPrecio.clear();
            tfLugar.clear();
            lblPrecioUnitario.setText("");
            dpFecha.setValue(LocalDate.now());

            data.setAll(compraDAO.findByMateriaPrima(mp.getId()));

        } catch (NumberFormatException e) {
            showAlert("Cantidad y precio deben ser números válidos");
        } catch (RuntimeException e) {
            showAlert("Error: " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.showAndWait();
    }

    @Override
    public void refresh() {
        cbMP.getItems().setAll(mpDAO.findAll());
        data.clear();
    }
}
