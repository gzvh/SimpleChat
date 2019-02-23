package com.multicastParty;

public class MessageParser {

    public static CustomMessage parse(String receivedMessageData) {
        String[] split = receivedMessageData.split(" ");
        CustomMessage customMessage = new CustomMessage();
        MessageType messageType = MessageType.valueOf(split[0]);
        customMessage.setMessageType(messageType);

        if (MessageType.JOIN.equals(messageType) || MessageType.LEFT.equals(messageType)) {
            customMessage.setRoom(split[1]);
            customMessage.setNickname(split[2]);
        } else if (MessageType.MSG.equals(messageType)) {
            customMessage.setNickname(split[1]);
            customMessage.setRoom(split[2]);
            if (split.length >= 4) {
                customMessage.setMessageBody(getAllFrom(split, 3));
            }
        } else if (MessageType.WHOIS.equals(messageType)) {
            customMessage.setRoom(split[1]);
            customMessage.setNickname(split[2]);
        } else if (MessageType.ROOM.equals(messageType)) {
            customMessage.setRoom(split[1]);
            customMessage.setNickname(split[2]);
        } else {
            customMessage.setNickname(split[1]);
            if (split.length >= 3) {
                customMessage.setMessageBody(getAllFrom(split, 2));
            }
        }
        return customMessage;
    }

    private static String getAllFrom(String[] split, int i) {
        StringBuilder sb = new StringBuilder();
        for(; i < split.length; i++) {
            sb.append(split[i]);
            sb.append(" ");
        }
        return sb.toString();
    }
}

