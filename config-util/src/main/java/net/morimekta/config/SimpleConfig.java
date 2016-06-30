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
package net.morimekta.config;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * Mutable configuration object backed by a tree map.
 */
public class SimpleConfig extends TreeMap<String,Object> implements ConfigBuilder {
    /**
     * Create an empty config instance.
     */
    public SimpleConfig() {}

    /**
     * Create an empty config instance with parent and base.
     *
     * @param base The base config (or super-config).
     */
    public SimpleConfig(Config base) {
        for (String key : base.keySet()) {
            put(key, base.get(key));
        }
    }

    @Override
    public Object get(String key) {
        return super.get(key);
    }

    @Override
    public boolean containsKey(String key) {
        return super.containsKey(key);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || !(o instanceof Config)) {
            return false;
        }
        Config other = (Config) o;

        if (!keySet().equals(other.keySet())) {
            return false;
        }

        try {
            for (String key : keySet()) {
                Object value = get(key);
                if (value instanceof Double) {
                    if (ValueUtil.asDouble(value) != other.getDouble(key)) {
                        return false;
                    }
                } else if (value instanceof Number) {
                    if (ValueUtil.asLong(value) != other.getLong(key)) {
                        return false;
                    }
                } else if (value instanceof Collection) {
                    Collection our = getSequence(key);
                    Collection their = other.getSequence(key);
                    if (our.size() != their.size()) return false;

                    Iterator outIt = our.iterator();
                    Iterator theirIt = their.iterator();

                    while (outIt.hasNext() && theirIt.hasNext()) {
                        Object ov = outIt.next();
                        Object tv = theirIt.next();
                        if (ov instanceof Double) {
                            if (ValueUtil.asDouble(ov) != ValueUtil.asDouble(tv)) {
                                return false;
                            }
                        } else if (ov instanceof Number) {
                            if (ValueUtil.asLong(ov) != ValueUtil.asLong(tv)) {
                                return false;
                            }
                        } else {
                            if (!value.equals(other.get(key))) {
                                return false;
                            }
                        }

                    }
                } else {
                    if (!value.equals(other.get(key))) {
                        return false;
                    }
                }
            }
        } catch (ConfigException e) {
            return false;
        }
        return true;
    }
}
