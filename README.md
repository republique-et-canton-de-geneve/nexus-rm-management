# 6799-nexus-rm-management : une console supplémentaire d'administration pour Nexus RM

La console d'administration de Nexus RM est notoirement insuffisante.
Cette application-ci met œuvre des fonctionnalités que la console d'administration de
Nexus RM n'inclut pas.

The application requires Java 21+.

## 1) Fonctionnalités

(À FAIRE)

## 2) Building the application

```
mvn clean install
```

## 3) Running the application locally

### Configuring the application: property file

Do the following:
- Go to directory `src/main/resources`
- Copy file `application-base.yml` (this file is under Git control)
  to a new file `application.yml` (this file is under Git ignore),
  in the same directory
- Edit file `application.yml`, provide the missing values;
  their value is `TO_BE_PROVIDED`.
  For the particular case of `trustStorePassword`, don't do anything yet

### Configuring the application: trust store

At État de Genève we unfortunately use self-signed certificates, so a trust
store must be provided for the HTTPS interaction between the application and
the Nexus RM server.

Do the following:
- Get a trust store file
  At État de Genève it is usually a `gina.jks` file
- Copy the file to directory `src/main/resources/security`
- Edit file `application.yml` again and set the password to the file

### Running locally

There are two ways to do so.

#### Maven

First, run
```
mvn spring-boot:run
```
to get the required parameters printed.

Then run
```
mvn spring-boot:run -Dspring-boot.run.arguments=<ARGS>
```
where `<ARGS>` is, for instance, `certificate`.

### JAR

First, run
```
$JAVA_HOME/bin/java -jar target/nexus-rm-management-<VERSION>.jar
```
to get the required parameters printed.

Then run
```
$JAVA_HOME/bin/java -jar target/nexus-rm-management-<VERSION>.jar <ARGS>
```
where `<ARGS>` is, for instance, `certificate`.

## 4) Running the application on a Linux server

TODO
