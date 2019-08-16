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
the results of each query to the 1Forge API need to be cached for no-longer than 5 minutes

## How to run
To run the application:
* Download from GitHub
* Use `sbt compile` to compile the code 
* `sbt run` after compile will start the application and allow you to make queries. 
* `sbt run` after compile will run the application tests


# Initial implementation

## Config 
The config was expanded to include:

```hocon
  oneforge {
    name = "live"
    key = "vdwrZJlhfann1VXi3ynTv2PPrP8uojWI"
  }
```

* Name: The name given to the actor system used to query the 1Forge API
* Key:  The key for the 1Forge API 

## Cache 
To handle the concurrency of the cache, I used a Typed Actor. This required modifying the imports to use a more recent
version of almost all libraries as the akka-actor version had to be the same as the version of akka-actor-typed used. 
The typed actor controls access to Map\[Rate.Pair, Rate\]. 

## Error Handling 
Errors were improved by giving better descriptions of what went wrong. Issues with the 1Forge API (such as invalid key)
are now passed through to the user instead of the generic response. 

# Second Implementation 

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
Instead of using the Typed Actor, a Scala concurrent.map is used. In this case, a TrieMap, however the cache allows
for a different implementation to be used. 

## Error Handling
Using HttpResponse allows sending different status codes (for example: 500). 

# Further Improvements/Experiments 

## Property Based Testing
ScalaCheck property based testing could expand upon the testing framework. 

## Pact
Contract testing using pact. 