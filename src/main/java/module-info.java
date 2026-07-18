module com.casadelosol.inventario {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.casadelosol.inventario to javafx.fxml;
    opens com.casadelosol.inventario.model to javafx.base;
    opens com.casadelosol.inventario.dao to javafx.fxml;
}
