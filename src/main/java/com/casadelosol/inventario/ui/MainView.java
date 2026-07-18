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

    private final Node stockNode;
    private final Node categoriaMPNode;
    private final Node materiaPrimaNode;
    private final Node categoriaPTNode;
    private final Node productoTerminadoNode;
    private final Node compraNode;
    private final Node recetaNode;
    private final Node produccionNode;
    private final Node ventaNode;
    private final Node reportesNode;

    private final ToggleGroup menuGroup;
    private final BorderPane contentArea;

    public MainView() {
        stockNode = new StockView().getView();
        categoriaMPNode = new CategoriaMPView().getView();
        materiaPrimaNode = new MateriaPrimaView().getView();
        categoriaPTNode = new CategoriaPTView().getView();
        productoTerminadoNode = new ProductoTerminadoView().getView();
        compraNode = new CompraView().getView();
        recetaNode = new RecetaView().getView();
        produccionNode = new ProduccionView().getView();
        ventaNode = new VentaView().getView();
        reportesNode = new ReportesView().getView();

        menuGroup = new ToggleGroup();
        contentArea = createContentArea();
        VBox sidebar = createSidebar();

        setLeft(sidebar);
        setCenter(contentArea);

        showNode(stockNode);
    }

    private VBox createSidebar() {
        VBox box = new VBox();
        box.getStyleClass().add("sidebar");
        box.setPrefWidth(220);

        Label title = new Label("Casa Del Sol");
        title.getStyleClass().add("sidebar-title");

        box.getChildren().add(title);
        box.getChildren().add(createMenuButton("◉  Stock General", stockNode));
        box.getChildren().add(createSectionLabel("MATERIAS PRIMAS"));
        box.getChildren().add(createMenuButton("▸  Categorías", categoriaMPNode));
        box.getChildren().add(createMenuButton("◆  Materias Primas", materiaPrimaNode));
        box.getChildren().add(createMenuButton("◇  Compras", compraNode));
        box.getChildren().add(createSectionLabel("PRODUCTOS"));
        box.getChildren().add(createMenuButton("▸  Categorías", categoriaPTNode));
        box.getChildren().add(createMenuButton("■  Productos", productoTerminadoNode));
        box.getChildren().add(createMenuButton("☰  Recetas", recetaNode));
        box.getChildren().add(createMenuButton("⚙  Producción", produccionNode));
        box.getChildren().add(createMenuButton("$  Ventas", ventaNode));
        box.getChildren().add(createSectionLabel("REPORTES"));
        box.getChildren().add(createMenuButton("▤  Reportes", reportesNode));

        return box;
    }

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("sidebar-section-label");
        return label;
    }

    private ToggleButton createMenuButton(String text, Node view) {
        ToggleButton btn = new ToggleButton(text);
        btn.getStyleClass().add("sidebar-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setToggleGroup(menuGroup);
        btn.setOnAction(e -> showNode(view));
        return btn;
    }

    private BorderPane createContentArea() {
        BorderPane area = new BorderPane();
        area.getStyleClass().add("content-area");
        area.setMinWidth(780);
        area.setMinHeight(600);
        return area;
    }

    private void showNode(Node view) {
        contentArea.setCenter(view);

        if (view instanceof Refreshable refreshable) {
            refreshable.refresh();
        }
    }
}
