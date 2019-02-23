package com.multicastParty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MulticastSender extends MulticastParent {

    private State state = State.REQUEST_NICKNAME;
    private String nickname;
    private String room;
    private String requestedNickname;
    private Map<String, Set<String>> rooms = new HashMap<>();

    public MulticastSender(MulticastSocket socket, InetAddress inetAddress, int port) {
        super(socket, inetAddress, port);
    }

    @Override
    public void run() {
        while (isRunning) {
            switch (state) {
                case REQUEST_NICKNAME: {
                    String nickname = getFromKeyboard(MessageType.NICK);
                    broadcastMessage(getRequestNicknameMessage(nickname));
                    requestedNickname = nickname;
                    setState(State.WAIT_FOR_NICKNAME);
                    break;
                }
                case WAIT_FOR_NICKNAME: {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case REQUEST_ROOM: {
                    String room = getFromKeyboard(MessageType.JOIN);
                    broadcastMessage(getJoinRoom(room));
                    this.room = room;
                    setState(State.ALL_SET);
                    broadcastMessage(getWhoIs());
                    break;
                }
                case ALL_SET: {
                    String message = getFromKeyboard(MessageType.MSG);
                    String brMessage;
                    brMessage = processKeyboardMessage(message);
                    broadcastMessage(brMessage);
                    break;
                }
                default:
            }
        }
    }

    private String processKeyboardMessage(String message) {
        String brMessage;
        if (isLeave(message)) {
            brMessage = getLeaveMessage();
            setState(State.REQUEST_ROOM);
        } else if (isWhoIs(message)) {
            brMessage = getWhoIs();
        } else {
            brMessage = getMessage(message);
        }
        return brMessage;
    }

    private String getWhoIs() {
        return String.format("%s %s %s", MessageType.WHOIS, room, nickname);
    }

    private boolean isWhoIs(String message) {
        return "WHOIS".equals(message);
    }

    private String getLeaveMessage() {
        return String.format("%s %s %s", MessageType.LEFT, room, nickname);
    }

    private boolean isLeave(String message) {
        return "LEAVE".equals(message);
    }

    private String getJoinRoom(String room) {
        return String.format("%s %s %s", MessageType.JOIN, room, nickname);
    }

    private String getMessage(String message) {
        return String.format("%s %s %s %s", MessageType.MSG, nickname, room, message);
    }

    private String getRequestNicknameMessage(String nickname) {
        return String.format("%s %s", MessageType.NICK, nickname);
    }

    private synchronized void broadcastMessage(String message) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket data = new DatagramPacket(buffer, buffer.length, inetAddress, port);
            socket.send(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFromKeyboard(MessageType type) {
        try {
            if (MessageType.NICK.equals(type)) {
                System.out.println("Podaj nickname: ");
            } else if (MessageType.MSG.equals(type)) {
                System.out.println("Wpisz wiadomość: ");
            } else if (MessageType.JOIN.equals(type)) {
                System.out.println("Dołącz do pokoju: ");
            }
            InputStreamReader inputStream = new InputStreamReader(System.in);
            BufferedReader inFromUser = new BufferedReader(inputStream);
            return inFromUser.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void applyRequestedName() {
        if (nickname == null && requestedNickname != null && State.WAIT_FOR_NICKNAME.equals(state)) {
            System.out.println("Nickname ok");
            setNickname(requestedNickname);
            setState(State.REQUEST_ROOM);
        }
    }

    public void sendBusy() {
        System.out.println("Sending nickname " + nickname + " busy");
        broadcastMessage(getNicknameBusy());
    }

    private String getNicknameBusy() {
        return String.format("%s %s %s", MessageType.NICK, nickname, "BUSY");
    }

    public String getNickname() {
        return nickname;
    }

    public synchronized void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRequestedNickname() {
        return requestedNickname;
    }

    public void setRequestedNickname(String requestedNickname) {
        this.requestedNickname = requestedNickname;
    }

    public synchronized void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public void stop() {
        this.isRunning = false;
    }

    public String getRoom() {
        return this.room;
    }

    public Map<String, Set<String>> getRooms() {
        return rooms;
    }

    public void sendImInTheRoom() {
        broadcastMessage(getImInRoom());
    }

    private String getImInRoom() {
        return String.format("%s %s %s", MessageType.ROOM, room, nickname);
    }
}

