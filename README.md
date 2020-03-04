Pipeline Config History Plugin
==============================
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/pipeline-config-history-plugin/master)](https://ci.jenkins.io/job/plugins/job/pipeline-config-history-plugin/)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/pipeline-config-history.svg)](https://plugins.jenkins.io/pipeline-config-history)

  

This Plugin tracks changes in your pipeline configuration on a build
basis (including replay builds):  
On each completed build built from a pipeline config (including
libraries) which differs from the latest history entry, a new history
entry is created.

**These information are stored:**

-   The Jenkinsfile (or the pipeline script if you're not using pipeline
    via scm)
-   Globally shared libraries
-   Shared Libraries accessible to the folder your pipeline might be
    included in.

The single configurations are viewable file-by-file with each file being
downloadable.

You can also easily compare pipeline changes between two builds with a
file-wise or all-in-one side-by-side or line-under-line diff view.  
Restoring old configurations however is not possible, since that would
require a modification in your Jenkinsfile and/ or library scm.

------------------------------------------------------------------------

# Pictures

#### Index Page

![](https://wiki.jenkins.io/download/attachments/175210534/image2019-5-15_13-44-54.png?version=1&modificationDate=1557920696000&api=v2)

#### Config Revision Overview Page

![](https://wiki.jenkins.io/download/attachments/175210534/image2019-5-15_14-11-7.png?version=1&modificationDate=1557922269000&api=v2)

#### Single File Page

![](https://wiki.jenkins.io/download/attachments/175210534/image2019-5-15_13-53-17.png?version=1&modificationDate=1557921198000&api=v2)

#### Single File Diff

![](https://wiki.jenkins.io/download/attachments/175210534/image2019-9-12_15-30-49.png?version=1&modificationDate=1568295051000&api=v2)  

#### All Diffs In One Page

![](https://wiki.jenkins.io/download/attachments/175210534/image2019-9-12_15-34-15.png?version=1&modificationDate=1568295256000&api=v2) 


------------------------------------------------------------------------

# Open issues

**TODO**: include issue tracker 

------------------------------------------------------------------------

# Changelog

See
[Changelog](https://github.com/jenkinsci/pipeline-config-history-plugin/blob/master/CHANGELOG.md)
on Github.

------------------------------------------------------------------------
# Notes

* This plugin uses *highlight.js* for syntax highlighting (code and diffs). See
    + [github](https://github.com/highlightjs/highlight.js/)
    + [homepage](https://highlightjs.org/)
    
* Development: Make `hpi:run` work with shared libraries
    * install shared library plugin (if not installed already)
    * install git (if you want to use the shared library plugin with git)