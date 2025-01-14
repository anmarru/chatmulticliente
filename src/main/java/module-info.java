module andrea {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    /*opens andrea to javafx.fxml;
    exports andrea; */
    // Abrir los subpaquetes al módulo javafx.fxml (para acceso reflexivo de FXML)
    opens andrea.cliente to javafx.fxml;
    opens andrea.servidor to javafx.fxml;

    // Exportar los subpaquetes para que otros módulos puedan acceder a sus clases públicas
    exports andrea;
    exports andrea.cliente;
    exports andrea.servidor;
}
