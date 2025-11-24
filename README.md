# Commons

This project contains multiple library components for use elsewhere.

## The Modules

- **Collect**
- **Maps**
- **Units**
- **Utils**


## Todo
- Change Spherical code to "two groups" 
  - high fidelity "double input" methods to
  - Lower fidelity "convenient type" inputs with "convenient type" output

- Is `FileLineIterator` still worthwhile.  Is there a better replacement in core Java?
- Consider removing SingleUseTimer in favor of Guava's `com.google.common.base.Stopwatch`
- Look at skipped tests
- - Is CheckedCallable correct.  When doesn't Uncheck.call return a Callable

## Remaining to be migrated ....


`out` package
- FileSink
- GzFileSink
- JsonFileSink
- JsonGzFileSink
- JsonWritable
- OutputSink

`util` package
- 
