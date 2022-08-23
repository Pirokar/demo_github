package im.threads.internal.utils;

import android.content.SharedPreferences;

import im.threads.business.models.ConsultConnectionMessage;
import im.threads.business.models.ConsultInfo;

public final class ConsultWriter {
    private static final String OPERATOR_STATUS = "OPERATOR_STATUS";
    private static final String OPERATOR_NAME = "OPERATOR_NAME";
    private static final String OPERATOR_TITLE = "OPERATOR_TITLE";
    private static final String OPERATOR_ORG_UNIT = "OPERATOR_ORG_UNIT";
    private static final String OPERATOR_ROLE = "OPERATOR_ROLE";
    private static final String OPERATOR_PHOTO = "OPERATOR_PHOTO";
    private static final String OPERATOR_ID = "OPERATOR_ID";
    private static final String SEARCHING_CONSULT = "SEARCHING_CONSULT";
    private final SharedPreferences sharedPreferences;

    /**
     * @param sharedPreferences only secured shared preferences are allowed!
     */
    public ConsultWriter(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void setSearchingConsult(boolean isSearching) {
        sharedPreferences.edit().putBoolean(ConsultWriter.class + SEARCHING_CONSULT, isSearching).commit();
    }

    public boolean isSearchingConsult() {
        return sharedPreferences.getBoolean(ConsultWriter.class + SEARCHING_CONSULT, false);
    }

    public void setCurrentConsultInfo(ConsultConnectionMessage message) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String consultId = message.getConsultId();
        editor
                .putString(OPERATOR_ID, consultId)
                .putString(OPERATOR_STATUS + consultId, message.getStatus())
                .putString(OPERATOR_NAME + consultId, message.getName())
                .putString(OPERATOR_TITLE + consultId, message.getTitle())
                .putString(OPERATOR_ORG_UNIT + consultId, message.getOrgUnit())
                .putString(OPERATOR_ROLE + consultId, message.getRole())
                .putString(OPERATOR_PHOTO + consultId, message.getAvatarPath())
                .commit();
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

    private String getRole(String id) {
        return sharedPreferences.getString(OPERATOR_ROLE + id, null);
    }

    private String getPhotoUrl(String id) {
        return sharedPreferences.getString(OPERATOR_PHOTO + id, null);
    }

    public String getCurrentPhotoUrl() {
        return getCurrentConsultId() != null ? getPhotoUrl(getCurrentConsultId()) : null;
    }

    public String getCurrentConsultId() {
        return sharedPreferences.getString(OPERATOR_ID, null);
    }

    public void setCurrentConsultLeft() {
        sharedPreferences.edit().putString(OPERATOR_ID, null).commit();
    }

    public boolean isConsultConnected() {
        return sharedPreferences.getString(OPERATOR_ID, null) != null;
    }

    public ConsultInfo getConsultInfo(String id) {
        return new ConsultInfo(getName(id), id,
                getStatus(id), getOrgUnit(id), getRole(id), getPhotoUrl(id));
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
