package com.sequenia.threads.utils;


import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ChatPhrase;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.model.ConsultTyping;
import com.sequenia.threads.model.DateRow;
import com.sequenia.threads.model.UserPhrase;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**

 */

public class SeekerTest {
    @Test
    public void seek() throws Exception {
        List<ChatItem> items = new ArrayList<>();
        items.add(new DateRow(0L));//0
        items.add(new UserPhrase(r(), "test", null, t(), null));//1
        items.add(new UserPhrase(r(), "test1", null, t(), null));//2
        items.add(new UserPhrase(r(), "test2", null, t(), null));//3
        items.add(new UserPhrase(r(), "test3", null, t(), null));//4
        items.add(new UserPhrase(r(), "test4", null, t(), null));//5
        items.add(new ConsultConnectionMessage(r(), r(), r(), false, t(), r(), r(), r(), r()));//6
        items.add(new ConsultPhrase(null, null, r(), r(), "test", t(), r(), r(), false, r(), false));//7
        items.add(new ConsultPhrase(null, null, r(), r(), "test1", t(), r(), r(), false, r(), false));//8
        items.add(new ConsultPhrase(null, null, r(), r(), "test2", t(), r(), r(), false, r(), false));//9
        items.add(new ConsultPhrase(null, null, r(), r(), "test3", t(), r(), r(), false, r(), false));//10
        items.add(new ConsultPhrase(null, null, r(), r(), "test4", t(), r(), r(), false, r(), false));//11
        items.add(new ConsultPhrase(null, null, r(), r(), "test5", t(), r(), r(), false, r(), false));//12
        items.add(new UserPhrase(r(), "answer", null, t(), null));//13
        items.add(new ConsultPhrase(null, null, r(), r(), "uniq", t(), r(), r(), false, r(), false));//14
        items.add(new ConsultTyping(r(), t(), r()));//15

        Seeker s = new Seeker();
        s.seek(items, true, r());
        Pair<Boolean, Integer> result = findHighlight(items);
        assertEquals("random message not found and not dup", false, result.first);
        assertEquals("random message not found and not dup", -1, ((int) result.second));
        s.seek(items, true, r());
        s.seek(items, true, r());
        s.seek(items, true, r());
        s.seek(items, false, r());
        result = findHighlight(items);
        assertEquals("random message not found and not dup", false, result.first);
        assertEquals("random message not found and not dup", -1, ((int) result.second));

        s.seek(items, true, "");
        s.seek(items, false, "");
        result = findHighlight(items);
        assertEquals("random message not found and not dup", false, result.first);
        assertEquals("random message not found and not dup", -1, ((int) result.second));

        s.seek(items, true, "uniq");
        result = findHighlight(items);
        assertEquals("search the uniq message in ubidirections must grant same result", false, result.first);
        assertEquals("search the uniq message in ubidirections must grant same result", 14, ((int) result.second));

        s.seek(items, false, "uniq");
        result = findHighlight(items);
        assertEquals("search the uniq message in ubidirections must grant same result", false, result.first);
        assertEquals("search the uniq message in ubidirections must grant same result", 14, ((int) result.second));

        s.seek(items, true, "test");
        result = findHighlight(items);
        assertEquals("must be 1 item and not dup", false, result.first);
        assertEquals("must be 1 item and not dup", 1, ((int) result.second));

        s.seek(items, false, "test");
        result = findHighlight(items);
        assertEquals("must be 1 item and not dup", false, result.first);
        assertEquals("backward must still choose last", 1, ((int) result.second));

        s.seek(items, true, "test");
        result = findHighlight(items);
        assertEquals("must be 2 item and not dup", false, result.first);
        assertEquals("must be 2 item and not dup", 2, ((int) result.second));

        s.seek(items, true, "test");
        result = findHighlight(items);
        assertEquals("must be 3 item and not dup", false, result.first);
        assertEquals("must be 3 item and not dup",3, ((int) result.second));

        s.seek(items, true, "test");
        result = findHighlight(items);
        assertEquals("must be 4 item and not dup", false, result.first);
        assertEquals("must be 4 item and not dup",4, ((int) result.second));

        s.seek(items, false, "test");
        result = findHighlight(items);
        assertEquals("must be 3 item and not dup", false, result.first);
        assertEquals("must correctly seek back",3, ((int) result.second));

        s.seek(items, false, "test");
        result = findHighlight(items);
        assertEquals("must be 2 item and not dup", false, result.first);
        assertEquals("must correctly seek back",2, ((int) result.second));

        s.seek(items, false, "test");
        result = findHighlight(items);
        assertEquals("must be 1 item and not dup", false, result.first);
        assertEquals("must correctly seek back",1, ((int) result.second));

        s.seek(items, false, "test");
        result = findHighlight(items);
        assertEquals("must be 1 item and not dup", false, result.first);
        assertEquals("must correctly seek back even after end",1, ((int) result.second));

        s.seek(items, false, "");
        result = findHighlight(items);
        assertEquals("must correctly clear list", false, result.first);
        assertEquals("must correctly clear list",-1, ((int) result.second));

        s.seek(items, false, "t");
        result = findHighlight(items);
        assertEquals("must correctly do initial search", false, result.first);
        assertEquals("must correctly do initial search",1, ((int) result.second));

        s.seek(items, true, "t");
        result = findHighlight(items);
        assertEquals("must correctly do initial search", false, result.first);
        assertEquals("must correctly do initial search",2, ((int) result.second));

        s.seek(items, false, "t");
        result = findHighlight(items);
        assertEquals("must correctly do initial search", false, result.first);
        assertEquals("must correctly do initial search",1, ((int) result.second));

        s.seek(items, false, "");
        result = findHighlight(items);
        assertEquals("must correctly clear list", false, result.first);
        assertEquals("must correctly clear list",-1, ((int) result.second));

        for (int i = 0; i < 25; i++) {
            s.seek(items, false, "test");
        }
        result = findHighlight(items);
        assertEquals("must correctly work with many queryies", false, result.first);
        assertEquals("must correctly work with many queryies",1, ((int) result.second));

        for (int i = 0; i < 25; i++) {
            s.seek(items, true, "test");
        }
        result = findHighlight(items);
        assertEquals("must correctly work with many queryies", false, result.first);
        assertEquals("must correctly work with many queryies",12, ((int) result.second));

        items.clear();
        for (int i = 0; i < 25; i++) {
            s.seek(items, true, "test");
        }
        result = findHighlight(items);
        assertEquals("must work on empty lists", false, result.first);
        assertEquals("must work on empty lists",-1, ((int) result.second));
        for (int i = 0; i < 25; i++) {
            s.seek(items, false, "test");
        }
        result = findHighlight(items);
        assertEquals("must work on empty lists", false, result.first);
        assertEquals("must work on empty lists",-1, ((int) result.second));
    }

    private Pair<Boolean, Integer> findHighlight(List<ChatItem> items) {
        int lastIndex = -1;
        boolean dup = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof ChatPhrase) {
                if (((ChatPhrase) items.get(i)).isHighlight()) {
                    if (lastIndex != -1) dup = true;
                    lastIndex = i;
                }
            }
        }
        return new Pair<>(dup, lastIndex);
    }

    private String r() {
        return String.valueOf(System.currentTimeMillis());
    }

    private long t() {
        return System.currentTimeMillis();
    }

    private class Pair<F, S> {
        public final F first;
        public final S second;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "first=" + first +
                    ", second=" + second +
                    '}';
        }
    }
}