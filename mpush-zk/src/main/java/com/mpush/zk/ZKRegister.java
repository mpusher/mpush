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

package com.mpush.zk;

import com.mpush.zk.node.ZKNode;

import java.util.Objects;

/**
 * Created by ohun on 16/9/22.
 *
 * @author ohun@live.cn (夜色)
 */
public final class ZKRegister {
    private ZKNode node;
    private ZKPath path;
    private boolean ephemeral = true;
    private ZKClient client;

    public void register() {
        Objects.requireNonNull(this.node);
        String path = node.getNodePath();
        if (path == null) {
            Objects.requireNonNull(this.path);
            path = this.path.getNodePath();
        }
        if (ephemeral) {
            client.registerEphemeralSequential(path, node.encode());
        } else {
            client.registerPersist(path, node.encode());
        }
    }

    public static ZKRegister build() {
        ZKRegister register = new ZKRegister();
        register.client = ZKClient.I;
        return register;
    }

    public ZKClient getClient() {
        return client;
    }

    public ZKRegister setClient(ZKClient client) {
        this.client = client;
        return this;
    }

    public ZKNode getNode() {
        return node;
    }

    public ZKRegister setNode(ZKNode node) {
        this.node = node;
        return this;
    }

    public ZKPath getPath() {
        return path;
    }

    public ZKRegister setPath(ZKPath path) {
        this.path = path;
        return this;
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public ZKRegister setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
        return this;
    }
}
