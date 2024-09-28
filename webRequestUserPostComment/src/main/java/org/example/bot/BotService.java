package org.example.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Contact;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.example.data.GetData;
import org.example.db.DB;
import org.example.entity.Comment;
import org.example.entity.Post;
import org.example.entity.State;
import org.example.entity.User;

import java.util.ArrayList;
import java.util.List;


public class BotService {
    static TelegramBot telegramBot=new TelegramBot("6722797447:AAGqlPr7NDP5kPDWZrjwvrYleF3BrXacDms");
    public static TgUser getOrCreateTgUser(Long id) {
        for (TgUser tgUser : DB.tgUsers) {
            if (tgUser.getId().equals(id)){
                return tgUser;
            }
        }
        TgUser tgUser=new TgUser();
        tgUser.setId(id);
        DB.tgUsers.add(tgUser);
        return tgUser;
    }
    public static String fixPhoneNum(String phoneNumber) {
        if (!phoneNumber.startsWith("+")){
            return "+"+phoneNumber;
        }
        else return phoneNumber;
    }
    static List<Post> userPosts=new ArrayList<>();
    static List<Comment> postComments=new ArrayList<>();
    static Post currentPost =new Post();
    public static void acceptSendWelcomeAndGetContact(TgUser tgUser) {
        SendMessage sendMessage=new SendMessage(
                tgUser.getId(),
                "Hello %s \uD83D\uDC4B\n \nPlease send your contact to continue..."
                        .formatted(tgUser.getName()));
        KeyboardButton keyboardButton=new KeyboardButton("Send Contact");
        keyboardButton.requestContact(true);
        sendMessage.replyMarkup(new ReplyKeyboardMarkup(keyboardButton).resizeKeyboard(true));
        telegramBot.execute(sendMessage);
    }

    public static void acceptContactAndAskUserSelection(TgUser tgUser, Contact contact) {
        String fixedPhoneNum=fixPhoneNum(contact.phoneNumber());
        tgUser.setPhone(fixedPhoneNum);

        SendMessage sendMessage1=new SendMessage(tgUser.getId(),
                """
                        Contact received %s✅
                        """.formatted(tgUser.getPhone())
        );
        sendMessage1.replyMarkup(new ReplyKeyboardRemove());
        SendResponse execute = telegramBot.execute(sendMessage1);
        tgUser.getLastMessageIds().add(execute.message().messageId());
        showUsers(tgUser);
        tgUser.setState(State.SHOW_USER_POSTS);
    }
    static void showUsers(TgUser tgUser){
        SendMessage sendMessage=new SendMessage(tgUser.getId(),"Choose ⤵️");
        InlineKeyboardMarkup userAndPostButtons = createUserAndPostButtons();
        sendMessage.replyMarkup(userAndPostButtons);
        SendResponse execute = telegramBot.execute(sendMessage);
        tgUser.setLastMessageId(execute.message().messageId());
        tgUser.getLastMessageIds().add(execute.message().messageId());
    }
    private static InlineKeyboardMarkup createUserAndPostButtons() {
        InlineKeyboardMarkup inlineCategoryButton =new InlineKeyboardMarkup();
        List<User> users = GetData.getDataUser();
        for (User user : users) {
            inlineCategoryButton.addRow(
                    new InlineKeyboardButton(user.getName()).callbackData(user.getId()+""),
                    new InlineKeyboardButton("posts").callbackData(user.getId()+"")
            );
        }
        return inlineCategoryButton;
    }
    public static void acceptSelectionAndShowPosts(TgUser tgUser, CallbackQuery callbackQuery) {
        List<Post> usersPosts = GetData.getDataPost();
        getUserPosts(callbackQuery.data(),usersPosts);

        EditMessageReplyMarkup editMessageReplyMarkup=new EditMessageReplyMarkup(tgUser.getId(),tgUser.getLastMessageId());
        InlineKeyboardMarkup userAndPostButtons = createPostTitleButtons(userPosts);

        editMessageReplyMarkup.replyMarkup(userAndPostButtons);
        telegramBot.execute(editMessageReplyMarkup);
        tgUser.setState(State.SHOW_POST_BODY);
    }
    private static List<Post> getUserPosts(String data, List<Post> usersPosts) {
        userPosts.clear();
        for (Post post : usersPosts) {
            if (data.equals(post.getUserId()+"")){
                userPosts.add(post);
            }
        }
        return userPosts;
    }
    private static InlineKeyboardMarkup createPostTitleButtons(List<Post> userPosts) {
        InlineKeyboardMarkup inlineCategoryButton =new InlineKeyboardMarkup();
        for (Post post : userPosts) {
            inlineCategoryButton.addRow(
                    new InlineKeyboardButton(post.getTitle()).callbackData(post.getId()+"")
            );
        }
        inlineCategoryButton.addRow(new InlineKeyboardButton("Go back").callbackData("back"));
        return inlineCategoryButton;
    }

    public static void acceptPostSelectionAndShowPostBody(TgUser tgUser, String data) {
        executeDeletingMsgs(tgUser);
        if (data.equals("back")){
            showUsers(tgUser);
            tgUser.setState(State.SHOW_USER_POSTS);
        }
        else {
            for (Post userPost : userPosts) {
                if (data.equals(userPost.getId()+"")){
                    currentPost =userPost;
                    break;
                }
            }
            SendMessage sendMessage=new SendMessage(
                    tgUser.getId(),
                    currentPost.getTitle() + "\n\n" + currentPost.getBody()
            );
            InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
            inlineKeyboardMarkup.addRow(new InlineKeyboardButton("Comments").callbackData("comments"));
            inlineKeyboardMarkup.addRow(new InlineKeyboardButton("Go back").callbackData("back"));
            sendMessage.replyMarkup(inlineKeyboardMarkup);

            SendResponse execute = telegramBot.execute(sendMessage);
            tgUser.getLastMessageIds().add(execute.message().messageId());
            tgUser.setLastMessageId(execute.message().messageId());
            tgUser.setState(State.COMMENT_OR_BACK);
        }
    }

    public static void acceptAndShowCommentOrBack(TgUser tgUser, CallbackQuery callbackQuery) {
        if (callbackQuery.data().equals("comments")){
            showComments(tgUser,currentPost.getId()+"");
        }
        else {
            EditMessageReplyMarkup editMessageReplyMarkup=new EditMessageReplyMarkup(tgUser.getId(),tgUser.getLastMessageId());
            editMessageReplyMarkup.replyMarkup( createPostTitleButtons(userPosts));
            telegramBot.execute(editMessageReplyMarkup);
            tgUser.setState(State.SHOW_POST_BODY);
        }
    }

    private static void showComments(TgUser tgUser, String data) {
        executeDeletingMsgs(tgUser);
        List<Comment> postsComments = GetData.getDataComment();
        getPostComments(data,postsComments);
        String allComments = generateCommentsView(postComments);
        SendMessage sendMessage=new SendMessage(tgUser.getId(),allComments);

        InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
//        inlineKeyboardMarkup.addRow(new InlineKeyboardButton("Add comment").callbackData("addComment"));
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton("Go back").callbackData("back"));

        sendMessage.replyMarkup(inlineKeyboardMarkup);

        SendResponse execute = telegramBot.execute(sendMessage);
        tgUser.setLastMessageId(execute.message().messageId());
        tgUser.getLastMessageIds().add(execute.message().messageId());
        tgUser.setState(State.SHOW_COMMENTS_OR_BACK);
    }

    private static String generateCommentsView(List<Comment> eachPostComments) {
        StringBuilder stringBuilder=new StringBuilder();
        for (Comment eachPostComment : eachPostComments) {
            stringBuilder.append(
                    "User: "+eachPostComment.getName()+"\n"+
                    eachPostComment.getEmail()+"\n\n"+
                    eachPostComment.getBody()+"\n\n"+
                    "➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖\n"
            );
        }

        return String.valueOf(stringBuilder);
    }

    private static List<Comment> getPostComments(String data, List<Comment> postsComments) {
        postComments.clear();
        for (Comment comment : postsComments) {
            if (data.equals(comment.getPostId()+"")){
                postComments.add(comment);
            }
        }
        return postComments;
    }

    public static void acceptSelectionAndShowCommentsOrBack(TgUser tgUser, CallbackQuery callbackQuery) {
        if (callbackQuery.data().equals("back")){
            acceptPostSelectionAndShowPostBody(tgUser,currentPost.getId()+"");
        }
    }

    public static void executeDeletingMsgs(TgUser tgUser) {
        if (!tgUser.getLastMessageIds().isEmpty()){
            for (Integer lastMessageId : tgUser.getLastMessageIds()) {
                DeleteMessage deleteMessage=new DeleteMessage(tgUser.getId(),lastMessageId);
                telegramBot.execute(deleteMessage);
            }
            tgUser.getLastMessageIds().clear();
        }
    }

}
