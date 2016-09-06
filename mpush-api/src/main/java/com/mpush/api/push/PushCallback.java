package com.mpush.api.push;

import com.mpush.api.router.ClientLocation;

public interface PushCallback {

    void onSuccess(String userId, ClientLocation location);

    void onFailure(String userId, ClientLocation location);

    void onOffline(String userId, ClientLocation location);

    void onTimeout(String userId, ClientLocation location);
}