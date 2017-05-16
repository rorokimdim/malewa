Malewa
======

Simple minded early-retirement planner.

Overview
--------

The goal of this project is to build a simple early retirement planner.
Given details of current investments and retirement goals, it should help
estimate a safe time to retire.

Setup
-----

To get an interactive development environment run:

.. parsed-literal:: lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

.. parsed-literal:: (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

.. parsed-literal:: lein clean

To create a release build run:

.. parsed-literal:: ./build-release

And open your browser in `release/index.html`. You will not
get live reloading, nor a REPL.

License
-------

Copyright © 2017 Amit Shrestha

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
