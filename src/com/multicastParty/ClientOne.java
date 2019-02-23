package com.multicastParty;

public class ClientOne {

    public static void main(String[] args) {
        Multicast multicastThread = new Multicast();
        multicastThread.run();
    }
}

