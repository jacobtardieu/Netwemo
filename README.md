Netwemo
=================================

Build: [![CircleCI](https://circleci.com/gh/jacobtardieu/Netwemo.svg?style=svg)](https://circleci.com/gh/jacobtardieu/Netwemo)

Netwemo is a scala/akka application reading data from Netatmo and switching on and off Wemo switches.

Currently it checks temperature and humidity.

## Running

You can compile, test, run using `activator` or `sbt`.

To run:
`./activator run`

## External dependencies

### Netwemo API

Netatmo developer IDs are required to access Netatmo API.

### Ouimeaux

Netwemo uses [Ouimeaux](https://github.com/iancmcc/ouimeaux) to talk to Belkin Wemo devices (via the REST API).
