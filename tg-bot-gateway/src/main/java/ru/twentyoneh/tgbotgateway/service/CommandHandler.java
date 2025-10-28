package ru.twentyoneh.tgbotgateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.twentyoneh.tgbotgateway.exception.UnknownCommandException;
import ru.twentyoneh.tgbotgateway.utils.Sender;

@Slf4j
@RequiredArgsConstructor
@Component
public class CommandHandler {

    public SendMessage handle(Update update) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        return switch (text) {
            case "/start" -> startCommandReceived(update);
            case "/help" -> helpCommandReceived(chatId);
//            case "/askquestion":

            default -> throw new UnknownCommandException("Неизвестная команда" + update.getMessage().getText());
        };
    }

    private SendMessage startCommandReceived(Update update) {
        Message msg = update.getMessage();

        Long chatId = msg.getChatId();
        String name = msg.getChat().getFirstName();
        String answer = "Привет, " + name + "!";
        log.info("Ответ пользователю: " + answer);

        return Sender.sendMessage(chatId, answer);
    }

    private SendMessage helpCommandReceived(Long chatId) {
        String help = "Бот для помощи студентам Noeflex, если нужно задать вопрос - /askquestion \n" +
                "Список команд: /start, /help, /askquestion";
        return Sender.sendMessage(chatId, help);
    }
}
