package com.sequenia.threads.utils;

import android.text.TextUtils;
import android.util.Log;

import com.advisa.client.api.InOutMessage;
import com.pushserver.android.PushMessage;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ChatPhrase;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.MessageState;
import com.sequenia.threads.model.Quote;
import com.sequenia.threads.model.UpcomingUserMessage;
import com.sequenia.threads.model.UserPhrase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by yuri on 26.07.2016.
 * {
 * "result":"http://pushservertest.mfms.ru/push-test/file/download/eaydpjhiw2t124vxg4c2rb00p4yar7kn2nq7",
 * "optional":{
 * "type":"image/png",
 * "name":"jeb.png",
 * "size":23610,
 * "lastModified":1467358448772
 * },
 * "progress":null
 * }
 */
public class MessageFormatter {
    private static final String TAG = "MessageFormatter ";

    private MessageFormatter() {
    }

    public static String format(UpcomingUserMessage upcomingUserMessage, String quoteMfmsFilePath, String mfmsFilePath) {
        try {
            Quote quote = upcomingUserMessage.getQuote();
            FileDescription fileDescription = upcomingUserMessage.getFileDescription();
            JSONObject formattedMessage = new JSONObject();
            formattedMessage.put("text", upcomingUserMessage.getText());
            if (quote != null) {
                JSONArray quotes = new JSONArray();
                formattedMessage.put("quotes", quotes);
                JSONObject quoteJson = new JSONObject();
                quotes.put(quoteJson);
                if (!TextUtils.isEmpty(quote.getText())) {
                    quoteJson.put("text", quote.getText());
                }
                if (quote.getFileDescription() != null && quoteMfmsFilePath != null) {
                    quoteJson.put("attachments", attachmentsFromFileDescription(quote.getFileDescription(), quoteMfmsFilePath));
                }
            }
            if (fileDescription != null && mfmsFilePath != null) {
                formattedMessage.put("attachments", attachmentsFromFileDescription(fileDescription, mfmsFilePath));
            }
            return formattedMessage.toString().replaceAll("\\\\", "");
        } catch (JSONException e) {
            Log.e(TAG, "error formatting json");
            e.printStackTrace();

        }
        return "";
    }

    public static String format(UserPhrase upcomingUserMessage, String quoteMfmsFilePath, String mfmsFilePath) {
        try {
            Quote quote = upcomingUserMessage.getQuote();
            FileDescription fileDescription = upcomingUserMessage.getFileDescription();
            JSONObject formattedMessage = new JSONObject();
            formattedMessage.put("text", upcomingUserMessage.getPhrase());
            if (quote != null) {
                JSONArray quotes = new JSONArray();
                formattedMessage.put("quotes", quotes);
                JSONObject quoteJson = new JSONObject();
                quotes.put(quoteJson);
                if (!TextUtils.isEmpty(quote.getText())) {
                    quoteJson.put("text", quote.getText());
                }
                if (quote.getFileDescription() != null && quoteMfmsFilePath != null) {
                    quoteJson.put("attachments", attachmentsFromFileDescription(quote.getFileDescription(), quoteMfmsFilePath));
                }
            }
            if (fileDescription != null && mfmsFilePath != null) {
                formattedMessage.put("attachments", attachmentsFromFileDescription(fileDescription, mfmsFilePath));
            }
            return formattedMessage.toString().replaceAll("\\\\", "");
        } catch (JSONException e) {
            Log.e(TAG, "error formatting json");
            e.printStackTrace();

        }
        return "";
    }

    public static ConsultPhrase format(PushMessage pushMessage) throws JSONException {
        JSONObject fullMessage = new JSONObject(pushMessage.getFullMessage());
        String messageId = pushMessage.getMessageId();
        long timeStamp = pushMessage.getSentAt();
        String message = fullMessage.getString("text") == null ? pushMessage.getShortMessage() : fullMessage.getString("text");
        JSONObject operatorInfo = fullMessage.getJSONObject("operator");
        final String name = operatorInfo.getString("name");
        String photoUrl = operatorInfo.getString("photoUrl");
        FileDescription fileDescription = fileDescriptionFromJson(fullMessage.getJSONArray("attachments"));
        if (fileDescription != null) fileDescription.setFrom(name);
        Quote quote = quoteFromJson(fullMessage.getJSONArray("quotes"));
        return new ConsultPhrase(
                fileDescription
                , quote
                , name
                , messageId
                , message
                , timeStamp
                , operatorInfo.getString("id")
                , photoUrl);
    }

    private static JSONArray attachmentsFromFileDescription(File file) throws JSONException {
        JSONArray attachments = new JSONArray();
        JSONObject attachment = new JSONObject();
        attachments.put(attachment);
        JSONObject optional = new JSONObject();
        attachment.put("optional", optional);
        String extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".") + 1).toLowerCase();
        String type = null;
        if (extension.equals("jpg")) type = "image/jpg";
        if (extension.equals("png")) type = "image/png";
        if (extension.equals("pdf")) type = "text/pdf";
        optional.put("type", type);
        optional.put("name", file.getName());
        optional.put("size", file.length());
        optional.put("lastModified", file.lastModified());
        return attachments;
    }

    private static JSONArray attachmentsFromMfmsPath(FileDescription fileDescription) throws JSONException {
        JSONArray attachments = new JSONArray();
        JSONObject attachment = new JSONObject();
        attachments.put(attachment);
        JSONObject optional = new JSONObject();
        attachment.put("optional", optional);
        if (fileDescription.getIncomingName() != null) {
            String extension = fileDescription.getIncomingName().substring(fileDescription.getIncomingName().lastIndexOf(".") + 1).toLowerCase();
            String type = null;
            if (extension.equals("jpg")) type = "image/jpg";
            if (extension.equals("png")) type = "image/png";
            if (extension.equals("pdf")) type = "text/pdf";
            optional.put("type", type);
        }
        optional.put("name", fileDescription.getIncomingName());
        optional.put("size", fileDescription.getSize());
        optional.put("lastModified", System.currentTimeMillis());
        return attachments;
    }

    public static JSONArray attachmentsFromFileDescription(FileDescription fileDescription, String mfmsFilepath) throws JSONException {
        JSONArray attachments = null;
        if (fileDescription.getFilePath() != null && new File(fileDescription.getFilePath().replaceAll("file://", "")).exists()) {
            attachments = attachmentsFromFileDescription(new File(fileDescription.getFilePath().replaceAll("file://", "")));
        } else if (fileDescription.getDownloadPath() != null) {
            attachments = attachmentsFromMfmsPath(fileDescription);
        }
        attachments.getJSONObject(0).put("result", mfmsFilepath);
        return attachments;
    }

    public static boolean hasFile(UpcomingUserMessage message) {
        return (message.getFileDescription() != null || (message.getQuote() != null && message.getQuote().getFileDescription() != null));
    }

    public static boolean hasFile(UserPhrase message) {
        return (message.getFileDescription() != null || (message.getQuote() != null && message.getQuote().getFileDescription() != null));
    }

    public static Quote quoteFromJson(JSONArray quotes) throws JSONException {
        Quote quote = null;
        FileDescription quoteFileDescription = null;
        String quoteString = null;
        String consultName = "";
        if (quotes.length() > 0 && quotes.getJSONObject(0) != null && (quotes.getJSONObject(0).has("text"))) {
            quoteString = quotes.getJSONObject(0).getString("text");
        }
        if (quotes.length() > 0//// TODO: 29.07.2016 need real timestamp of quote
                && quotes.getJSONObject(0) != null
                && (quotes.getJSONObject(0).has("attachments"))
                && (quotes.getJSONObject(0).getJSONArray("attachments").length() > 0
                && (quotes.getJSONObject(0).getJSONArray("attachments").getJSONObject(0).has("result")))) {
            String header = null;
            if (quotes.getJSONObject(0).getJSONArray("attachments").getJSONObject(0).has("optional")) {
                header = quotes.getJSONObject(0).getJSONArray("attachments").getJSONObject(0).getJSONObject("optional").getString("name");
            }
            quoteFileDescription = fileDescriptionFromJson(quotes.getJSONObject(0).getJSONArray("attachments"));

        }
        if (quotes.length() > 0 && quotes.getJSONObject(0) != null && quotes.getJSONObject(0).has("operator")) {
            consultName = quotes.getJSONObject(0).getJSONObject("operator").getString("name");
        }
        if (quoteString != null || quoteFileDescription != null) {
            quote = new Quote(consultName, quoteString, quoteFileDescription, System.currentTimeMillis());
        }
        if (quoteFileDescription != null) {
            quoteFileDescription.setFrom(consultName);
        }
        return quote;
    }

    public static FileDescription fileDescriptionFromJson(JSONArray jsonArray) throws JSONException {
        FileDescription fileDescription = null;
        if (jsonArray.length() > 0
                && jsonArray.getJSONObject(0) != null) {
            String header = null;
            if (jsonArray.getJSONObject(0).has("optional")) {
                header = jsonArray.getJSONObject(0).getJSONObject("optional").getString("name");
            }
            fileDescription = new FileDescription(
                    null
                    , null
                    , jsonArray.getJSONObject(0).getJSONObject("optional").getLong("size")
                    , System.currentTimeMillis());
            fileDescription.setDownloadPath(jsonArray.getJSONObject(0).getString("result"));
            fileDescription.setIncomingName(header);
        }
        return fileDescription;
    }

    public static String getStartMessage(String clientName, String clientId, String email) {
        JSONObject object = new JSONObject();
        try {
            object.put("name", clientName);
            object.put("clientId", clientId);
            object.put("email", email);
            object.put("type", "CLIENT_INFO");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object.toString().replaceAll("\\\\", "");
    }

    public static ArrayList<ChatItem> format(List<InOutMessage> messages) {
        ArrayList<ChatItem> out = new ArrayList<>();
        try {
            for (InOutMessage message : messages) {
                if (message.content == null || message.content.length() == 0) continue;
                try {
                    JSONObject body = new JSONObject(message.content);
                    String messageId = String.valueOf(message.messageId);
                    long timeStamp = message.sentAt.millis;
                    String phraseText = body.has("text") ? body.getString("text") : null;
                    JSONObject operatorInfo = body.has("operator") ? body.getJSONObject("operator") : null;
                    final String name = operatorInfo != null ? operatorInfo.getString("name") : null;
                    String photoUrl = operatorInfo != null ? operatorInfo.getString("photoUrl") : null;
                    FileDescription fileDescription = body.has("attachments") ? fileDescriptionFromJson(body.getJSONArray("attachments")) : null;
                    if (fileDescription != null) fileDescription.setFrom(name);
                    Quote quote = body.has("quotes") ? quoteFromJson(body.getJSONArray("quotes")) : null;
                    String operId = operatorInfo != null ? operatorInfo.getString("id") : UUID.randomUUID().toString();
                    if (!message.incoming) {
                        out.add(new ConsultPhrase(fileDescription, quote, name, messageId, phraseText, timeStamp, operId, photoUrl));
                    } else {

                        out.add(new UserPhrase(messageId, phraseText, quote, timeStamp, fileDescription, MessageState.STATE_SENT_AND_SERVER_RECEIVED));
                    }
                } catch (JSONException e) {
                    String content = message.content;
                    String type = content.contains("подключился") ? ConsultConnectionMessage.TYPE_JOINED : ConsultConnectionMessage.TYPE_LEFT;
                    ConsultConnectionMessage m =
                            new ConsultConnectionMessage(content.substring(content.indexOf(" "+1)==-1?0:content.indexOf(" "+1))
                                    , type
                                    , content.substring(content.indexOf(" "))
                                    , true
                                    , message.sentAt.millis
                                    , null);
                    out.add(m);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "" + messages);// TODO: 12.08.2016
            e.printStackTrace();
        }
        return out;
    }
}
