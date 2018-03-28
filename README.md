# hystrix-dropwizard-bundle
[![Build Status](https://travis-ci.org/zapodot/hystrix-dropwizard-bundle.svg?branch=master)](https://travis-ci.org/zapodot/hystrix-dropwizard-bundle)
[![Coverage Status](https://coveralls.io/repos/zapodot/hystrix-dropwizard-bundle/badge.svg)](https://coveralls.io/r/zapodot/hystrix-dropwizard-bundle)
[![Apache V2 License](http://img.shields.io/badge/license-Apache%20V2-blue.svg)](//github.com/zapodot/embedded-db-junit/blob/master/LICENSE)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.zapodot/hystrix-dropwizard-bundle/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.zapodot/hystrix-dropwizard-bundle)
[![Libraries.io for GitHub](https://img.shields.io/librariesio/github/zapodot/hystrix-dropwizard-bundle.svg)](https://libraries.io/github/zapodot/hystrix-dropwizard-bundle)
[![Analytics](https://ga-beacon.appspot.com/UA-40926073-4/hystrix-dropwzard-bundle/README.md)](https://github.com/igrigorik/ga-beacon)

A [Hystrix](//github.com/Netflix/Hystrix) bundle for [DropWizard](//github.com/dropwizard/dropwizard) which adds a Hystrix event stream to the admin context and enables Metrics reporting of Hystrix metrics

## Licence
This library is released under an [Apache Licence (V2)](http://www.apache.org/licenses/LICENSE-2.0).

## Why do I need it?
* You use both DropWizard and Hystrix in your project (or are planning to..) and want your application's Hystrix metrix available in a [Hystrix Dashboard](//github.com/Netflix/Hystrix/tree/master/hystrix-dashboard) (either directly or via [Turbine](//github.com/Netflix/Turbine))
* It is easy to set up as it really requires you to add only a single line of code in your application ([see example below](#add-bundle-to-your-application))

## Alternatives
* If you want a even tighter integration between Hystrix and DropWizard, you should have a look at [Yammer's Tenacity project](//github.com/yammer/tenacity). Note: Tenacity implies (but does not require) that you are using [Breakerbox](//github.com/yammer/breakerbox) for managing your Hystrix runtime configuration.

## Usage
This library is distributed through the Sonatype OSS Repo and should thus be widely available.
### Add dependency
#### Maven
```xml
<dependency>
    <groupId>org.zapodot</groupId>
    <artifactId>hystrix-dropwizard-bundle</artifactId>
    <version>1.0.1</version>
</dependency>
```

#### SBT
```scala
libraryDependencies += "org.zapodot" % "hystrix-dropwizard-bundle" % "1.0.1"
```

### Add bundle to your application

```java
    @Override
    public void initialize(final Bootstrap<AppConfiguration> bootstrap) {
        ...
        bootstrap.addBundle(HystrixBundle.withDefaultSettings());
        ...
    }
```
This will initialize the bundle using default settings which means that:
 * a *HystrixMetricsStreamServlet* is added to the admin context of your application with servlet mapping */hystrix.stream*
 * the *HystrixCodaHaleMetricsPublisher* is registered for Hystrix which will push all Hystrix metrics to DropWizard (previously: CodaHale) Metrics

If you need some other configuration, use the provided Builder (see example below)
```java
    @Override
    public void initialize(final Bootstrap<AppConfiguration> bootstrap) {
        ...
        bootstrap.addBundle(HystrixBundle.builder()
                                            .withApplicationStreamPath("/my-path")
                                            .disableStreamServletInAdminContext()
                                            .disableMetricsPublisher()
                                            .build()
                                            );
        ...
    }
```

### Overriding publishing settings
Starting with version 0.9, you may now implement your own mechanism for specifying whether Hystrix Metrics should be 
published to DropWizard Metrics by adding your own lambda expression to the withMetricsPublisherPredicate builder method
```java
    HystrixBundle.builder()
                .withMetricsPublisherPredicate((c) -> true) // c is your application configuration POJO
                .build();
``` 
__Note__: The now defunct version 0.8 implemented another approach which included overriding a method on Hystrix bundle. 
Putting this on the builder allows us to keep the specialized constructor private which provides us with more flexibility in the future. 
