Pipeline Config History Plugin
==============================
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/pipeline-config-history-plugin/master)](https://ci.jenkins.io/job/plugins/job/pipeline-config-history-plugin/)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/pipeline-config-history.svg)](https://plugins.jenkins.io/pipeline-config-history)

Jenkins plugin which, not unlike [JobConfigHistory](https://plugins.jenkins.io/jobConfigHistory),
creates a history of pipeline configurations, triggered by their builds' "onCompletion" events. 

Here you especially are able to see and compare changes in pipeline Libraries, 
which are hidden in jobConfigHistory (or replay).


Make hpi:run work with shared libraries
---------------------------------------
* install shared library plugin (if not installed already)
* install git (if you want to use the shared library plugin with git)