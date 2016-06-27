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

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * TODO(morimekta): Make a real class description.
 */
public class SubCommandSet<SubCommandDef> extends BaseArgument {
    private final List<SubCommand<SubCommandDef>> subCommands;
    private final Consumer<SubCommandDef> consumer;

    private boolean                   applied;
    private ArgumentParser            parser;

    public SubCommandSet(String name, String usage,
                         Consumer<SubCommandDef> consumer) {
        super(name, usage, null, false, true, false);

        this.subCommands = new LinkedList<>();
        this.consumer = consumer;
    }

    /**
     * Add a sub-command to the sub-command-set.
     *
     * @param subCommand The sub-command to add.
     * @return The sub-command-set.
     */
    public SubCommandSet add(SubCommand<SubCommandDef> subCommand) {
        this.subCommands.add(subCommand);
        return this;
    }

    @Override
    public String getSingleLineUsage() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(String.join(" | ", subCommands.stream()
                                                .filter(s -> !s.isHidden())
                                                .map(SubCommand::getName)
                                                .collect(Collectors.toList())));
        sb.append("] [...]");

        return sb.toString();
    }

    @Override
    public String getPrefix() {
        return getName();
    }

    @Override
    public void validate() throws ArgumentException {
        if (!applied) {
            throw new ArgumentException(getName() + " not chosen.");
        }
        parser.validate();
    }

    @Override
    public int apply(ArgumentList args) {
        if (applied) {
            throw new ArgumentException(getName() + " already selected.");
        }

        String name = args.get(0);
        for (SubCommand<SubCommandDef> cmd : subCommands) {
            if (cmd.getName().equals(name) || cmd.getAliases().contains(name)) {
                SubCommandDef instance = cmd.newInstance();
                parser = cmd.getArgumentParser(instance);
                applied = true;
                break;
            }
        }

        if (!applied) {
            throw new ArgumentException("No such " + getName() + " " + name);
        }
        applied = true;

        // Skip the sub-command name itself, and parse the remaining args
        // in the sub-command argument argumentParser.
        ArgumentList copy = new ArgumentList(args);
        copy.consume(1);
        parser.parse(copy);

        return args.remaining();
    }
}
