Build GitHub:

[![Build with GitHub](https://github.com/republique-et-canton-de-geneve/nexus-rm-management/actions/workflows/maven.yml/badge.svg)](https://github.com/republique-et-canton-de-geneve/nexus-rm-management/blob/main/.github/workflows/maven.yml)

SonarCloud analysis:

[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=republique-et-canton-de-geneve_nexus-rm-management&metric=bugs)](https://sonarcloud.io/summary/new_code?id=republique-et-canton-de-geneve_nexus-rm-management)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=republique-et-canton-de-geneve_nexus-rm-management&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=republique-et-canton-de-geneve_nexus-rm-management)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=republique-et-canton-de-geneve_nexus-rm-management&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=republique-et-canton-de-geneve_nexus-rm-management)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=republique-et-canton-de-geneve_nexus-rm-management&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=republique-et-canton-de-geneve_nexus-rm-management)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=republique-et-canton-de-geneve_nexus-rm-management&metric=coverage)](https://sonarcloud.io/summary/new_code?id=republique-et-canton-de-geneve_nexus-rm-management)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=republique-et-canton-de-geneve_nexus-rm-management&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=republique-et-canton-de-geneve_nexus-rm-management)

Licence:

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

nexus-rm-management : an additional admin console for Nexus RM
==============================================================

The Nexus RM administration is insufficient.
This application implements features that the Nexus RM
administration console does not include.

The application requires Java 21+.

# 1) Features


| Name                                   |                                                                                                                                                                  Description                                                                                                                                                                   |                         Arguments | Example                                                  |
|:---------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|----------------------------------:|----------------------------------------------------------|
| Extraction of the expired certificates |                                                          This option will check all certificates expiry date and return all the certificates that are already expired and those who will be in the next month. the return will be in a CSV file and sorted by most recent to oldest.                                                           |                  1 or certificate | certificate                                              |
| Extraction of the heavy components     |        This option will check all components assets size and write all the assets that have a size larger than 5 MB into a CSV file. At État de Genève we noticed that, after migrating Nexus RM from an embedded OrientDB database to a PostgreSQL database, processing time for this functionality was reduced by a factor of 300.         |              2 or heavyComponents | heavyComponents                                          |
| Extraction of user permissions         |                                                                                                                                        This option will retrieve the permissions for a specified user.                                                                                                                                         |       3 or permissions and userID | permissions U135                                         |
| Deletion of a component                | This option will identify components for deletion. By default, it will run in dryRun mode, simulating the deletion and generating an Excel file with the components that would be deleted. To execute in dryRun mode, set the argument list to "4" or to "deleteComponents". To perform actual deletion, set the argument list to "4 realRun". | 4 or deleteComponents and realRun | dryRun:<br> deleteComponents <br> realRun:<br> 4 realRun |


# 2) Building the application

```
mvn clean package
```

# 3) Running the application locally

## Pre-step : configuring the application: property file

Do the following:
- Go to directory `src/main/resources`
- Copy file `application-base.yml` (this file is under Git control)
  to a new file `application.yml` (this file is under Git ignore),
  in the same directory
- Edit file `application.yml`, provide the missing values;
  their value is `TO_BE_PROVIDED`.
  For the particular case of `trustStorePassword`, don't do anything yet

## Pre-step : configuring the application: trust store

At État de Genève we unfortunately use self-signed certificates, so a trust
store must be provided for the HTTPS interaction between the application and
the Nexus RM server.

Do the following:
- Get a trust store file.
  At État de Genève it is usually a `gina.jks` file
- Rename the file into `truststore.jks`
- Copy file `truststore.jks` to directory `src/main/resources/security`
- Edit file `application.yml` again and set the password of the trust store
  file

## Running locally

There are several ways to do so.

### Maven

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

### IntelliJ

If you use the IntelliJ, running the application from there is the most
convenient way.

Just run class `Application`.
