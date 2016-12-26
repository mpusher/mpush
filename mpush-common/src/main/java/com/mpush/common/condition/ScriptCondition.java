/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */

package com.mpush.common.condition;

import com.mpush.api.common.Condition;

import javax.script.*;
import java.util.Map;

/**
 * Created by ohun on 16/10/24.
 *
 * @author ohun@live.cn (夜色)
 */
public final class ScriptCondition implements Condition {
    private static final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private static final ScriptEngine jsEngine = scriptEngineManager.getEngineByName("js");

    private final String script;

    public ScriptCondition(String script) {
        this.script = script;
    }

    @Override
    public boolean test(Map<String, Object> env) {
        try {
            return (Boolean) jsEngine.eval(script, new SimpleBindings(env));
        } catch (Exception e) {
            return false;
        }
    }
}
