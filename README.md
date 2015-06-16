##Outline

This is a simple wrapper around a standard HashMap to support configurable expiry. 

Keys can be written with an associated timeout. 

Eviction is automatically scheduled using a background thread. 


###Notes on the Implementation
As data is written to the ExpiryMap entries recording the expiry time are placed on a PriorityQueue. The queue is ordered 
by the entries expiry time so items expiring soonest move to the head of the queue. 

A separate thread is used to read expiry entries from the queue. If the expiry time of the most recent entry is in the 
future the thread waits for this expiry time. 

Should a new entry be put into the map whilst the expiry thread is blocked the thread is notified, picks up the new 
expiry time, then goes back into a waiting state. 



###Notes on Requirements

####1. Write clean, simple, correct code.
I tried to keep it pretty simple. The cache is two classes. The Map itself and an Expiry service. A unit test for each
and a small acceptance test that links it all together for the happy path only. 
 
####2. The ExpireMap interface may be called concurrently by multiple threads.
The interface includes synchronization. There is a test for this.

####3. ExpireMap should only take space proportional to the current number of entries in it.
Each valid entry in the map will consume space for a single hashmap entry (key and value) as well as an additional 
ExpiryEntry. The Expiry entry has a fixed overhead of an additional pointer to the key and a long for the expiry time. 

####4. The timeout should be enforced as accurately as the underlying operating system allows. 5. Write the code in java or C++. If in java, it's ok to use data structures in java collections, but try to avoid using any of the built-in schedulers.
The ExpiringMap uses System.nanotime to track the time between the entry being inserted and its expiry. This is coupled
with nanosecond control on a the wait() method used to pause the expiry process when it is not required.  

####6. Try to be efficient in the big O time of each of three methods in the interface.
The code here only affects the put method (synchronously). All other work is done on a worker thread. No direct traversals
 are performed. Expirey entries are arranged in a PriorityQueue ready for consumption. 

####7. Write a few unit tests.

####8. Complete the assignment in 2 days.



##Running
To run the tests:
```
export JAVA_HOME=<JDK1.8 PATH>

mvn test
```
