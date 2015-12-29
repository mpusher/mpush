package com.shinemo.mpush.gateway.router;

import com.shinemo.mpush.api.Router;
import com.shinemo.mpush.api.ClientLocation;

/**
 * Created by ohun on 2015/12/23.
 */
public class RemoteRouter implements Router<ClientLocation> {
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
}
