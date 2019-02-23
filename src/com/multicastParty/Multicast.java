package com.multicastParty;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Multicast implements Runnable {

    private static final int PORT = 2222;
    private static final String HOST = "239.0.0.222";

    @Override
    public void run() {
        try {
            InetAddress inetAddress = InetAddress.getByName(HOST);
            MulticastSocket listenerSocket = getNewSocket(inetAddress);
            MulticastSocket senderSocket = getNewSocket(inetAddress);

            listenerSocket.setSoTimeout(3000);

            MulticastListener listener = new MulticastListener(listenerSocket, inetAddress, PORT);
            MulticastSender sender = new MulticastSender(senderSocket, inetAddress, PORT);

            new Thread(listener).start();
            new Thread(sender).start();

            CustomPool customPool = CustomPool.getInstance();
            customPool.setSenderThread(sender);

            Thread.sleep(3600 * 1000);
            sender.stop();
            listener.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MulticastSocket getNewSocket(InetAddress inetAddress) throws IOException {
        MulticastSocket socket = new MulticastSocket(PORT);
        socket.joinGroup(inetAddress);
        return socket;
    }
}
