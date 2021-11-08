# Changelog

## [Unreleased]
* Bugfix: incorrectly hardcoded endpoints instead of using the config value

## [0.4.0] - 2021-10-29
* Update the reference config to reflect the new API endpoint
* Move from sttp 2 -> sttp 3
* Upgrade remaining dependencies

## [0.3.0] - 2020-12-04
* Add two commands to configure specific schedules for the heating:
  * one that (within certain temperature boundaries) "forces" the heatpump by assigning <2 consecutive hour time brackets
  * One that just sets a simple schedule and therefore allowing gas usage
  Note that in order to use this properly, you need to configure the settings of the Itho config panel on the CV/Heatpum unit itself properly as well. 
* Fix issue where data was (implicitly) read as UTC, should by Europe/Amsterdam (as that's how it's supplied by the APIs)
* Improve running totals notebook in order to show more info at a single glance

## [0.2.0] - 2020-08-27
* Energydevices now have distinct IDs (for identification) and energyDeviceIds (for measurement data)
* Patch data analysis notebook -- UUIDs of devices in Fifthplay ecosystem suddenly changed

## [0.1.0] - 2020-06-16
* Initial release
* Extract data of monitoring devices
* List devices and capabilities
* Enable or disable cooling mode
