# forex
Implementation of Paidy Forex challenge.

## Base Use Case
> An internal user of the application should be able to ask for an exchange rate between 2 given currencies, and get back a rate that is not older than 5 minutes. The application should at least support 10.000 requests per day.

## Assumptions


## Further Improvements
### Caching
Free tier allows for 5,000 requests per day. To support more than that, a cache could
be implemented which would only call the API if the time that that rate was updated is 
older than 5 minutes. This time could then be tuned up or down to 


## How to run
In order to run the application, you need to create a oneforge.conf file in 
src/main/resources with the following:

```hocon
oneforge {
  apiKey = "YOUR API KEY"
}
```

Once this is created, compile the application using sbt and run:

```
sbt compile
sbt run
```

To run any tests, use "test" instead of "run".