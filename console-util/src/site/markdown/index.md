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
parser.add(new Option("--connect_timeout", "C", "ms",
                      "Connection timeout in milliseconds. 0 means infinite.",
                      i32(this::setConnectTimeout), "10000"));
parser.add(new Option("--read_timeout",    "R", "ms",
                      "Request timeout in milliseconds. 0 means infinite.",
                      i32(this::setReadTimeout), "10000"));
parser.add(new Property('H',          "key", "value",
                        "Header",
                        i32(this::setHeader)));
parser.add(new Flag("--help",            "h?",
                    "This help message.",this::setHelp));
parser.add(new Argument("URL",
                        "The endpoint URI",
                        this::setUrl));

parser.parse(args);
```

Note that the interfaces may still change in the near future.

### Sub-Commands

Sub-commands require a little more setup to work. But not too much. First you
need some interface that all your sub-commands implement, and create a
`SubCommandSet` instance to hold them. The sub-command set is essentially an
`Argument` class that needs to be the last argument in the argument parser, and
you need to have a sub-command setter method:

- To set the sub-command: `void setSubCommand(Type instance);`

For each sub-command to be added you need to create an implementation class, and
provide three methods:

- To create the sub-command instance: `Type newInstance();`
- To create the argument parser for the instance: `ArgumentParser createParser(Type instance);`

If you add this to the ArgumentParser above:

```java
parser.add(new SubCommandSet<MySubCommandInterface>(
         "cmd", "The sub-command", this::setSubCommand)
    .add(new SubCommand<>("sub-1", "First sub-command...", false,
                          SubCommandA::new,
                          MySubCommandInterface::createParser))
    .add(...))

parser.parser(args);

// ...

getSubCommand().run();
```

And example sub-command implementation (assumes overrides matches interface).

```java
class SubCommandA implements SubCommand {
    @Override
    public ArgumentParser() {
        ArgumentParser parser = new ArgumentParser("program sub-1", "v0.1.2",
                                                   "My first subcommand.");
        
        // ...

        return parser;
    }
    
    @Override
    void run() {
        // ...
    }
}
```

Note that:

- The `SubCommandSet` parsing consumes all arguments regardless of what the
  given parser actually consumed. This is so the sub-command argument-parser can
  behave in the exact same manner as the 'root' argument-parser.
- The `SubCommandSet` argument is always required, and can only be applied once.
  I am planning to make the `required` property of the sub-command optional, but
  this was easier in to verify.
- The SubCommand implementations does not implement the `createParser` method as
  in my example, I just find this patters easier to use.
