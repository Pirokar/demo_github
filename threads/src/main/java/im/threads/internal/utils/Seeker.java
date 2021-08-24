package im.threads.internal.utils;

import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ChatPhrase;

import java.util.List;

public final class Seeker {
    private String lastQuery = "";

    public List<ChatItem> seek(List<ChatItem> target,
                               boolean forward,
                               String query) {
        if (target == null || target.size() == 0) return target;
        if (query == null || query.length() == 0) {
            for (ChatItem ci : target) {
                if (ci instanceof ChatPhrase) if (((ChatPhrase) ci).isHighlight()) {
                    ((ChatPhrase) ci).setHighLighted(false);
                }
            }
            lastQuery = "";
            return target;
        }
        int lastChosenIndex = -1;
        if (lastQuery.equals(query)) {
            for (ChatItem ci : target) {
                if (ci instanceof ChatPhrase) {
                    if (((ChatPhrase) ci).isHighlight()) {
                        lastChosenIndex = target.lastIndexOf(ci);
                        break;
                    }
                }
            }

        }
        for (ChatItem ci : target) {
            if (ci instanceof ChatPhrase) if (((ChatPhrase) ci).isHighlight()) {
                ((ChatPhrase) ci).setHighLighted(false);
            }
        }
        lastQuery = query;
        //для поиска сообщений в чате - пробегаемся по всем сообщениям и отмечаем
        //те, которые соответствуют запросу
        for (ChatItem chatItem : target) {
            if (chatItem instanceof ChatPhrase) {
                ((ChatPhrase) chatItem).setFound(
                        ((ChatPhrase) chatItem).getPhraseText() != null
                                && ((ChatPhrase) chatItem).getPhraseText().toLowerCase().contains(query));
            }
        }
        if (forward) {
            if (lastChosenIndex == 0) {//if it is last
                ((ChatPhrase) target.get(lastChosenIndex)).setHighLighted(true);
                return target;
            } else {
                int initial = lastChosenIndex == -1 ? target.size() - 1 : lastChosenIndex - 1;
                for (int i = initial; i >= 0; i--) {
                    if (target.get(i) instanceof ChatPhrase
                            && ((ChatPhrase) target.get(i)).getPhraseText() != null
                            && ((ChatPhrase) target.get(i)).getPhraseText().toLowerCase().contains(query)) {
                        ((ChatPhrase) target.get(i)).setHighLighted(true);
                        return target;
                    }
                }
                if (lastChosenIndex != -1) {
                    ((ChatPhrase) target.get(lastChosenIndex)).setHighLighted(true);
                }
                return target;
            }
        } else {
            if (lastChosenIndex == -1) {
                for (int i = target.size() - 1; i >= 0; i--) {
                    if (target.get(i) instanceof ChatPhrase
                            && ((ChatPhrase) target.get(i)).getPhraseText() != null
                            && ((ChatPhrase) target.get(i)).getPhraseText().toLowerCase().contains(query)) {
                        ((ChatPhrase) target.get(i)).setHighLighted(true);
                        return target;
                    }
                }
                return target;
            }
            for (int i = lastChosenIndex + 1; i < target.size(); i++) {
                if (target.get(i) instanceof ChatPhrase
                        && ((ChatPhrase) target.get(i)).getPhraseText() != null
                        && ((ChatPhrase) target.get(i)).getPhraseText().toLowerCase().contains(query)) {
                    ((ChatPhrase) target.get(i)).setHighLighted(true);
                    return target;
                }
            }
            ((ChatPhrase) target.get(lastChosenIndex)).setHighLighted(true);
            return target;
        }
    }
}
