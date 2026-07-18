package com.casadelosol.inventario.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class MainView extends BorderPane {

    private final VBox sidebar;
    private final BorderPane contentArea;
    private final ToggleGroup menuGroup;

    private final StockView stockView;
    private final CategoriaMPView categoriaMPView;
    private final MateriaPrimaView materiaPrimaView;
    private final CategoriaPTView categoriaPTView;
    private final ProductoTerminadoView productoTerminadoView;
    private final CompraView compraView;
    private final RecetaView recetaView;
    private final ProduccionView produccionView;
    private final VentaView ventaView;
    private final ReportesView reportesView;

    public MainView() {
        stockView = new StockView();
        categoriaMPView = new CategoriaMPView();
        materiaPrimaView = new MateriaPrimaView();
        categoriaPTView = new CategoriaPTView();
        productoTerminadoView = new ProductoTerminadoView();
        compraView = new CompraView();
        recetaView = new RecetaView();
        produccionView = new ProduccionView();
        ventaView = new VentaView();
        reportesView = new ReportesView();

        menuGroup = new ToggleGroup();

        sidebar = createSidebar();
        contentArea = createContentArea();

        setLeft(sidebar);
        setCenter(contentArea);

        showView(stockView.getView());
    }

    private VBox createSidebar() {
        VBox box = new VBox();
        box.getStyleClass().add("sidebar");
        box.setPrefWidth(220);

        Label title = new Label("Casa Del Sol");
        title.getStyleClass().add("sidebar-title");

        box.getChildren().add(title);
        box.getChildren().add(createMenuButton("Stock General", stockView.getView()));
        box.getChildren().add(createSectionLabel("Materias Primas"));
        box.getChildren().add(createMenuButton("Categorías", categoriaMPView.getView()));
        box.getChildren().add(createMenuButton("Materias Primas", materiaPrimaView.getView()));
        box.getChildren().add(createMenuButton("Compras", compraView.getView()));
        box.getChildren().add(createSectionLabel("Productos"));
        box.getChildren().add(createMenuButton("Categorías", categoriaPTView.getView()));
        box.getChildren().add(createMenuButton("Productos", productoTerminadoView.getView()));
        box.getChildren().add(createMenuButton("Recetas", recetaView.getView()));
        box.getChildren().add(createMenuButton("Producción", produccionView.getView()));
        box.getChildren().add(createMenuButton("Ventas", ventaView.getView()));
        box.getChildren().add(createSectionLabel("Reportes"));
        box.getChildren().add(createMenuButton("Reportes", reportesView.getView()));

        return box;
    }

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 15 5 5 5; -fx-underline: true;");
        return label;
    }

    private ToggleButton createMenuButton(String text, Node view) {
        ToggleButton btn = new ToggleButton(text);
        btn.getStyleClass().add("sidebar-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setToggleGroup(menuGroup);
        btn.setOnAction(e -> showView(view));
        return btn;
    }

    private BorderPane createContentArea() {
        BorderPane area = new BorderPane();
        area.getStyleClass().add("content-area");
        return area;
    }

    private void showView(Node view) {
        contentArea.setCenter(view);

        if (view instanceof Refreshable refreshable) {
            refreshable.refresh();
        }
    }
}
