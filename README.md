Pipeline Config History Plugin
==============================

Jenkins plugin which, not unlike jobConfigHistory (https://wiki.jenkins.io/display/JENKINS/JobConfigHistory+Plugin),
creates a history of pipeline configurations, triggered by their builds' "onCompletion" events. 

Here you especially are able to see and compare changes in pipeline Libraries, 
which are hidden in jobConfigHistory (or replay).

Links
------------
* [Wiki-page](https://wiki.jenkins.io/display/JENKINS/Pipeline+Configuration+History)

Continuous build
----------------
* Jenkins own CI: [![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/pipeline-config-history-plugin/master)](https://ci.jenkins.io/job/plugins/job/pipeline-config-history-plugin/)


Make hpi:run work with shared libraries
---------------------------------------
* install shared library plugin (if not installed already)
* install git (if you want to use the shared library plugin with git)