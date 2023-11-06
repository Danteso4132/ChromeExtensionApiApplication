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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    static final List<Long> registeredChats = new ArrayList<>();

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
        //LOG.debug("Received update: {}", update);
        LOG.debug("update.getCallbackQuery = {}", update.getCallbackQuery());
        if(update.hasMessage() && update.getMessage().hasText() && !update.hasCallbackQuery()){
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText){
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/play":
                    registeredChats.add(chatId);
                    sendGameMessage(chatId);
                    break;
                default:
                    sendMessage(chatId, "Hi");
            }
        }
        else if (update.hasCallbackQuery()){
//            DeleteMessage deleteMessage = DeleteMessage.builder().chatId(update.getCallbackQuery().getMessage().getChatId()).messageId(update.getCallbackQuery().getMessage().getMessageId()).build();
//            try{
//                execute(deleteMessage);
//            }
//            catch (TelegramApiException e){
//                LOG.debug(e.getStackTrace().toString());
//            }
            boolean answerIsCorrect = validateAnswer(update.getCallbackQuery());
            String result = answerIsCorrect ? "Correct!\n" : "Wrong!\n";
            removeButtonsFromMessage(update.getCallbackQuery().getMessage());
            //editMessageText(update.getCallbackQuery().getMessage(), result + update.getCallbackQuery().getMessage().getText());
            sendMessage(update.getCallbackQuery().getMessage().getChatId(), result);
        }

    }

    private void editMessageText(Message message, String text){
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

    private void removeButtonsFromMessage(Message message){
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
        for (int i = 1; i < GameEngine.TERMS_IN_ONE_GAME + 1; i++){
            int i1 = messageText.indexOf(i + ":");
            int i2 = messageText.indexOf(i + 1 + ":");
            if (i2 != -1){
                parsedDescriptions.add(messageText.substring(i1 + 3, i2 - 1));
            }
            else{
                parsedDescriptions.add(messageText.substring(i1 + 3));
            }
            if (i == selectedOption){
                markSelectedOptionAsBold(message, i1, i2);
            }
        }
        Boolean answerIsCorrect = gameEngine.verifyAnswer(term, parsedDescriptions.get(selectedOption - 1));
//        LOG.debug("Parsed descriptions: {}", parsedDescriptions);
        LOG.debug("For term {}\nSelected option:{}\n{}\nanswerIsCorrect?: {}", term, selectedOption, parsedDescriptions.get(selectedOption - 1), answerIsCorrect);

        if (answerIsCorrect){
            gameEngine.incrementScoreForTerm(term);
        }
        else{
            gameEngine.decrementScoreForTerm(term);
        }
        return answerIsCorrect;
    }

    private void markSelectedOptionAsBold(Message message, int start, int end){
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
        }
        else{
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

    private void sendGameMessage(Long chatId){
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        StringBuilder messageBuilder = new StringBuilder();
        User user = userRepository.findByUsername("root"); //TODO add login
        LOG.debug("User found: {}", user.getUsername());

        LOG.debug("Terms found: {}", termRepository.findByScore_CorrectIsLessThanEqualAndUser(5, user));
        List<Term> all = termRepository.findAll();
        LOG.debug("All terms: {}", all);

        Map<Term, List<Description>> termWithRandomDescriptions = gameEngine.getTermWithRandomDescriptions(user);
        LOG.debug("Got term with random descriptions: {}", termWithRandomDescriptions.keySet());
        Term term = termWithRandomDescriptions.keySet().iterator().next();
        messageBuilder.append("Please select a correct description for term\n\n");
        messageBuilder.append(term.getName());
        messageBuilder.append("\n\n");
        int counter = 1;
        LOG.debug("descriptions size = {}", termWithRandomDescriptions.get(term).size());
        for (Description d : termWithRandomDescriptions.get(term)){
            messageBuilder.append(counter).append(": ").append(d.getDescription()).append("\n");
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(String.valueOf(counter));
            inlineKeyboardButton.setCallbackData(String.valueOf(counter));
            buttons.add(List.of(inlineKeyboardButton));
            LOG.debug("inlineButton = {}", inlineKeyboardButton);
            counter++;
        }

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(buttons);
        LOG.debug("buttons.size = {}", buttons.size());
        LOG.debug("markup = {}", inlineKeyboardMarkup);
        LOG.debug("markupKeyboard = {}", inlineKeyboardMarkup.getKeyboard());
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
    private void runScheduledGame(){
        LOG.debug("Cron event");
        for (Long registeredChat : registeredChats) {
            LOG.debug("Running game from cron for chat {}", registeredChat);
            sendGameMessage(registeredChat);
        }

    }

}