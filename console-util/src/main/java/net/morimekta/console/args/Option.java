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

import java.util.function.Consumer;

/**
 * Named option that for each invocation takes a single argument value,
 * either the next argument, or from the same argument after a '='
 * delimiter.
 */
public class Option extends BaseOption {
    private final Consumer<String> setter;

    private boolean applied = false;

    public Option(String name,
                  String shortNames,
                  String metaVar,
                  String usage,
                  Consumer<String> setter) {
        this(name, shortNames, metaVar, usage, setter, null);
    }

    public Option(String name,
                  String shortNames,
                  String metaVar,
                  String usage,
                  Consumer<String> setter,
                  String defaultValue) {
        this(name, shortNames, metaVar, usage, setter, defaultValue, false, false, false);
    }

    public Option(String name,
                  String shortNames,
                  String metaVar,
                  String usage,
                  Consumer<String> setter,
                  String defaultValue,
                  boolean repeated,
                  boolean required,
                  boolean hidden) {
        super(name, shortNames, metaVar, usage, defaultValue, repeated, required, hidden);
        this.setter = setter;
    }

    @Override
    public int applyShort(String opts, ArgumentList args) {
        if (opts.length() == 1) {
            return apply(args);
        } else {
            if (applied && !isRepeated()) {
                throw new ArgumentException("Option " + nameOrShort() + " used more than once");
            }
            applied = true;

            String value = opts.substring(1);
            setter.accept(value);
            return 1;
        }
    }

    @Override
    public void validate() throws ArgumentException {
        if (isRequired() && !applied) {
            throw new ArgumentException("Option " + nameOrShort() + " is required");
        }
    }

    @Override
    public int apply(ArgumentList args) throws ArgumentException {
        if (applied && !isRepeated()) {
            throw new ArgumentException("Option " + nameOrShort() + " used more than once");
        }
        applied = true;

        String current = args.get(0);
        if (getName() != null && current.startsWith(getName() + "=")) {
            String value = current.substring(getName().length() + 1);
            setter.accept(value);
            return 1;
        }

        if (args.remaining() < 2) {
            throw new ArgumentException("Missing value after " + getName());
        }
        setter.accept(args.get(1));
        return 2;
    }
}
