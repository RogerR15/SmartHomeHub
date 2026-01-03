package smarthome;

//Acesta este contractul care asigura decuplarea intre Hub și Dispozitive.


/**
 * Interfata SmartObserver - Contractul pe care il respecta toti observatorii.
 * * Rol in Design Pattern: OBSERVER INTERFACE
 * Aceasta permite decuplarea: HomeHub nu stie ca vorbeste cu un "ThermostatWidget" sau "LampWidget",
 * ci doar cu un "SmartObserver". Astfel, putem adauga widget-uri noi fara sa modificam codul Hub-ului.
 */

public interface SmartObserver {
    // Metoda prin care Hub-ul trimite notificari.
    // type = ce s-a schimbat (ex: "TEMP", "LIGHT")
    // value = noua valoare

    void update(String type, Object value);
}