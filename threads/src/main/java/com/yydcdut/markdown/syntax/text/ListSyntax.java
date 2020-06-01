package com.yydcdut.markdown.syntax.text;

import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import com.yydcdut.markdown.MarkdownConfiguration;
import com.yydcdut.markdown.live.EditToken;
import com.yydcdut.markdown.span.MDBaseListSpan;
import com.yydcdut.markdown.span.MDOrderListSpan;
import com.yydcdut.markdown.span.MDUnOrderListSpan;
import com.yydcdut.markdown.syntax.Syntax;
import com.yydcdut.markdown.syntax.SyntaxKey;
import com.yydcdut.markdown.utils.SyntaxUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuyidong on 2018/5/28.
 */

public class ListSyntax implements Syntax {
    private static final int START_POSITION = 2;

    private int mUnorderColor;

    /**
     * Constructor
     *
     * @param markdownConfiguration RxMDConfiguration
     */
    public ListSyntax(@NonNull MarkdownConfiguration markdownConfiguration) {
        mUnorderColor = markdownConfiguration.getUnOrderListColor();
    }

    @Override
    public boolean isMatch(@NonNull CharSequence charSequence) {
        if (TextUtils.isEmpty(charSequence)) {
            return false;
        }
        String[] lines = charSequence.toString().split("\n");
        final int length = lines.length;
        for (int i = 0; i < length; i++) {
            String line = lines[i].trim();
            if (checkOrderLegal(line) || checkUnorderLegal(line)) {
                return true;
            } else {
                continue;
            }
        }
        return false;
    }

    @NonNull
    @Override
    public CharSequence format(@NonNull CharSequence charSequence, int lineNumber) {
        if (!(charSequence instanceof SpannableStringBuilder)) {
            return charSequence;
        }
        SpannableStringBuilder ssb = (SpannableStringBuilder) charSequence;
        int currentLineIndex = 0;
        String[] lines = charSequence.toString().split("\n");
        ArrayList<ListBean> list = new ArrayList<>(lines.length);
        SparseArray<MDBaseListSpan> listSpanByLineArray = new SparseArray<>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (checkOrderLegal(line.trim())) {
                currentLineIndex = formatOrder(ssb, line, list, i, currentLineIndex);
            } else if (checkUnorderLegal(line.trim())) {
                currentLineIndex = formatUnorder(ssb, line, list, i, currentLineIndex);
            } else {
                list.add(new ListBean(currentLineIndex, line, 0, -2, -1));
                currentLineIndex += (line + "\n").length();
            }
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            ListBean bean = list.get(i);
            if (bean != null && bean.number != -2) {
                MDBaseListSpan span;
                    if (bean.isOrder) {
                        span = setOrderSpan(bean.nested, bean.start, bean.line, ssb, bean.number, bean.originalNumber);
                    } else {
                        span = setUnorderSpan(bean.nested, bean.start, bean.line, bean.type, ssb, mUnorderColor);
                    }
                listSpanByLineArray.put(i, span);
            }
        }
        int previousLineIndex = -1;
        MDBaseListSpan previousSpan = null;
        for (int i = 0; i < listSpanByLineArray.size(); i++) {
            int lineIndex = listSpanByLineArray.keyAt(i);
            MDBaseListSpan span = listSpanByLineArray.get(lineIndex);
            if (previousSpan != null && previousLineIndex + 1 == lineIndex) {
                span.setParent(span);
            }
            previousLineIndex = lineIndex;
            previousSpan = span;
        }
        return ssb;
    }

    private static boolean checkOrderLegal(@NonNull String text) {
        if (text.length() < 3) {
            return false;
        }
        if (Character.isDigit(text.charAt(0))) {
            int dotPosition = 1;
            final int length = text.length();
            for (int i = 1; i < length; i++) {
                if (Character.isDigit(text.charAt(i))) {//一直都是数字
                    continue;
                } else {
                    dotPosition = i;
                    break;
                }
            }
            return (text.charAt(dotPosition) == SyntaxKey.DOT && text.charAt(dotPosition + 1) == ' ');
        }
        return false;
    }

    private static boolean checkUnorderLegal(@NonNull String text) {
        if (text.startsWith(SyntaxKey.KEY_UNORDER_LIST_ASTERISK)
                || text.startsWith(SyntaxKey.KEY_UNORDER_LIST_PLUS)
                || text.startsWith(SyntaxKey.KEY_UNORDER_LIST_HYPHEN)) {
            return true;
        } else {
            return false;
        }
    }

    private static int formatOrder(SpannableStringBuilder ssb, String line, ArrayList<ListBean> list, final int lineIndex, int currentLinePosition) {
        int nested = calculateOrderNested(line);
        if (nested < 0) {
            list.add(new ListBean(currentLinePosition, line, 0, -1, -1));
        } else if (SyntaxUtils.existCodeBlockSpan(ssb, currentLinePosition, currentLinePosition + (line).length())) {
            list.add(new ListBean(currentLinePosition, line, 0, -1, -1));
        } else {
            int originalNumber = calculateOrderNumber(line);
            if (lineIndex == 0) {
                list.add(new ListBean(currentLinePosition, line, nested, 1, originalNumber));
            } else {
                ListBean previousBean = list.get(lineIndex - 1);
                if (previousBean == null) {
                    // не могу представить пока когда такое возможно
                    list.add(new ListBean(currentLinePosition, line, 0, -1, originalNumber));
                } else {
                    if (nested == previousBean.nested + 1) {
                        // Вложенный добавляем с номером 1
                        list.add(new ListBean(currentLinePosition, line, nested, 1, originalNumber));
                    } else if (nested == previousBean.nested) {
                        // На одном уровне вложенности добавляем с номером  previousBean.number + 1
                        list.add(new ListBean(currentLinePosition, line, nested, previousBean.number + 1, originalNumber));
                    } else if (nested + 1 == previousBean.nested) {
                        // На одном уровне вложенности добавляем с номером  previousBean.number + 1
                        list.add(new ListBean(currentLinePosition, line, nested, previousBean.number + 1, originalNumber));
                    } else {
                        list.add(new ListBean(currentLinePosition, line, 0, 1, originalNumber));
                    }
                }
            }
        }
        currentLinePosition += (line + "\n").length();
        return currentLinePosition;
    }

    private static MDBaseListSpan setOrderSpan(int nested, int start, @NonNull String line, @NonNull SpannableStringBuilder ssb, int number, int originalNumber) {
        ssb.delete(start, start + nested * 3 + String.valueOf(originalNumber).length());
        ssb.insert(start, String.valueOf(number));
        MDOrderListSpan mdOrderListSpan = new MDOrderListSpan(10, nested, number);
        ssb.setSpan(mdOrderListSpan,
                start, start + line.length() - (nested * 3 + String.valueOf(originalNumber).length()),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return mdOrderListSpan;
    }

    private static int calculateOrderNested(@NonNull String line) {
        if (line.length() < 3) {
            return -1;
        }
        int nested = 0;
        for (int i = 0; i < line.length() && i + 1 < line.length() && i + 2 < line.length(); i += 3) {
            char first = line.charAt(i);
            char second = line.charAt(i + 1);
            char three = line.charAt(i + 2);
            if (first == ' ' && second == ' ' && three == ' ') {
                nested++;
            }
        }
        return nested;
    }

    private static int calculateNotOrderNested(@NonNull String line) {
        if (line.length() < 2) {
            return -1;
        }
        int nested = 0;
        for (int i = 0; i < line.length() && i + 1 < line.length(); i += 2) {
            char first = line.charAt(i);
            char second = line.charAt(i + 1);
            if (first == ' ' && second == ' ') {
                nested++;
            }
        }
        return nested;
    }

    private static int calculateOrderNumber(String text) {
        if (text.length() < 3) {
            return -1;
        }
        String s = text.trim();
        String[] tmp = s.split(". ");
        if (tmp.length > 0) {
            try {
                return Integer.parseInt(tmp[0]);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    private static int formatUnorder(SpannableStringBuilder ssb, String line, ArrayList<ListBean> list, final int lineIndex, int currentLinePosition) {
        if (line.startsWith(SyntaxKey.IGNORE_UNORDER_LIST_HYPHEN) || line.startsWith(SyntaxKey.IGNORE_UNORDER_LIST_ASTERISK) || line.startsWith(SyntaxKey.IGNORE_UNORDER_LIST_2)
                || line.startsWith(SyntaxKey.IGNORE_UNORDER_LIST_3) || line.startsWith(SyntaxKey.IGNORE_UNORDER_LIST_4) || line.startsWith(SyntaxKey.IGNORE_UNORDER_LIST_5)) {
            list.add(new ListBean(currentLinePosition, line, 0, 0));
        } else if (SyntaxUtils.existCodeBlockSpan(ssb, currentLinePosition, currentLinePosition + (line).length())) {
            list.add(new ListBean(currentLinePosition, line, 0, 0));
        } else if (line.startsWith(SyntaxKey.KEY_UNORDER_LIST_ASTERISK)) {
            list.add(new ListBean(currentLinePosition, line, 0, MDUnOrderListSpan.TYPE_KEY_2));
        } else if (line.startsWith(SyntaxKey.KEY_UNORDER_LIST_PLUS)) {
            list.add(new ListBean(currentLinePosition, line, 0, MDUnOrderListSpan.TYPE_KEY_0));
        } else if (line.startsWith(SyntaxKey.KEY_UNORDER_LIST_HYPHEN)) {
            list.add(new ListBean(currentLinePosition, line, 0, MDUnOrderListSpan.TYPE_KEY_1));
        } else if (!line.startsWith(SyntaxKey.KEY_LIST_HEADER)) {
            list.add(new ListBean(currentLinePosition, line, 0, 0));
        } else {
            int nested = calculateNotOrderNested(line);
            if (nested > 0) {
                int previousType = calculateUnorderType(line);
                ListBean previousBean = list.get(lineIndex - 1);
                if (previousBean != null && (nested <= previousBean.nested + 1)) {
                    list.add(new ListBean(currentLinePosition, line, nested, previousType));
                } else {
                    list.add(new ListBean(currentLinePosition, line, 0, 0));
                }
            }
        }
        currentLinePosition += (line + "\n").length();
        return currentLinePosition;
    }

    private static int calculateUnorderType(String text) {
        String trim = text.trim();
        switch (trim) {
            case SyntaxKey.KEY_UNORDER_LIST_CHAR_ASTERISK:
                return MDUnOrderListSpan.TYPE_KEY_0;
            case SyntaxKey.KEY_UNORDER_LIST_CHAR_PLUS:
                return MDUnOrderListSpan.TYPE_KEY_2;
            case SyntaxKey.KEY_UNORDER_LIST_CHAR_HYPHEN:
                return MDUnOrderListSpan.TYPE_KEY_1;
            default:
                return 0;
        }
    }

    private static MDBaseListSpan setUnorderSpan(int nested, int start, @NonNull String line, int type, @NonNull SpannableStringBuilder ssb, int color) {
        ssb.delete(start, start + nested * SyntaxKey.KEY_LIST_HEADER.length() + START_POSITION);
        MDUnOrderListSpan span = new MDUnOrderListSpan(10, color, nested, type);
        ssb.setSpan(span,
                start, start + line.length() - (nested * SyntaxKey.KEY_LIST_HEADER.length() + START_POSITION),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    @NonNull
    @Override
    public List<EditToken> format(@NonNull Editable editable) {
        return new ArrayList<>();
    }

    private static class ListBean {
        final int start;
        final String line;//without "\n"
        final int nested;
        final boolean isOrder;
        final int number;
        final int originalNumber;
        final int type;

        public ListBean(int start, String line, int nested, int number, int originalNumber) {
            this.start = start;
            this.line = line;
            this.nested = nested;
            this.number = number;
            this.originalNumber = originalNumber;
            this.isOrder = true;
            this.type = 0;
        }

        public ListBean(int start, String line, int nested, int type) {
            this.start = start;
            this.line = line;
            this.nested = nested;
            this.isOrder = false;
            this.number = -1;
            this.originalNumber = -1;
            this.type = type;
        }
    }
}
