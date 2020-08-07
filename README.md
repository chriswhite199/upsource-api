# Upsource Java REST API Client

[![CircleCI](https://circleci.com/gh/chriswhite199/upsource-api.svg?style=shield)](https://circleci.com/gh/chriswhite199/upsource-api)

Creates a simple REST API client for the Jetbrains Upsource web service.

## Building

By default, the services documents on the main Upsource API docs are used for code generation:

*  https://upsource.jetbrains.com/~api_doc/index.html

As there isn't any obviously available OpenAPI spec for Upsource, or JSON Schema files, the json files linked from the
above URL is used. If links are added / removed then the `api-lib/build.gradle` file will need to be updated 
accordingly.

`./gradlew build`

## Sample Java 11 HttpClient wrapper

See `http-lib` for a sample wrapper using Java 11's HttpClient library to implement the `UpsourceRPC` service.

Check out the unit tests for `http-lib` for sample usage.

## Enum serialization

The Enums of the `com.github.chriswhite199.upsourceapi.dto.enums` package require serialization as integer values.

Each enum has a `getNumber()` field that should be used as the wire integer (which should match up with the enum 
ordinal). The `http-lib` uses Jackson for serialization and this is configured using the following:
 
```java
new ObjectMapper()
    .configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, true)
```

You should note that the enum values are 1 based rather than zero based, and as such a deprecated enum value `_NA_` is
provided to ensure the ordinal values start from 1.