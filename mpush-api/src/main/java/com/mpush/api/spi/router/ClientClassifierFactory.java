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

package com.mpush.api.spi.router;

import com.mpush.api.router.ClientClassifier;
import com.mpush.api.spi.Factory;
import com.mpush.api.spi.SpiLoader;

/**
 * Created by ohun on 16/10/26.
 *
 * @author ohun@live.cn (夜色)
 */
public interface ClientClassifierFactory extends Factory<ClientClassifier> {

    static ClientClassifier create() {
        return SpiLoader.load(ClientClassifierFactory.class).get();
    }
}
