package com.casadelosol.inventario.ui;

import com.casadelosol.inventario.dao.ProduccionDAO;
import com.casadelosol.inventario.dao.VentaDAO;
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

import java.time.LocalDate;

public class ReportesView implements Refreshable {

    private final DatePicker dpProdDesde = new DatePicker(LocalDate.now().withDayOfMonth(1));
    private final DatePicker dpProdHasta = new DatePicker(LocalDate.now());
    private final TableView<ProduccionDAO.ResumenProduccion> prodTable = new TableView<>();
    private final ObservableList<ProduccionDAO.ResumenProduccion> prodData = FXCollections.observableArrayList();
    private final Label prodTotalLabel = new Label();

    private final DatePicker dpVentaDesde = new DatePicker(LocalDate.now().withDayOfMonth(1));
    private final DatePicker dpVentaHasta = new DatePicker(LocalDate.now());
    private final TableView<VentaDAO.ResumenVenta> ventaTable = new TableView<>();
    private final ObservableList<VentaDAO.ResumenVenta> ventaData = FXCollections.observableArrayList();
    private final Label ventaTotalLabel = new Label();

    private final ProduccionDAO prodDAO = new ProduccionDAO();
    private final VentaDAO ventaDAO = new VentaDAO();

    public Node getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Reportes");
        title.getStyleClass().add("section-title");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab prodTab = new Tab("Producción", createProductionReport());
        Tab ventaTab = new Tab("Ventas", createSalesReport());

        tabPane.getTabs().addAll(prodTab, ventaTab);

        root.getChildren().addAll(title, tabPane);

        refresh();
        return root;
    }

    private Node createProductionReport() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));

        GridPane filterRow = new GridPane();
        filterRow.setHgap(10);
        filterRow.setVgap(10);

        Button btnGenerar = new Button("Generar Reporte");
        btnGenerar.getStyleClass().add("btn-primary");
        btnGenerar.setOnAction(e -> generarReporteProduccion());

        filterRow.add(new Label("Desde:"), 0, 0);
        filterRow.add(dpProdDesde, 1, 0);
        filterRow.add(new Label("Hasta:"), 2, 0);
        filterRow.add(dpProdHasta, 3, 0);
        filterRow.add(btnGenerar, 4, 0);

        TableColumn<ProduccionDAO.ResumenProduccion, String> colProd = new TableColumn<>("Producto");
        colProd.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().productoNombre()));
        colProd.setPrefWidth(300);

        TableColumn<ProduccionDAO.ResumenProduccion, Number> colCant = new TableColumn<>("Cantidad Producida");
        colCant.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().totalCantidad()));
        colCant.setPrefWidth(150);

        TableColumn<ProduccionDAO.ResumenProduccion, Number> colCount = new TableColumn<>("N° Producciones");
        colCount.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().totalProducciones()));
        colCount.setPrefWidth(130);

        prodTable.getColumns().addAll(colProd, colCant, colCount);
        prodTable.setItems(prodData);
        prodTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        prodTotalLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 0 0;");

        section.getChildren().addAll(filterRow, prodTable, prodTotalLabel);
        return section;
    }

    private Node createSalesReport() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));

        GridPane filterRow = new GridPane();
        filterRow.setHgap(10);
        filterRow.setVgap(10);

        Button btnGenerar = new Button("Generar Reporte");
        btnGenerar.getStyleClass().add("btn-primary");
        btnGenerar.setOnAction(e -> generarReporteVentas());

        filterRow.add(new Label("Desde:"), 0, 0);
        filterRow.add(dpVentaDesde, 1, 0);
        filterRow.add(new Label("Hasta:"), 2, 0);
        filterRow.add(dpVentaHasta, 3, 0);
        filterRow.add(btnGenerar, 4, 0);

        TableColumn<VentaDAO.ResumenVenta, String> colProd = new TableColumn<>("Producto");
        colProd.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().productoNombre()));
        colProd.setPrefWidth(250);

        TableColumn<VentaDAO.ResumenVenta, Number> colCant = new TableColumn<>("Cantidad Vendida");
        colCant.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().totalCantidad()));
        colCant.setPrefWidth(130);

        TableColumn<VentaDAO.ResumenVenta, Number> colTotal = new TableColumn<>("Total ($)");
        colTotal.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().totalImporte()));
        colTotal.setPrefWidth(130);

        TableColumn<VentaDAO.ResumenVenta, Number> colCount = new TableColumn<>("N° Ventas");
        colCount.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().totalVentas()));
        colCount.setPrefWidth(110);

        ventaTable.getColumns().addAll(colProd, colCant, colTotal, colCount);
        ventaTable.setItems(ventaData);
        ventaTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        ventaTotalLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 0 0;");

        section.getChildren().addAll(filterRow, ventaTable, ventaTotalLabel);
        return section;
    }

    private void generarReporteProduccion() {
        LocalDate desde = dpProdDesde.getValue();
        LocalDate hasta = dpProdHasta.getValue();
        if (desde == null || hasta == null) {
            showAlert("Seleccione fecha desde y hasta");
            return;
        }
        if (hasta.isBefore(desde)) {
            showAlert("La fecha 'hasta' debe ser posterior a 'desde'");
            return;
        }

        var resumen = prodDAO.getResumenPorPeriodo(desde, hasta);
        prodData.setAll(resumen);

        double totalCantidad = resumen.stream().mapToDouble(ProduccionDAO.ResumenProduccion::totalCantidad).sum();
        prodTotalLabel.setText("Total producido: " + totalCantidad + " unidades");
    }

    private void generarReporteVentas() {
        LocalDate desde = dpVentaDesde.getValue();
        LocalDate hasta = dpVentaHasta.getValue();
        if (desde == null || hasta == null) {
            showAlert("Seleccione fecha desde y hasta");
            return;
        }
        if (hasta.isBefore(desde)) {
            showAlert("La fecha 'hasta' debe ser posterior a 'desde'");
            return;
        }

        var resumen = ventaDAO.getResumenPorPeriodo(desde, hasta);
        ventaData.setAll(resumen);

        double totalCantidad = resumen.stream().mapToDouble(VentaDAO.ResumenVenta::totalCantidad).sum();
        double totalImporte = resumen.stream().mapToDouble(VentaDAO.ResumenVenta::totalImporte).sum();
        ventaTotalLabel.setText("Total vendido: " + totalCantidad + " unidades  |  Total recaudado: $" + String.format("%.2f", totalImporte));
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.showAndWait();
    }

    @Override
    public void refresh() {
        dpProdDesde.setValue(LocalDate.now().withDayOfMonth(1));
        dpProdHasta.setValue(LocalDate.now());
        dpVentaDesde.setValue(LocalDate.now().withDayOfMonth(1));
        dpVentaHasta.setValue(LocalDate.now());
        prodData.clear();
        ventaData.clear();
        prodTotalLabel.setText("");
        ventaTotalLabel.setText("");
    }
}
