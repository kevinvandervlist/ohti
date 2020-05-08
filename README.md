# ohti - An alternative Mijn Itho Daalderop portal

[![Build Status](https://travis-ci.com/kevinvandervlist/ohti.svg?token=kieE72RdKXcsawrKB9K3&branch=master)](https://travis-ci.com/kevinvandervlist/ohti)

An alternative for [mijn itho daalderop](https://mijn.ithodaalderop.nl/#/login), which in turn is an OEM version of [niko's Fifthplay](https://www.fifthplay.com/). 
See [here](https://tweakers.net/productreview/212044/itho-daalderop-spider-connect.html) for a (dutch) discussion on why an alternative is useful. 

## My usecase
* A more meaningful portal that -- in one glance -- show's me all the information I'm interested in. 
* An (easy) way to export data from the portal, so the data is not locked in the online environment. 

## Future work
* Implement more of the API (compared to just what I personally need).

## Example usage

```
$ java -Dconfig.file=application.conf -Dlogback.configurationFile=./logback.xml -jar ohti.jar help
09:50:42.145 [main] ERROR nl.kevinvandervlist.ohti.main.Main$ - Available tasks are:
09:50:42.149 [main] ERROR nl.kevinvandervlist.ohti.main.Main$ - * daily-aggregate-sqlite
09:50:42.149 [main] ERROR nl.kevinvandervlist.ohti.main.Main$ - * catchup-daily-aggregate-sqlite
09:50:42.149 [main] ERROR nl.kevinvandervlist.ohti.main.Main$ - * detailed-data-sqlite
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
