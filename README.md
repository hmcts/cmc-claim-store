# CMC claim store

[![codecov](https://codecov.io/gh/hmcts/cmc-claim-store/branch/master/graph/badge.svg)](https://codecov.io/gh/hmcts/cmc-claim-store)

This is the backend service for Civil Money Claims.  
The service provides a set of RESTful endpoints for the CMC frontend application.  
The two main responsibilities of this application are:
 - data access layer for the CMC service,
 - executing business logic of the CMC service e.g. calculating response deadline. 

Internally state is persisted to a relational database.  
The service also delegates some responsibilities to other RESTful services e.g. idam-api or pdf-service.

## Getting started

### Prerequisites

- [JDK 8](https://www.oracle.com/java)
- [Docker](https://www.docker.com)

#### Environment variables

The following environment variables are required:

- `STAFF_NOTIFICATIONS_SENDER`, email address staff notifications are sent from
- `STAFF_NOTIFICATIONS_RECIPIENT`, email address staff notifications are sent to
- `SPRING_MAIL_HOST`, the host of an SMTP server, `localhost` for the dockerized local environment 
- `SPRING_MAIL_PORT`, the port of an SMTP server, `1025` for the dockerized local environment
- `PDF_SERVICE_URL`, the base url of PDF Service instance, `http://localhost:5500` for the dockerized local environment

### Building

The project uses [Gradle](https://gradle.org) as a build tool but you don't have install it locally since there is a
`./gradlew` wrapper script.  

To build project please execute the following command:

```bash
$ ./gradlew build
```

### Running

Before you run an application you have to define `CLAIM_STORE_DB_USERNAME` and `CLAIM_STORE_DB_PASSWORD` environment variables.

When environment variables has been defined, you need to create distribution by executing following command:

```bash
$ ./gradlew assemble
```

If you want your code to become available to other Docker projects (e.g. for local environment testing), you need to build the image:

```bash
$ docker-compose build claim-store-api
```

When the code has been compiled you can execute it by running the following command:

```bash
$ docker-compose up
```

As a result the following containers will get created and started:

 - Database exposing port `5430`
 - API exposing ports `4400`
 
### Smoke and functional tests for Automated Acceptance Testing environment

The [`src/aat`](src/aat) source set contains automatic tests which are executed in the delivery pipeline. They are intended to run against real Claim Store instances after deployments to consecutive slots and environments.

They can be run against local instance of Claim Store as well, but need some environment variables exported:

- `SMOKE_TEST_CITIZEN_USERNAME`, `SMOKE_TEST_SOLICITOR_USERNAME`, `SMOKE_TEST_USER_PASSWORD` - credentials of the pre-created test users (you can create them manually yourself). It's assumed they both have the same password at the moment,
- `GENERATED_USER_EMAIL_PATTERN` - this is used to generate names for users created by tests on the fly. It should resolve to a valid email address and can have up to one [`printf`](https://en.wikipedia.org/wiki/Printf_format_string) string placeholder where a randomized value will be inserted. For example, if you were to export a value of `some-user-%s@server.com`, it would resolve to something like `some-user-w8a0wuqqvy@server.com` at runtime. You don't have to use a placeholder if you don't want to, but it should be a valid email address as to avoid unnecessary errors in GOV.UK Notify,
- `TEST_URL` - base URL of a running Claim Store instance.

To run smoke tests (non-destructive read operations):

```bash
$ ./gradlew smoke
```

To run functional tests (will create data in the system):

```bash
$ ./gradlew functional
```

#### Handling database

Database will get initiated when you run `docker-compose up` for the first time by execute all scripts from `database` directory.

You don't need to migrate database manually since migrations are executed every time the application bootstraps.

### API documentation

API documentation is provided with Swagger:
 - `http://localhost:4400/swagger-ui.html` - UI to interact with the API resources

NOTE: Swagger scans classes in the `uk.gov.hmcts.cmc.claimstore.controllers` package.

## Developing

### Unit tests

To run all unit tests please execute the following command:

```bash
$ ./gradlew test
```

### Coding style tests

To run all checks (including unit tests) please execute the following command:

```bash
$ ./gradlew check
```

## Versioning

We use [SemVer](http://semver.org/) for versioning.
For the versions available, see the tags on this repository.

## Troubleshooting

### IDE Settings

#### Project Lombok Plugin
When building the project in your IDE (eclipse or IntelliJ), Lombok plugin will be required to compile. 

For IntelliJ IDEA, please add the Lombok IntelliJ plugin:
* Go to `File > Settings > Plugins`
* Click on `Browse repositories...`
* Search for `Lombok Plugin`
* Click on `Install plugin`
* Restart IntelliJ IDEA

Plugin setup for other IDE's are available on [https://projectlombok.org/setup/overview]

#### JsonMappingException when running tests in your IDE
Add the `-parameters` setting to your compiler arguments in your IDE (Make sure you recompile your code after).  
This is because we use a feature of jackson for automatically deserialising based on the constructor.  
For more info see: https://github.com/FasterXML/jackson-modules-java8/blob/a0d102fa0aea5c2fc327250868e1c1f6d523856d/parameter-names/README.md

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.
