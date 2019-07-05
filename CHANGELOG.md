# Changelog
All notable changes to this project are documented in this file.

## [1.4] - tbd
* New feature: Make this plugin configurable in so far as that you
  + can limit the number of history entries created per pipeline job.
  + can limit the age of history entries for pipeline jobs.
* Fixed: every root build file is called Jenkinsfile.
* Fixed: missing name in "file view" links in diff overview page.


## [1.3] - 31.05.19
* Add badge icon and diff link to build

## [1.2] - 27.05.19
* Fix single file view link
* Bump jacoco-maven-plugin from 0.8.2 to 0.8.4
* Fixed: No history entries created for pipeline branches containing a slash

## [1.1] - 22.05.19
* Change dependency scope and configure enforcer
* Configure maven-enforcer-plugin to exclude commons-lang3 from requireUpperBoundDeps

## [1.0] - 21.05.19
### First release
* Released this plugin for the first time.
