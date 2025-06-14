# CWMP CPE Horde

[![Tests](https://github.com/Viasat/cwmp-cpe-horde/actions/workflows/test.yaml/badge.svg)](https://github.com/Viasat/cwmp-cpe-horde/actions/workflows/test.yaml)

TR-069 (AKA "CPE WAN Management Protocol", or CWMP) is a standardized protocol for managing edge devices over a WAN. See https://www.broadband-forum.org/pdfs/tr-069-1-6-1.pdf for the latest (_caveat lector_) specification document.

This CWMP Customer Premises Equipment (CPE) implementation is not tested to be _fully compliant_ with the TR-069 specification.

The purpose is, rather, to provide:

1. A basic, OOTB tool for testing a TR-069 ACS (the server side)
2. A flexible and powerful toolkit for CWMP users to devise more targeted testing scenarios
3. A starting point for anyone wishing to build out a more fully-compliant CWMP client
4. A teaching/demo codebase illustrating TR-069 messaging in action

## Features

1. Convenient "lab" of cwmp clients can be pointed at an ACS.
2. REPL interaction to set parameters locally and cause a VALUE_CHANGE inform.
3. Handles instance wildcards in parameter and object names.
4. AddObject support.
5. Basic inform events: BOOTSTRAP, BOOT, PERIODIC, VALUE_CHANGE
6. CPE-side parameters.
7. Connection Request support.
8. Currently divides the `PeriodicInformInterval` by 1000 (!) to cause more frequent interaction.

## How-to

Get usage:

```
./mgr help
```

Run a horde of CWMP CPE instances:

```
# first, copy example-config.edn and add your ACS URL and customize as you like
CONFIG_FILE_PATH=config.edn ./mgr run
```

Your horde of CWMP CPE instances will show up in the ACS as devices with MAC
addresses numbered counting up from the OUI in your config file. For example,
if your OUI is `FEFEFE` and you specify `:instance-count 3`, then the following
devices will be registered with your ACS:

```
FEFEFE000000
FEFEFE000001
FEFEFE000002
```

## REPL

See the `(comment ...)` at the end of `src/clojure/viasat/cwmp_cpe/main.clj` for some ready-to-go, basic REPL interaction.

## Potential TODOs

* This tool is more like a framework, than a library. This seems OK, and
  perhaps inevitable, in order to have anything that works "OOTB". Also, this is
  in keeping with the pattern of how such things are often delivered: as working
  frameworks into which vendors are expected to insert their own custom
  functionality. HOWEVER, it would be good to have a clean, well-documented
  approach to proprietary extension, as will be required by almost any
  interesting test scenarios.
    * Would it be good to have well-documented hooks with some kind of
      middleware pattern? E.g., `before-set-parameter-values`,
      `after-set-parameter-values`, etc.?
    * Would it be better to refactor away from the "framework" approach, and
      provide an example composition that works OOTB, but with a documented use
      pattern to compose your own somewhere else, and just use this repo as a set
      of libraries?
* Make the `PeriodicInformInterval` multiplier configurable instead of hard-coded to `1/1000`.
* Finish the file-on-disk stateful-device implementation.
* full TR-069 spec compliance
* TR-369 support
    * Note that the Broadband Forum has published an open-source reference implementation for TR-369 already: https://github.com/BroadbandForum/obuspa
* auth -- currently assumes a testing environment in which the ACS does not require authentication
* mTLS
* refactor to avoid using a blocking thread per cwmp-client instance

