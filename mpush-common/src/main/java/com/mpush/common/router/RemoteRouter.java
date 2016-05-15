package com.mpush.common.router;

import com.mpush.api.router.Router;
import com.mpush.api.router.ClientLocation;

/**
 * Created by ohun on 2015/12/23.
 *
 * @author ohun@live.cn
 */
public final class RemoteRouter implements Router<ClientLocation> {
    private final ClientLocation clientLocation;

    public RemoteRouter(ClientLocation clientLocation) {
        this.clientLocation = clientLocation;
    }

    @Override
    public ClientLocation getRouteValue() {
        return clientLocation;
    }

    @Override
    public RouterType getRouteType() {
        return RouterType.REMOTE;
    }

    @Override
    public String toString() {
        return "RemoteRouter{" + clientLocation + '}';
    }
}
