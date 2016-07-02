Console Utilities
=================

Utilities for facilitating interactive command line interfaced (cli) targeted
mainly at utilities used in the linux terminals.

## Terminal

The `Terminal` class is the base of the main mean of interactive input in
console-util. The terminal itself is mostly just handling the terminal itself,
and you need to use one of the Input* classes to get interactive input. Though
there are some short-hands. E.g.:

```java
try (Terminal term = new Terminal()) {
    String username = new InputLine(term, "username").readLine();
    String password = new InputPassword(term, "password").readPassword();

    if (term.confirm("Log in now?")) {
        // ...
    }
    // ...
}
```

## Argument Parser

There is also a proper argument parser library too. It does not use annotations,
so you have to code up the arguments, but with heavy use of functional
interfaces, it should be pretty compact regardless. Example usage:

```java
ArgumentParser parser = new ArgumentParser("pvdrpc", "v2.0.0", "Providence RPC Tool");
parser.add(new Option("--connect_timeout", "C", "ms", "Connection timeout in milliseconds. 0 means infinite.", i32(this::setConnectTimeout), "10000"));
parser.add(new Option("--read_timeout",    "R", "ms", "Request timeout in milliseconds. 0 means infinite.",    i32(this::setReadTimeout), "10000"));
parser.add(new Property('H',          "key", "value", "Header",                                                i32(this::setHeader)));
parser.add(new Flag(  "--help",            "h?",      "This help message.",                                    this::setHelp));
parser.add(new Argument("URL",                        "The endpoint URI",                                      this::setUrl));

parser.parse(args);
```

Note that the interfaces may still change in the near future.
