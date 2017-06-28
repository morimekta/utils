Config Utilities
================

Utilities for handling config files with a type safe interface. The purpose
of the config utilities is to be an interface between 'flat' structured
config files, and a deeply structured config. The input and output formats
of the config is always flat, or maximally sectioned in named 'prefix sections'.

**NOTE**: Since the `providence-config` project was more or less completed,
I have concluded that this library should be *deprecated* and the type-safe config
provided by providence used instead. But this library will live on for quite
a while, until `providence-config` is proven stable and has replaced `config-util`
in the most critical positions.