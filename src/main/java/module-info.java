module smarthome {
    // Spunem JavaFX ce librarii folosim
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.media;

    // Deschidem pachetul catre JavaFX ca sa poata incarca fereastra
    opens smarthome to javafx.fxml;

    // Exportam pachetul tau ca sa poata fi rulat
    exports smarthome;
}