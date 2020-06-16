# ohti - An alternative Mijn Itho Daalderop portal

[![Build Status](https://travis-ci.com/kevinvandervlist/ohti.svg?token=kieE72RdKXcsawrKB9K3&branch=master)](https://travis-ci.com/kevinvandervlist/ohti)

An alternative for [mijn itho daalderop](https://mijn.ithodaalderop.nl/#/login), which in turn is an OEM version of [niko's Fifthplay](https://www.fifthplay.com/). 
See [here](https://tweakers.net/productreview/212044/itho-daalderop-spider-connect.html) for a (dutch) discussion on why an alternative is useful. 

## Getting started

### CLI Tool
* Download the latest release
* Setup your `application.conf` based on the [provided `reference.conf`](main/src/main/resources/reference.conf). Make sure to _at least_ set the credentials to the portal here.
* Setup your logback configuration based on the [provided template](main/src/main/resources/logback.xml).
* Run the tool: `java -Dconfig.file=application.conf -Dlogback.configurationFile=./logback.xml -jar ohti.jar`

### API
* Download the latest release
* Use [the `api`](api/src/main/scala/nl/kevinvandervlist/ohti/api/PortalAPI.scala) like [the examples shown here](main/src/main/scala/nl/kevinvandervlist/ohti/main/Main.scala)
* Make sure to setup `application.conf` in your client application

## Example usage
```
$ java -Dconfig.file=application.conf -Dlogback.configurationFile=./logback.xml -jar ohti.jar help
19:29:28.296 [main] ERROR nl.kevinvandervlist.ohti.main.Main$ - Available tasks are:
19:29:28.308 [main] ERROR nl.kevinvandervlist.ohti.main.Main$ - * enable-cooling-mode
19:29:28.309 [main] ERROR nl.kevinvandervlist.ohti.main.Main$ - * disable-cooling-mode
19:29:28.309 [main] ERROR nl.kevinvandervlist.ohti.main.Main$ - * catchup-detailed-data-sqlite
19:29:28.309 [main] ERROR nl.kevinvandervlist.ohti.main.Main$ - * catchup-daily-aggregate-sqlite
19:29:28.309 [main] ERROR nl.kevinvandervlist.ohti.main.Main$ - * detailed-data-sqlite
19:29:28.309 [main] ERROR nl.kevinvandervlist.ohti.main.Main$ - * daily-aggregate-sqlite
```

### Enabling or disable cooling mode
If you have a device that supports cooling (such as [HP Cool Cube warmtepomp](https://www.ithodaalderop.nl/nl-NL/professional/product/07-40-50-400)), you can enable or disable the cooling mode automatically.
For example, this can be useful if you want to be able to optimize consumption of privately generated solar energy. 

```
$ java -Dconfig.file=application.conf -Dlogback.configurationFile=./logback.xml -jar ohti.jar disable-cooling-mode
[...]
14:43:31.104 [main] INFO  nl.kevinvandervlist.ohti.main.Main$ - Executing task disable-cooling-mode
14:43:32.000 [main] INFO  n.k.ohti.main.DisableCoolingMode$ - Successfully updated device <uuid>: cooling is now disabled
14:43:32.000 [main] INFO  nl.kevinvandervlist.ohti.main.Main$ - Completed task disable-cooling-mode
14:43:32.001 [main] INFO  n.k.ohti.api.AsyncPortalAPI - Stopping pool...
```
### Extracting monitoring data from the mijn itho daalderop portal
If you want export all monitoring data from the online portal, for example for offline analysis, this is also possible. 
There are two modes: one exporting daily aggregates containing precomputing data (e.g. own consumption based on feedback and raw solar energy production). 
The other mode will just export _all_ monitoring data from a given device, in a given resolution for a given timespan. 
Note that there is a limit of 15 minute windows -- higher precision is either not stored, or not exposed by Fifthplay. 

```
$ java -Dconfig.file=application.conf -Dlogback.configurationFile=./logback.xml -jar ohti.jar daily-aggregate-sqlite
[...]
# Daily aggregated power information, with precalculated summaries
$ sqlite3 daily-aggregate.sqlite 'SELECT name,producedPower,compensatedPowerBalance,gasUsage FROM periodic_usage ORDER BY name DESC LIMIT 2;'
2020-04-22|27800.100070524|-19292.999969|0.5025
2020-04-21|27845.599906324|-20527.999985|0.3125
# 15-minute interval raw monitoring data, logged for all devices
$ sqlite3 detailed-data.sqlite 'SELECT * from monitoring_data LIMIT 3;'
70b4058a-0e98-4ecb-be3c-f52a6626944c|1588802400000|1588803300000|CentralMeterGasUsage|m3|0.0
70b4058a-0e98-4ecb-be3c-f52a6626944c|1588803300000|1588804200000|CentralMeterGasUsage|m3|0.0
70b4058a-0e98-4ecb-be3c-f52a6626944c|1588804200000|1588805100000|CentralMeterGasUsage|m3|0.0
```

## My usecase
* A more meaningful portal that -- in one glance -- show's me all the information I'm interested in. 
* An (easy) way to export data from the portal, so the data is not locked in the online environment. 
* A more accessible platform by providing an API implementation for JVM languages such as Scala, Java or Kotlin. 
* The payloads are very verbose, contain a lot of repetitive info and sometimes unused or in my view irrelevant info. 
I'll drop these parts of the payload _unless_ there is a usecase for it. 

## Future work
* Publish the API part somewhere
* Implement more of the API (compared to just what I personally need).
