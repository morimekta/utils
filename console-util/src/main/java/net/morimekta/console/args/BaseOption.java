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

/**
 * Argument definition base interface.
 */
public abstract class BaseOption extends BaseArgument {
    private final String  shortNames;
    private final String  metaVar;

    protected BaseOption(String name,
                         String shortNames,
                         String metaVar,
                         String usage,
                         String defaultValue,
                         boolean repeated,
                         boolean required,
                         boolean hidden) {
        super(name, usage, defaultValue, repeated, required, hidden);
        this.shortNames = shortNames == null ? "" : shortNames;
        this.metaVar = metaVar;

        if (getName() == null && getShortNames().length() == 0) {
            throw new IllegalArgumentException("Option must have name or short name");
        }
        if (getName() != null && !getName().startsWith("--")) {
            throw new IllegalArgumentException("Option name does not start with '--'");
        }
    }

    /**
     * Each character of the shortNames string is handled as a short option
     * that is parsed with the -[short] style. If the string is empty or null,
     * no short options are provided.
     *
     * @return The short names
     */
    public String getShortNames() {
        return shortNames;
    }

    /**
     * Meta variable to show in usage printout.
     *
     * @return The meta variable.
     */
    public String getMetaVar() {
        return metaVar;
    }

    @Override
    public String getSingleLineUsage() {
        if (isHidden()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        if (!isRequired()) {
            sb.append('[');
        }

        if (shortNames.length() > 0) {
            sb.append('-')
              .append(shortNames.charAt(0));
        } else {
            sb.append(getName());
        }

        if (getMetaVar() != null) {
            sb.append(' ')
              .append(getMetaVar());
        }

        if (!isRequired()) {
            sb.append(']');
        }

        return sb.toString();
    }

    @Override
    public String getPrefix() {
        StringBuilder sb = new StringBuilder();
        if (getName() != null) {
            sb.append(getName());
        }
        if (getShortNames().length() > 0) {
            if (getName() != null) {
                sb.append(" (");
            }
            boolean first = true;
            for (char c : getShortNames().toCharArray()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append('-')
                  .append(c);
            }
            if (getName() != null) {
                sb.append(")");
            }
        }
        if (getMetaVar() != null) {
            sb.append(' ')
              .append(getMetaVar());
        }
        return sb.toString();
    }

    /**
     * When handling a list of short options, except for the last short
     * option.
     *
     * @param opts The remaining characters of the short opt list.
     * @param args The list of arguments including the short opt list.
     * @return The number of arguments consumed. If 0 is returned, will handle
     *         as the short option char was the only thing being consumed.
     */
    public abstract int applyShort(String opts, ArgumentList args);

    /**
     * Parse the argument list, including the argument string that triggered
     * the call. And handle it's value or values.
     *
     * @param args The list of arguments.
     * @return The number of arguments consumed.
     * @throws ArgumentException If the passed arguments are not valid.
     */
    @Override
    public abstract int apply(ArgumentList args);

    /**
     * Handy getter mostly to be used in exception naming.
     *
     * @return The argument's name or first short name.
     */
    protected String nameOrShort() {
        if (getName() != null) {
            return getName();
        }
        return "-" + getShortNames().substring(0, 1);
    }
}
