# Changelog

## [Unreleased]
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
