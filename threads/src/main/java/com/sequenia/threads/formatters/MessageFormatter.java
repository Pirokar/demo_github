package com.sequenia.threads.formatters;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.advisa.client.api.InOutMessage;
import com.pushserver.android.PushMessage;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultInfo;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
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

    public static String format(
            UpcomingUserMessage upcomingUserMessage
            , String quoteMfmsFilePath
            , String mfmsFilePath) {
        if (upcomingUserMessage == null) return "";
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
            //   return formattedMessage.toString().replaceAll("\\\\", "");
            return formattedMessage.toString();
        } catch (JSONException e) {
            Log.e(TAG, "error formatting json");
            e.printStackTrace();

        }
        return "";
    }

    public static String format(
            UserPhrase upcomingUserMessage
            , ConsultInfo consultInfo
            , String quoteMfmsFilePath
            , String mfmsFilePath) {
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
                if (null != consultInfo) quoteJson.put("operator", consultInfo.toJson());
                if (quote.getFileDescription() != null && quoteMfmsFilePath != null) {
                    quoteJson.put("attachments", attachmentsFromFileDescription(quote.getFileDescription(), quoteMfmsFilePath));
                }
            }
            if (fileDescription != null && mfmsFilePath != null) {
                formattedMessage.put("attachments", attachmentsFromFileDescription(fileDescription, mfmsFilePath));
            }
            //   return formattedMessage.toString().replaceAll("\\\\", "");
            return formattedMessage.toString();
        } catch (JSONException e) {
            Log.e(TAG, "error formatting json");
            e.printStackTrace();

        }
        return "";
    }

    public static ChatItem format(PushMessage pushMessage) throws JSONException {
        JSONObject fullMessage = new JSONObject(pushMessage.getFullMessage());
        if (fullMessage.has("type") && (fullMessage.getString("type").equalsIgnoreCase("OPERATOR_JOINED") || fullMessage.getString("type").equalsIgnoreCase("OPERATOR_LEFT"))) {
            return getConsultConnectionFromPush(pushMessage);
        }
        String messageId = pushMessage.getMessageId();
        long timeStamp = pushMessage.getSentAt();
        String message = fullMessage.getString("text") == null ? pushMessage.getShortMessage() : fullMessage.getString("text");
        JSONObject operatorInfo = fullMessage.getJSONObject("operator");
        final String name = operatorInfo.getString("name");
        String photoUrl = operatorInfo.isNull("photoUrl") ? null : operatorInfo.getString("photoUrl");
        String status = operatorInfo.has("status") && !operatorInfo.isNull("status") ? operatorInfo.getString("status") : null;
        JSONArray attachmentsArray = fullMessage.has("attachments") ? fullMessage.getJSONArray("attachments") : null;
        FileDescription fileDescription = null;
        if (null != attachmentsArray)
            fileDescription = fileDescriptionFromJson(fullMessage.getJSONArray("attachments"));
        if (fileDescription != null) {
            fileDescription.setFrom(name);
            fileDescription.setTimeStamp(timeStamp);
        }
        Quote quote = null;
        if (fullMessage.has("quotes")) quote = quoteFromJson(fullMessage.getJSONArray("quotes"));
        if (quote != null && quote.getFileDescription() != null) {
            quote.getFileDescription().setTimeStamp(timeStamp);
        }
        boolean gender = operatorInfo.isNull("gender") ? false : operatorInfo.getString("gender").equalsIgnoreCase("male");
        return new ConsultPhrase(
                fileDescription
                , quote
                , name
                , messageId
                , message
               /* , timeStamp*/
                , System.currentTimeMillis()// FIXME: 06.09.2016 temporary
                , String.valueOf(operatorInfo.getLong("id"))
                , photoUrl
                , false
                , status
                , gender
        );
    }

    public static ConsultConnectionMessage getConsultConnectionFromPush(PushMessage pushMessage) throws JSONException {
        JSONObject fullMessage = new JSONObject(pushMessage.getFullMessage());
        String messageId = pushMessage.getMessageId();
        long timeStamp = pushMessage.getSentAt();
        JSONObject operator = fullMessage.getJSONObject("operator");
        long operatorId = operator.getLong("id");
        String name = operator.isNull("name") ? null : operator.getString("name");
        String status = operator.isNull("status") ? null : operator.getString("status");
        String type = fullMessage.getString("type").equalsIgnoreCase("OPERATOR_JOINED") ? ConsultConnectionMessage.TYPE_JOINED : ConsultConnectionMessage.TYPE_LEFT;
        boolean gender = operator.isNull("gender") ? false : operator.getString("gender").equalsIgnoreCase("male");
        String photourl = operator.isNull("photoUrl") ? null : operator.getString("photoUrl");
        String title = pushMessage.getShortMessage() == null ? null : pushMessage.getShortMessage().split(" ")[0];
        return new ConsultConnectionMessage(
                String.valueOf(operatorId)
                , type
                , name
                , gender
              /*  , timeStamp*/
                , System.currentTimeMillis() // FIXME: 06.09.2016 temporary
                , photourl
                , status
                , title
                , pushMessage.getMessageId());
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
            try {
                consultName = quotes.getJSONObject(0).getJSONObject("operator").getString("name");
            } catch (JSONException e) {
                Log.e(TAG, "" + quotes);
                e.printStackTrace();
            }
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
                    , 0);// TODO: 18.08.2016 set incoming time
            fileDescription.setDownloadPath(jsonArray.getJSONObject(0).getString("result"));
            fileDescription.setIncomingName(header);
        }
        return fileDescription;
    }

    public static List<ChatItem> formatMessages(List<PushMessage> messages) {
        List<ChatItem> list = new ArrayList<>();
        try {
            for (int i = 0; i < messages.size(); i++) {
                list.add(format(messages.get(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
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
                if (message == null || message.content == null || message.content.length() == 0)
                    continue;
                try {
                    JSONObject body = new JSONObject(message.content);
                    String messageId = String.valueOf(message.messageId);
                    long timeStamp = message.sentAt.millis;
                    JSONObject operatorInfo = body.has("operator") ? body.getJSONObject("operator") : null;
                    String name = null;
                    if (operatorInfo != null && operatorInfo.has("name") && !operatorInfo.isNull("name")) {
                        name = operatorInfo.getString("name");
                    }
                    String photoUrl = null;
                    if (operatorInfo != null && operatorInfo.has("photoUrl") && !operatorInfo.isNull("photoUrl")) {
                        photoUrl = operatorInfo.getString("photoUrl");
                    }
                    String operatorId = null;
                    if (operatorInfo != null && operatorInfo.has("id") && !operatorInfo.isNull("id")) {
                        operatorId = operatorInfo.getString("id");
                    }
                    String status = null;
                    if (operatorInfo != null && operatorInfo.has("status") && !operatorInfo.isNull("status")) {
                        status = operatorInfo.getString("status");
                    }
                    boolean gender = false;
                    if (operatorInfo != null && operatorInfo.has("gender") && !operatorInfo.isNull("gender")) {
                        gender = operatorInfo.getString("gender").equalsIgnoreCase("male");
                    }
                    if (body.has("type") && !body.isNull("type") && (body.getString("type").equalsIgnoreCase("OPERATOR_JOINED") || body.getString("type").equalsIgnoreCase("OPERATOR_LEFT"))) {
                        String type = body.getString("type").equalsIgnoreCase(ConsultConnectionMessage.TYPE_JOINED) ? ConsultConnectionMessage.TYPE_JOINED : ConsultConnectionMessage.TYPE_LEFT;
                        out.add(new ConsultConnectionMessage(operatorId, type, name, gender, timeStamp, photoUrl, status, null, messageId));
                    } else {
                        String phraseText = body.has("text") ? body.getString("text") : null;
                        FileDescription fileDescription = body.has("attachments") ? fileDescriptionFromJson(body.getJSONArray("attachments")) : null;
                        if (fileDescription != null) {
                            fileDescription.setFrom(name);
                            fileDescription.setTimeStamp(timeStamp);
                        }
                        Quote quote = body.has("quotes") ? quoteFromJson(body.getJSONArray("quotes")) : null;
                        if (quote != null && quote.getFileDescription() != null)
                            quote.getFileDescription().setTimeStamp(timeStamp);
                        if (!message.incoming) {
                            out.add(new ConsultPhrase(fileDescription
                                    , quote
                                    , name
                                    , messageId
                                    , phraseText
                                    , timeStamp
                                    , operatorId
                                    , photoUrl
                                    , true
                                    , status
                                    , gender));
                        } else {
                            if (fileDescription != null) {
                                if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru")) {
                                    fileDescription.setFrom("Я");
                                } else {
                                    fileDescription.setFrom("I");
                                }
                            }
                            out.add(new UserPhrase(messageId, phraseText, quote, timeStamp, fileDescription, MessageState.STATE_WAS_READ));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (e.getMessage() != null && e.getMessage().contains("annot be converted to")) {
                        String cont = message.content;
                        String type = cont.contains("присоедини")?ConsultConnectionMessage.TYPE_JOINED:ConsultConnectionMessage.TYPE_LEFT;
                        String title =null;
                        String name = null;
                        try {
                            name =  cont.split(" ")[1];
                            title =    cont.split(" ")[0];
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        ConsultConnectionMessage ccm = new ConsultConnectionMessage(name,type,name,cont.contains("лся")?true:false,message.sentAt.millis,null,null,title,String.valueOf(message.messageId));
                        out.add(ccm);
                    }
                    Log.e(TAG, "error parsing message" + message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "error while formatting");
            Log.e(TAG, "" + messages);
            e.printStackTrace();
        }
        return out;
    }

    private static ConsultConnectionMessage getConsultConnectionMessageFromInout(InOutMessage message) throws JSONException {
        JSONObject body = new JSONObject(message.content);
        String type = body.getString("type").equalsIgnoreCase("OPERATOR_JOINED") ? ConsultConnectionMessage.TYPE_JOINED : ConsultConnectionMessage.TYPE_LEFT;
        JSONObject operator = body.getJSONObject("operator");
        long operatorId = operator.getLong("id");
        String name = operator.isNull("name") ? null : operator.getString("name");
        String status = operator.isNull("status") ? null : operator.getString("status");
        boolean gender = operator.isNull("gender") ? false : operator.getString("gender").equalsIgnoreCase("male");
        String photourl = operator.isNull("photoUrl") ? null : operator.getString("photoUrl");
        String title = "";
        try {
            title = body.getString("text").split(" ")[0];
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ConsultConnectionMessage c =
                new ConsultConnectionMessage(
                        String.valueOf(operatorId)
                        , type
                        , name
                        , gender
                        , message.sentAt.millis
                        , photourl
                        , status
                        , title
                        , String.valueOf(message.messageId));
        return c;
    }

    public static List<String> getReadIds(Bundle b) {
        ArrayList<String> ids = new ArrayList<>();
        try {
            if (b==null)return new ArrayList<>();
            Object o = b.get("readInMessageIds");

            if (o instanceof ArrayList) {
                Log.i(TAG, "getReadIds o instanceof ArrayList");
                ids.addAll((Collection<? extends String>) b.get("readInMessageIds"));

                Log.e(TAG, "getReadIds = ");
            }
            if (o instanceof String) {
                Log.i(TAG, "getReadIds o instanceof String " + o);
                String contents = (String) o;
                if (!contents.contains(",")) {
                    ids.add((String) o);
                } else {
                    String[] idsArray = contents.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
                    ids.addAll(Arrays.asList(idsArray));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
    }

    public static String getMessageTyping() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "TYPING");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString().replace("\\\\", "");
    }
}
