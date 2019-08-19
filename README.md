# forex
Implementation of Paidy Forex challenge. 

# Base Use Case
> An internal user of the application should be able to ask for an 
> exchange rate between 2 given currencies, and get back a rate that 
> is not older than 5 minutes. The application should at least support 
> 10.000 requests per day.

* Ask for exchange rate between 2 given currencies 
* Not older than 5 minutes 
* Support 10.000 requests per day

## Caching 
Free tier allows for 5.000 requests per day. To support the 10.000 of the base use case,
the results of each query to the 1Forge API need to be cached. Anything cached can be no older than 5 minutes

## How to run
To run the application:
* Download from GitHub
* Use `sbt compile` to compile the code 
* `sbt run` after compile will start the application and allow you to make queries. 
* `sbt test` after compile will run the application tests


# Initial implementation

## Config 
I expanded the config to include:

```hocon
  oneforge {
    name = "live"
    key = "vdwrZJlhfann1VXi3ynTv2PPrP8uojWI"
  }
```

* Name: The name given to the actor system used to query the 1Forge API
* Key:  The key for the 1Forge API 

## Cache 
To handle the concurrency of the cache, I used a Typed Actor. To do this, I had to modify the imports to include akka-actor-typed.
Making this change resulted in updating a large number of other versions as akka-actor-typed had to be the same version as
akka-actor, which then resulted in a domino effect where multiple libraries were updated. 

The Typed Actor controls access to a Map\[Rate.Pair, Rate\], which was used to store the rates. 

## Error Handling 
I improved the errors by giving better descriptions of what went wrong. Issues with the 1Forge API (such as invalid key)
were passed through to the user instead of the generic response. 

# Second Implementation 
After I finished the initial implementation, I went back to the Git repo for the interview and saw an old pull request 
containing answers to the interview. I used two ideas from this repo: 
* Using a concurrent Map for the cache
* Using HttpResponse to send better error messages

## Config 
In the second pass, I added a time-to-live parameter into the config. This allows the user to set the ttl on the config.
This change means that the 5 minute original requirement can be adjusted without having to recompile the codebase.
```hocon
  oneforge {
    name = "live"
    key = "vdwrZJlhfann1VXi3ynTv2PPrP8uojWI"
    ttl = 5
  }
```

## Cache 
Instead of using the Typed Actor, I used a Scala concurrent.map. In this case, a TrieMap, however the cache allows
for a different implementation to be used. 

I decided to use the as it greatly reduced the amount of code that was needed, while still controlling access to the 
map that was used for caching.

## Error Handling
I updated the exception handler to return HttpResponses. This allows for sending different status codes (for example: 500),
which could then be expanded upon later on.

# Testing 
In order to test the application, I reworked the Live Interpreter to take in traits Cache and ServiceCaller. These could
then be mocked when testing the Live Interpreter, and tested separately to ensure that they worked as well.

# Further Improvements/Experiments 

## Testing
There are a few different ways I believe that testing could be improved. The AkkaServiceCaller could be tested using 
HttpResponses.

Other ways testing could be improved would be bringing in other styles of testing such as Property Based testing 
(ScalaCheck) or Contract testing (Pact). 

## Eager caching 
A potential improvement I am going to call "eager caching". The idea would be that when the cache is deciding on whether 
something should be dropped or not, it could ping the 1Forge api to see how many requests were left in the day, and how
long it had to use it. This would mean that on slow days, it would eagerly drop the cached value to give the more 
up-to-date values. 


# Breakdown
This section gives a breakdown of all changes made on a per-file basis:

* src/main/resources/reference.conf: Added 1Forge config containing name of Actor System; API key; ttl for the cache (set to 5 as per requirements)
* src/main/scala/forex/config/config: Added 1Forge config case class (and reader)
* src/main/scala/forex/interfaces/api/rates/Routes: Now check to make sure that any pairs given to the api are unique
* src/main/scala/forex/interfaces/api/utils/ApiExceptionHandler: Added HttpResponses to the output; return localized messages from exceptions; added pass-through exception for 1Forge errors
* src/main/scala/forex/main/Processes: Call the Live interpreter instead of Dummy Interpreter
* src/main/scala/forex/services/oneforge/AkkaServiceCaller: Implemented the Service Caller Trait for calling 1Forge API using Akka Http
* src/main/scala/forex/services/oneforge/Cache: Trait for different implementations of a cache for the Live Interpreter
* src/main/scala/forex/services/oneforge/ConcurrentMapCache: Implementation of the Cache using a concurrent Map
* src/main/scala/forex/services/oneforge/Error: Added an ApiCallError for passing errors from 1Forge API
* src/main/scala/forex/services/oneforge/Interpreters: Deleted Dummy Interpreter and added Live implementation
* src/main/scala/forex/services/oneforge/ServiceCaller: Service Caller trait for accessing the 1Forge API