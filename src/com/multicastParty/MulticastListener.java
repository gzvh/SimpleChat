package com.multicastParty;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class MulticastListener extends MulticastParent {

    public static final String BUSY = "BUSY";

    public MulticastListener(MulticastSocket multicastSocket, InetAddress inetAddress, int port) {
        super(multicastSocket, inetAddress, port);
    }


    @Override
    public void run() {
        while (isRunning) {
            try {
                byte[] responseBuff = new byte[2048];
                DatagramPacket response = new DatagramPacket(responseBuff, responseBuff.length);
                socket.receive(response);
                String receivedMessageData = new String(response.getData(), response.getOffset(), response.getLength());
                CustomMessage customMessage = MessageParser.parse(receivedMessageData);
                handleMessage(customMessage);
            } catch (SocketTimeoutException te) {
                MulticastSender sender = CustomPool.getInstance().getSenderThread();
                sender.applyRequestedName();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        leaveGroup();
    }

    private void handleMessage(CustomMessage customMessage) {
        MulticastSender senderThread = CustomPool.getInstance().getSenderThread();
        String nickname = senderThread.getNickname();
        if (nickname != null) {
            switch (customMessage.getMessageType()) {
                case NICK: {
                    handleNickRequest(customMessage, senderThread, nickname);
                    break;
                }
                case JOIN: {
                    handleJoin(customMessage, senderThread, nickname);
                    break;
                }
                case LEFT: {
                    handleLeft(customMessage, senderThread, nickname);
                    break;
                }
                case WHOIS: {
                    handleWhoIs(customMessage, senderThread, nickname);
                    break;
                }
                case ROOM: {
                    handleRoom(customMessage, senderThread);
                    break;
                }
                case MSG: {
                    handleSimpleMessage(customMessage, senderThread);
                    break;
                }
                default:
            }

        } else if (MessageType.NICK.equals(customMessage.getMessageType()) && BUSY.equals(customMessage.getMessageBody())) {
            handleNickBusy(customMessage, senderThread);
        }
    }

    private void handleNickBusy(CustomMessage customMessage, MulticastSender senderThread) {
        if (customMessage.getNickname().equals(senderThread.getRequestedNickname())) {
            //nasz nick jest zajęty
            System.out.println("Ninkname " + customMessage.getNickname() + " zajęty");
            senderThread.setState(State.REQUEST_NICKNAME);
        }
    }

    private void handleSimpleMessage(CustomMessage customMessage, MulticastSender senderThread) {
        if (customMessage.getRoom().equals(senderThread.getRoom())) {
            printMessage(customMessage);
        }
    }

    private void handleRoom(CustomMessage customMessage, MulticastSender senderThread) {
        Map<String, Set<String>> roomsMap = senderThread.getRooms();
        Set<String> nicknames = Optional.ofNullable(roomsMap.get(customMessage.getRoom())).orElse(new HashSet<>());
        nicknames.add(customMessage.getNickname());
        roomsMap.put(customMessage.getRoom(), nicknames);
    }

    private void handleNickRequest(CustomMessage customMessage, MulticastSender senderThread, String nickname) {
        if (nickname.equals(customMessage.getNickname()) && !BUSY.equals(customMessage.getMessageBody())) {
            senderThread.sendBusy();
        }
    }

    private void handleJoin(CustomMessage customMessage, MulticastSender senderThread, String nickname) {
        if (nickname.equals(customMessage.getNickname())) {
            System.out.println("Dołączyłeś do pokoju " + customMessage.getRoom());
        } else if (senderThread.getRoom().equals(customMessage.getRoom())) {
            System.out.println(customMessage.getNickname() + " dołączył do pokoju");
            Set<String> nicknames = senderThread.getRooms().get(customMessage.getRoom());
            Optional.ofNullable(nicknames).ifPresent(names -> names.add(customMessage.getNickname()));
        }
    }

    private void handleLeft(CustomMessage customMessage, MulticastSender senderThread, String nickname) {
        if (nickname.equals(customMessage.getNickname())) {
            System.out.println("Wyszedłeś z pokoju " + customMessage.getRoom());
        } else if (senderThread.getRoom().equals(customMessage.getRoom())) {
            System.out.println(customMessage.getNickname() + " wyszedł z pokoju");
        }
        Set<String> nicknames = senderThread.getRooms().get(customMessage.getRoom());
        Optional.ofNullable(nicknames)
                .ifPresent(names -> names.remove(customMessage.getNickname()));
    }

    private void handleWhoIs(CustomMessage customMessage, MulticastSender senderThread, String nickname) {
        String messageRoom = customMessage.getRoom();
        String currentRoom = senderThread.getRoom();
        if (messageRoom.equals(currentRoom)) {
            if (customMessage.getNickname().equals(nickname)) {
                if (senderThread.getRooms().containsKey(currentRoom)) {
                    System.out.println("Osoby w pokoju " + messageRoom + ": ");
                    senderThread.getRooms().get(currentRoom).forEach(System.out::println);
                }
            }
            senderThread.sendImInTheRoom();
        }
    }

    private void printMessage(CustomMessage customMessage) {
        System.out.println(String.format("%s: %s", customMessage.getNickname(), customMessage.getMessageBody()));
    }

    private void leaveGroup() {
        try {
            socket.leaveGroup(inetAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

