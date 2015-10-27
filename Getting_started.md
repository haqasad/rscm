# Introduction #

This page guides you into creating a context-aware app for ANDROID using the RSCM middleware.

# Basics #

The source code of the project includes a number of projects. The two most important are:
  * **RSCM runtime**: This is the heart of the RSCM system. It must be installed on the target device and includes a service that runs in the background as needed. This app does not define a launcher activity, and thus it has no icon in the device's launcher (home) screen. The _RSCM runtime_ is available directly from Play at [RSCM\_runtime](https://play.google.com/store/apps/details?id=org.aspectsense.rscm.runtime)
  * **RSCM library**: This is a library project that includes the code to be linked to by any other RSCM project. In principle, it is possible to use RSCM directly from your apps without using any of the _RSCM library_ code (i.e. by binding to the _RSCM runtime_ service directly) but that would consist needless repetition.

# Guides #

Sections:
  * **Creating a context-aware app** [Creating\_a\_context\_aware\_app](Creating_a_context_aware_app.md)
  * **Creating a context sensor plugin** [Creating\_a\_context\_sensor\_plugin](Creating_a_context_sensor_plugin.md)
  * **Creating a context reasoner plugin** [Creating\_a\_context\_reasoner\_plugin](Creating_a_context_reasoner_plugin.md)