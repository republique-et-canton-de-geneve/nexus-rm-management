# 6799-nexus-rm-management : une console supplémentaire de gestion pour Nexus RM

La console de gestion de Nexus RM est notoirement insuffisante.
Cette application met œuvre des fonctionnalités que la console de gestion de
Nexus RM n'inclut pas.

## 1) Fonctionnalités

(À FAIRE)

## 2) Construction de l'application 

```
mvn clean install
```

## 3) Exécution de l'application

### Exécution en local

Au choix :

soit
```
mvn spring-boot:run
```

soit
```
mvn clean package -DskipTests
$JAVA_HOME/bin/java -jar target/police-integration-edemarches-<VERSION>.jar 
```

### Exécution sur un serveur Linux

(À FAIRE)
