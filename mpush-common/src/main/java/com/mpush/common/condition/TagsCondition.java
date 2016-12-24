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

import java.util.Map;
import java.util.Set;

/**
 * Created by ohun on 16/10/24.
 *
 * @author ohun@live.cn (夜色)
 */
public final class TagsCondition implements Condition {
    private final Set<String> tagList;

    public TagsCondition(Set<String> tags) {
        this.tagList = tags;
    }

    @Override
    public boolean test(Map<String, Object> env) {
        //2.按标签过滤,目前只有include, 后续会增加exclude
        String tags = (String) env.get("tags");
        return tags != null && tagList.stream().anyMatch(tags::contains);
    }
}
