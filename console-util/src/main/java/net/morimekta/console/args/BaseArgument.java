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
 * Arguments are part of the args list that is not designated with an
 * option name. E.g. files to read etc. Since a single program can have
 * multiple arguments, it's handled as a 'first accepted'.
 *
 * If the argument parsing hits a non '-' prefixed string, or has passed
 * the 'stop' option ('--'), it will try to parse it as an argument. It
 * will go through the available arguments until one has consumed it by
 * returning &gt; 0 on {@link BaseArgument#apply(ArgumentList)}.
 */
public abstract class BaseArgument {
    private final String  name;
    private final String  usage;
    private final String  defaultValue;
    private final boolean repeated;
    private final boolean required;
    private final boolean hidden;

    protected BaseArgument(String name,
                           String usage,
                           String defaultValue,
                           boolean repeated,
                           boolean required,
                           boolean hidden) {
        this.name = name;
        this.usage = usage;
        this.defaultValue = defaultValue;
        this.repeated = repeated;
        this.required = required;
        this.hidden = hidden;
    }

    /**
     * The argument name. This is visible in the single-line and usage
     * print-outs.
     *
     * @return The argument name.
     */
    public String getName() {
        return name;
    }

    /**
     * The argument usage description.
     *
     * @return The usdage description.
     */
    public String getUsage() {
        return usage;
    }

    /**
     * A default value descriptor.
     *
     * @return The default value or null.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * If the argument can be repeated (for arguments means to be multi-valued).
     *
     * @return True if the argument is repeated.
     */
    public boolean isRepeated() {
        return repeated;
    }

    /**
     * If the argument is required (must be set).
     *
     * @return True if the argument is required.
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * True if the argument should be hidden by default. Passing showHidden to
     * printUsage will print the option. It will be hidden from singleLineUsage
     * regardless.
     *
     * @return If the argument is hidden.
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Get the argument's single line usage string.
     *
     * @return The single-line usage string.
     */
    public abstract String getSingleLineUsage();

    /**
     * Prefix part of the usage usage message.
     *
     * @return The usage prefix.
     */
    public abstract String getPrefix();

    /**
     * Called on all the arguments after the parsing is done to validate
     * if all requirements have been passed. Should throw an
     * {@link ArgumentException} if is did not validate with the appropriate
     * error message.
     */
    public abstract void validate() throws ArgumentException;

    /**
     * Try to apply to the argument. The method shoud return 0 if the
     * argument is rejected, otherwise the number of argument strings
     * that was consumed.
     *
     * @param args The argument list.
     * @return The number of args consumed.
     */
    public abstract int apply(ArgumentList args);
}
