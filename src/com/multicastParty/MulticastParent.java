package com.multicastParty;

import java.net.InetAddress;
import java.net.MulticastSocket;

public abstract class MulticastParent implements Runnable {
    protected MulticastSocket socket;
    protected InetAddress inetAddress;
    protected int port;
    protected boolean isRunning = true;

    public MulticastParent(MulticastSocket socket, InetAddress inetAddress, int port) {
        this.socket = socket;
        this.inetAddress = inetAddress;
        this.port = port;
    }

    public void stop() {
        this.isRunning = false;
    }
}

