module C2026_J18 {
    // Spunem JavaFX ce librarii folosim
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.media;

    // Deschidem pachetul catre JavaFX ca sa poata incarca fereastra
    opens C2026_J18 to javafx.fxml;

    // Exportam pachetul tau ca sa poata fi rulat
    exports C2026_J18;
}