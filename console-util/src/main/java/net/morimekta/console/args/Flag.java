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
 * Similar to {@link Option}, but without any value argument. Can only
 * toggle boolean values.
 */
public class Flag extends BaseOption {
    public static final String NEGATE = "--no_";

    private final Consumer<Boolean> setter;

    private boolean applied = false;

    public Flag(String name,
                String shortNames,
                String usage,
                Consumer<Boolean> setter) {
        this(name, shortNames, usage, setter, null, false);
    }

    public Flag(String name,
                String shortNames,
                String usage,
                Consumer<Boolean> setter,
                Boolean defaultValue,
                boolean hidden) {
        super(name, shortNames, null,  usage, defaultValue == null ? null : defaultValue.toString(), false, false, hidden);
        this.setter = setter;
    }

    @Override
    public String getSingleLineUsage() {
        // Do not show "long" info in single-line usage if the unary option
        // has a short name.
        if (getShortNames().length() > 0) {
            return null;
        }

        return super.getSingleLineUsage();
    }

    @Override
    public void validate() {
    }

    @Override
    public int applyShort(String opts, ArgumentList args) {
        if (applied) {
            throw new ArgumentException(nameOrShort() + " is already set.");
        }
        applied = true;

        setter.accept(true);
        return 0;
    }

    @Override
    public int apply(ArgumentList args) {
        if (getName() == null) {
            throw new IllegalStateException("No long option for -[" + getShortNames() + "]");
        }

        if (applied) {
            throw new ArgumentException(nameOrShort() + " is already set.");
        }
        applied = true;

        String current = args.get(0);
        if (current.equals(getName())) {
            setter.accept(true);
        } else if (current.equals(altName())) {
            setter.accept(false);
        } else if (current.startsWith(getName() + "=")) {
            String value = current.substring(getName().length() + 1);
            setter.accept(Boolean.parseBoolean(value));
        }
        return 1;
    }

    public String altName() {
        if (getName() != null && getName().startsWith("--")) {
            return NEGATE + getName().substring(2);
        }
        return null;
    }
}
