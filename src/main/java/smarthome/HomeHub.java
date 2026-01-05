package smarthome;

/**
 * Clasa HomeHub - Reprezinta starea centrala a sistemului.
 * * Rol in Design Pattern: SUBJECT (SUBIECT)
 * Aceasta clasa detine datele (temperatura, lumini, etc.) si o lista de observatori.
 * Atunci cand o stare se modifica (ex: setTemperature), metoda notifyObservers()
 * este apelata pentru a anunta toate widget-urile sa se actualizeze.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class HomeHub {

    private static final String API_KEY = "4ee001a6550871897d4d45bc287c4491";
    private static final String API_URL_TEMPLATE = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric";

    private String userName = "Yasmina"; // Default

    private String profileImagePath = "/resources/yasmina.png";

    //lista privata de observatori
    private List<SmartObserver> observers = new ArrayList<>();



    //starea interna a casei
    private double temperature = 21.0;
    private boolean heatingOn = true;
    private boolean lightsOn = false;
    private boolean isLocked = true;
    private int blindsLevel = 0;
    private boolean musicPlaying = false;
    private boolean acPower = false;
    private double acTemp = 22.0;
    private int fanSpeed = 1;

    private String city = "Iasi"; // Orasul default
    private double outsideTemp = 0.0;

    // IMPLEMENTARE OBSERVER PATTERN
    public void attach(SmartObserver o) {
        observers.add(o);
    }



    /**
     * Parcurge lista de observatori si apeleaza metoda update() a fiecaruia.
     * @param type Tipul evenimentului (ex: "TEMP", "LIGHT") pentru filtrare eficienta.
     * @param value Noua valoare a starii.
     */
    private void notifyObservers(String type, Object value) {
        for (SmartObserver o : observers) {
            o.update(type, value);
        }
    }

    // GETTERS
    public double getTemperature() {
        return temperature;
    }
    public String getUserName() { return userName; }
    public double getAcTemperature() {
        return acTemp;
    }
    public int getFanSpeed() { return fanSpeed; }
    public String getCity() { return city; }
    public double getOutsideTemp() { return outsideTemp; }
    public boolean isHeatingOn() {
        return heatingOn;
    }
    public String getProfileImagePath() {
        return profileImagePath;
    }



    // SETTERS - declanseaza notificarile
    public void setUserName(String name) {
        this.userName = name;
        notifyObservers("USER", name);
    }

    public void toggleLights(boolean status) {
        this.lightsOn = status;
        notifyObservers("LIGHT", status);
    }

    public void setDoorLocked(boolean locked) {
        this.isLocked = locked;
        notifyObservers("LOCKED", locked);
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
        notifyObservers("TEMP", temperature);
    }


    public void setHeatingPower(boolean on) {
        this.heatingOn = on;
        notifyObservers("HEATING", on);
    }

    public void setAcPower(boolean on) {
        this.acPower = on;
        notifyObservers("AC_POWER", on);
    }

    public void setAcTemperature(double t) {
        this.acTemp = t;
        notifyObservers("AC_TEMP", t);
    }

    public void setFanSpeed(int speed) {
        this.fanSpeed = speed;
        notifyObservers("AC_FAN", speed);
    }

    public void setBlindsLevel(int level) {
        this.blindsLevel = level;
        notifyObservers("BLINDS", level);
    }

    public void setMusicPlaying(boolean playing) {
        this.musicPlaying = playing;
        notifyObservers("MUSIC", playing);
    }

    public void setCity(String newCity) {
        this.city = newCity;
        // Cand utilizatorul schimba orasul, apelam API-ul din nou
        System.out.println("LOG: Oras schimbat in " + newCity + ". Caut vremea...");
        fetchRealWeather(newCity);
    }

    public void setProfileImagePath(String path) {
        this.profileImagePath = path;
        notifyObservers("PROFILE", path);
    }


    // METHODS
    /**
     * Realizeaza un apel HTTP asincron (pe un alt thread) catre OpenWeatherMap API.
     * Este crucial sa rulam pe un thread separat pentru a nu ingheta interfata grafica (JavaFX Application Thread).
     */
    private void fetchRealWeather(String cityName) {
        //rulam intr-un thread separat pentru a nu bloca interfata
        System.out.println("DEBUG: Pregatesc firul de executie pentru " + cityName);
        new Thread(() -> {
           try{

               //construim URL-ul pentru API
               String cleanCity = cityName.trim().replace(" ", "%20");
               String urlString = String.format(API_URL_TEMPLATE, cleanCity, API_KEY);
               System.out.println("LOG: Conectare la URL: " + urlString);

               URL url = new URI(urlString).toURL();

               System.out.println("DEBUG: Deschid conexiunea HTTP...");
               //deschidem conexiunea
               HttpURLConnection conn = (HttpURLConnection) url.openConnection();
               conn.setRequestMethod("GET");
               conn.setConnectTimeout(5000); // Timeout 5 secunde
               conn.setReadTimeout(5000);

               System.out.println("DEBUG: Astept raspunsul serverului...");

               int responseCode = conn.getResponseCode();
               System.out.println("LOG: Cod Raspuns Server: " + responseCode);
               if(responseCode == 200) {
                    //200 = OK
                    //citim raspunsul
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();

                    //extragem temperatura din JSON
                    String json = content.toString();
                    System.out.println("LOG: JSON Primit: " + json); // Vedem exact ce primim

                    //temperatura
                    double temp = parseTempSmart(json);

                    //conditia
                    String condition = parseWeatherCondition(json);

                    //actualizam starea interna si notificam observatorii
                    this.outsideTemp = temp;
                    notifyObservers("CITY", cityName);

                   notifyObservers("WEATHER_ICON", condition);

                    System.out.println("LOG: Temperatura extrasa cu succes: " + temp);
                    System.out.println("Vremea actualizata pentru " + cityName + ": " + temp);

                } else {
                    System.out.println("LOG: Eroare! Serverul a raspuns cu codul " + responseCode);
                }
           } catch (Exception e) {
               System.out.println("Error fetching weather data: " + e.getMessage());
//               e.printStackTrace();
           }
        }).start();
    }

    // Helper mic pentru a extrage "temp":XX.X din textul JSON fara librarii externe
    private double parseTempSmart(String json) {
        try {
            // Impartim textul dupa cuvantul "temp"
            String[] parts = json.split("\"temp\"");
            if (parts.length > 1) {
                // Luam partea de dupa "temp" (ex: ":2.5,"...)
                String after = parts[1];

                // Cautam unde incepe numarul (primul digit sau minus)
                int startIndex = -1;
                for (int i = 0; i < after.length(); i++) {
                    char c = after.charAt(i);
                    if (Character.isDigit(c) || c == '-') {
                        startIndex = i;
                        break;
                    }
                }

                if (startIndex != -1) {
                    // Cautam unde se termina numarul (virgula sau acolada)
                    int endIndex = startIndex + 1;
                    while (endIndex < after.length()) {
                        char c = after.charAt(endIndex);
                        // Daca e cifra sau punct, continuam. Daca e altceva, stop.
                        if (Character.isDigit(c) || c == '.') {
                            endIndex++;
                        } else {
                            break;
                        }
                    }

                    String numStr = after.substring(startIndex, endIndex);
                    return Double.parseDouble(numStr);
                }
            }
        } catch (Exception e) {
            System.out.println("LOG: Nu am putut citi numarul din JSON.");
        }
        return 0.0; // Fallback
    }


    private String parseWeatherCondition(String json) {
        try {
            // JSON-ul arata cam asa: "weather":[{"id":800,"main":"Clear", ...
            // Cautam prima aparitie a lui "main":"
            // ATENTIE: Exista un "main" si la temperatura. Cel de vreme e primul in lista de obicei,
            // dar ca sa fim siguri, cautam intai "weather"

            int weatherIndex = json.indexOf("\"weather\"");
            if (weatherIndex != -1) {
                String subJson = json.substring(weatherIndex); // Taiem tot ce e inainte de weather

                String searchKey = "\"main\":\"";
                int mainIndex = subJson.indexOf(searchKey);

                if (mainIndex != -1) {
                    int start = mainIndex + searchKey.length();
                    int end = subJson.indexOf("\"", start);

                    if (end != -1) {
                        return subJson.substring(start, end); // Returneaza "Clouds", "Clear", "Rain" etc.
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Eroare parsare conditie meteo: " + e.getMessage());
        }
        return "Clear"; // Default daca nu gasim
    }

}