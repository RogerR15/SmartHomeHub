# 🏠 Smart Home Hub (Observer Pattern)

![Java](https://img.shields.io/badge/Java-21-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue)
![Build](https://img.shields.io/badge/Build-Maven-brightgreen)
![License](https://img.shields.io/badge/License-Educational-lightgrey)

**Proiect pentru disciplina:** Programare independentă de platformă (PIP)
**Echipa:** Robert Popa, Francesco Fotache
**Grupa:** 1304B

---

## 📝 Descriere

Acesta este un panou de control centralizat pentru o casă inteligentă (**Smart Home Dashboard**), dezvoltat integral în **Java + JavaFX**.

Obiectivul principal al proiectului este demonstrarea practică a **Design Pattern-ului Observer**. Arhitectura aplicației este decuplată:
* **Subiectul (`HomeHub`):** Gestionează starea datelor (temperatură, lumini, muzică, securitate etc.).
* **Observatorii (`Widgets`):** Componente UI independente care se abonează la Hub și reacționează automat la notificări.

---

## 🏗️ Arhitectură și SOLID

Aplicația respectă principiile **SOLID** și bunele practici de programare orientată pe obiecte:

### 📐 Aplicarea Principiilor SOLID
* **S - Single Responsibility:** `HomeHub` gestionează exclusiv logica de business și datele, în timp ce Widget-urile se ocupă doar de afișarea grafică.
* **O - Open/Closed:** Sistemul permite adăugarea de noi tipuri de widget-uri (extensie) fără a modifica codul sursă al Hub-ului (închis modificării logicii de notificare).
* **L - Liskov Substitution:** Orice clasă care extinde `BaseWidget` poate fi utilizată în interfață fără a afecta funcționarea aplicației.
* **I - Interface Segregation:** Interfața `SmartObserver` conține o singură metodă esențială (`update`), fără a forța observatorii să implementeze funcționalități inutile.
* **D - Dependency Inversion:** `HomeHub` (nivel înalt) nu depinde de clase concrete (ex: `LampWidget`), ci de abstracția `SmartObserver`.

### 🧩 Componente Principale
1.  **HomeHub (Model/Subject):** Nu conține cod UI. Notifică observatorii doar când starea se schimbă.
2.  **Widgets (View/Observers):** Implementează interfața `SmartObserver`. Exemple:
    * *AirConditionerWidget* (Control complex cu slider circular).
    * *MusicWidget* (Player audio complet funcțional).
    * *BlindsWidget* (Vizualizare efect luminozitate).
3.  **MainApp (Client):** Configurează scena și realizează legăturile (`hub.attach(widget)`).

> **Notă:** Proiectul include apeluri asincrone (Multithreading) pentru preluarea datelor meteo reale de la OpenWeatherMap API, fără a bloca interfața grafică.

---

## 🛠️ Cerințe de Sistem

Pentru a rula proiectul, asigurați-vă că aveți instalate:

* **Java JDK:** Versiunea 17 sau 21.
* **IDE:** IntelliJ IDEA (Recomandat) sau Eclipse.
* **Build System:** Maven (inclus și configurat în `pom.xml`).

---

## 🚀 Instrucțiuni de Rulare

Acest proiect este configurat folosind **Maven**, care gestionează automat dependențele JavaFX.

### Metoda 1: Rulare prin Maven (RECOMANDAT)
Aceasta este metoda standard și cea mai sigură. Proiectul este configurat să ruleze direct clasa principală `smarthome.MainApp`.

1.  În Eclipse/IntelliJ, dați click dreapta pe proiect.
2.  Selectați **Run As -> Maven Build...**
3.  La "Goals" introduceți: `clean javafx:run`
4.  Apăsați **Run**.

### Metoda 2: Rulare Manuală din IDE (Click Dreapta -> Run)
Dacă doriți să rulați aplicația **fără** a folosi comanda Maven (ex: pentru debugging rapid):

1.  Navigați în folderul `src/main/java/smarthome/`.
2.  Rulați clasa **`Launcher.java`** (Click Dreapta -> Run As Java Application).

> **Notă:** Pentru rularea manuală (Metoda 2), este necesar să folosiți `Launcher` și nu `MainApp` pentru a evita erorile legate de Java Modules (ex: *"Runtime components are missing"*).

---

## ✨ Funcționalități Cheie

### 🎨 Interfață Grafică (UI/UX)
* **Design Modern:** Stil "Dark Mode" cu elemente inspirate din iOS (Apple HomeKit).
* **Custom Title Bar:** Bară de titlu personalizată cu butoane vectoriale (SVG) funcționale (Minimize, Maximize, Close), integrată perfect în design.
* **Navigare Fluidă:** Trecerea între Dashboard și Setări se face păstrând starea ferestrei (poziție, dimensiune).

### ⚙️ Funcționalități Smart
* **Scene Automate:** Butoane rapide ("Morning", "Cinema", "Away") care modifică simultan luminile, temperatura și jaluzelele.
* **Real-time Weather:** Integrare cu API extern pentru afișarea temperaturii reale din orașul selectat.
* **Profil Utilizator:**
    * Posibilitatea de a schimba numele și orașul.
    * **Smart Crop:** Algoritm care procesează orice imagine încărcată și o decupează automat în format circular perfect pentru avatar.

### 📱 Widget-uri Implementate
1.  **Termostat:** Control temperatură cu slider colorat dinamic.
2.  **Muzică:** Player cu listă de redare, bară de progres și coperți.
3.  **Aer Condiționat:** Slider circular custom (trigonometrie) pentru setarea temperaturii.
4.  **Securitate & Lumini:** Comutatoare on/off cu feedback vizual instantaneu.
5.  **Jaluzele:** Vizualizare grafică a nivelului de deschidere a ferestrei.

---

## 📂 Structura Proiectului

```text
SmartHomeHub/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── smarthome/
│   │   │       ├── Launcher.java      # Entry point (Fix pentru module)
│   │   │       ├── MainApp.java       # Configurare GUI
│   │   │       ├── HomeHub.java       # Subiectul (Backend Logic)
│   │   │       ├── SmartObserver.java # Interfața Observer
│   │   │       └── Widgets.java       # Toate clasele Widget (Observers)
│   │   │       
│   │   └── resources/
│   │       ├── resources/             # Imagini, iconițe și fișiere audio
│   │       └── style.css              # Stilizare CSS
├── pom.xml                            # Configurare Maven
└── README.md                          # Documentație