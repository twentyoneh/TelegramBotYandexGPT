package ru.twentyoneh.tgbotgateway.utils;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class Sender {
    public static SendMessage sendMessage (Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(text);
        return sendMessage;
    }
}
