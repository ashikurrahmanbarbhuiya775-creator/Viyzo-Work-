package com.viyzo.worker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

/**
 * Viyzo Adaptive Network Resilience.
 *
 * Observes Android's selected default network and reports whether it has
 * validated Internet access. Android itself chooses/hands over Wi-Fi,
 * cellular and Ethernet networks; this class deliberately does not scan
 * nearby networks, request location, or force a potentially unsafe switch.
 *
 * API retry/backoff and offline request queuing must be applied by each
 * repository/API operation; this monitor never claims a request succeeded.
 */
public final class AdaptiveNetworkEngine {
    public enum State { ONLINE_WIFI, ONLINE_CELLULAR, ONLINE_ETHERNET, ONLINE_OTHER, OFFLINE, UNKNOWN }

    public interface Listener {
        void onNetworkStateChanged(State state, boolean metered);
    }

    private final ConnectivityManager connectivity;
    private final ConnectivityManager.NetworkCallback callback;
    private volatile State state = State.UNKNOWN;
    private volatile boolean metered = true;
    private volatile boolean registered = false;
    private Listener listener;

    public AdaptiveNetworkEngine(Context context, Listener listener) {
        Context app = context.getApplicationContext();
        connectivity = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.listener = listener;
        callback = new ConnectivityManager.NetworkCallback() {
            @Override public void onAvailable(Network network) { refresh(); }
            @Override public void onCapabilitiesChanged(Network network, NetworkCapabilities caps) { refresh(); }
            @Override public void onLost(Network network) { refresh(); }
        };
    }

    public synchronized void start() {
        if (registered || connectivity == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivity.registerDefaultNetworkCallback(callback);
            } else {
                NetworkRequest request = new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
                connectivity.registerNetworkCallback(request, callback);
            }
            registered = true;
        } catch (SecurityException | RuntimeException ignored) {
            state = State.UNKNOWN;
        }
        refresh();
    }

    public synchronized void stop() {
        if (!registered || connectivity == null) return;
        try { connectivity.unregisterNetworkCallback(callback); }
        catch (RuntimeException ignored) { }
        registered = false;
    }

    public State getState() { return state; }
    public boolean hasValidatedInternet() {
        return state == State.ONLINE_WIFI || state == State.ONLINE_CELLULAR
                || state == State.ONLINE_ETHERNET || state == State.ONLINE_OTHER;
    }
    public boolean isMetered() { return metered; }

    private void refresh() {
        State next = State.OFFLINE;
        boolean nextMetered = true;
        try {
            Network active = connectivity == null ? null : connectivity.getActiveNetwork();
            NetworkCapabilities caps = active == null ? null : connectivity.getNetworkCapabilities(active);
            if (caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                nextMetered = !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) next = State.ONLINE_WIFI;
                else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) next = State.ONLINE_CELLULAR;
                else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) next = State.ONLINE_ETHERNET;
                else next = State.ONLINE_OTHER;
            }
        } catch (SecurityException | RuntimeException ignored) {
            next = State.UNKNOWN;
        }
        state = next;
        metered = nextMetered;
        Listener current = listener;
        if (current != null) current.onNetworkStateChanged(next, nextMetered);
    }
}
