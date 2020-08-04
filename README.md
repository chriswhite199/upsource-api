# Upsource Java REST API Client

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