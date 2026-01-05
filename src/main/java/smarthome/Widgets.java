package smarthome;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.io.InputStream;

import java.util.*;
import java.util.ArrayList;

import javafx.animation.ScaleTransition;
import javafx.scene.Cursor;
import javafx.util.Duration;

public class Widgets {
}
    // Clasa de baza pentru stil ( card design )
    abstract class BaseWidget extends VBox implements SmartObserver {
        protected HomeHub hub;

    public BaseWidget(HomeHub hub) {
        this.hub = hub;

        this.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #2b2b2b, #1f1f1f);" +
                        "-fx-background-radius: 25;" +
                        "-fx-border-color: rgba(255,255,255,0.1);" +
                        "-fx-border-radius: 25;" +
                        "-fx-border-width: 1;"
            );

        this.setPadding(new Insets(20));
        this.setSpacing(10);
        this.setPrefSize(240, 240); // Carduri putin mai late
        this.setEffect(new DropShadow(20, Color.rgb(0,0,0, 0.6)));

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), this);
        scaleUp.setToX(1.03); // Se mareste pe orizontala
        scaleUp.setToY(1.03); // Se mareste pe verticala

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), this);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        // Activare animatie la mouse
        this.setOnMouseEntered(e -> {
            scaleUp.playFromStart();
            this.setCursor(Cursor.HAND);
            this.setStyle("-fx-background-color: rgba(60, 60, 60, 0.9); -fx-background-radius: 25; -fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 25;");
        });

        this.setOnMouseExited(e -> {
            scaleDown.playFromStart();
            this.setCursor(Cursor.DEFAULT);
            // Revenim la culoarea originala
            this.setStyle("-fx-background-color: linear-gradient(to bottom right, #2b2b2b, #1f1f1f); -fx-background-radius: 25; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 25; -fx-border-width: 1;");
        });

    }


        //titlu+buton power
        protected HBox createHeader(String title, Control control) {
            Label lbl = new Label(title);
            lbl.setTextFill(Color.web("#aaaaaa"));
            lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox header = new HBox(lbl, spacer, control);

            header.setAlignment(Pos.CENTER_LEFT);
            return header;
        }

        protected ImageView loadIcon(String path, int size) {
            ImageView iv = new ImageView();
            iv.setFitWidth(size);
            iv.setFitHeight(size);
            iv.setPreserveRatio(true);
            try {
                InputStream is = getClass().getResourceAsStream(path);
                if (is != null) iv.setImage(new Image(is));
            } catch (Exception e) {
                //
            }
            return iv;
        }

        protected ToggleButton createIOSSwitch() {
            ToggleButton btn = new ToggleButton();
            btn.setPrefSize(40, 24);
            // CSS pentru aspect de switch
            String styleOff =
                    "-fx-background-color: #555; -fx-background-radius: 20; -fx-content-display: right;";
            String styleOn =
                    "-fx-background-color: #34C759; -fx-background-radius: 20; -fx-content-display: right;"; // Verde Apple

            btn.setStyle(styleOff);

            //cerc interior
            btn.setGraphic(new Circle(8, Color.WHITE));

            btn.selectedProperty().addListener((o, old, val) -> {
                btn.setStyle(val ? styleOn : styleOff);
                // Mutăm cercul dreapta/stânga (simulat prin aliniere)
                btn.getGraphic().setTranslateX(val ? 8 : -8);
            });

            // stare initiala
            btn.getGraphic().setTranslateX(-8);
            return btn;
        }

        protected void styleAppleSlider(Slider slider, String activeColor) {
            // CSS direct in cod pentru bara si buton
            slider.getStylesheets().add("data:text/css," +
                    ".slider .thumb {" +
                    "    -fx-background-color: white;" +
                    "    -fx-background-radius: 20;" +
                    "    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 5, 0, 0, 1);" +
                    "    -fx-pref-width: 24;" +
                    "    -fx-pref-height: 24;" +
                    "}" +
                    ".slider .track {" +
                    "    -fx-background-radius: 10;" +
                    "    -fx-pref-height: 8;" +
                    "}"
            );

            // Listener care coloreaza bara din stanga (efectul de fill)
            slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateSliderFill(slider, activeColor);
            });

            // rulat putin mai tarziu ca sa gaseasca .track in scena
            Platform.runLater(() -> updateSliderFill(slider, activeColor));
        }

        void updateSliderFill(Slider slider, String color) {
            // Calculez cat la suta e plin
            double max = slider.getMax();
            double min = slider.getMin();
            double val = slider.getValue();
            double percentage = (val - min) / (max - min) * 100.0;

            // Caut elementul grafic "track"
            Region track = (Region) slider.lookup(".track");
            if (track != null) {
                // Aplic un gradient: culoarea activa in stanga, gri in dreapta
                String style = String.format(
                        "-fx-background-color: linear-gradient(to right, %s %.1f%%, #3a3a3c %.1f%%); " +
                                "-fx-background-radius: 10;",
                        color, percentage, percentage
                );
                track.setStyle(style);
            }
        }

    }

/**
 * O componenta grafica personalizata (Custom Control).
 * Implementeaza un slider circular folosind matematica vectoriala (Arc Tangenta)
 * pentru a calcula unghiul mouse-ului fata de centrul cercului.
 */
class CircularSlider extends StackPane {

    // ===== CONFIG =====
    private static final double SIZE = 180;
    private static final double RADIUS = 60;
    private static final double STROKE = 8;

    // Start 225 (stanga-jos) -> Lungime 270
    private static final double START_ANGLE = 225;
    private static final double SWEEP_ANGLE = 270;

    // ===== STATE =====
    private final double min;
    private final double max;
    private double value;

    // ===== UI =====
    private final Circle track;
    private final Arc progress;
    private final Circle thumb;
    private final Label valueLabel;

    private java.util.function.Consumer<Double> onValueChanged;

    public CircularSlider(double min, double max, double initial) {
        this.min = min;
        this.max = max;
        this.value = initial;

        // Dimensiune fixa totala
        setPrefSize(SIZE, SIZE);
        setMinSize(SIZE, SIZE);
        setMaxSize(SIZE, SIZE);

        // 1. PANE PENTRU DESENE (Acesta nu muta elementele singur)
        Pane drawingPane = new Pane();
        drawingPane.setPrefSize(SIZE, SIZE);

        // Calculam centrul fix
        double cx = SIZE / 2;
        double cy = SIZE / 2;

        // 2. TRACK (Cerc Gri) - Coordonate fixe
        track = new Circle(cx, cy, RADIUS);
        track.setFill(null);
        track.setStroke(Color.web("#2c2c2e"));
        track.setStrokeWidth(STROKE);
        track.setStrokeLineCap(StrokeLineCap.ROUND);

        // 3. PROGRESS (Arc Albastru) - Coordonate fixe
        progress = new Arc();
        progress.setCenterX(cx);
        progress.setCenterY(cy);
        progress.setRadiusX(RADIUS);
        progress.setRadiusY(RADIUS);
        progress.setStartAngle(START_ANGLE);
        progress.setLength(0);

        progress.setType(ArcType.OPEN);
        progress.setFill(null); // Fara umplere
        progress.setStroke(Color.web("#0A84FF"));
        progress.setStrokeWidth(STROKE);
        progress.setStrokeLineCap(StrokeLineCap.ROUND);
        progress.setMouseTransparent(true);

        // 4. THUMB (Buton)
        thumb = new Circle(cx, cy, 10); // Initial in centru, il mutam in updateUI
        thumb.setFill(Color.WHITE);
        thumb.setStroke(Color.web("#dddddd"));
        thumb.setStrokeWidth(1);
        thumb.setEffect(new DropShadow(10, Color.BLACK));
        thumb.setMouseTransparent(true);

        // Adaugam formele in Pane-ul fix
        drawingPane.getChildren().addAll(track, progress, thumb);

        // 5. LABEL (Textul)
        valueLabel = new Label();
        valueLabel.setTextFill(Color.WHITE);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        valueLabel.setMouseTransparent(true);
        // Label-ul va fi centrat automat de StackPane (parintele)

        // Adaugam Pane-ul cu desene si Label-ul in StackPane-ul principal
        getChildren().addAll(drawingPane, valueLabel);

        // Ascultam mouse-ul pe tot containerul
        setOnMousePressed(e -> updateFromMouse(e.getX(), e.getY()));
        setOnMouseDragged(e -> updateFromMouse(e.getX(), e.getY()));

        updateUI();
    }

    private void updateFromMouse(double x, double y) {
        if (isDisabled()) return;

        double cx = SIZE / 2;
        double cy = SIZE / 2;

        double dx = x - cx;
        double dy = cy - y; // Y inversat

        // Calcul unghi
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        if (angle < 0) angle += 360;

        double relative = START_ANGLE - angle;
        if (relative < 0) relative += 360;

        if (relative > SWEEP_ANGLE + 20) relative = 0;
        if (relative > SWEEP_ANGLE) relative = SWEEP_ANGLE;

        double percent = relative / SWEEP_ANGLE;
        double newValue = min + percent * (max - min);

        newValue = Math.round(newValue * 2) / 2.0;

        if (newValue != value) {
            value = newValue;
            updateUI();
            if (onValueChanged != null) onValueChanged.accept(value);
        }
    }

    private void updateUI() {
        valueLabel.setText(String.format("%.1f°", value));

        double percent = (value - min) / (max - min);
        double arcLength = percent * SWEEP_ANGLE;

        // Lungime negativa pentru sens orar
        progress.setLength(-arcLength);

        // Calcul pozitie Thumb (folosind centrul fix al Pane-ului)
        double cx = SIZE / 2;
        double cy = SIZE / 2;

        double angleRad = Math.toRadians(START_ANGLE - arcLength);
        double thumbX = cx + RADIUS * Math.cos(angleRad);
        double thumbY = cy - RADIUS * Math.sin(angleRad); // minus pt Y

        thumb.setCenterX(thumbX);
        thumb.setCenterY(thumbY);
    }

    public void setValue(double v) {
        value = Math.max(min, Math.min(max, v));
        updateUI();
    }

    public void setOnValueChanged(java.util.function.Consumer<Double> c) {
        this.onValueChanged = c;
    }

    public void setActive(boolean active) {
        setOpacity(active ? 1.0 : 0.5);
        setDisable(!active);
        progress.setVisible(active);
        thumb.setVisible(active);
        valueLabel.setTextFill(active ? Color.WHITE : Color.GRAY);

        if (active) {
            updateUI();
        }
    }
}


    // 1. LAMPA
    class LampWidget extends BaseWidget {
        private final ImageView icon;
        private Image imgOn, imgOff;
        private final Label statusText;
        private final ToggleButton powerBtn;

        public LampWidget(HomeHub hub) {
            super(hub);

            // switch button
            powerBtn = createIOSSwitch();

            // la apasare, schimba starea in Hub
            powerBtn.setOnAction(e -> hub.toggleLights(powerBtn.isSelected()));

            getChildren().add(createHeader("Smart Lamp", powerBtn));

            //icon
            icon = loadIcon("/resources/bulb_off.png", 80);
            try {
                InputStream sOn = getClass().getResourceAsStream("/resources/bulb_on.png");
                InputStream sOff = getClass().getResourceAsStream("/resources/bulb_off.png");
                if (sOn != null) imgOn = new Image(sOn);
                if (sOff != null) imgOff = new Image(sOff);
            } catch (Exception e) {
//                e.printStackTrace();
            }

            VBox spacer = new VBox(icon);
            spacer.setAlignment(Pos.CENTER);
            VBox.setVgrow(spacer, Priority.ALWAYS); // impinge textul jos
            getChildren().add(spacer);

            //status text
            statusText = new Label("OFF");
            statusText.setTextFill(Color.WHITE);
            statusText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
            getChildren().add(statusText);
        }

        @Override
        public void update(String type, Object value) {
            if("LIGHT".equals(type)) {
                boolean on = (boolean) value;
                Platform.runLater(() -> {
                    powerBtn.setSelected(on);
                    statusText.setText(on ? "ON" : "OFF");
                    statusText.setTextFill(on ? Color.GOLD : Color.WHITE);
                    if(on && imgOn != null) {
                        icon.setImage(imgOn);
                        icon.setEffect(new DropShadow(20, Color.GOLD));
                    } else if (imgOff != null) {
                        icon.setImage(imgOff);
                        icon.setEffect(null);
                    }
                });
            }
        }
    }

    // 2. SECURITATE
    class SecurityWidget extends BaseWidget {
        private final ImageView icon;
        private Image imgLocked, imgUnlocked;
        private final Label statusText;
        private final ToggleButton lockBtn;

        public SecurityWidget(HomeHub hub) {
            super(hub);

            // switch button
            lockBtn = createIOSSwitch();
            lockBtn.setSelected(true);

            // Actiune: Cand apesi butonul, schimbi starea in Hub
            lockBtn.setOnAction(e -> hub.setDoorLocked(lockBtn.isSelected()));
            getChildren().add(createHeader("Front Door", lockBtn));

            // incarcare icon
            icon = loadIcon("/resources/lock_closed.png", 70);
            try {
                InputStream sLocked = getClass().getResourceAsStream("/resources/lock_closed.png");
                InputStream sOpen = getClass().getResourceAsStream("/resources/lock_open.png");
                if (sLocked != null) imgLocked = new Image(sLocked);
                if (sOpen != null) imgUnlocked = new Image(sOpen);
            } catch (Exception e) {
//                e.printStackTrace();
            }

            VBox centerBox = new VBox(icon);
            centerBox.setAlignment(Pos.CENTER);
            VBox.setVgrow(centerBox, Priority.ALWAYS);
            getChildren().add(centerBox);

            // text status
            statusText = new Label("SECURE");
            statusText.setTextFill(Color.web("#FF453A"));
            statusText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
            getChildren().add(statusText);
        }
            @Override
            public void update(String type, Object value){
                if("LOCKED".equals(type)){
                    boolean locked = (boolean) value;
                    Platform.runLater(() -> {
                        // actualizare buton vizual
                        lockBtn.setSelected(locked);

                        if(locked){
                            // Stare incuiat
                            if(imgLocked != null) {
                                icon.setImage(imgLocked);
                            }
                            statusText.setText("SECURE");
                            statusText.setTextFill(Color.web("#FF453A"));
                        } else{
                            // Stare descuiat
                            if(imgUnlocked != null) {
                                icon.setImage(imgUnlocked);
                                statusText.setText("UNLOCKED");
                                statusText.setTextFill(Color.web("#30D158"));
                            }
                        }
                    });
                }
            }


        }



    // 3. TERMOSTAT
    class ThermostatWidget extends BaseWidget {
        private ImageView icon;
        private Image imgCold, imgNormal, imgHot;
        private Slider tempSlider;
        private Label tempLabel;
        private ToggleButton powerBtn;

        public ThermostatWidget(HomeHub hub) {
            super(hub);

            powerBtn = createIOSSwitch();
            powerBtn.setSelected(hub.isHeatingOn());

            // Actiune: Cand apesi butonul, pornesti/opresti caldura
            powerBtn.setOnAction(e -> {
                boolean isOn = powerBtn.isSelected();
                hub.setHeatingPower(isOn);
                updateUIState(isOn); // Actualizam vizual restul widget-ului
            });

            //header
            getChildren().add(createHeader("Thermostat", powerBtn));

            //icon
            icon = loadIcon("/resources/temp_normal.png", 70);
            try {
                InputStream sCold = getClass().getResourceAsStream("/resources/temp_cold.png");
                InputStream sNormal = getClass().getResourceAsStream("/resources/temp_normal.png");
                InputStream sHot = getClass().getResourceAsStream("/resources/temp_hot.png");
                if (sCold != null) imgCold = new Image(sCold);
                if (sNormal != null) imgNormal = new Image(sNormal);
                if (sHot != null) imgHot = new Image(sHot);
            } catch (Exception e) {
//                e.printStackTrace();
            }


            // temperatura - text - stare initiala
            tempLabel = new Label("21.0 °C");
            tempLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
            tempLabel.setTextFill(Color.WHITE);

            VBox centerBox = new VBox(10, icon, tempLabel);
            centerBox.setAlignment(Pos.CENTER);
            VBox.setVgrow(centerBox, Priority.ALWAYS);
            getChildren().add(centerBox);

            //slider
            tempSlider = new Slider(16, 30,21);

            styleAppleSlider(tempSlider, "#FF9F0A");

            // Actiune: Trimite noua temperatura la Hub
            tempSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (hub.isHeatingOn()) {
                    double temp = Math.round(newVal.doubleValue() * 10.0) / 10.0;

                    // (Optional) Dubla verificare de siguranta
                    if (temp < 16.0) temp = 16.0;

                    hub.setTemperature(temp);
                }
            });

            getChildren().add(tempSlider);

            updateUIState(hub.isHeatingOn());
        }

        private void updateUIState(boolean isOn) {
            tempSlider.setDisable(!isOn); // Blocheaza slider

            if (isOn) {
                tempLabel.setOpacity(1.0);
                icon.setOpacity(1.0);
            } else {
                tempLabel.setOpacity(0.4); // Semi-transparent (inactiv)
                icon.setOpacity(0.4);
            }
        }

        @Override
        public void update(String type, Object value) {
                Platform.runLater(() -> {
            if("TEMP".equals(type)) {
                double temp = (double) value;
                // sincronizsre slider
                if (Math.abs(tempSlider.getValue() - temp) < 0.01) {
                    tempSlider.setValue(temp);
                }

                tempLabel.setText(String.format("%.1f °C", temp));

                // schimbare icon
                String activeColor;
                if (temp < 18.0) {
                    icon.setImage(imgCold);
                    tempLabel.setTextFill(Color.web("#87CEEB"));
                    activeColor = "#0A84FF"; // albastru
                } else if (temp > 24) {
                    icon.setImage(imgHot);
                    tempLabel.setTextFill(Color.web("#FF6347"));
                    activeColor = "#FF453A"; // rosu
                } else {
                    icon.setImage(imgNormal);
                    tempLabel.setTextFill(Color.WHITE);
                    activeColor = "#FFFFFF"; // alb
                }

                // actualizare culoare slider
                updateSliderFill(tempSlider, activeColor);
            }
                    // Actualizare STARE ON/OFF (Vizual butonul verde)
                    if ("HEATING".equals(type)) {
                        boolean on = (boolean) value;
                        powerBtn.setSelected(on);
                        updateUIState(on);
                    }
                });
        }
    }

    // 4. JALUZELE
    class BlindsWidget extends BaseWidget {

        public BlindsWidget(HomeHub hub) {
            super(hub);
            getChildren().add(createHeader("Blinds", new Label("")));
        }

        @Override
        public void update(String type, Object value) {}
    }

    // 5. MUZICA
    class MusicWidget extends BaseWidget {
        private ImageView coverArt;
        private Label songTitle;
        private Label artistLabel;

        private Button playBtn;
        private ImageView playBtnIconView;
        private ProgressBar progressBar;

        private Image imgPlay, imgPause;


        // PLAYER-UL JAVAFX REAl
        private MediaPlayer mediaPlayer;

        private List<Song> playlist = new ArrayList<>();
        private int currentIndex = 0;

        private record Song(String title, String artist, String colorHex, String fileName) {}

        public MusicWidget(HomeHub hub){
            super(hub);

            // INCARCARE RESURSE
            try{
                imgPlay = new Image(getClass().getResourceAsStream("/resources/play.png"));
                imgPause = new Image(getClass().getResourceAsStream("/resources/pause.png"));
            } catch(Exception e){

            }

            // DEFINIRE PLAYLIST (Adaugare muzica)
            playlist.add(new Song("Chill Vibes", "Pixabay", "#FF2D55", "song1.mp3"));
            playlist.add(new Song("Cyberpunk", "Synthwave", "#BF5AF2", "song2.mp3"));
            playlist.add(new Song("Piano Mood", "Classical", "#0A84FF",  "song3.mp3"));

            // Header
            HBox header = createHeader("Spotify Player", new Label(""));
            header.setPadding(new Insets(0, 0, 5, 0));
            getChildren().add(header);

            // UI SETUP
            coverArt = new ImageView();
            StackPane coverContainer = new StackPane();
            coverContainer.setPrefSize(70, 70);
            coverContainer.setMaxSize(70, 70);
            updateCoverStyle(playlist.get(0).colorHex, coverContainer);

            Label noteIcon = new Label("♫");
            noteIcon.setTextFill(Color.WHITE);
            noteIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
            coverContainer.getChildren().add(noteIcon);

            songTitle = new Label(playlist.get(0).title);
            songTitle.setTextFill(Color.WHITE);
            songTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));


            artistLabel = new Label(playlist.get(0).artist);
            artistLabel.setTextFill(Color.web("b3b3b3"));
            artistLabel.setFont(Font.font("Segoe UI", 12));

            VBox textBox = new VBox(4, songTitle, artistLabel);
            textBox.setAlignment(Pos.CENTER_LEFT);
            HBox contentBox = new HBox(15, coverContainer, textBox);
            contentBox.setAlignment(Pos.CENTER_LEFT);
            getChildren().add(contentBox);

            progressBar = new ProgressBar(0);
            progressBar.setMaxWidth(Double.MAX_VALUE);
            progressBar.setPrefHeight(6);
            progressBar.setStyle("-fx-accent: #1DB954; -fx-control-inner-background: #404040; -fx-text-box-border: transparent;");

            VBox progressBox = new VBox(progressBar);
            progressBox.setPadding(new Insets(10, 0, 5, 0));
            getChildren().add(progressBox);

            // Controale
            Button prevBtn = createIconMediaBtn("/resources/prev.png");
            Button nextBtn = createIconMediaBtn("/resources/next.png");

            playBtn = new Button();
            playBtn.setPrefSize(50, 50);
            playBtn.setStyle("-fx-background-color: #1DB954; -fx-background-radius: 100; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(29, 185, 84, 0.4), 10, 0, 0, 0);");

            playBtnIconView = new ImageView();
            playBtnIconView.setFitWidth(20);
            playBtnIconView.setFitHeight(20);
            if(imgPlay != null){
                playBtnIconView.setImage(imgPlay);
                playBtn.setGraphic(playBtnIconView);
            } else{
                playBtn.setText("▶");
            }

            // ACTIUNI
            loadSong(0);

            playBtn.setOnAction(e -> {
                boolean isPlaying =
                        mediaPlayer != null &&
                                mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;

                hub.setMusicPlaying(!isPlaying);
            });

            prevBtn.setOnAction(e -> changeTrack(-1));
            nextBtn.setOnAction(e -> changeTrack(1));

            HBox controls = new HBox(25, playBtn, prevBtn, nextBtn);
            controls.setAlignment(Pos.CENTER);
            controls.setPadding(new  Insets(5, 0, 0, 0));
            getChildren().add(controls);
        }

        // LOGICA AUDIO
        private void loadSong(int index){
            // 1. Oprim player-ul vechi daca exista
            if(mediaPlayer != null){
                mediaPlayer.stop();
                mediaPlayer.dispose(); // Eliberam resursele
            }

            Song song = playlist.get(index);

            try {
                String path = getClass().getResource("/resources/" + song.fileName).toExternalForm();
                javafx.scene.media.Media media = new javafx.scene.media.Media(path);

                mediaPlayer = new javafx.scene.media.MediaPlayer(media);

                mediaPlayer.setVolume(0.5);

                mediaPlayer.setOnEndOfMedia(() -> changeTrack(1));

                mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                    if(mediaPlayer.getTotalDuration() != null){
                        double progress = newTime.toMillis() / mediaPlayer.getTotalDuration().toMillis();
                        progressBar.setProgress(progress);
                    }
                });
            } catch (Exception e) {
                System.out.println("Nu s-a putut incarca fisierul audio " + song.fileName);
                e.printStackTrace();
            }
        }

        private void changeTrack(int direction) {
            currentIndex = (currentIndex + direction + playlist.size()) % playlist.size();
            Song nextSong = playlist.get(currentIndex);

            // ACTUALIZAM UI
            songTitle.setText(nextSong.title);
            artistLabel.setText(nextSong.artist);

            StackPane cover = (StackPane) ((HBox)getChildren().get(1)).getChildren().get(0);
            updateCoverStyle(nextSong.colorHex, cover);

            // Incarcam melodia noua
            boolean wasPlaying = (mediaPlayer.getStatus() == javafx.scene.media.MediaPlayer.Status.PLAYING);
            loadSong(currentIndex);

            // Daca melodia anterioara canta, o pornim si pe asta automat
            if(wasPlaying){
                mediaPlayer.play();
            } else {
                // Resetam bara daca suntem pe pauza
                progressBar.setProgress(0);
            }
        }

        private void updateCoverStyle(String color, StackPane cover) {
            cover.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 5);");
        }

        // Helper butoane (acelasi ca inainte)

        private Button createIconMediaBtn(String iconPath) {
            Button b = new  Button();
            b.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            try {
                ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
                iv.setFitWidth(24);
                iv.setFitHeight(24);
                b.setGraphic(iv);
            } catch (Exception e) {b.setText("?"); b.setTextFill(Color.WHITE);}
            b.setOnMouseEntered(e -> b.setOpacity(0.7));
            b.setOnMouseExited(e -> b.setOpacity(1));
            return b;
        }

        @Override
        public void update(String type, Object value) {
            if (!"MUSIC".equals(type)) return;

            boolean shouldPlay = (boolean) value;

            Platform.runLater(() -> {
                if (mediaPlayer == null) return;

                if (shouldPlay) {
                    mediaPlayer.play();
                    if (imgPause != null)
                        playBtnIconView.setImage(imgPause);
                } else {
                    mediaPlayer.pause();
                    if (imgPlay != null)
                        playBtnIconView.setImage(imgPlay);
                }
            });
        }
    }


    // 6. AER CONDITIONAT
    class AirConditionerWidget extends BaseWidget {
        private ToggleButton powerBtn;
        private Label statusLabel;
        private CircularSlider tempKnob;
        private ImageView icon;

        private Button btnLow, btnMed, btnHigh;
        private HBox fanContainer;

        public AirConditionerWidget(HomeHub hub) {
            super(hub);

            this.setPrefSize(240, 505);
            this.setAlignment(Pos.TOP_CENTER);
//            this.setSpacing(30);

            //header
            powerBtn = createIOSSwitch();
            powerBtn.setOnAction(e -> hub.setAcPower(powerBtn.isSelected()));
            getChildren().add(createHeader("Air Conditioner", powerBtn));

            //icon
            icon = loadIcon("/resources/air_conditioner.png", 80);
            icon.setOpacity(0.3);


            if (icon.getImage() == null) {}
            VBox iconContainer = new VBox(icon);
            iconContainer.setAlignment(Pos.CENTER);
            iconContainer.setPadding(new Insets(5, 0, 5, 0)); // Spatiu sus/jos
            getChildren().add(iconContainer);

            // Setam min: 16 grade, max: 30 grade, initial: 22
            tempKnob = new CircularSlider(16, 30, 22);

            // Cand invartim de el, anuntam Hub-ul
            tempKnob.setOnValueChanged(val -> {
                hub.setAcTemperature(val);
            });

            tempKnob.setDisable(true);
            tempKnob.setActive(false);

            // container pentru centrul knob-ului
            VBox knobContainer = new VBox(tempKnob);
            knobContainer.setAlignment(Pos.CENTER);
            VBox.setVgrow(knobContainer, Priority.ALWAYS);
            getChildren().add(knobContainer);

            //fan speed
            Label fanLabel = new Label("Fan Speed");
            fanLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            fanLabel.setTextFill(Color.GRAY);

            // butoane fanSpeed
            btnLow = createFanBtn("LO", 1, hub);
            btnMed = createFanBtn("MED", 2, hub);
            btnHigh = createFanBtn("HI", 3, hub);


            fanContainer = new HBox(10, btnLow, btnMed, btnHigh);
            fanContainer.setAlignment(Pos.CENTER);
            fanContainer.setDisable(true); // Initial dezactivat (AC e oprit)
            fanContainer.setOpacity(0.5);

            VBox fanBox = new VBox(5, fanLabel, fanContainer);
            fanBox.setAlignment(Pos.CENTER);
            getChildren().add(fanBox);

            //status text
            statusLabel = new Label("OFF");
            statusLabel.setTextFill(Color.GRAY);
            statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

            VBox footer = new VBox(statusLabel);
            footer.setAlignment(Pos.CENTER);
            getChildren().add(footer);

            // Setare initiala UI
            updateFanUI(1);
        }

        // Helper pentru crearea butoanelor de ventilator
        private Button createFanBtn(String text, int level, HomeHub hub) {
            Button b = new Button(text);
            b.setPrefSize(50, 30);
            b.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
            b.setCursor(Cursor.HAND);

            // Actiune: Trimite viteza la Hub
            b.setOnAction(e -> hub.setFanSpeed(level));

            return b;
        }

        // Functie care coloreaza butoanele (Albastru cel activ, Gri restul)
        private void updateFanUI(int activeLevel) {
            String activeStyle = "-fx-background-color: #0A84FF; -fx-text-fill: white; -fx-background-radius: 15;";
            String inactiveStyle = "-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: gray; -fx-background-radius: 15;";

            btnLow.setStyle(activeLevel == 1 ? activeStyle : inactiveStyle);
            btnMed.setStyle(activeLevel == 2 ? activeStyle : inactiveStyle);
            btnHigh.setStyle(activeLevel == 3 ? activeStyle : inactiveStyle);
        }


        @Override
        public void update(String type, Object value) {
            if ("AC_POWER".equals(type)) {
                boolean on = (boolean) value;
                Platform.runLater(() -> {
                    // actualizare buton power
                    powerBtn.setSelected(on);
                    tempKnob.setActive(on);

                    // activare/dezactivare UI
                    fanContainer.setDisable(!on);
                    fanContainer.setOpacity(on ? 1.0 : 0.5);

                    // actualizare status text si icon
                    if (on) {
                        statusLabel.setText("COOLING to " + hub.getAcTemperature() + "°");
                        statusLabel.setTextFill(Color.web("#0A84FF"));

                        icon.setOpacity(1.0);
                        icon.setEffect(new DropShadow(10, Color.web("#0A84FF")));
                    } else {
                        statusLabel.setText("OFF");
                        statusLabel.setTextFill(Color.GRAY);

                        icon.setOpacity(0.3);
                        icon.setEffect(null);
                    }
                });
            } else if ("AC_TEMP".equals(type)) {
                double temp = (double) value;
                // actualizare knob si text
                Platform.runLater(() -> {
                    tempKnob.setValue(temp);

                    if (powerBtn.isSelected()) {
                        statusLabel.setText("COOLING to " + temp + "°");
                    }
                });
            } else if("AC_FAN".equals(type)) Platform.runLater(() -> {
                // actualizare butoane fan
                int speed = (int) value;
                updateFanUI(speed);
            });
        }
    }

        // LOGURI
        class LogWidget extends BaseWidget {
            private final ListView<String> list;

            public LogWidget(HomeHub hub) {
                super(hub);

                getChildren().add(createHeader("Logs Observer", new Label("")));
                list = new ListView<>();
                //lista transparenta și fara border
                list.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
                VBox.setVgrow(list, Priority.ALWAYS);
                getChildren().add(list);
            }

            @Override
            public void update(String type, Object val) {
                String msg = "> " + type + ": " + val;
                Platform.runLater(() -> list.getItems().addFirst(msg));
            }
        }

