/**
 * Clasa principala a aplicatiei.
 * Extinde Application din JavaFX si configureaza interfata grafica.
 * * Rol in Design Pattern: CLIENT
 * Aici se initializeaza Subiectul (HomeHub) si Observatorii (Widgets),
 * si se realizeaza legatura dintre ei (hub.attach(widget)).
 */

package C2026_J18;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import java.util.function.Consumer;


public class MainApp extends Application {
    private BorderPane dashboardRoot;
    private Scene mainScene;

    // UI meteo (actualizate din retea)
    private Label tempLabel;
    private Label cityLabel;

    // Variabile pentru drag & drop la fereastra
    private double xOffset = 0;
    private double yOffset = 0;

    private ImageView headerProfileImg;

    @Override
    public void start(Stage stage) {
        // Ascundem bara standard de Windows pentru a o customiza
        stage.initStyle(javafx.stage.StageStyle.UNDECORATED);

        try {
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/resources/home-icon.png")));

            // seteaza ca icon a ferestrei (si a Taskbar-ului)
            stage.getIcons().add(icon);
        } catch (Exception e) {
            // Nu am putut incarca iconita
        }


        //BACKEND (Subiectul)
        HomeHub hub = new HomeHub();

        // CONFIGURARE FEREASTRA PRINCIPALA
        dashboardRoot = new BorderPane();
        // Gradient fundal pentru aspect modern
        dashboardRoot.setStyle("-fx-background-color: linear-gradient(to bottom, #111111, #000000);");

        // HEADER => profil, greeting, ceas, meteo
        // Partea stanga
        VBox titleBox = new VBox(5);

        // Label pentru mesaj de salut
        Label greet = new Label();
        greet.setTextFill(Color.WHITE);
        greet.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));

        // Functie pentru actualizarea mesajului de salut in functie de ora
        Runnable updateGreeting = () -> {
            int hour = java.time.LocalTime.now().getHour();
            String timeGreeting;
            if (hour >= 5 && hour < 12) timeGreeting = "Good Morning, ";
            else if (hour >= 12 && hour < 18) timeGreeting = "Good Afternoon, ";
            else timeGreeting = "Good Evening, ";

            greet.setText(timeGreeting + hub.getUserName() + "!");

        };
        updateGreeting.run(); // Set initial

        // Poza profil utilizator
        double radius = 40;
        double diameter = radius * 2;
        StackPane imageHolder = new StackPane();
        imageHolder.setPrefSize(diameter, diameter);
        imageHolder.setMinSize(diameter, diameter);
        imageHolder.setMaxSize(diameter, diameter);

        headerProfileImg = new ImageView();
        StackPane.setAlignment(headerProfileImg, Pos.TOP_CENTER);
        imageHolder.getChildren().add(headerProfileImg);

        loadProfileImage(hub.getProfileImagePath()); // Set default sau personalizata

        imageHolder.setClip(new Circle(radius, radius, radius)); // Crop circular

        // Contur pozei cu efect de shadow
        Circle borderRing = new Circle(radius);
        borderRing.setFill(Color.TRANSPARENT);
        borderRing.setStroke(Color.web("#FFFFFF", 0.25));
        borderRing.setStrokeWidth(3);
        borderRing.setEffect(new DropShadow(15, Color.web("#000000", 0.5)));

        StackPane profilePicContainer = new StackPane(imageHolder, borderRing);

        //Grupare poza + text
        HBox profileArea = new HBox(15, profilePicContainer, titleBox);
        profileArea.setAlignment(Pos.CENTER_LEFT);


        // Subtitlu
        Label sub = new Label("Your home is secure and cozy.");
        sub.setTextFill(Color.GRAY);
        sub.setFont(Font.font("Segoe UI", 16));
        titleBox.getChildren().addAll(greet, sub);

        // Ceas
        Label timeLabel = new Label();
        timeLabel.setTextFill(Color.web("#dddddd")); // Gri deschis
        timeLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));

        // Configuram animatia care actualizeaza ceasul la fiecare secunda
        DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss  |  EEE, d MMM");
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> timeLabel.setText(LocalDateTime.now().format(format))), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        //Meteo
        ImageView weatherIconView = new ImageView();
        try {
            weatherIconView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/resources/sun.png"))));
        } catch (Exception e) {
            //
        }
        weatherIconView.setFitWidth(28);
        weatherIconView.setFitHeight(28);

        DropShadow sunGlow = new DropShadow(15, Color.ORANGE);
        weatherIconView.setEffect(sunGlow);

        // Temperatura
        tempLabel = new Label("--°");
        tempLabel.setTextFill(Color.WHITE);
        tempLabel.setFont(Font.font("Segoe UI", FontWeight.LIGHT, 26));

        // Oras
        cityLabel = new Label(hub.getCity().toUpperCase());
        cityLabel.setTextFill(Color.web("#aaaaaa"));
        cityLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));

        // Grupare
        VBox textContainer = new VBox(-1, tempLabel, cityLabel);
        textContainer.setAlignment(Pos.CENTER_LEFT);

        HBox weatherBadge = getHBox(weatherIconView, textContainer);


        VBox infoBox = new VBox(10, timeLabel, weatherBadge);
        infoBox.setAlignment(Pos.CENTER_RIGHT);

        infoBox.setFillWidth(false);

        // Buton setari
        Button settingsBtn = new Button();
        settingsBtn.setPrefSize(40, 40);

        try {
            // Icon setari
            java.io.InputStream is = getClass().getResourceAsStream("/resources/settings.png");
            if (is != null) {
                ImageView iv = new ImageView(new Image(is));
                iv.setFitWidth(24);
                iv.setFitHeight(24);

                // Efect pentru a face iconita alba
                ColorAdjust whiteEffect = new ColorAdjust();
                whiteEffect.setBrightness(1.0);
                iv.setEffect(whiteEffect);
                settingsBtn.setGraphic(iv);
            } else {

                settingsBtn.setText("NaN");
            }
        } catch (Exception e) {
            settingsBtn.setText("NaN");
        }

        // Stilizare buton
        settingsBtn.setFont(Font.font(18));
        settingsBtn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-background-radius: 50; -fx-cursor: hand;");

        // Action pentru butonul de setari
        settingsBtn.setOnAction(e -> switchToSettingsPage(hub));

        // Punem toate pe header
        HBox rightArea = new HBox(25, infoBox, settingsBtn);
        rightArea.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox();
        header.setPadding(new Insets(40, 40, 20, 40)); // Margini
        header.setAlignment(Pos.CENTER_LEFT);

        // Spacer-ul care impinge ceasul in dreapta
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(profileArea, spacer, rightArea);
        header.setPadding(new Insets(20,40,10,40));

        HBox scenesBar = createSceneBar(hub);
        VBox topContainer = new VBox(10);
        topContainer.getChildren().addAll(header, scenesBar);

        dashboardRoot.setTop(topContainer);

        // WIDGETURI

        // Declaram Widget-urile
        var acWidget = new AirConditionerWidget(hub); // Cel inalt
        var lampWidget = new LampWidget(hub);
        var securityWidget = new SecurityWidget(hub);
        var thermostatWidget = new ThermostatWidget(hub);
        var blinds = new BlindsWidget(hub);
        var music = new MusicWidget(hub);
        var logWidget = new LogWidget(hub);

        // Conectam Observerii -- Asigura functionalitatea butoanelor
        hub.attach(acWidget);
        hub.attach(lampWidget);
        hub.attach(securityWidget);
        hub.attach(thermostatWidget);
        hub.attach(logWidget);
        hub.attach(music);
        hub.attach(blinds);

        // Construim Coloane Verticale
        VBox col1 = new VBox(25, lampWidget, securityWidget);
        VBox col2 = new VBox(25, thermostatWidget, music);
        VBox col3 = new VBox(25, blinds, logWidget);
        VBox col4 = new VBox(25, acWidget);

        //Punem coloanele intr-un HBox orizontal
        HBox mainLayout = new HBox(25, col1, col2, col3, col4);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(30));

        // Punem layout-ul in centrul ecranului
        dashboardRoot.setCenter(mainLayout);

        // Zona de jos - Footer
        Label footer = new Label("Smart Home Hub v2.0 • Connected");
        footer.setTextFill(Color.DARKGRAY);
        footer.setPadding(new Insets(20));
        BorderPane.setAlignment(footer, Pos.CENTER);
        dashboardRoot.setBottom(footer);

        // Notificari din backend pentru actualizari UI generale
        hub.attach((type, val) -> {
            // Actualizare salut
            if ("USER".equals(type)) {
                Platform.runLater(updateGreeting);
            }

            // Actualizare meteo
            if ("CITY".equals(type)) {
                Platform.runLater(() -> {
                    // Actualizam textul cand vine temperatura de pe net
                    String t = String.format("%.1f", hub.getOutsideTemp());
                    tempLabel.setText(t + "°");

                    cityLabel.setText(hub.getCity().toUpperCase());
                });
            }

            // Actualizare profil
            if ("PROFILE".equals(type)) {
                Platform.runLater(() -> loadProfileImage((String) val));
            }

            // Actulizare icon meteo
            if ("WEATHER".equals(type)) {
                String condition = (String) val;
                Platform.runLater(() -> updateWeatherIcon(weatherIconView, condition));
            }

        });

        // Crearea bara titlu custom
        HBox customTitleBar = createCustomTitleBar(stage);
        VBox rootContainer = new VBox(customTitleBar, dashboardRoot);
        VBox.setVgrow(dashboardRoot, Priority.ALWAYS);
        rootContainer.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 20, 0, 0, 0);");


        // Afisare scena
        mainScene = new Scene(rootContainer, 1300, 850);
        mainScene.getStylesheets().add("data:text/css," +
                ".list-cell{-fx-text-fill: #00ff00; -fx-font-family: 'Consolas';} " +
                ".list-view .scroll-bar:vertical {-fx-opacity: 0;}");
        stage.setTitle("Proiect PIP 2025");
        stage.setScene(mainScene);
        stage.show();

        // Incarcam initial vremea
        hub.setCity(hub.getCity());

    }

    // Metode helper (UI & Widgets)
    private void updateWeatherIcon(ImageView iconView, String condition) {
        String path; // Default

        // Verificam ce ne-a dat API-ul si alegem poza
        // Valorile posibile de la OpenWeatherMap: Thunderstorm, Drizzle, Rain, Snow, Clear, Clouds
        switch (condition) {
            case "Clouds":
            case "Mist":
            case "Fog":
            case "Haze":
                path = "/resources/cloud.png";
                break;
            case "Rain":
            case "Drizzle":
                path = "/resources/rain.png";
                break;
            case "Snow":
                path = "/resources/snow.png";
                break;
            case "Thunderstorm":
                path = "/resources/storm.png";
                break;
            case "Clear":
            default:
                path = "/resources/sun.png";
                break;
        }

        try {
            iconView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(path))));

            // Efect vizual (Glow) diferit in functie de vreme
            if (path.contains("sun")) {
                iconView.setEffect(new DropShadow(15, Color.ORANGE));
            } else if (path.contains("cloud")) {
                iconView.setEffect(new DropShadow(10, Color.GRAY));
            } else if (path.contains("rain") || path.contains("storm")) {
                iconView.setEffect(new DropShadow(10, Color.BLUE));
            } else {
                iconView.setEffect(null);
            }

        } catch (Exception e) {
            System.out.println("Nu am gasit iconita pentru vreme: " + path);
        }
    }

    private void loadProfileImage(String path) {
        if (headerProfileImg == null) return;

        //Fotografia utilizatorului
        double radius = 44;
        double diameter = radius * 2;
        try {
            Image sourceImage;
            // Verificam daca e imagine din resurse (default) sau de pe disc (aleasa de user)
            if (path.startsWith("/")) {
                sourceImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
            } else {
                sourceImage = new Image(path); // Incarcare de pe disc (file:...)
            }

            headerProfileImg.setImage(sourceImage);
            headerProfileImg.setPreserveRatio(true);
            headerProfileImg.setSmooth(true);

            // Resetam viewport-ul (important cand schimbam poze cu forme diferite)
            headerProfileImg.setViewport(null);

            // LOGICA SMART CROP
            if (sourceImage.getWidth() < sourceImage.getHeight()) {
                headerProfileImg.setFitWidth(diameter);
                headerProfileImg.setFitHeight(-1); // Auto
            } else {
                headerProfileImg.setFitHeight(diameter);
                headerProfileImg.setFitWidth(-1); // Auto
            }

        } catch (Exception e) {
            System.out.println("Eroare incarcare imagine: " + e.getMessage());
        }
    }

    private static HBox getHBox(ImageView weatherIconView, VBox textContainer) {
        HBox weatherBadge = new HBox(10, weatherIconView, textContainer);
        weatherBadge.setAlignment(Pos.CENTER_LEFT);
        weatherBadge.setPadding(new Insets(4, 12, 4, 8));

        weatherBadge.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.08);" + // Foarte transparent
                        "-fx-background-radius: 20;" + // Rotunjit
                        "-fx-border-color: rgba(255, 255, 255, 0.1);" + // Bordura fina
                        "-fx-border-radius: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);" // Umbra discreta
        );
        return weatherBadge;
    }

    private void switchToSettingsPage(HomeHub hub) {

        // ROOT-ul paginii
        VBox settingsRoot = new VBox();
        settingsRoot.setAlignment(Pos.CENTER);
        settingsRoot.setStyle("-fx-background-color: linear-gradient(to bottom, #111111, #000000);"); // Acelasi gradient ca dashboard
        VBox.setVgrow(settingsRoot, Priority.ALWAYS);


        // Titlu
        Label title = new Label("Settings");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));
        title.setTextFill(Color.WHITE);

        HBox headerBox = new HBox(20, title);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 20, 0)); // Spatiu sub titlu
        headerBox.setMaxWidth(400);

        // Card central
        VBox card = new VBox(25); // Spatiere mare intre elemente
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(400);
        card.setPadding(new Insets(40));

        card.setStyle(
                "-fx-background-color: rgba(30, 30, 30, 0.6);" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: rgba(255, 255, 255, 0.1);" +
                        "-fx-border-radius: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 20, 0, 0, 10);"
        );

        // Input pentru numele utilizatorului
        Label name = new Label("User Name");
        name.setFont(Font.font(18));
        name.setTextFill(Color.GRAY);

        TextField userName = new TextField(hub.getUserName());
        VBox nameGroup = new VBox(5, name, userName);
        nameGroup.setAlignment(Pos.CENTER_LEFT);
        styleInput(userName);

        // Input oras
        Label cityLabel = new Label("City");
        cityLabel.setFont(Font.font(18));
        cityLabel.setTextFill(Color.GRAY);

        TextField cityInput = new TextField(hub.getCity());
        styleInput(cityInput);

        VBox cityGroup = new VBox(5, cityLabel, cityInput);
        cityGroup.setAlignment(Pos.CENTER_LEFT);

        // Poza profil
        Label picLabel = new Label("Profile Picture");
        picLabel.setFont(Font.font(18));
        picLabel.setTextFill(Color.GRAY);

        double previewSize = 100;
        double radius = previewSize / 2;

        ImageView settingPreviewImg = new ImageView();
        settingPreviewImg.setPreserveRatio(true);
        settingPreviewImg.setSmooth(true);

        // Functie smart pentru ajustarea dimensiunii imaginii
        Consumer<Image> updateImageSmart = (img) -> {
            settingPreviewImg.setImage(img);
            // Resetam dimensiunile vechi
            settingPreviewImg.setFitWidth(0); settingPreviewImg.setFitHeight(0);

            if (img.getWidth() < img.getHeight()) {
                settingPreviewImg.setFitWidth(previewSize);
            } else {
                settingPreviewImg.setFitHeight(previewSize);
            }
        };

        // Incarcam imaginea curenta
        try {
            String currentPath = hub.getProfileImagePath();
            Image initialImg;
            if (currentPath.startsWith("/")) initialImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream(currentPath)));
            else initialImg = new Image(currentPath);

            updateImageSmart.accept(initialImg); // Aplicam logica smart
        } catch (Exception e) { /* fallback */ }

        StackPane previewContainer = new StackPane(settingPreviewImg);
        previewContainer.setMaxSize(previewSize, previewSize);
        previewContainer.setMinSize(previewSize, previewSize);
        StackPane.setAlignment(settingPreviewImg, Pos.CENTER);

        previewContainer.setClip(new Circle(radius, radius, radius)); // Crop circular

        previewContainer.setEffect(new DropShadow(10, Color.BLACK));
        StackPane borderContainer = new StackPane(previewContainer);
        borderContainer.setStyle("-fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 2; -fx-border-radius: 50%;");
        borderContainer.setMaxSize(previewSize+4, previewSize+4);

        // Buton pentru alegere imagine
        Button changePicBtn = new Button("Choose Image...");
        changePicBtn.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");

        final String[] tempNewPath = { null }; // retinere cale noua temporar

        changePicBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Profile Image");
            // Filtram doar imagini
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );

            // Fereastra principala (luam Stage-ul din scena)
            Stage stage = (Stage) mainScene.getWindow();
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null) {
                // Convertim calea fisierului in URL (file:/C:/Users/...)
                String imagePath = selectedFile.toURI().toString();

                // Actualizam preview-ul
                updateImageSmart.accept(new Image(imagePath));

                // Salvam calea temporar
                tempNewPath[0] = imagePath;
            }
        });

        HBox picBox = new HBox(20, previewContainer, changePicBtn);
        picBox.setAlignment(Pos.CENTER);

        // Buton SAVE
        Button saveBtn = getButton(hub, userName, cityInput, tempNewPath);
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setStyle("-fx-background-color: #34C759; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-size: 16px; -fx-padding: 12; -fx-cursor: hand; -fx-font-weight: bold;");
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle("-fx-background-color: #30D158; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-size: 16px; -fx-padding: 12; -fx-cursor: hand; -fx-font-weight: bold;"));
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle("-fx-background-color: #34C759; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-size: 16px; -fx-padding: 12; -fx-cursor: hand; -fx-font-weight: bold;"));


        // Buton back
        Button backBtn = new Button("Back");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #0A84FF; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0;");

        backBtn.setOnAction(e -> {
            if (mainScene.getRoot() instanceof VBox rootBox) {
                rootBox.getChildren().set(1, dashboardRoot);
            } else {
                // Reconstruim daca s-a pierdut bara
                Stage stage = (Stage) mainScene.getWindow();
                HBox customTitleBar = createCustomTitleBar(stage);
                VBox rootContainer = new VBox(customTitleBar, dashboardRoot);
                VBox.setVgrow(dashboardRoot, Priority.ALWAYS);
                rootContainer.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 20, 0, 0, 0);");
                mainScene.setRoot(rootContainer);
            }
        });

        // Asamblare card
        card.getChildren().addAll(picBox, nameGroup, cityGroup, new Region(), saveBtn, backBtn);

        // Asamblare root Settings
        settingsRoot.getChildren().addAll(headerBox, card);

        if (mainScene.getRoot() instanceof VBox rootBox) {
            if (rootBox.getChildren().size() > 1) {
                rootBox.getChildren().set(1, settingsRoot);
            } else {
                rootBox.getChildren().add(settingsRoot);
            }
        } else {
            // Reconstruim structura daca e stricata cand intram in setari
            Stage stage = (Stage) mainScene.getWindow();
            HBox customTitleBar = createCustomTitleBar(stage);
            VBox rootContainer = new VBox(customTitleBar, settingsRoot);
            VBox.setVgrow(settingsRoot, Priority.ALWAYS);
            rootContainer.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 20, 0, 0, 0);");
            mainScene.setRoot(rootContainer);
        }
    }

    private void styleInput(TextField tf) {
        // Stil modern: fundal putin transparent, bordura fina
        tf.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 12;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: rgba(255,255,255,0.1);" +
                        "-fx-border-radius: 10;"
        );
        // Cand dai click (Focus), se lumineaza bordura
        tf.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                tf.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.1);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;" +
                                "-fx-padding: 12;" +
                                "-fx-background-radius: 10;" +
                                "-fx-border-color: #0A84FF;" + // Albastru
                                "-fx-border-radius: 10;"
                );
            } else {
                // Revenire la stil normal
                tf.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.05);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;" +
                                "-fx-padding: 12;" +
                                "-fx-background-radius: 10;" +
                                "-fx-border-color: rgba(255,255,255,0.1);" +
                                "-fx-border-radius: 10;"
                );
            }
        });
    }

    private Button getButton(HomeHub hub, TextField userName, TextField cityInput, String[] tempNewPath) {
        Button saveBtn = new Button("Save Changes");

        // Stilizare buton Save
        saveBtn.setStyle("-fx-background-color: #34C759; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-size: 16px; -fx-padding: 10 30; -fx-cursor: hand;");

        saveBtn.setOnAction(e -> {
            // Salvam numele
            String newName = userName.getText();
            if (!newName.isEmpty()) {
                hub.setUserName(newName); // Salvam in backend
            }

            // Salvam orasul
            if(!cityInput.getText().isEmpty()) {
                hub.setCity(cityInput.getText());
            }

            // Salvam calea noii imagini
            if (tempNewPath[0] != null) {
                hub.setProfileImagePath(tempNewPath[0]);
            }

            // Actualizam UI: revenire dashboard
            if (mainScene.getRoot() instanceof VBox rootBox) {
                if (rootBox.getChildren().size() > 1) {
                    rootBox.getChildren().set(1, dashboardRoot);
                } else {
                    rootBox.getChildren().add(dashboardRoot);
                }
            } else {
                // CAZ DE EROARE (Fix pentru ClassCastException):
                // Am pierdut bara (Root-ul e BorderPane). O reconstruim
                Stage stage = (Stage) mainScene.getWindow();
                HBox customTitleBar = createCustomTitleBar(stage); // Recream bara

                VBox rootContainer = new VBox(customTitleBar, dashboardRoot);
                VBox.setVgrow(dashboardRoot, Priority.ALWAYS);
                rootContainer.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 20, 0, 0, 0);");

                mainScene.setRoot(rootContainer); // Resetam radacina corecta
            }
        });
        return saveBtn;
    }

    private HBox createSceneBar(HomeHub hub) {
        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(0, 0, 20, 0));

        // Definim cele 4 butoane
        Button btnMorning = createPillSceneBtn("Morning", "/resources/sun.png", "#FF9F0A", e -> {
            hub.setBlindsLevel(100);
            hub.toggleLights(false);
            hub.setHeatingPower(true);
            hub.setTemperature(22.0);
            hub.setAcPower(false);
            hub.setMusicPlaying(true);
            hub.setDoorLocked(false);
        });

        Button btnCinema = createPillSceneBtn("Cinema", "/resources/popcorn.png", "#BF5AF2", e -> {
            hub.setBlindsLevel(0);
            hub.toggleLights(false);
            hub.setHeatingPower(false);
            hub.setAcPower(true);
            hub.setAcTemperature(22.0);
            hub.setMusicPlaying(false);
            hub.setDoorLocked(true);
        });

        Button btnAway = createPillSceneBtn("Away", "/resources/door.png", "#FF453A", e -> {
            hub.setDoorLocked(true);
            hub.toggleLights(false);
            hub.setMusicPlaying(false);
            hub.setBlindsLevel(35);
            hub.setAcPower(false);
            hub.setHeatingPower(true);
            hub.setTemperature(16.0);
        });

        Button btnNight = createPillSceneBtn("Night", "/resources/moon.png", "#0A84FF", e -> {
            hub.setDoorLocked(true);
            hub.setBlindsLevel(0);
            hub.toggleLights(false);
            hub.setMusicPlaying(false);
            hub.setAcPower(false);
            hub.setHeatingPower(true);
            hub.setTemperature(19.5);
        });

        bar.getChildren().addAll(btnMorning, btnCinema, btnAway, btnNight);

        return bar;
    }

    // Helper pentru design-ul butonului
    private Button createPillSceneBtn(String text, String iconPath, String color, EventHandler<ActionEvent> action) {
        Button btn = new Button(text);

        // Icon
        try {
            java.io.InputStream is = getClass().getResourceAsStream(iconPath);
            if (is != null) {
                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(new javafx.scene.image.Image(is));
                iv.setFitWidth(20);  // Dimensiune pentru iconita
                iv.setFitHeight(20);

                btn.setGraphic(iv); // Setam imaginea ca grafica a butonului
            }
        } catch (Exception e) {
            System.out.println("Nu s-a putut incarca iconita scenei: " + iconPath);
        }

        btn.setGraphicTextGap(10);

        // STIL APPLE: Gri inchis, Rotunjit, Font alb
        String baseStyle =
                "-fx-background-color: rgba(255,255,255,0.1);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 30;" + // Forma de capsula
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 25;" + // Mai lat
                        "-fx-cursor: hand;" +
                        "-fx-border-color: rgba(255,255,255,0.1);" +
                        "-fx-border-radius: 30;";

        btn.setStyle(baseStyle);

        // Hover Effect: Se lumineaza putin bordura si fundalul
        btn.setOnMouseEntered(e -> btn.setStyle(baseStyle + "-fx-background-color: rgba(255,255,255,0.2); -fx-border-color: " + color + ";"));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));

        // Click Action cu efect vizual
        btn.setOnAction(e -> {
            // Rulam logica scenei
            action.handle(e);

            // Efect de "Flash" colorat cand apesi
            btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 30; -fx-padding: 10 25; -fx-font-size: 15px; -fx-font-weight: bold;");

            // Revine la normal dupa 300ms
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> btn.setStyle(baseStyle));
                }
            }, 300);
        });

        return btn;
    }

    private HBox createCustomTitleBar(Stage stage) {
        HBox titleBar = new HBox();
        titleBar.setPadding(new Insets(10, 20, 10, 20));
        titleBar.setAlignment(Pos.CENTER_LEFT);

        titleBar.setStyle("-fx-background-color: #111111; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");

        // Titlu aplicatie
        Label appTitle = new Label("Smart Home Hub");
        appTitle.setTextFill(Color.GRAY);
        appTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // SVG paths pentru butoanele minimize/maximize/restore/close
        String minPath = "M0,7 L10,7 L10,8 L0,8 Z";
        String maxPath = "M0,0 L10,0 L10,10 L0,10 L0,0 Z M1,1 L9,1 L9,9 L1,9 Z";
        String restorePath = "M2,0 L10,0 L10,8 L8,8 L8,10 L0,10 L0,2 L2,2 L2,0 Z M2,2 L2,8 L8,8 L8,2 Z M4,1 L9,1 L9,6 L8,6 L8,4 L4,4 Z";
        String closePath = "M0,0 L10,10 M0,10 L10,0";

        // Maximize/Restore logic
        Button minBtn = createIconBtn(minPath, false, e -> stage.setIconified(true));
        Button maxBtn = createIconBtn(maxPath, false, null); // Action il punem manual mai jos

        maxBtn.setOnAction(e -> {
            boolean isMax = stage.isMaximized();
            stage.setMaximized(!isMax);

            // SCHIMBAM ICONITA DINAMIC
            SVGPath icon = (SVGPath) maxBtn.getGraphic();
            if (!isMax) {
                // Daca tocmai am maximizat -> punem iconita de Restore
                icon.setContent(restorePath);
            } else {
                // Daca am revenit la normal -> punem iconita de Maximize
                icon.setContent(maxPath);
            }
        });

        // Close
        Button closeBtn = createIconBtn(closePath, true, e -> {
            Platform.exit(); System.exit(0);
        });

        HBox buttons = new HBox(0, minBtn, maxBtn, closeBtn); // Spatiere 0 intre ele (ca la Windows)
        buttons.setAlignment(Pos.CENTER_RIGHT);

        titleBar.getChildren().addAll(appTitle, spacer, buttons);

        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            // IMPORTANT: Nu lasam sa muti fereastra daca e Maximizata!
            if (stage.isMaximized()) return;

            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        titleBar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                maxBtn.fire(); // Simulam apasarea butonului
            }
        });

        return titleBar;
    }

    private Button createIconBtn(String svgData, boolean isCloseBtn, EventHandler<ActionEvent> action) {
        // 1. Cream forma vectoriala (Iconita)
        SVGPath path = new SVGPath();
        path.setContent(svgData);
        path.setFill(Color.web("#aaaaaa")); // Culoare initiala gri
        path.setStroke(Color.TRANSPARENT);

        // Daca e butonul de Close, folosim stroke pentru X, altfel fill
        if (isCloseBtn) {
            path.setStroke(Color.web("#aaaaaa"));
            path.setStrokeWidth(1.2);
            path.setFill(Color.TRANSPARENT);
        }

        // 2. Cream Butonul
        Button btn = new Button();
        btn.setGraphic(path);

        // Marimi standard Windows
        btn.setPrefSize(46, 32);
        btn.setMinSize(46, 32);
        btn.setMaxSize(46, 32);

        // Stil de baza (Transparent)
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: default;");

        // 3. Logica de HOVER (Efecte vizuale)
        btn.setOnMouseEntered(e -> {
            if (isCloseBtn) {
                // E butonul Close -> Fundal ROSU, Iconita ALBA
                btn.setStyle("-fx-background-color: #E81123;");
                path.setStroke(Color.WHITE);
            } else {
                // E alt buton -> Fundal GRI TRANSPARENT, Iconita ALBA
                btn.setStyle("-fx-background-color: rgba(255,255,255,0.1);");
                path.setFill(Color.WHITE);
            }
        });

        btn.setOnMouseExited(e -> {
            // Revenim la normal
            btn.setStyle("-fx-background-color: transparent;");
            if (isCloseBtn) {
                path.setStroke(Color.web("#aaaaaa"));
            } else {
                path.setFill(Color.web("#aaaaaa"));
            }
        });

        if (action != null) btn.setOnAction(action);
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}