package im.threads.utils;

import android.content.SharedPreferences;
import android.os.Bundle;

import im.threads.model.ConsultConnectionMessage;
import im.threads.model.ConsultInfo;
import im.threads.model.ConsultPhrase;

/**
 * Created by yuri on 14.07.2016.
 */
public class ConsultWriter {
    private static final String TAG = "ConsultWriter ";
    public static final String OPERATOR_STATUS = "OPERATOR_STATUS";
    public static final String OPERATOR_NAME = "OPERATOR_NAME";
    public static final String OPERATOR_TITLE = "OPERATOR_TITLE";
    public static final String OPERATOR_ORG_UNIT = "OPERATOR_ORG_UNIT";
    public static final String OPERATOR_PHOTO = "OPERATOR_PHOTO";
    public static final String OPERATOR_ID = "OPERATOR_ID";
    public static final String SEARCHING_CONSULT = "SEARCHING_CONSULT";
    private SharedPreferences sharedPreferences;

    public ConsultWriter(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void setSearchingConsult(boolean isSearching) {
        sharedPreferences.edit().putBoolean(ConsultWriter.class + SEARCHING_CONSULT, isSearching).commit();
    }

    public boolean istSearchingConsult() {
        return sharedPreferences.getBoolean(ConsultWriter.class + SEARCHING_CONSULT, false);
    }

    public void setCurrentConsultInfo(String consultId, Bundle info) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(OPERATOR_STATUS + consultId, info.getString("operatorStatus")).commit();
        editor.putString(OPERATOR_NAME + consultId, info.getString("operatorName")).commit();
        if (info.getString("alert") != null) {
            String title = info.getString("alert").split(" ")[0];
            editor.putString(OPERATOR_TITLE + consultId, title).commit();
        }
        editor.putString(OPERATOR_PHOTO + consultId, info.getString("operatorPhoto")).commit();
        setCurrentConsultId(consultId);
    }

    public void setCurrentConsultInfo(ConsultConnectionMessage message) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String consultId = message.getConsultId();
        if (consultId != null) setCurrentConsultId(consultId);
        editor.putString(OPERATOR_STATUS + consultId, message.getStatus()).commit();
        editor.putString(OPERATOR_NAME + consultId, message.getName()).commit();
        editor.putString(OPERATOR_TITLE + consultId, message.getTitle()).commit();
        editor.putString(OPERATOR_ORG_UNIT + consultId, message.getOrgUnit()).commit();
        editor.putString(OPERATOR_PHOTO + consultId, message.getAvatarPath()).commit();
        setCurrentConsultId(consultId);
    }

    public void setCurrentConsultInfo(ConsultPhrase consultPhrase) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String consultId = consultPhrase.getConsultId();
        if (consultId == null) return;
        setCurrentConsultId(consultPhrase.getConsultId());
        editor.putString(OPERATOR_STATUS + consultId, consultPhrase.getStatus()).commit();
        editor.putString(OPERATOR_NAME + consultId, consultPhrase.getConsultName()).commit();
        editor.putString(OPERATOR_PHOTO + consultId, consultPhrase.getAvatarPath()).commit();
        setCurrentConsultId(consultId);
    }


    public void setCurrentConsultId(String consultId) {
        sharedPreferences.edit().putString(OPERATOR_ID, consultId).commit();
    }

    public String getName(String id) {
        return sharedPreferences.getString(OPERATOR_NAME + id, null);
    }

    public String getCurrentConsultName() {
        if (getCurrentConsultId()==null)return null;
        return getName(getCurrentConsultId());
    }

    public String getCurrentConsultStatus() {
        if (getCurrentConsultId()==null)return null;
        return getStatus(getCurrentConsultId());
    }


    public String getStatus(String id) {
        return sharedPreferences.getString(OPERATOR_STATUS + id, null);
    }

    public String getConsultTitle(String id) {
        return sharedPreferences.getString(OPERATOR_TITLE + id, null);
    }

    public String getCurrentConsultTitle() {
        if (getCurrentConsultId()==null)return null;
        return getConsultTitle(getCurrentConsultId());
    }

    public String getOrgUnit(String id) {
        return sharedPreferences.getString(OPERATOR_ORG_UNIT + id, null);
    }

    private String getCurrentConsultOrgUnit() {
        if (getCurrentConsultId() == null) {
            return null;
        } else {
            return getOrgUnit(getCurrentConsultId());
        }
    }

    public String getPhotoUrl(String id) {
        return sharedPreferences.getString(OPERATOR_PHOTO + id, null);
    }

    public String getCurrentPhotoUrl() {
        if (getCurrentConsultId() == null) return null;
        return getPhotoUrl(getCurrentConsultId());
    }


    public String getCurrentConsultId() {
        return sharedPreferences.getString(OPERATOR_ID, null);
    }

    public void setCurrentConsultLeft() {
        sharedPreferences.edit().putString(OPERATOR_ID, null).commit();
    }


    public boolean isConsultConnected() {
        String id = sharedPreferences.getString(OPERATOR_ID, null);
        return id != null;
    }

    public ConsultInfo getConsultInfo(String id) {
        return new ConsultInfo(getName(id), getConsultTitle(id),
                getStatus(id), getOrgUnit(id), getPhotoUrl(id));
    }

    public ConsultInfo getCurrentConsultInfo() {

        String currentId = getCurrentConsultId();

        if (currentId == null) {
            return null;
        } else {
            return getConsultInfo(currentId);
        }
    }
}
