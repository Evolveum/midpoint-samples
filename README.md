# Configuration Samples for MidPoint Project

This project contains configuration samples for [midPoint](https://github.com/Evolveum/midpoint).

You can contribute your own samples into samples/contrib directory.
See samples/contrib/README for the instructions.

## Testing samples

To test the samples use `test.bat` or `test.sh` scripts which run included test build.
The test project `samples-test` is separate and not dependent on samples project.
It depends on midPoint to run the tests, but midPoint's distribution depends on samples,
so to avoid the cycle the testing project is independent (but close to the samples it tests).
It is recommended to run the tests after any changes in the samples to check that they
at least conform to schema.
