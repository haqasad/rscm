![http://rscm.googlecode.com/svn/trunk/artwork/rscm_240x100.png](http://rscm.googlecode.com/svn/trunk/artwork/rscm_240x100.png)

# Really Simple Context Middleware  #

The RSCM (Really Simple Context Middleware) is a software project allowing for the development of context-aware applications for the ANDROID platform, via existing (or newly developed) context plugins.

The underlying architecture is based on the context plugin system developed as part of the IST-MUSIC project [1, 2]. This architecture facilitates a separation between context producing components (the plugins) and context consuming components (the apps). In this ANDROID implementation of the architecture, the plugins are individual APKs which are automatically discovered upon installation.

The main idea is to have individual context plugins that are reusable and can be discovered dynamically. These plugins are _producers_ of context data. Context plugins are classified into context sensors and context reasoners. The former are plugins that are simply used to produce context data directly from underlying sensors (e.g. by acting as wrappers of such entities such as the LocationManager). The latter are also used to produce context data, but usually of higher-level (e.g. describing the user's activity). Context reasoners typically require input of other context types, which means that they act both as context consumers and context producers.

On the other hand, applications that wish to leverage context information bind to the RSCM middleware which manages and mediates the underlying plugins, thus revealing the apps from having to realize the corresponding functionality directly into their code.

## Main components ##

The main components of this project are:
  * **[RSCM library](http://code.google.com/p/rscm/source/browse/trunk/#trunk%2Frscm-library)**: Contains the main bulk of code needed to develop apps using RCSM (typically, this is the first JAR file that you need to _include_ when you develop a new plugin or a new context-aware app).
  * **[RSCM runtime](http://code.google.com/p/rscm/source/browse/trunk/#trunk%2Frscm-runtime)**: This is the runtime middleware, consisting of a service that runs in the background and facilitates the mediation between the context producers (plugins) and the context consumers (apps). This service is automatically started when needed. If not available, the app prompts the users to install it by taking them to the corresponding market page.

## Optional components ##

The optional components are not required for running apps with RSCM, but they can be useful during development.
  * **[Context Viewer](http://code.google.com/p/rscm/source/browse/trunk/#trunk%2Frscm-viewer)**: Provides a simple app for viewing the available plugins.

## Plugin components ##

The project includes a number of predefined plugins that can be used either as examples, or actual components to be included in your projects:
  * **Battery sensor plugin**: Reports on the remaining battery level of the device
  * **Power sensor plugin**: Reports on whether the device is connected to or disconnected from the power outlet
  * **Location (coarse) plugin**: Reports on the user's coarse (rough) location
  * **Location (fine) plugin**: Reports on the user's fine (more accurate) location
  * **User activity plugin**: A reasoner plugin that demonstrates how context reasoners are formed (this particular implementation depends on battery and power readings and reports the user status accordingly (e.g. "The user is walking", or "The user sleeps", etc).

## Quick start (also known as 'hello world') ##

If you want to take RSCM for a quick spin, then follow these steps:

A. Download and install on your device the following APKs (or their later versions) from the [Downloads](http://code.google.com/p/rscm/downloads/list) section (in the given order):
  1. rscm-runtime\_0.2.1.apk
  1. plugin-sensor-battery\_0.2.1.apk
  1. plugin-sensor-power\_0.2.1.apk
All these files are ANDROID services and thus do not have any UI.

B. Download and install on your device the following APK (or its later version) also from the [Downloads](http://code.google.com/p/rscm/downloads/list) section:
  1. sample-app-QuickStart\_0.2.1.apk
This is the quickstart app. Launch it and view how the widgets are updated as the battery level changes or as you connect/disconnect the device from power.
For more details, consult the [quickstart app source code](http://code.google.com/p/rscm/source/browse/trunk/sample-apps/sample-app-QuickStart/src/org/aspectsense/rscm/quickstart/MyContextAwareActivity.java).

## Getting started with development ##

Visit the [wiki](http://code.google.com/p/rscm/w/list) pages to read about how to get started:
  * [Getting started](Getting_started.md)
  * [Creating a context-aware app](Creating_a_context_aware_app.md)
  * [Creating a context sensor plugin](Creating_a_context_sensor_plugin.md)
  * [Creating a context reasoner plugin](Creating_a_context_reasoner_plugin.md)

## References ##
1. http://ist-music.berlios.de/site/

2. Nearchos Paspallis, Middleware-based development of context-aware applications with reusable components, Ph.D. Thesis, University of Cyprus, September 2009, p. 286 ([PDF](http://nearchos.aspectsense.com/phd/paspallis_phd_thesis_2009-final.pdf))