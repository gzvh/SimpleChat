package com.multicastParty;

public class CustomPool {

    private static CustomPool customPool;
    private MulticastSender senderThread;

    private CustomPool() {
    }

    static public CustomPool getInstance() {
        if (customPool == null) {
            customPool = new CustomPool();
        }

        return customPool;
    }

    public MulticastSender getSenderThread() {
        return senderThread;
    }

    public void setSenderThread(MulticastSender senderThread) {
        this.senderThread = senderThread;
    }
}

