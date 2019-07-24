# Changelog
All notable changes to this project are documented in this file.

## [1.4] - 24.07.19
* New feature: Make this plugin configurable in so far as that you
  + can limit the number of history entries created per pipeline job.
  + can limit the age of history entries for pipeline jobs.
* New feature: Make the file and diff views resizable.
* New feature make each diff view in the "show all diffs view" hideable.
* Fixed: every root build file is called Jenkinsfile.
* Fixed: missing name in "file view" links in diff overview page.
* Fixed: plugin layout affected the Jenkins layout.
* Fixed: some layout problems concerning diffs
* Change diff layout to equally sized columns.

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
