package smarthome;

//Acesta este contractul care asigura decuplarea intre Hub și Dispozitive.


/*
    Interfata OBSERVER - defineste regula pe care trebuie sa o respecte orice dispozitiv
                       - asigura decuplarea: Hub-ul nu depinde de clasele concrete
                      (Lampa, Termostat, etc), ci doar de interfata SmartObserver
 */

public interface SmartObserver {
    // Metoda prin care Hub-ul trimite notificari.
    // type = ce s-a schimbat (ex: "TEMP", "LIGHT")
    // value = noua valoare

    void update(String type, Object value);
}