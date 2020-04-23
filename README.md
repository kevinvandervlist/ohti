# ohti - An alternative Mijn Itho Daalderop portal

[![Build Status](https://travis-ci.com/kevinvandervlist/ohti.svg?token=kieE72RdKXcsawrKB9K3&branch=master)](https://travis-ci.com/kevinvandervlist/ohti)

An alternative for [mijn itho daalderop](https://mijn.ithodaalderop.nl/#/login)

## Example usage

```
$ java -Dconfig.file=application.conf -Dlogback.configurationFile=./logback.xml -jar ohti.jar help
09:50:42.145 [main] ERROR nl.kevinvandervlist.ohti.main.Main$ - Available tasks are:
09:50:42.149 [main] ERROR nl.kevinvandervlist.ohti.main.Main$ - * list-devices
09:50:42.149 [main] ERROR nl.kevinvandervlist.ohti.main.Main$ - * usage-details
09:50:42.149 [main] ERROR nl.kevinvandervlist.ohti.main.Main$ - * daily-aggregate-sqlite
$ java -Dconfig.file=application.conf -Dlogback.configurationFile=./logback.xml -jar ohti.jar daily-aggregate-sqlite
[...]
$ sqlite3 daily-aggregate.sqlite 'SELECT name,producedPower,compensatedPowerBalance,gasUsage FROM periodic_usage ORDER BY name DESC LIMIT 2;'
2020-04-22|27800.100070524|-19292.999969|0.5025
2020-04-21|27845.599906324|-20527.999985|0.3125
```