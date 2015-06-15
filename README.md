##Outline

This is a simple wrapper around a standard HashMap to support configurable expiry. 

Keys can be written with an associated timeout. 

Any action performed on the map will trigger eviction of expired entries from the map. This preserves O(n) space 
where n is the number of valid entries at any point in time. 

The eviction process will only examine entries that must be expired at this point of time.
So the eviction process runs at O(nE) where nE is the number entries requiring expiry. 



##Running
To run the tests:
```
export JAVA_HOME=<JDK1.8 PATH>

mvn test
```
