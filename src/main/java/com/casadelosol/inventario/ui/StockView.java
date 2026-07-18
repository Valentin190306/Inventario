package com.casadelosol.inventario.ui;

import com.casadelosol.inventario.dao.MateriaPrimaDAO;
import com.casadelosol.inventario.dao.ProductoTerminadoDAO;
import com.casadelosol.inventario.model.MateriaPrima;
import com.casadelosol.inventario.model.ProductoTerminado;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class StockView implements Refreshable {

    private static final double STOCK_BAJO_MP = 100;
    private static final double STOCK_BAJO_PT = 10;

    private final TableView<MateriaPrima> mpTable = new TableView<>();
    private final TableView<ProductoTerminado> ptTable = new TableView<>();
    private final ObservableList<MateriaPrima> mpData = FXCollections.observableArrayList();
    private final ObservableList<ProductoTerminado> ptData = FXCollections.observableArrayList();
    private final Label alertLabel = new Label();

    private final MateriaPrimaDAO mpDAO = new MateriaPrimaDAO();
    private final ProductoTerminadoDAO ptDAO = new ProductoTerminadoDAO();

    public Node getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Stock General");
        title.getStyleClass().add("section-title");

        root.getChildren().add(title);
        root.getChildren().add(alertLabel);
        root.getChildren().add(createMPTable());
        root.getChildren().add(createPTTable());

        refresh();
        return root;
    }

    private Node createMPTable() {
        VBox section = new VBox(10);
        Label subtitle = new Label("Materias Primas");
        subtitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TableColumn<MateriaPrima, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colNombre.setPrefWidth(200);

        TableColumn<MateriaPrima, String> colUnidad = new TableColumn<>("Unidad");
        colUnidad.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidadMedida()));
        colUnidad.setPrefWidth(80);

        TableColumn<MateriaPrima, Number> colStock = new TableColumn<>("Stock Actual");
        colStock.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getStockActual()));
        colStock.setPrefWidth(120);

        mpTable.getColumns().addAll(colNombre, colUnidad, colStock);
        mpTable.setItems(mpData);
        mpTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        mpTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(MateriaPrima item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && item.getStockActual() < STOCK_BAJO_MP) {
                    setStyle("-fx-background-color: #ffcccc;");
                } else {
                    setStyle("");
                }
            }
        });

        section.getChildren().addAll(subtitle, mpTable);
        return section;
    }

    private Node createPTTable() {
        VBox section = new VBox(10);
        Label subtitle = new Label("Productos Terminados");
        subtitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TableColumn<ProductoTerminado, String> colNombre = new TableColumn<>("Nombre / Variante");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colNombre.setPrefWidth(250);

        TableColumn<ProductoTerminado, Number> colPrecio = new TableColumn<>("Precio Venta");
        colPrecio.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPrecioVenta()));
        colPrecio.setPrefWidth(100);

        TableColumn<ProductoTerminado, Number> colStock = new TableColumn<>("Stock Actual");
        colStock.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getStockActual()));
        colStock.setPrefWidth(120);

        ptTable.getColumns().addAll(colNombre, colPrecio, colStock);
        ptTable.setItems(ptData);
        ptTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        ptTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ProductoTerminado item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && item.getStockActual() < STOCK_BAJO_PT) {
                    setStyle("-fx-background-color: #ffcccc;");
                } else {
                    setStyle("");
                }
            }
        });

        section.getChildren().addAll(subtitle, ptTable);
        return section;
    }

    @Override
    public void refresh() {
        mpData.setAll(mpDAO.findAll());
        ptData.setAll(ptDAO.findAll());

        long mpBajo = mpData.stream().filter(m -> m.getStockActual() < STOCK_BAJO_MP).count();
        long ptBajo = ptData.stream().filter(p -> p.getStockActual() < STOCK_BAJO_PT).count();

        if (mpBajo > 0 || ptBajo > 0) {
            alertLabel.getStyleClass().add("status-alert");
            alertLabel.setText("⚠ " + mpBajo + " materias primas y " + ptBajo + " productos con stock bajo");
        } else {
            alertLabel.setText("✔ Todos los productos tienen stock suficiente");
            alertLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        }
    }
}
