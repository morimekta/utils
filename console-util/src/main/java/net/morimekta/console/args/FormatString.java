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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Localizable;

import java.util.Locale;

/**
 * Simple non-translated localizable (l10n) formatted string for use with
 * org.kohsuke.args4j.
 */
public class FormatString implements Localizable {
    private final String format;
    private final Locale locale;

    /**
     * Create a format-string with the given format.
     * @param format The string format.
     *
     * @see String#format(String, Object...) for formatting reference.
     */
    public FormatString(String format) {
        this(format, Locale.getDefault());
    }

    /**
     * Make a format-string with given format for given locale.
     *
     * @param format The string format.
     * @param locale The locale to format it for.
     */
    public FormatString(String format, Locale locale) {
        this.format = format;
        this.locale = locale;
    }

    @Override
    public String formatWithLocale(Locale locale, Object... args) {
        return String.format(locale, format, args);
    }

    @Override
    public String format(Object... args) {
        return String.format(locale, format, args);
    }

    /**
     * Convenience method to make an exception that matches the given
     * arguments without translations.
     *
     * @param cli The CLI parser to reference.
     * @param format The string format.
     * @param args The string arguments.
     * @return The exception generated.
     */
    public static CmdLineException except(CmdLineParser cli, String format, String... args) {
        return new CmdLineException(cli, new FormatString(format), args);
    }
}
