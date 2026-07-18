module com.casadelosol.inventario {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports com.casadelosol.inventario;
    opens com.casadelosol.inventario to javafx.graphics, javafx.fxml;
    opens com.casadelosol.inventario.model to javafx.base;
    opens com.casadelosol.inventario.dao to javafx.fxml;
}
