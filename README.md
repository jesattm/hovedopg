# hovedopg
My main assignment on the 5th and last semester of Computer Science on EAAA.

Below are excerpts in danish from my paper describing the system.


## Synopsis
Cordulus’ webservice Tycho håndterer vejrstationer og deres tilknyttede brugere. Servicen benytter sig af Event Sourcing, hvor systemets tilstand gemmes som en sekvens af hændelser. Denne tilgang giver problemer med zero-downtime deployments, samt mindsker genbrugeligheden af tilstanden på tværs af services. Min opgave har været at flytte den udledte tilstand af event stream’en fra hukommelsen ud i en ny ekstern service’s MySQL database. Resultatet er Tycho genskabt som en webservice med en State-Based arkitektur. 

### Opgavebeskrivelse
Cordulus tilbyder deres kunder en service, hvor de kan købe et abonnement og hente hyperlokale vejrdata fra en lokation, som kunden selv vælger. Servicen hedder Cordulus Farm. Kunden lejer en vejrstation, som de kan installere f.eks. ved en mark. Kunden får derefter adgang til vejrdata fra deres egen samt andre vejrstationer gennem en app. 
En organisation (landmand eller lignende) har en konto, som kan holde på en enhed (vejrstation). Organisationen ejer ikke enheden, men gennem kontoen har den adgang til den data som enheden genererer. Hvis en enhed går ned, skal den erstattes af en ny enhed. Kontoen skal nu tilføje den nye enhed og fjerne den gamle. Selvom det er to forskellige fysiske enheder, skal datahistorikken vises som om det er én samlet enhed. Systemet, som indeholder denne logik, hedder Tycho. 

### Opgavens formål
Lige nu bruger man Event Sourcing i Tycho. Man har en Store i hukommelsen, hvor der skabes den aktuelle data-tilstand ud fra events. Denne Store har det problem at data kan gå tabt, hvis en ny instans af Tycho skal startes op. Når der er to samtidige instanser af Tycho, bliver kald til Tycho fordelt mellem de to instanser via Load Balancing i AWS. En mængde data lagres i hukommelsen i hvert system. Hvis f.eks. et kald til Tycho ved en fejl duplikeres og de samme to kald balanceres ud til begge systemer, er der ikke nogen måde for systemerne at håndtere denne duplikeringsfejl. Derfor vil Cordulus gerne gå fra lagring i hukommelsen, til at bruge persistent lagring i form af en database. 

## Databasemodel
Nedenstående script beskriver min databasemodel.
```
CREATE TABLE accounts (
  id CHAR(36) NOT NULL,
  api_key CHAR(36) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE devices (
  id CHAR(36) NOT NULL,
  account_id CHAR(36) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT devices_fk_account_id FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE
);

CREATE TABLE holds (
  id INT(11) NOT NULL AUTO_INCREMENT,
  device_id CHAR(36) NOT NULL,
  label CHAR(8) NOT NULL,
  imei CHAR(15) NOT NULL,
  start DATETIME NOT NULL,
  end DATETIME,
  PRIMARY KEY (id),
  CONSTRAINT holds_fk_device_id FOREIGN KEY (device_id) REFERENCES devices (id) ON DELETE CASCADE
);
```
