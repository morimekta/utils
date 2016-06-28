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
import java.util.function.Predicate;

/**
 * TODO(morimekta): Make a real class description.
 */
public class Argument extends BaseArgument {
    private final Consumer<String> consumer;
    private final Predicate<String> predicate;

    private boolean applied;

    public Argument(String name, String usage, String defaultValue,
                       Consumer<String> consumer, Predicate<String> predicate,
                       boolean repeated, boolean required, boolean hidden) {
        super(name, usage, defaultValue, repeated, required, hidden);
        this.consumer = consumer;
        this.predicate = predicate == null ? s -> true : predicate;
    }

    @Override
    public String getSingleLineUsage() {
        if (isHidden()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        if (!isRequired()) {
            sb.append("[");
        }
        sb.append(getName());
        if (isRepeated()) {
            sb.append("...");
        }
        if (!isRequired()) {
            sb.append("]");
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
            throw new ArgumentException("Argument \"" + getName() + "\" is required");
        }
    }

    @Override
    public int apply(ArgumentList args) {
        if (applied && !isRepeated()) {
            return 0;
        }
        String cur = args.get(0);
        if (!predicate.test(cur)) {
            return 0;
        }
        applied = true;
        consumer.accept(cur);
        return 1;
    }
}
