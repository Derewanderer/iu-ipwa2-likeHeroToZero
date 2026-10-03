# Like Hero To Zero

Ein lokal lauffaehiger Prototyp fuer die Fallstudie IPWA02-01. Die Anwendung zeigt oeffentlich den jeweils neuesten gespeicherten CO2-Datensatz eines Landes und bietet angemeldeten Wissenschaftler:innen das Erfassen und Korrigieren von Daten.

## Techstack

- Java 17 oder neuer und Maven 3.9+
- Spring Boot 3.4, Spring MVC/Beans und Thymeleaf
- Spring Security fuer den geschuetzten Wissenschaftler:innenbereich
- Spring Data JPA und Hibernate
- H2 als relationale, dateibasierte Datenbank (keine Docker-Installation erforderlich)

## Lokal unter Windows starten

1. JDK 17+ und Maven 3.9+ installieren und pruefen:

   ```powershell
   java -version
   mvn -version
   ```

2. Im Projektordner die Anwendung starten:

   ```powershell
   mvn spring-boot:run
   ```

3. Im Browser `http://localhost:8080` oeffnen.

Die lokale Demo-Anmeldung lautet `scientist` / `local-demo-change-me`. Vor einem Einsatz ausserhalb der lokalen Demo ein eigenes Passwort setzen und nicht in Git speichern:

```powershell
$env:DEMO_PASSWORD = "Ein-lokales-eigenes-Passwort"
mvn spring-boot:run
```

Die H2-Datenbank wird unter `data/likehero.mv.db` angelegt und bleibt nach einem Neustart erhalten. Sie ist nicht im Repository eingecheckt. Zum Zuruecksetzen der lokalen Datenbank die Anwendung beenden und die Datei gezielt loeschen.

## Funktionen

- Oeffentliche Laenderauswahl und Anzeige des neuesten gespeicherten CO2-Werts samt Jahr
- Geschuetzte Anmeldung fuer Wissenschaftler:innen
- Daten manuell hinzufuegen und vorhandene Land-Jahr-Datensaetze korrigieren
- Import der Zeitreihe fuer ein Land ueber die World-Bank-API
- Offline-Demo mit den mitgelieferten World-Bank-Startdaten fuer Kanada, China, Deutschland, Frankreich und die USA

Im Wissenschaftler:innenbereich muss fuer den Online-Import der ISO-3-Laendercode angegeben werden, zum Beispiel `DEU`. Die Anwendung importiert die verfuegbaren Daten fuer dieses Land und aktualisiert bereits vorhandene Land-Jahr-Eintraege.

## Datenquelle und Einheiten

Die mitgelieferte Offline-Demodatei stammt aus der World-Bank-API. Das fruehere World-Development-Indicators-Kennzeichen `EN.ATM.CO2E.KT` ist im aktuellen API-Datenbestand archiviert. Deshalb verwendet dieser Prototyp den derzeit aktiven World-Bank-Indikator `EN.GHG.CO2.MT.CE.AR5` (CO2-Emissionen ohne LULUCF, in Mt CO2e) und rechnet die Werte fuer die Anzeige in kt CO2e um. Die Messdefinition ist damit nicht identisch mit der historischen CO2-kt-Zeitreihe; diese Einschraenkung sollte bei der Interpretation und im Fallstudienbericht genannt werden.

- Daten-API: [World Bank Indicator API](https://api.worldbank.org/v2/country/DEU/indicator/EN.GHG.CO2.MT.CE.AR5?format=json)
- Indikator-Metadaten: [World Bank Indicator EN.GHG.CO2.MT.CE.AR5](https://api.worldbank.org/v2/indicator/EN.GHG.CO2.MT.CE.AR5?format=json)
- Datenbankquelle und Aktualisierungsstand des aktuellen API-Bestands werden in der Anwendung mit den importierten Datensaetzen angezeigt.

## Projekt pruefen

```powershell
mvn test
```

Der Prototyp enthaelt bewusst nur die beiden MUST-Funktionen aus der Fallstudie. Ein Freigabeworkflow fuer Aenderungen (COULD) ist nicht umgesetzt.
