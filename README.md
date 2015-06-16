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


###Further optimisations
- Each put() request peeks at the expiry queue as well as adding to it. The peak could be cached using a field containing 
the next expiring value. This feels like over optimisation without a concrete driver.  

- The Expiry Service trades some performance for simplicity, notably a second traversal of the queue inside the synchronised
block, before going into wait. This sits in tension with the blocking take() method that retrieves the next item from the 
 queue and the notify call which can occurs during a put if a more freshly expiring item arrives. This method could be 
 tweaked to better suit a specific use case that favoured either write or expiry performance. 


###Notes on Requirements

####1. Write clean, simple, correct code.
I tried to keep it simple. The cache is two functional classes. The Map itself and an Expiry Service. A unit test for each
and a small acceptance test that links it all together for the happy path only. 
 
####2. The ExpireMap interface may be called concurrently by multiple threads.
The interface includes synchronization. There is a (probably slightly superfluous) test for this.

####3. ExpireMap should only take space proportional to the current number of entries in it.
Each not-expired entry in the map will consume space for a single hashmap entry (key and value) as well as an additional 
ExpiryEntry. The Expiry entry has a fixed overhead of an additional pointer to the key and a long for the expiry time
along with overhead for the data structure itself. 

####4. The timeout should be enforced as accurately as the underlying operating system allows. 5. Write the code in java or C++. If in java, it's ok to use data structures in java collections, but try to avoid using any of the built-in schedulers.
The ExpiringMap uses nanotime to track elapsed time between the entry being inserted and its expiry. This is coupled
with nanosecond control on a the wait() method used to pause the expiry process when it is not required.  

####6. Try to be efficient in the big O time of each of three methods in the interface.
Expiry code only affects the put method, synchronously. This uses a priority queue so is log(n) time for insert. All other
operations are performed on the worker thread. There is a small basic performance test (Performance.java). This shows 
reasonable performance for small batches of inserts and expiries.  

####7. Write a few unit tests.

####8. Complete the assignment in 2 days.



##Running
To run the tests:
```
export JAVA_HOME=<JDK1.8 PATH>

mvn test
```
