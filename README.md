# 6799-nexus-rm-management : an additional admin console for Nexus RM

The Nexus RM administration is insufficient.
This application implements features that the Nexus RM
administration console does not include.

The application requires Java 21+.

## 1) Features


| Name                               |                                                                                                                                                             Description                                                                                                                                                              |                          Arguments | Example                                                         |
|:-----------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|-----------------------------------:|-----------------------------------------------------------------|
| Extraction of expired certificates |                                                     This program will check all certificates expiry date and return all the certificates that are already expired and those who will be in the next month. the return will be in a CSV file and sorted by most recent to oldest.                                                     |                   1 or certificate | certificate                                                     |
| Extraction of heavy components     |                                                                                                   This program will check all components assets size, and write all the assets that have a size superior to 5 Mo into a CSV file.                                                                                                    |2 or heavyComponents | heavyComponents                                                 |
| Extraction of user permissions                              |                                                                                                                                   This program will retrieve the permissions for a specified user.                                                                                                                                   |        3 or permissions and userID | permissions U135                                                |
| Dry Run and Real Run for Component Deletion                              | This program will identify components for deletion. By default, it will run in dryRun mode, simulating the deletion and generating an Excel file with the components that would be deleted. To execute in dryRun mode, add the argument "4" or "deleteComponents". To perform actual deletion, add "realRun" as the second argument. |  4 or deleteComponents and realRun | dryRun:<br> deleteComponents <br> realRun:<br> 4 realRun |


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
