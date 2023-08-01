# GraalJSExecutor

GraalJSExecutor is a REST API wrapper around the GraalJs javascript interpreter for managing and executing JavaScript scripts with the GraalVM. It allows you to add scripts for execution, manage their execution, and retrieve their statuses and outputs.

## Features

- Evaluate arbitrary JavaScript code.
- Review the list of scripts, their IDs, execution statuses.
- Get detailed script info, including script body and its console output/error.
- Forcibly stop any running or scheduled script.
- Remove inactive scripts from the list by their ID.

## Technologies Used

- Java 17
- Spring Data JPA
- Spring MVC
- GraalVM


## Prerequisites - Required software
* [Java JDK version 17+ should be installed in the system](https://www.oracle.com/java/technologies/downloads/#java17).

## How to Build:
To build the application execute the following commands in the project folder (where pom.xml and mvnw are located):

```bash
./mvnw clean package # this will build the project
```

After the build is completed, the folder `/target` will be created with a compiled `.jar` ready to be launched.

## How to Run:
Now you can launch the server at the default port `8080`
(if the option `--server.port={PORT}` is not provided):
```bash
java -jar ./target/*.jar 
```
It may take up to around 15 sec for the server to start. This will start the application and you can access the application by navigating to http://localhost:8080 in your web browser.
### Configuring the application
The application can be configured using the `application.properties` file. This file is located in the `src/main/resources` directory. Here, you can configure properties such as the server port, database settings, and logging.