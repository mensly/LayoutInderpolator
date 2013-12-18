Layout Inderpolator
============================

The goal of this project is to provide a simple way to add a little bit of flavour animation to
what would otherwise be rather bland screens as part of a mobile app intro sections. It is inspired
by the end result of using the [Jazz Hands][1] library for iOS, but is not a port of functionality.


Integrating With A Project
============================
See provided sample for gradle integration from a cloned repo.

Alternatively simply include the [jar file][2] in your project libs directory. For gradle add a
dependency for all jar files in the libs directory:

    compile fileTree(dir: 'libs', include: '*.jar')

Use
============================
InderpolatorView is a view group that presents each child one at a time. The user can scroll
between these pages, or advance using other means such as a button if desired. The views can be
added programatically or in xml as in the sample.

Interpolation is based on view ids. If a view id is only present in one page of a transition, it
will simply fade and animate off the screen. If a view id is present on both pages in the same
position, they will fade between the two, remaining stationary. If they are not in the same
position, the latter view takes precedence and will animate in from the size and shape of the, now
hidden, former view. The purpose of these is to make the two views represent the same object, but
in the sample they are coloured differently for clarity. Marking a view as invisible in the xml
will cause it to fade out from its other position.


Sample Application
============================
A compiled apk of the sample application is available [here][3].

 [1]: https://github.com/IFTTT/JazzHands
 [2]: http://mens.ly/files/inderpolator/layoutinderpolator.0.1.jar
 [3]: http://mens.ly/files/inderpolator/layoutinderpolator-sample.0.1.apk
