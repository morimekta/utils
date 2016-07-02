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

import net.morimekta.console.chr.CharUtil;
import net.morimekta.util.Strings;
import net.morimekta.util.io.IndentedPrintWriter;

import org.apache.commons.lang3.text.WordUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Argument argumentParser class. This is the actual argumentParser that is initialized with
 * a set of options, arguments and properties, and is then initialized with
 * the appropriate fields.
 */
public class ArgumentParser {
    /**
     * Create an argument argumentParser instance.
     *
     * @param program The program name.
     * @param version The program version.
     * @param description The program description.
     */
    public ArgumentParser(String program, String version, String description) {
        this(program, version, description, ArgumentOptions.defaults());
    }

    /**
     * Create an argument argumentParser instance.
     *
     * @param program The program name.
     * @param version The program version.
     * @param description The program description.
     * @param argumentOptions The argument options.
     */
    public ArgumentParser(String program, String version, String description, ArgumentOptions argumentOptions) {
        this.program = program;
        this.version = version;
        this.description = description;
        this.argumentOptions = argumentOptions;

        this.options = new LinkedList<>();
        this.arguments = new LinkedList<>();
        this.longNameOptions = new HashMap<>();
        this.shortOptions = new HashMap<>();
    }

    /**
     * The name of the program. Should be essentially what the user types on
     * the command line to invoke the program.
     *
     * @return The program name.
     */
    public String getProgram() {
        return program;
    }

    /**
     * The program version string.
     *
     * @return The program version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Short description of the program. Should be the string that is shown
     * on the top of the program usage help, usually just a few words. Should
     * be capitalized.
     *
     * @return The program description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Add a command line option.
     *
     * @param option The option to add.
     * @return The argument argumentParser.
     */
    public ArgumentParser add(BaseOption option) {
        this.options.add(option);
        if (option.getName() != null) {
            if (longNameOptions.containsKey(option.getName())) {
                throw new IllegalArgumentException("Option " + option.getName() + " already exists.");
            }

            longNameOptions.put(option.getName(), option);
        }

        if (option instanceof Flag) {
            String alt = ((Flag) option).altName();
            if (alt != null) {
                if (longNameOptions.containsKey(alt)) {
                    throw new IllegalArgumentException("Option " + alt + " already exists.");
                }

                longNameOptions.put(alt, option);
            }
        }

        if (option.getShortNames()
                  .length() > 0) {
            for (char s : option.getShortNames()
                                .toCharArray()) {
                if (shortOptions.containsKey(s)) {
                    throw new IllegalArgumentException("Short option -" + s + " already exists.");
                }

                shortOptions.put(s, option);
            }
        }

        return this;
    }

    /**
     * Add a sub-command.
     *
     * @param arg The command to add.
     * @return The argument argumentParser.
     */
    public ArgumentParser add(BaseArgument arg) {
        if (arg instanceof BaseOption) {
            return add((BaseOption) arg);
        }

        // No arguments can be added after a sub-command-set.
        if (arguments.size() > 0 && arguments.getLast() instanceof SubCommandSet) {
            throw new IllegalArgumentException("No arguments can be added after a sub-command set.");
        }

        arguments.add(arg);
        return this;
    }

    /**
     * Parse arguments from the main method.
     *
     * @param args The argument array.
     */
    public void parse(String... args) {
        parse(new ArgumentList(args));
    }

    /**
     * Parse arguments from the main method.
     *
     * @param args The argument list.
     */
    public void parse(ArgumentList args) {
        while (args.remaining() > 0) {
            String cur = args.get(0);

            if (cur.equals("--")) {
                // The remaining *must* be arguments / sub-commands.
                args.consume(1);
                break;
            } else if (cur.startsWith("--")) {
                // long opt.
                if (cur.contains("=")) {
                    cur = cur.substring(0, cur.indexOf("="));
                }
                BaseOption opt = longNameOptions.get(cur);
                if (opt == null) {
                    throw new ArgumentException("No option for " + cur);
                }
                args.consume(opt.apply(args));
            } else if (cur.startsWith("-")) {
                // short opt.
                String remaining = cur.substring(1);
                while (remaining.length() > 0) {
                    BaseOption opt = shortOptions.get(remaining.charAt(0));
                    if (opt == null) {
                        throw new ArgumentException("No short opt for -" + remaining.charAt(0));
                    }
                    int skip = opt.applyShort(remaining, args);
                    if (skip == 0) {
                        remaining = remaining.substring(1);
                    } else {
                        args.consume(skip);
                        break;
                    }
                }
            } else {
                if (cur.startsWith("@")) {
                    File f = new File(cur.substring(1));
                    if (f.exists() && f.isFile()) {
                        try (FileInputStream fis = new FileInputStream(f);
                             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
                            List<String> lines = reader.lines()
                                                       .map(String::trim)
                                                       .filter(l -> !(l.isEmpty() || l.startsWith("#")))
                                                       .collect(Collectors.toList());
                            if (lines.size() > 0) {
                                ArgumentList list = new ArgumentList(lines.toArray(new String[lines.size()]));
                                parse(list);
                            }
                        } catch (ArgumentException e) {
                            throw new ArgumentException(e, "Argument file " + f.getName() + ": " + e.getMessage());
                        } catch (IOException e) {
                            throw new ArgumentException(e, e.getMessage());
                        }
                        args.consume(1);
                        continue;
                    }
                }

                // Argument / sub-command.
                int consumed = 0;
                for (BaseArgument arg : arguments) {
                    consumed = arg.apply(args);
                    if (consumed > 0) {
                        break;
                    }
                }
                if (consumed == 0) {
                    throw new ArgumentException("No option found for " + args.get(0));
                }
                args.consume(consumed);
            }
        }

        // Then consume the rest as arguments.
        while (args.remaining() > 0) {
            // Argument / sub-command.
            int consumed = 0;
            for (BaseArgument arg : arguments) {
                consumed = arg.apply(args);
                if (consumed > 0) {
                    break;
                }
            }
            if (consumed == 0) {
                throw new ArgumentException("No argument found for " + args.get(0));
            }
            args.consume(consumed);
        }
    }

    /**
     * Validate all options and arguments.
     */
    public void validate() {
        options.forEach(BaseArgument::validate);
        arguments.forEach(BaseArgument::validate);
    }

    public void printUsage(OutputStream out) {
        printUsage(out, false);
    }

    public void printUsage(OutputStream out, boolean showHidden) {
        printUsage(new PrintWriter(out), showHidden);
    }

    /**
     * Print the option usage list. Essentially printed as a list of options
     * with the description indented where it overflows the available line
     * width.
     *
     * @param writer The output printer.
     * @param showHidden Whether to show hidden options.
     */
    public void printUsage(PrintWriter writer, boolean showHidden) {
        printUsage(new IndentedPrintWriter(writer), showHidden);
    }

    /**
     * Get the program description line. Contains essentially the line
     * "description - version".
     *
     * @return The program description.
     */
    public String getProgramDescription() {
        return description + " - " + version;
    }

    /**
     * Get the single line usage string for the parser. Contains essentially
     * the line "program options args".
     *
     * @return The single line usage.
     */
    public String getSingleLineUsage() {
        StringBuilder writer = new StringBuilder();
        writer.append(program);

        // first just list up all the unary short opts.
        String sh = "-";
        for (BaseOption opt : options) {
            if (opt instanceof Flag) {
                sh = sh + opt.getShortNames();
            }
        }

        if (sh.length() > 1) {
            writer.append('[').append(sh).append(']');
        }

        for (BaseOption opt : options) {
            if (opt instanceof Flag && opt.getShortNames().length() > 0) {
                // already included as short opt.
                continue;
            }
            String usage = opt.getSingleLineUsage();
            if (usage != null) {
                writer.append(' ').append(usage);
            }
        }
        for (BaseArgument arg : arguments) {
            String usage = arg.getSingleLineUsage();
            if (usage != null) {
                writer.append(' ').append(usage);
            }
        }

        return writer.toString();
    }

    public void printUsage(IndentedPrintWriter writer, boolean showHidden) {
        int usageWidth = argumentOptions.getUsageWidth();

        int prefixLen = 0;
        for (BaseOption opt : options) {
            prefixLen = Math.max(prefixLen, opt.getPrefix().length());
        }
        for (BaseArgument cmd : arguments) {
            prefixLen = Math.max(prefixLen, cmd.getName().length());
        }
        prefixLen = Math.min(prefixLen, (usageWidth / 3) - USAGE_EXTRA_CHARS);
        String indent = Strings.times(" ", prefixLen + USAGE_EXTRA_CHARS);

        if (options.size() > 0) {
            boolean first = true;

            if (argumentOptions.getOptionComparator() != null) {
                options.sort(argumentOptions.getOptionComparator());
            }

            for (BaseOption opt : options) {
                if (opt.isHidden() && !showHidden) {
                    continue;
                }

                String prefix = opt.getPrefix();
                String usage = opt.getUsage();
                if (argumentOptions.getDefaultsShown() && opt.getDefaultValue() != null) {
                    usage = usage + " (default: " + opt.getDefaultValue() + ")";
                }

                if (first) {
                    first = false;
                } else {
                    writer.appendln();
                }
                writer.begin(indent);

                printSingleUsageEntry(writer, prefix, usage, prefixLen, usageWidth);

                writer.end();
            }
        }

        if (arguments.size() > 0) {
            for (BaseArgument arg : arguments) {
                if (arg.isHidden() && !showHidden) {
                    continue;
                }

                String prefix = arg.getPrefix();
                String usage = arg.getUsage();
                if (argumentOptions.getDefaultsShown() && arg.getDefaultValue() != null) {
                    usage = usage + " (default: " + arg.getDefaultValue() + ")";
                }

                writer.appendln();
                writer.begin(indent);

                printSingleUsageEntry(writer, prefix, usage, prefixLen, usageWidth);

                writer.end();
            }
        }

        writer.newline()
              .flush();
    }

    private void printSingleUsageEntry(IndentedPrintWriter writer,
                                       String prefix,
                                       String usage,
                                       int prefixLen,
                                       int usageWidth) {
        String[] descLines;
        int printLinesFrom = 0;

        writer.append(" ");

        if (prefix.length() > prefixLen) {
            writer.append(prefix);
            if (prefix.length() > (prefixLen * 1.7)) {
                descLines = WordUtils.wrap(usage, usageWidth - prefixLen - USAGE_EXTRA_CHARS).split("[\\n]");
            } else {
                writer.append(" : ");

                String[] tmp = WordUtils.wrap(usage, usageWidth - prefix.length() - USAGE_EXTRA_CHARS).split("[\\n]", 2);
                writer.append(tmp[0]);

                if (tmp.length > 1) {
                    descLines = WordUtils.wrap(tmp[1].replaceAll("\\n", " "), usageWidth - prefixLen - USAGE_EXTRA_CHARS).split("[\\n]");
                } else {
                    descLines = new String[0];
                }
            }
        } else {
            writer.append(CharUtil.leftJust(prefix, prefixLen));
            writer.append(" : ");
            descLines = WordUtils.wrap(usage, usageWidth - prefixLen - USAGE_EXTRA_CHARS).split("[\\n]");
            writer.append(descLines[0]);
            printLinesFrom = 1;
        }

        for (int i = printLinesFrom; i < descLines.length; ++i) {
            writer.appendln(descLines[i]);
        }
    }

    private static final int USAGE_EXTRA_CHARS = 4;

    private final String                 program;
    private final String                 version;
    private final String                 description;
    private final ArgumentOptions        argumentOptions;

    private final LinkedList<BaseOption>     options;
    private final LinkedList<BaseArgument>   arguments;

    private final Map<String, BaseOption>    longNameOptions;
    private final Map<Character, BaseOption> shortOptions;
}
