package im.threads.internal.markdown;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;

import com.yydcdut.markdown.MarkdownConfiguration;
import com.yydcdut.markdown.MarkdownProcessor;
import com.yydcdut.markdown.loader.DefaultLoader;
import com.yydcdut.markdown.syntax.text.TextFactory;
import com.yydcdut.markdown.theme.ThemeDefault;

import java.lang.ref.WeakReference;

import im.threads.ChatStyle;
import im.threads.internal.Config;

public class MarkdownProcessorHolder {

    private static WeakReference<MarkdownProcessor> incomingProcessorWeakReference = new WeakReference<>(null);
    private static WeakReference<MarkdownProcessor> outgoingProcessorWeakReference = new WeakReference<>(null);

    public static MarkdownProcessor getMarkdownProcessor(Type type) {
        MarkdownProcessor markdownProcessor = null;
        switch (type) {
            case INCOMING:
                markdownProcessor = incomingProcessorWeakReference.get();
                if (markdownProcessor == null) {
                    markdownProcessor = prepareIncomingProcessor();
                }
                break;
            case OUTGOING:
                markdownProcessor = outgoingProcessorWeakReference.get();
                if (markdownProcessor == null) {
                    markdownProcessor = prepareOutgoingProcessor();
                }
                break;
        }
        return markdownProcessor;
    }

    private static MarkdownProcessor prepareIncomingProcessor() {
        MarkdownProcessor markdownProcessor = new MarkdownProcessor(Config.instance.context);
        markdownProcessor.factory(TextFactory.create());
        markdownProcessor.config(getIncomingConfiguration());
        incomingProcessorWeakReference = new WeakReference<>(markdownProcessor);
        return markdownProcessor;
    }

    private static MarkdownConfiguration getIncomingConfiguration() {
        MarkdownConfiguration markdownConfiguration = Config.instance.getChatStyle().incomingMarkdownConfiguration;
        if (markdownConfiguration == null) {
            markdownConfiguration = getDefaultIncomingConfiguration();
        }
        return markdownConfiguration;
    }

    private static MarkdownConfiguration getDefaultIncomingConfiguration() {
        Context context = Config.instance.context;
        ChatStyle style = Config.instance.getChatStyle();
        return new MarkdownConfiguration.Builder(context)
                .setHeader1RelativeSize(1.6f)//default relative size of header1
                .setHeader2RelativeSize(1.5f)//default relative size of header2
                .setHeader3RelativeSize(1.4f)//default relative size of header3
                .setHeader4RelativeSize(1.3f)//default relative size of header4
                .setHeader5RelativeSize(1.2f)//default relative size of header5
                .setHeader6RelativeSize(1.1f)//default relative size of header6
                .setBlockQuotesLineColor(Color.LTGRAY)//default color of block quotes line
                .setBlockQuotesRelativeSize(1)//default relative size of block quotes text size
                .setHorizontalRulesColor(Color.LTGRAY)//default color of horizontal rules's background
                .setHorizontalRulesHeight(Color.LTGRAY)//default height of horizontal rules
                .setCodeFontColor(Color.LTGRAY)//default color of inline code's font
                .setCodeBgColor(Color.LTGRAY)//default color of inline code's background
                .setTheme(new ThemeDefault())//default code block theme
                .setTodoColor(Color.DKGRAY)//default color of todo
                .setTodoDoneColor(Color.DKGRAY)//default color of done
                .setRxMDImageLoader(new DefaultLoader(context))//default image loader
                .setDefaultImageSize(200, 200)//default image width & height
                .setLinkFontColor(style.incomingMessageLinkColor)
                .setOnLinkClickCallback((view, link) -> context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link))))
                .build();
    }

    private static MarkdownProcessor prepareOutgoingProcessor() {
        MarkdownProcessor markdownProcessor = new MarkdownProcessor(Config.instance.context);
        markdownProcessor.factory(TextFactory.create());
        markdownProcessor.config(getOutgoingConfiguration());
        incomingProcessorWeakReference = new WeakReference<>(markdownProcessor);
        return markdownProcessor;
    }

    private static MarkdownConfiguration getOutgoingConfiguration() {
        MarkdownConfiguration markdownConfiguration = Config.instance.getChatStyle().outgoingMarkdownConfiguration;
        if (markdownConfiguration == null) {
            markdownConfiguration = getDefaultOutgoingConfiguration();
        }
        return markdownConfiguration;
    }

    private static MarkdownConfiguration getDefaultOutgoingConfiguration() {
        Context context = Config.instance.context;
        ChatStyle style = Config.instance.getChatStyle();
        return new MarkdownConfiguration.Builder(context)
                .setHeader1RelativeSize(1.6f)//default relative size of header1
                .setHeader2RelativeSize(1.5f)//default relative size of header2
                .setHeader3RelativeSize(1.4f)//default relative size of header3
                .setHeader4RelativeSize(1.3f)//default relative size of header4
                .setHeader5RelativeSize(1.2f)//default relative size of header5
                .setHeader6RelativeSize(1.1f)//default relative size of header6
                .setBlockQuotesLineColor(Color.LTGRAY)//default color of block quotes line
                .setBlockQuotesRelativeSize(1)//default relative size of block quotes text size
                .setHorizontalRulesColor(Color.LTGRAY)//default color of horizontal rules's background
                .setHorizontalRulesHeight(Color.LTGRAY)//default height of horizontal rules
                .setCodeFontColor(Color.LTGRAY)//default color of inline code's font
                .setCodeBgColor(Color.LTGRAY)//default color of inline code's background
                .setTheme(new ThemeDefault())//default code block theme
                .setTodoColor(Color.DKGRAY)//default color of todo
                .setTodoDoneColor(Color.DKGRAY)//default color of done
                .setRxMDImageLoader(new DefaultLoader(context))//default image loader
                .setDefaultImageSize(200, 200)//default image width & height
                .setLinkFontColor(style.outgoingMessageLinkColor)
                .setOnLinkClickCallback((view, link) -> context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link))))
                .build();
    }

    public enum Type {
        INCOMING, OUTGOING
    }
}
