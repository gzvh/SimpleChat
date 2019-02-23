package com.multicastParty;

public class ClientTwo {

    public static void main(String[] args) {
        Multicast multicastThread = new Multicast();
        multicastThread.run();
    }
}

