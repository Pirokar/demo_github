package im.threads.utils;

import im.threads.model.ChatItem;
import im.threads.model.ChatPhrase;

import java.util.List;

/**
 *
 */

public class Seeker {
    private String lastQuery = "";
    private static final String TAG = "Seeker ";

    public List<ChatItem> seek(List<ChatItem> target,
                               boolean forward,
                               String query) {
        if (target == null || target.size() == 0) return target;
        if (query == null || query.length() == 0) {
            for (ChatItem ci : target) {
                if (ci instanceof ChatPhrase) if (((ChatPhrase) ci).isHighlight())
                    ((ChatPhrase) ci).setHighLighted(false);
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
            if (ci instanceof ChatPhrase) if (((ChatPhrase) ci).isHighlight())
                ((ChatPhrase) ci).setHighLighted(false);
        }
        lastQuery = new String(query);
        if (forward) {
            if (lastChosenIndex == 0) {//if it is last
                ((ChatPhrase) target.get(lastChosenIndex)).setHighLighted(true);
                return target;
            } else {
                boolean isFound = false;
                int initial = lastChosenIndex == -1 ? target.size() - 1 : lastChosenIndex - 1;
                for (int i = initial; i > 0; i--) {
                    if (target.get(i) instanceof ChatPhrase
                            && ((ChatPhrase) target.get(i)).getPhraseText() != null
                            && ((ChatPhrase) target.get(i)).getPhraseText().toLowerCase().contains(query)) {
                        ((ChatPhrase) target.get(i)).setHighLighted(true);
                        return target;
                    }
                }
                if (!isFound && lastChosenIndex == -1) {
                    return target;
                } else if (!isFound) {
                    ((ChatPhrase) target.get(lastChosenIndex)).setHighLighted(true);
                    return target;
                }
            }
        } else {
            if (lastChosenIndex == -1) {
                for (int i = target.size() - 1; i > 0; i--) {
                    if (target.get(i) instanceof ChatPhrase
                            && ((ChatPhrase) target.get(i)).getPhraseText() != null
                            && ((ChatPhrase) target.get(i)).getPhraseText().toLowerCase().contains(query)) {
                        ((ChatPhrase) target.get(i)).setHighLighted(true);
                        return target;
                    }
                }
                return target;
            }
            if ((lastChosenIndex - 1) < 0) {
                ((ChatPhrase) target.get(lastChosenIndex)).setHighLighted(true);
                return target;
            }
            boolean isFound = false;
            for (int i = lastChosenIndex + 1; i < target.size(); i++) {
                if (target.get(i) instanceof ChatPhrase
                        && ((ChatPhrase) target.get(i)).getPhraseText() != null
                        && ((ChatPhrase) target.get(i)).getPhraseText().toLowerCase().contains(query)) {
                    ((ChatPhrase) target.get(i)).setHighLighted(true);
                    return target;
                }
            }
            if (!isFound && lastChosenIndex == -1) {
                return target;
            } else if (!isFound) {
                ((ChatPhrase) target.get(lastChosenIndex)).setHighLighted(true);
                return target;
            }
        }
        return target;
    }
}
