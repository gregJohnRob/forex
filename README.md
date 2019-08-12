# forex
Implementation of Paidy Forex challenge.

## Base Use Case
> An internal user of the application should be able to ask for an 
> exchange rate between 2 given currencies, and get back a rate that 
> is not older than 5 minutes. The application should at least support 
> ten-thousand requests per day.

## Assumptions


## Caching
Free tier allows for 5,000 requests per day. To support more than that, created a cache 
typed actor. This required adding a new import of "akka-actor-typed". An actor was used
as it would allow akka to handle the concurrent access to the cache. 

From there, multiple errors occurred due to different versions of the akka libraries 
being imported. To solve this, I went through the different libraries on Github, and 
used the different versions of their build files to align the different versions. 

This still left one more error, which I found by looking at the akka-http documentation.
version 10.1.0 of akka-http set akka-stream to be "provided", meaning that I had to do
an explicit import, where before it was pulled in with akka-http. 

## How to run
In order to run the application, you need to place your API key into  
src/main/resources/application.conf

Compile the application using sbt and run:

```
sbt compile
sbt run
```

To run any tests, use "test" instead of "run".

## Error cases
OneForge API is down (not handled)
OneForge cache crashes (not handled)
