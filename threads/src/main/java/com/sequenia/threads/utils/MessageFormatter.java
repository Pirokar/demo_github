package com.sequenia.threads.utils;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

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

    public static JSONObject formatMessage(String message, String quote, String filePath, String mfmsFilePath) throws JSONException {
        JSONObject upcomingMessage = new JSONObject();
        if (!TextUtils.isEmpty(quote)) {
            JSONArray quotes = new JSONArray();
            upcomingMessage.put("quotes", quotes);
            JSONObject quoteobject = new JSONObject();
            quoteobject.put("text", quote);
            quotes.put(quoteobject);
        }
        upcomingMessage.put("text", message == null ? "" : message);
        if (!TextUtils.isEmpty(mfmsFilePath)) {
            if (!new File(filePath).exists()) {
                Log.e(TAG, "file with path " + filePath + " doesn't exist");
                return upcomingMessage;
            }
            JSONArray attachments = new JSONArray();
            JSONObject attachment = new JSONObject();
            attachment.put("result", mfmsFilePath);
            attachment.put("progress","");
            JSONObject optional = new JSONObject();
            attachment.put("optional", optional);
            String extension = filePath.substring(filePath.lastIndexOf(".")+1).toLowerCase();
            String type = null;
            File file = new File(filePath);
            if (extension.equals("jpg")) type = "image/jpg";
            if (extension.equals("png")) type = "image/png";
            if (extension.equals("pdf")) type = "text/pdf";
            optional.put("type", type);
            optional.put("name", file.getName());
            optional.put("size", file.length());
            optional.put("lastModified", file.lastModified());
            attachments.put(attachment);
            upcomingMessage.put("attachments", attachments);
        }
        return upcomingMessage;
    }
}
