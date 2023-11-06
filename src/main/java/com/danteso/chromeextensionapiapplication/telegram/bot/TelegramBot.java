package com.danteso.chromeextensionapiapplication.telegram.bot;

import com.danteso.chromeextensionapiapplication.entity.Description;
import com.danteso.chromeextensionapiapplication.entity.Term;
import com.danteso.chromeextensionapiapplication.game.GameEngine;
import com.danteso.chromeextensionapiapplication.repo.TermRepository;
import com.danteso.chromeextensionapiapplication.security.entity.User;
import com.danteso.chromeextensionapiapplication.security.repo.UserRepository;
import com.danteso.chromeextensionapiapplication.telegram.config.BotConfig;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@AllArgsConstructor
@EnableScheduling
public class TelegramBot extends TelegramLongPollingBot {


    private final Logger LOG = LoggerFactory.getLogger(TelegramBot.class);

    private final BotConfig botConfig;
    private final TermRepository termRepository;
    private final UserRepository userRepository;
    private final GameEngine gameEngine;
    static final Map<User, Long> registeredChats = new HashMap<>();

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        LOG.debug("update.getCallbackQuery = {}", update.getCallbackQuery());
        if (update.hasMessage() && update.getMessage().hasText() && !update.hasCallbackQuery()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();


            if (messageText.startsWith("start")) {
                startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
            } else if (messageText.startsWith("/play")) {
                sendGameMessage(chatId);
            } else if (messageText.startsWith("/add")) {
                String userId = messageText.substring(5);
                boolean subscriberAdded = addSubscriber(userId, chatId);
                String message = subscriberAdded ? "User successfully logged in" : "Following userId is invalid: " + userId;
                sendMessage(chatId, message);
                if (subscriberAdded){
                    sendGameMessage(chatId);
                }
            }
            else if (messageText.startsWith("/checkUser")){
                User u = userRepository.findByTelegramChatId(chatId);
                sendMessage(chatId, "user obtained = " + u);
            }
            else {
                sendMessage(chatId, "Unable to process command");
            }
        } else if (update.hasCallbackQuery()) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            boolean answerIsCorrect = validateAnswer(update.getCallbackQuery());
            String result = answerIsCorrect ? "Correct!\n" : "Wrong!\n";
            User userByTelegramChatId = userRepository.findByTelegramChatId(chatId);
            registeredChats.put(userByTelegramChatId, chatId);
            removeButtonsFromMessage(update.getCallbackQuery().getMessage());
            //editMessageText(update.getCallbackQuery().getMessage(), result + update.getCallbackQuery().getMessage().getText());
            sendMessage(update.getCallbackQuery().getMessage().getChatId(), result);
            sendGameMessage(chatId);
        }

    }

    private void editMessageText(Message message, String text) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setText(text);
        editMessageText.setMessageId(message.getMessageId());
        editMessageText.setChatId(message.getChatId());
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            LOG.debug(e.getMessage());
        }
    }

    private void removeButtonsFromMessage(Message message) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(message.getChatId());
        editMessageReplyMarkup.setMessageId(message.getMessageId());
        editMessageReplyMarkup.setReplyMarkup(null);
        try {
            execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            LOG.debug(e.getMessage());
        }
    }

    private boolean validateAnswer(CallbackQuery callbackQuery) {
        Message message = callbackQuery.getMessage();
        String messageText = message.getText();
        Integer selectedOption = Integer.valueOf(callbackQuery.getData());
        String term = "";
        Pattern p = Pattern.compile("\\n\\n(.*)\\n\\n");
        Matcher m = p.matcher(messageText);
        if (m.find()) {
            term = messageText.substring(m.start() + 2, m.end() - 2);
            LOG.debug("term = {}", term);
        }

        List<String> parsedDescriptions = new ArrayList<>();
        for (int i = 1; i < GameEngine.TERMS_IN_ONE_GAME + 1; i++) {
            int i1 = messageText.indexOf(i + ":");
            int i2 = messageText.indexOf(i + 1 + ":");
            if (i2 != -1) {
                parsedDescriptions.add(messageText.substring(i1 + 3, i2 - 1));
            } else {
                parsedDescriptions.add(messageText.substring(i1 + 3));
            }
            if (i == selectedOption) {
                markSelectedOptionAsBold(message, i1, i2);
            }
        }
        Boolean answerIsCorrect = gameEngine.verifyAnswer(term, parsedDescriptions.get(selectedOption - 1));
//        LOG.debug("Parsed descriptions: {}", parsedDescriptions);
        LOG.debug("For term {}\nSelected option:{}\n{}\nanswerIsCorrect?: {}", term, selectedOption, parsedDescriptions.get(selectedOption - 1), answerIsCorrect);

        if (answerIsCorrect) {
            gameEngine.incrementScoreForTerm(term);
        } else {
            gameEngine.decrementScoreForTerm(term);
        }
        return answerIsCorrect;
    }

    private void markSelectedOptionAsBold(Message message, int start, int end) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.enableHtml(true);
        editMessageText.setChatId(message.getChatId());
        editMessageText.setMessageId(message.getMessageId());
        StringBuilder textWithBald = new StringBuilder();
        if (end != -1) {
            textWithBald
                    .append(message.getText(), 0, start)
                    .append("<b>")
                    .append(message.getText(), start, end)
                    .append("</b>")
                    .append(message.getText().substring(end));
        } else {
            textWithBald
                    .append(message.getText(), 0, start)
                    .append("<b>")
                    .append(message.getText(), start, message.getText().length())
                    .append("</b>");
        }
        editMessageText.setText(textWithBald.toString());
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            LOG.debug(e.getMessage());
        }
    }

    private void startCommandReceived(Long chatId, String name) {
        String answer = "Hi, " + name + ", nice to meet you!";
        sendMessage(chatId, answer);
    }

    private void sendGameMessage(Long chatId) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        StringBuilder messageBuilder = new StringBuilder();
        User user = userRepository.findByTelegramChatId(chatId);
        LOG.debug("chat id: {}", chatId);
        LOG.debug("User id: {}", user.getId());

        Map<Term, List<Description>> termWithRandomDescriptions = gameEngine.getTermWithRandomDescriptions(user);
        LOG.debug("Got term with random descriptions: {}", termWithRandomDescriptions.keySet());
        Term term = termWithRandomDescriptions.keySet().iterator().next();
        messageBuilder.append("Please select a correct description for term\n\n");
        messageBuilder.append(term.getName());
        messageBuilder.append("\n\n");
        int counter = 1;
        LOG.debug("descriptions size = {}", termWithRandomDescriptions.get(term).size());
        for (Description d : termWithRandomDescriptions.get(term)) {
            messageBuilder.append(counter).append(": ").append(d.getDescription()).append("\n");
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(String.valueOf(counter));
            inlineKeyboardButton.setCallbackData(String.valueOf(counter));
            buttons.add(List.of(inlineKeyboardButton));
            LOG.debug("inlineButton = {}", inlineKeyboardButton);
            counter++;
        }

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(buttons);
        Collection<Long> values = registeredChats.values();
        registeredChats.remove(user);
        LOG.debug("Was chats.size = {}, now size = {}", values.size(), registeredChats.values().size());
        sendMessageWithButtons(chatId, messageBuilder.toString(), inlineKeyboardMarkup);
    }

    private void sendMessageWithButtons(Long chatId, String textToSend, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        LOG.debug("Sending message {}", sendMessage);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            LOG.debug(e.getMessage());
        }
    }

    private void sendMessage(Long chatId, String textToSend) {
        sendMessageWithButtons(chatId, textToSend, null);
    }

    //Event happens every minute
    @Scheduled(cron = "0 * * * * *")
    private void runScheduledGame() {
        for (Long registeredChat : registeredChats.values()) {
            LOG.debug("Running game from cron for chat {}", registeredChat);
            sendGameMessage(registeredChat);
        }

    }


    public boolean addSubscriber(String userId, Long chatId) {
        Optional<User> userById = userRepository.findById(UUID.fromString(userId));
        boolean userFound = false;
        if (userById.isPresent()) {
            userFound = true;
            User user = userById.get();
            user.setTelegramChatId(chatId);
            userRepository.save(user);
            registeredChats.put(user, chatId);
        }
        return userFound;
    }

}