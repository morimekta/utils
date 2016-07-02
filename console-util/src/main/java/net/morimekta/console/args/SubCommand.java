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

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Sub command base class.
 *
 * @param <SubCommandDef> The sub-command instance type.
 */
public class SubCommand<SubCommandDef> {
    private final String       name;
    private final String       usage;
    private final List<String> aliases;
    private final boolean      hidden;

    private final Supplier<SubCommandDef>                 instanceFactory;
    private final Function<SubCommandDef, ArgumentParser> parserFactory;

    public SubCommand(String name, String usage, boolean hidden,
                      Supplier<SubCommandDef> instanceFactory,
                      Function<SubCommandDef, ArgumentParser> parserFactory,
                      String... aliases) {
        this(name, usage, hidden, instanceFactory, parserFactory, ImmutableList.copyOf(aliases));
    }

    public SubCommand(String name, String usage, boolean hidden,
                      Supplier<SubCommandDef> instanceFactory,
                      Function<SubCommandDef, ArgumentParser> parserFactory,
                      Iterable<String> aliases) {
        this.name = name;
        this.usage = usage;
        this.instanceFactory = instanceFactory;
        this.parserFactory = parserFactory;
        this.hidden = hidden;
        this.aliases = ImmutableList.copyOf(aliases);
    }

    /**
     * The sub-command name.
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * The basic usage description.
     *
     * @return The usage description.
     */
    public String getUsage() {
        return usage;
    }

    /**
     * If the sub-command is hidden by default.
     *
     * @return True if hidden.
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Get the list of sub-command aliases.
     *
     * @return The aliases.
     */
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * Instantiate the selected commands implementation.
     *
     * @return The new sub-command isntance.
     */
    public SubCommandDef newInstance() {
        return instanceFactory.get();
    }

    /**
     * Get the sub-commands internal argument argumentParser initializes with it's
     * own options.
     *
     * @param instance The instance to make parser for.
     * @return The argument argumentParser.
     */
    public ArgumentParser getArgumentParser(SubCommandDef instance) {
        return parserFactory.apply(instance);
    }
}
