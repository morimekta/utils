/*
 * Copyright (c) 2016, Stein Eldar Johnsen
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.morimekta.console.args;

import net.morimekta.util.Strings;
import net.morimekta.util.io.IndentedPrintWriter;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.morimekta.console.args.ArgumentParser.USAGE_EXTRA_CHARS;
import static net.morimekta.console.args.ArgumentParser.printSingleUsageEntry;

/**
 * The argument part of the sub-command. The sub-command set is
 * a collection of sub-commands that react to CLI arguments. It will
 * <b>always</b> trigger (and throw {@link ArgumentException} if
 * not valid), so the sub-command <b>must</b> be added last.
 *
 * @param <SubCommandDef> The sub-command interface.
 */
public class SubCommandSet<SubCommandDef> extends BaseArgument {
    private final List<SubCommand<SubCommandDef>>        subCommands;
    private final Map<String, SubCommand<SubCommandDef>> subCommandMap;
    private final Consumer<SubCommandDef>                consumer;
    private final ArgumentOptions                        argumentOptions;

    private boolean                   applied;
    private ArgumentParser            parser;

    /**
     * Create a sub-command set.
     *
     * @param name The name of the sub-command.
     * @param usage The usage description.
     * @param consumer The sub-command consumer.
     */
    public SubCommandSet(String name, String usage,
                         Consumer<SubCommandDef> consumer) {
        this(name, usage, consumer, ArgumentOptions.defaults());
    }

    /**
     * Create an optional sub-command set.
     *
     * @param name The name of the sub-command.
     * @param usage The usage description.
     * @param consumer The sub-command consumer.
     * @param options Extra argument options.
     */
    public SubCommandSet(String name, String usage,
                         Consumer<SubCommandDef> consumer,
                         ArgumentOptions options) {
        this(name, usage, consumer, null, true, options);
    }

    /**
     * Create an optional sub-command set.
     *
     * @param name The name of the sub-command.
     * @param usage The usage description.
     * @param consumer The sub-command consumer.
     * @param defaultValue The default sub-command.
     */
    public SubCommandSet(String name, String usage,
                         Consumer<SubCommandDef> consumer,
                         String defaultValue) {
        this(name, usage, consumer, defaultValue, defaultValue == null, ArgumentOptions.defaults());
    }

    /**
     * Create an optional sub-command set.
     *
     * @param name The name of the sub-command.
     * @param usage The usage description.
     * @param consumer The sub-command consumer.
     * @param defaultValue The default sub-command.
     * @param required If the sub-command is required.
     * @param options Extra argument options.
     */
    public SubCommandSet(String name, String usage,
                         Consumer<SubCommandDef> consumer,
                         String defaultValue,
                         boolean required,
                         ArgumentOptions options) {
        super(name, usage, defaultValue, false, required, false);

        this.argumentOptions = options;
        this.subCommands = new ArrayList<>();
        this.subCommandMap = new HashMap<>();
        this.consumer = consumer;
    }

    /**
     * Add a sub-command to the sub-command-set.
     *
     * @param subCommand The sub-command to add.
     * @return The sub-command-set.
     */
    public SubCommandSet add(SubCommand<SubCommandDef> subCommand) {
        if (subCommandMap.containsKey(subCommand.getName())) {
            throw new IllegalArgumentException("SubCommand with name " + subCommand.getName() + " already exists");
        }
        this.subCommands.add(subCommand);
        this.subCommandMap.put(subCommand.getName(), subCommand);
        for (String alias : subCommand.getAliases()) {
            if (subCommandMap.containsKey(alias)) {
                throw new IllegalArgumentException("SubCommand (" + subCommand.getName() + ") alias " + alias + " already exists");
            }
            this.subCommandMap.put(alias, subCommand);
        }
        return this;
    }

    /**
     * Add a set of sub-commands to the sub-command-set.
     *
     * @param subCommands The sub-commands to add.
     * @return The sub-command-set.
     */
    @SafeVarargs
    public final SubCommandSet addAll(SubCommand<SubCommandDef>... subCommands) {
        for (SubCommand<SubCommandDef> subCommand : subCommands) {
            add(subCommand);
        }
        return this;
    }

    /**
     * Print the sub-command list.
     *
     * @param out The output stream.
     */
    public void printUsage(OutputStream out) {
        printUsage(out, false);
    }

    /**
     * Print the sub-command list.
     *
     * @param out The output stream.
     */
    public void printUsage(PrintWriter out) {
        printUsage(out, false);
    }

    /**
     * Print the sub-command list.
     *
     * @param out The output stream.
     * @param showHidden If hidden sub-commands should be printed.
     */
    public void printUsage(OutputStream out, boolean showHidden) {
        printUsage(new PrintWriter(new OutputStreamWriter(out, UTF_8)), showHidden);
    }

    /**
     * Print the sub-command list.
     *
     * @param writer The output printer.
     * @param showHidden Whether to show hidden options.
     */
    public void printUsage(PrintWriter writer, boolean showHidden) {
        if (writer instanceof IndentedPrintWriter) {
            printUsageInternal((IndentedPrintWriter) writer, showHidden);
        } else {
            printUsageInternal(new IndentedPrintWriter(writer), showHidden);
        }
    }

    /**
     * Print the option usage list for the command.
     *
     * @param out The output stream.
     * @param name The sub-command to print help for.
     */
    public void printUsage(OutputStream out, String name) {
        printUsage(out, name, false);
    }

    /**
     * Print the option usage list for the command.
     *
     * @param out The output stream.
     * @param name The sub-command to print help for.
     * @param showHidden If hidden sub-commands should be shown.
     */
    public void printUsage(OutputStream out, String name, boolean showHidden) {
        printUsage(new PrintWriter(new OutputStreamWriter(out, UTF_8)), name, showHidden);
    }

    /**
     * Print the option usage list for the command.
     *
     * @param writer The output printer.
     * @param name The sub-command to print help for.
     */
    public void printUsage(PrintWriter writer, String name) {
        printUsage(writer, name, false);
    }

    /**
     * Get the single line usage string for a given sub-command.
     *
     * @param name The sub-command to print help for.
     * @return The usage string.
     */
    public String getSingleLineUsage(String name) {
        for (SubCommand<SubCommandDef> cmd : subCommands) {
            if (name.equals(cmd.getName())) {
                return cmd.getArgumentParser(cmd.newInstance()).getSingleLineUsage();
            }
        }
        throw new ArgumentException("No such " + getName() + " " + name);
    }

    /**
     * Print the option usage list. Essentially printed as a list of options
     * with the description indented where it overflows the available line
     * width.
     *
     * @param writer The output printer.
     * @param name The sub-command to print help for.
     * @param showHidden Whether to show hidden options.
     */
    public void printUsage(PrintWriter writer, String name, boolean showHidden) {
        for (SubCommand<SubCommandDef> cmd : subCommands) {
            if (name.equals(cmd.getName())) {
                cmd.getArgumentParser(cmd.newInstance()).printUsage(writer, showHidden);
                return;
            }
        }
        throw new ArgumentException("No such " + getName() + " " + name);
    }

    @Override
    public String getSingleLineUsage() {
        StringBuilder sb = new StringBuilder();
        if (!isRequired()) {
            sb.append('[');
        }
        List<String> visible =
                subCommands.stream()
                           .filter(s -> !s.isHidden())
                           .map(SubCommand::getName)
                           .collect(Collectors.toList());
        // TODO(morimekta): Figure out a smarter (or more controlled) way of
        // choosing name vs command listing.
        if (visible.size() > 4 || visible.size() == 0) {
            sb.append(getName());
        } else {
            sb.append('[');
            sb.append(String.join(" | ", visible));
            sb.append(']');
        }
        sb.append(" [...]");
        if (!isRequired()) {
            sb.append(']');
        }

        return sb.toString();
    }

    @Override
    public String getPrefix() {
        return getName();
    }

    @Override
    public void validate() throws ArgumentException {
        if (isRequired() && !applied) {
            throw new ArgumentException(getName() + " not chosen");
        }
        parser.validate();
    }

    @Override
    public int apply(ArgumentList args) {
        if (applied) {
            throw new ArgumentException(getName() + " already selected");
        }

        String name = args.get(0);
        SubCommand<SubCommandDef> cmd = subCommandMap.get(name);
        if (cmd == null) {
            throw new ArgumentException("No such " + getName() + ": " + name);
        }
        applied = true;

        // Skip the sub-command name itself, and parse the remaining args
        // in the sub-command argument argumentParser.
        ArgumentList subArgs = new ArgumentList(args);
        subArgs.consume(1);

        SubCommandDef instance = cmd.newInstance();
        parser = cmd.getArgumentParser(instance);
        parser.parse(subArgs);
        consumer.accept(instance);

        return args.remaining();
    }

    private void printUsageInternal(IndentedPrintWriter writer, boolean showHidden) {
        int usageWidth = argumentOptions.getUsageWidth();

        int prefixLen = 0;
        for (SubCommand<SubCommandDef> cmd : subCommands) {
            prefixLen = Math.max(prefixLen,
                                 cmd.getName()
                                    .length());
        }
        prefixLen = Math.min(prefixLen, (usageWidth / 3) - USAGE_EXTRA_CHARS);
        String indent = Strings.times(" ", prefixLen + USAGE_EXTRA_CHARS);

        boolean first = true;
        for (SubCommand<SubCommandDef> arg : subCommands) {
            if (arg.isHidden() && !showHidden) {
                continue;
            }

            String prefix = arg.getName();
            String usage = arg.getUsage();

            if (first) {
                first = false;
            } else {
                writer.appendln();
            }
            writer.begin(indent);

            printSingleUsageEntry(writer, prefix, usage, prefixLen, usageWidth);

            writer.end();
        }

        writer.newline()
              .flush();
    }
}
