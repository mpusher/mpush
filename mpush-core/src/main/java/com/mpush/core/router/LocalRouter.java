/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.core.router;

import com.mpush.api.connection.Connection;
import com.mpush.api.router.Router;

/**
 * Created by ohun on 2015/12/23.
 *
 * @author ohun@live.cn
 */
public final class LocalRouter implements Router<Connection> {
    private final Connection connection;

    public LocalRouter(Connection connection) {
        this.connection = connection;
    }

    public int getClientType() {
        return connection.getSessionContext().getClientType();
    }

    @Override
    public Connection getRouteValue() {
        return connection;
    }

    @Override
    public RouterType getRouteType() {
        return RouterType.LOCAL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalRouter that = (LocalRouter) o;

        return getClientType() == that.getClientType();

    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getClientType());
    }

    @Override
    public String toString() {
        return "LocalRouter{" + connection + '}';
    }
}
