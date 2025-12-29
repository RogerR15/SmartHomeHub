# Proiect PIP - Smart Home Hub (Observer Pattern)

**Echipa:** Robert Popa, Francesco Fotache
**Grupa:** 1304B

---

## 📝 Descriere
Acesta este un panou de control pentru o casă inteligentă (Smart Home Dashboard) realizat în **JavaFX**.
Proiectul demonstrează utilizarea **Design Pattern-ului Observer**: hub-ul central (`Subject`) notifică automat widget-urile (`Observers` - ex: Aer Condiționat, Jaluzele, Temperatură) atunci când starea sistemului se schimbă.

## 🛠️ Cerințe de Sistem
* **Java JDK:** Versiunea 17 sau 21.
* **IDE:** IntelliJ IDEA (recomandat) sau Eclipse.
* **Build System:** Maven (inclus în proiect).

---

## 🚀 Instrucțiuni de Rulare (Simplificat)

Acest proiect este configurat folosind **Maven**, deci nu necesită descărcarea manuală a JavaFX SDK și nici configurarea parametrilor VM.

### Pasul 1: Deschiderea Proiectului
1.  Deschideți IntelliJ IDEA.
2.  Selectați **File -> Open** și alegeți folderul `SmartHomeHub` (cel care conține fișierul `pom.xml`).
3.  Așteptați câteva secunde ca Maven să descarce automat dependențele necesare (JavaFX Controls, FXML).

### Pasul 2: Pornirea Aplicației
Din cauza restricțiilor moderne Java, vă rugăm să rulați aplicația folosind clasa ajutătoare `Launcher` pentru a evita erorile de tip "Runtime components missing".

1.  Navigați în folderul: `src/main/java/smarthome/`
2.  Dă click dreapta pe clasa **`MainApp.java`**.
3.  Selectați **Run**.

*(Nu este necesară nicio configurare de VM Options sau Path)*.

---

## ✨ Funcționalități Cheie
* **Custom Title Bar:** Bară de titlu personalizată (Dark Mode) cu butoane vectoriale (SVG) funcționale pentru Minimize, Maximize și Close.
* **Navigare Sigură:** Sistemul păstrează bara de titlu intactă la navigarea între Dashboard și Setări.
* **Setări Persistente:** Modificarea numelui utilizatorului, orașului sau a pozei de profil se reflectă instantaneu în Dashboard.
* **Smart Crop:** Algoritm care centrează și decupează automat poza de profil în format circular.