# A Lion in the West
Western third person shooter.

# Compilare
Prima di importare il progetto dentro Eclipse, bisogna installare un estensione per utilizzare Gradle.
``` bash
Help > Eclipse Marketplace...

Nella barra di ricerca, cercate "Gradle" e installate la seconda che vi esce (quella con il logo di un elefante blu)
```

Dopo aver installato l'estensione e riavviato Eclipse, possiamo importare il progetto.
``` bash
File > Import... > Gradle > Existing Gradle Project
e selezioni la cartella del progetto
```

Una volta importato il progetto, dovrebbero apparirvi degli errori, perche' la versione di java del progetto e quella cghe avete installato, non coincidono.
Se questo e' il caso, modificate i file 'build.gradle' nella cartella principale e nelle sotto cartelle: 'lwjgl3' e 'teavm'.

``` bash
Cercate le variabili:
sourceCompatibility = LA VOSTRA VERSIONE DI JAVA
targetCompatibility = LA VOSTRA VERSIONE DI JAVA
```

Se non vi da' altri errori, siete pronti per eseguire il progetto.
