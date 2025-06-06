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


| Name                                                             |                                                                                                                                                                                                                                                       Description                                                                                                                                                                                                                                                        |                  Arguments | Example                                          |
|:-----------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|---------------------------:|--------------------------------------------------|
| Extraction of the expired certificates                           |                                                                                                                                                This option checks all certificates expiry date and return all the certificates that are already expired and those who will be in the next month. The output is dumped in a CSV file and sorted by most recent to oldest.                                                                                                                                                 |                          1 | 1                                                |
| Extraction of the heavy components                               |                                                                                       This option checks all components assets size and selects the assets that have a size larger than 5 MB. At État de Genève we noticed that, after migrating Nexus RM from an embedded OrientDB database to a PostgreSQL database, processing time for this functionality was reduced by a factor of 300. The output is dumped in a CSV file.                                                                                        |                          2 | 2                                                |
| Extraction of user permissions                                   |                                                                                                                                                                                                             This option retrieves the permissions for a specified user. The output is dumped in a CSV file.                                                                                                                                                                                                              |               3 and userID | 3 MARTIN                                         |
| Deletion of a component                                          |                                                               This option identifies the components that are ready for deletion. By default, it runs in dryRun mode, simulating the deletion and generating a CSV file with the components that would be deleted. To execute in dryRun mode, set the argument list to "4" or to "deleteComponents". To perform actual deletion, set the argument list to "4 realRun". The output is dumped in a CSV file.                                                                | 4 and (optionally) realRun | In dryRun mode: 4<br>In real run mode: 4 realRun |
| Display of the roles and embedded roles                          |                                                                                                                                                                      This option dumps the list of all roles. For every role, its sub-roles and its external roles are displayed. The output is dumped in the log file and on the standard output.                                                                                                                                                                       |                          5 | 5                                                |
| Display of the users by role                                     | This option dumps the list of the users who have some role. The search is deep, that is, it accounts for the fact that a role can contain roles, recursively. Partial role names are accepted, e.g., you can use "ADMI" to search for the users having either role ADMIN-RESTRICTED or role ADMIN-GLOBAL. Watch out: the search is partial, because the Nexus RM API (as well as the Nexus RM GUI) returns only a fraction of the users of type "LDAP". The output is dumped in the log file and on the standard output. |            6 and role name | 6 nx                                            |
| Display of the roles by privilege |                                       This option dumps the list of the roles which have some privilege. The search is shallow, that is, it returns only the roles having the specified privilege - not the roles which embed a role that has the specified privilege. Partial privilege names are accepted, e.g., you can use "REA" to search for the roles having either privilege READ or privilege READ-ONLY. The output is dumped in the log file and on the standard output.                                       |       7 and privilege name | 7 nx                                           |

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
