# Keycloak extensions

## Export realm configuration

To use the same configuration when starting keycloak in the docker,
you have to run keycloak locally, do the settings that you want, and export the conf.

To export the conf, run from the root of this repo: 
`PATH/TO/kc.sh export --file rezoleo-realm-export.json --realm rezoleo`.
Keycloak must be stopped before running this command.

It will output a json file that is imported in keycloak inside docker.

## Debugging the extensions

By running the docker-compose, you are by default in debug mode.
To debug remotely, in IntelliJ, create a `Remote JVM Debug` run configuration,
connected to the port `8787`.

## Build the extensions

By default, the extensions are imported in the docker keycloak.
To update your changes, you have to run `mvn install` and then restart your container.

## Running the test

Tests are ran from the folder `/test` with the command `npm run test`.

## Add a new extension

To add a new extension, take a look at how keycloak does it 
(example for a [validator](https://github.com/keycloak/keycloak/blob/main/services/src/main/java/org/keycloak/userprofile/validator/DuplicateEmailValidator.java)).

## Extending the tests

To help you add a new e2e test, you can use the playwright test generator: `npx playwright codegen localhost:8080`. 