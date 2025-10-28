package ru.twentyoneh.tgbotgateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.twentyoneh.tgbotgateway.config.BotConfig;
import ru.twentyoneh.tgbotgateway.exception.EmptyMessageException;
import ru.twentyoneh.tgbotgateway.exception.UnknownCommandException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    final private BotConfig config;
    final private QuestionHandler questionHandler;
    final private CommandHandler commandHandler;


    public TelegramBot(BotConfig config, QuestionHandler questionHandler, CommandHandler commandHandler) {
        this.config = config;
        this.commandHandler = commandHandler;
        this.questionHandler = questionHandler;
//        setCommands();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            var chatId = update.getMessage().getChatId();

            if (text.startsWith("/")) {
                try {
                    execute(commandHandler.handle(update));
                } catch (UnknownCommandException e) {
                    log.error("Error occurred UnknownCommandException: " + e.getMessage());
                    sendErrorMessage(chatId, "Неизвестная команда, используйте /help для получения списка доступных команд");
                } catch (RuntimeException e) {
                    log.error("Error occurred RuntimeException: " + e.getMessage());
                    sendErrorMessage(chatId, "Произошла ошибка, повторите попытку позже");
                } catch (TelegramApiException e) {
                    log.error("Error occurred TelegramApiException: " + e.getMessage());
                }

            }
            else {
                try {
                    execute(questionHandler.handle(update));
                } catch (TelegramApiException e) {
                    log.error("Error occurred TelegramApiException: " + e.getMessage());
                }
            }
        }
    }

    public void sendErrorMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    private void setCommands() {
        List<BotCommand> listofCommands = Arrays.asList(
                new BotCommand("/start", "Начальное приветствие"),
                new BotCommand("/askquestion", "Задать вопрос"),
                new BotCommand("/help", "Помощь")
        );

            SetMyCommands setMyCommands = new SetMyCommands();
            setMyCommands.setCommands(listofCommands);
            setMyCommands.setScope(new BotCommandScopeDefault());
        try {
            execute(setMyCommands);
        }
        catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
}
