package im.threads.internal.opengraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

final class OGParser {

    private static final String DECODE_UTF8 = "UTF-8";
    private static final String TITLE = "og:title";
    private static final String IMAGE = "\"og:image\"";
    private static final String URL = "og:url";
    private static final String DESC = "og:description";

    private static final String TWITTER_TITLE = "twitter:title";
    private static final String TWITTER_IMAGE = "\"twitter:image\"";
    private static final String TWITTER_URL = "twitter:url";
    private static final String TWITTER_DESC = "twitter:description";

    private static final String HEAD_START_TAG = "<head";
    private static final String HEAD_END_TAG = "</head>";
    private static final String META_START_TAG = "<meta";
    private static final String CONTENT_PROPERTY = "content=\"";

    OGParser() {
    }

    OGData parse(InputStream inputStream) throws IOException {
        OGData.Builder ogDataBuilder = new OGData.Builder();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, DECODE_UTF8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String headContents = "", metaTags = "", sourceTextLine;
        boolean readingHead = false;
        while ((sourceTextLine = bufferedReader.readLine()) != null) {
            int headStart, headEnd;
            if (sourceTextLine.contains(HEAD_START_TAG)) {
                headStart = sourceTextLine.indexOf(">", sourceTextLine.indexOf(HEAD_START_TAG));
                if (headStart + 1 < sourceTextLine.length()) {
                    headContents = headContents + sourceTextLine.substring(headStart + 1);
                }
                if (sourceTextLine.contains(HEAD_END_TAG)) {
                    parseFromOneLineHeader(ogDataBuilder, headContents);
                }
                readingHead = true;
            } else if (sourceTextLine.contains(HEAD_END_TAG)) {
                sourceTextLine = sourceTextLine.trim();
                headEnd = sourceTextLine.indexOf(HEAD_END_TAG);
                if (headEnd != 0) {
                    headContents = headContents + sourceTextLine.substring(0, headEnd);
                    String meta = formattingMetaTags(headContents).replace("\'", "\"");
                    BufferedReader stringReader = new BufferedReader(new StringReader(meta));
                    String metaTagLine;
                    while ((metaTagLine = stringReader.readLine()) != null) {
                        fillOGData(ogDataBuilder, metaTagLine);
                    }
                }
                break;
            } else if (readingHead) {
                headContents = headContents + sourceTextLine.trim();
            }

            if (readingHead && sourceTextLine.contains(META_START_TAG)) {
                metaTags = metaTags + sourceTextLine + "\n";
                fillOGData(ogDataBuilder, sourceTextLine);
            }
        }
        bufferedReader.close();
        return ogDataBuilder.build();
    }

    private void parseFromOneLineHeader(OGData.Builder ogDataBuilder, String content) {
        int first = content.indexOf(META_START_TAG), last = content.lastIndexOf(META_START_TAG);
        while (first < last) {
            int tabLength = META_START_TAG.length();
            fillOGData(ogDataBuilder, content.substring(first, content.indexOf(META_START_TAG, first + tabLength)));
            first = content.indexOf(META_START_TAG, first + tabLength);
        }
    }

    private String formattingMetaTags(String headText) {
        String formattedText = "";
        int start = headText.indexOf(META_START_TAG), end = headText.indexOf(">", start) + 1;
        formattedText = formattedText + headText.substring(start, end) + "\n";
        int length = headText.length();
        while (end < length) {
            start = headText.indexOf(META_START_TAG, end);
            end = headText.indexOf(">", start) + 1;
            if (start >= 0 && start < length) {
                formattedText = formattedText + headText.substring(start, end) + "\n";
            } else {
                return formattedText;
            }
        }
        return formattedText;
    }

    private void fillOGData(OGData.Builder ogDataBuilder, String line) {
        int start = line.indexOf(CONTENT_PROPERTY) + CONTENT_PROPERTY.length();
        int end = line.indexOf("\"", start);
        if (line.contains(TITLE) || line.contains(TWITTER_TITLE)) {
            ogDataBuilder.title(line.substring(start, end));
        } else if (line.contains(IMAGE) || line.contains(TWITTER_IMAGE)) {
            ogDataBuilder.image(line.substring(start, end));
        } else if (line.contains(URL) || line.contains(TWITTER_URL)) {
            ogDataBuilder.url(line.substring(start, end));
        } else if (line.contains(DESC) || line.contains(TWITTER_DESC)) {
            ogDataBuilder.description(line.substring(start, end));
        }
    }
}
