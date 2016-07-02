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
    private final String negateName;

    private boolean applied = false;

    public Flag(String name,
                String shortNames,
                String usage,
                Consumer<Boolean> setter) {
        this(name, shortNames, usage, setter, null);
    }

    public Flag(String name,
                String shortNames,
                String usage,
                Consumer<Boolean> setter,
                Boolean defaultValue) {
        this(name, shortNames, usage, setter, defaultValue, false);
    }

    public Flag(String name,
                String shortNames,
                String usage,
                Consumer<Boolean> setter,
                Boolean defaultValue,
                String negateName) {
        this(name, shortNames, usage, setter, defaultValue, negateName, false);
    }

    public Flag(String name,
                String shortNames,
                String usage,
                Consumer<Boolean> setter,
                Boolean defaultValue,
                boolean hidden) {
        this(name, shortNames, usage, setter, defaultValue, makeNegateName(name), hidden);
    }

    public Flag(String name,
                String shortNames,
                String usage,
                Consumer<Boolean> setter,
                Boolean defaultValue,
                String negateName,
                boolean hidden) {
        super(name, shortNames, null, usage, defaultValue == null ? null : defaultValue.toString(), false, false, hidden);
        this.setter = setter;
        this.negateName = negateName;
    }

    /**
     * The alternative (negating) long name for the flag.
     *
     * @return The negating name.
     */
    public String getNegateName() {
        return this.negateName;
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
            throw new ArgumentException(nameOrShort() + " is already applied");
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
            throw new ArgumentException(nameOrShort() + " is already applied");
        }
        applied = true;

        String current = args.get(0);
        if (current.equals(getName())) {
            setter.accept(true);
        } else if (current.equals(getNegateName())) {
            setter.accept(false);
        } else if (current.startsWith(getName() + "=")) {
            String value = current.substring(getName().length() + 1);
            setter.accept(Boolean.parseBoolean(value));
        } else {
            throw new IllegalArgumentException("Argument not matching flag " + nameOrShort() + ": " + current);
        }
        return 1;
    }

    private static String makeNegateName(String name) {
        if (name != null && name.startsWith("--")) {
            return NEGATE + name.substring(2);
        }
        return null;
    }
}
