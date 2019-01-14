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

The following environment variables are required - values given are for the dockerized local environment (cmc-integration-tests: https://github.com/hmcts/cmc-integration-tests):
```
GOV_NOTIFY_API_KEY=[YOU NEED THIS FROM SOMEONE WITH GOV NOTIFY ADMIN ACCESS]
GOV_PAY_AUTH_KEY_CMC=[THIS IS GENERIC FOR TEAM - ASK]
CMC_DB_HOST=localhost
CMC_DB_PORT=5431
CMC_DB_USERNAME=cmc
CMC_DB_PASSWORD=cmc
CLAIM_STORE_DB_HOST=localhost
CLAIM_STORE_DB_PORT=5430
CLAIM_STORE_DB_USERNAME=claimstore
CLAIM_STORE_DB_PASSWORD=claimstore
IDAM_API_URL=http://localhost:8080
IDAM_S2S_AUTH_URL=http://localhost:4552
FRONTEND_BASE_URL=https://localhost:3333
FEATURE_TOGGLES_API_URL=http://localhost:8580
RESPOND_TO_CLAIM_URL=https://localhost:3333/first-contact/start
CLAIM_STORE_TEST_SUPPORT_ENABLED=true
STAFF_NOTIFICATIONS_SENDER=no-reply@example.com
STAFF_NOTIFICATIONS_RECIPIENT=civilmoneyclaims+staff-int-tests@gmail.com
RPA_NOTIFICATIONS_SENDER=no-reply@example.com
RPA_NOTIFICATIONS_SEALEDCLAIMRECIPIENT=civilmoneyclaims+rpa-claim-issued@gmail.com
RPA_NOTIFICATIONS_MORETIMEREQUESTEDRECIPIENT=civilmoneyclaims+rpa-more-time-requested@gmail.com
RPA_NOTIFICATIONS_RESPONSERECIPIENT=civilmoneyclaims+rpa-defence-response@gmail.com
RPA_NOTIFICATIONS_COUNTYCOURTJUDGEMENTRECIPIENT=civilmoneyclaims+rpa-county-court-judgement@gmail.com
SPRING_MAIL_HOST=localhost
SPRING_MAIL_PORT=1025
PDF_SERVICE_URL=http://localhost:5500
DOCUMENT_MANAGEMENT_URL=false
CORE_CASE_DATA_API_URL=false
FEATURE_TOGGLE_CORE_CASE_DATA=true
IDAM_CASEWORKER_ANONYMOUS_USERNAME=civilmoneyclaims+anonymouscitizen@gmail.com
IDAM_CASEWORKER_ANONYMOUS_PASSWORD=Password12
IDAM_CASEWORKER_SYSTEM_USERNAME=civilmoneyclaims+systemupdate@gmail.com
IDAM_CASEWORKER_SYSTEM_PASSWORD=Password12
SEND_LETTER_URL=false
APPINSIGHTS_INSTRUMENTATIONKEY=fake-key
OAUTH2_CLIENT_SECRET=123456
```

And with CCD:
```
CORE_CASE_DATA_API_URL=http://ccd-data-store-api:4452
FEATURE_TOGGLES_CCD_ENABLED=true
FEATURE_TOGGLES_CCD_ASYNC_ENABLED=false
```

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

## Preview Environment

We get a fully functional environment in Azure Kubernetes (AKS) per pull request. For more
info see: https://tools.hmcts.net/confluence/display/ROC/AKS+-+Azure+Managed+Kubernetes

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
