package im.threads.internal.utils;

import android.content.SharedPreferences;

import im.threads.internal.model.ConsultConnectionMessage;
import im.threads.internal.model.ConsultInfo;

public final class ConsultWriter {
    private static final String OPERATOR_STATUS = "OPERATOR_STATUS";
    private static final String OPERATOR_NAME = "OPERATOR_NAME";
    private static final String OPERATOR_TITLE = "OPERATOR_TITLE";
    private static final String OPERATOR_ORG_UNIT = "OPERATOR_ORG_UNIT";
    private static final String OPERATOR_PHOTO = "OPERATOR_PHOTO";
    private static final String OPERATOR_ID = "OPERATOR_ID";
    private static final String SEARCHING_CONSULT = "SEARCHING_CONSULT";
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

    private void setCurrentConsultId(String consultId) {
        sharedPreferences.edit().putString(OPERATOR_ID, consultId).commit();
    }

    public String getName(String id) {
        return sharedPreferences.getString(OPERATOR_NAME + id, null);
    }

    private String getStatus(String id) {
        return sharedPreferences.getString(OPERATOR_STATUS + id, null);
    }

    private String getOrgUnit(String id) {
        return sharedPreferences.getString(OPERATOR_ORG_UNIT + id, null);
    }

    private String getPhotoUrl(String id) {
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
        return new ConsultInfo(getName(id), id,
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
