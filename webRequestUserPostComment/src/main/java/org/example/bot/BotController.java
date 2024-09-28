package org.example.bot;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.example.entity.State;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.example.bot.BotService.*;

public class BotController {
    ExecutorService executorService = Executors.newFixedThreadPool(10);

    public void start() {
        telegramBot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                executorService.execute(() -> {
                    try {
                        handleUpdate(update);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void handleUpdate(Update update) {

        if (update.message() != null) {
            Message message = update.message();
            TgUser tgUser = getOrCreateTgUser(message.chat().id());
            tgUser.setName(message.from().firstName());
            if (message.text() != null) {
                if (message.text().equals("/start")) {
                    executeDeletingMsgs(tgUser);
                    BotService.acceptSendWelcomeAndGetContact(tgUser);
                }
                if (message.text().equals("/mainpanel")) {
                    executeDeletingMsgs(tgUser);
                    showUsers(tgUser);
                }
            }
            else if (message.contact()!=null){
                BotService.acceptContactAndAskUserSelection(tgUser,message.contact());
            }
        }
        else if (update.callbackQuery()!=null){
            CallbackQuery callbackQuery=update.callbackQuery();
            TgUser tgUser = getOrCreateTgUser(callbackQuery.from().id());
            if (tgUser.getState().equals(State.SHOW_USER_POSTS)){
                BotService.acceptSelectionAndShowPosts(tgUser,callbackQuery);
            }else if (tgUser.getState().equals(State.SHOW_POST_BODY)){
                BotService.acceptPostSelectionAndShowPostBody(tgUser,callbackQuery.data());
            }else if (tgUser.getState().equals(State.COMMENT_OR_BACK)){
                BotService.acceptAndShowCommentOrBack(tgUser,callbackQuery);
            }else if (tgUser.getState().equals(State.SHOW_COMMENTS_OR_BACK)){
                BotService.acceptSelectionAndShowCommentsOrBack(tgUser,callbackQuery);
            }

        }

    }
}