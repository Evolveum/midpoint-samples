# Configuration Samples for MidPoint Project

This project contains configuration samples for [midPoint](https://github.com/Evolveum/midpoint).

You can contribute your own samples into samples/contrib directory.
See samples/contrib/README for the instructions.

## Testing samples

To test the samples fill out `test-setenv.sh` or `test-setenv.bat` to set-up connection parameters for
Native Postgresql repository with database schema already prepared.
Use `test.bat` or `test.sh` scripts which run included test build.
The test project `samples-test` is separate and not directly part of the samples project,
but it depends on both midPoint (to run the tests) and on the samples project (tested stuff).

This separation is made because midPoint's distribution depends on the samples too.
To avoid the cycle (samples-with-tests -> midpoint -> samples-with-tests) the testing project
is separate Maven project but still kept close to the samples it tests, in the same repository.

Now the dependencies are clear:

* samples don't depend on anything;
* midPoint depends on samples (when creating distribution package);
* samples test depends on both samples and various midPoint runtime modules to run the tests.

It is recommended to run the tests after any changes in the samples to check that they
at least conform to schema.
