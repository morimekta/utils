/*
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
package net.morimekta.util.json;

/**
 * Unchecked (runtime) JsonException wrapper that can be used in streams etc.
 */
public class UncheckedJsonException extends RuntimeException {
    public UncheckedJsonException(JsonException e) {
        super(e);
    }

    public JsonException getCause() {
        return (JsonException) super.getCause();
    }

    public String getLine() {
        return getCause().getLine();
    }

    public int getLineNo() {
        return getCause().getLineNo();
    }

    public int getLinePos() {
        return getCause().getLinePos();
    }

    public int getLen() {
        return getCause().getLen();
    }

    public String describe() {
        return getCause().describe();
    }

    @Override
    public String getMessage() {
        return getCause().getMessage();
    }

    @Override
    public String getLocalizedMessage() {
        return getCause().getLocalizedMessage();
    }

    @Override
    public String toString() {
        return String.format("UncheckedJsonException(%s)", describe());
    }
}
