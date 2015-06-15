# expiring-map

This is a very simple wrapper around a standard HashMap to support configurable expiry. 

Keys can be written with an associated timeout. Any action performed on the map will trigger eviction of expired entries from the map. 

The eviction process will only examine those entries that need to be expired O(# entries requiring expiry) 

