package im.threads.internal.opengraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

final class OGParser {

    private static final String DECODE_UTF8 = "UTF-8";

    private static final String OG_TITLE = "og:title";
    private static final String OG_IMAGE = "og:image";
    private static final String OG_URL = "og:url";
    private static final String OG_DESC = "og:description";

    private static final String TWITTER_TITLE = "twitter:title";
    private static final String TWITTER_IMAGE = "twitter:image";
    private static final String TWITTER_URL = "twitter:url";
    private static final String TWITTER_DESC = "twitter:description";

    private static final String HEAD_START_TAG = "<head";
    private static final String HEAD_END_TAG = "</head>";
    private static final String META_START_TAG = "<meta";
    private static final String PROPERTY_NAME = "property=\"";
    private static final String PROPERTY_CONTENT = "content=\"";

    OGParser() {
    }

    OGData parse(InputStream inputStream) throws IOException {
        return parseHeader(readHeader(inputStream));
    }

    private String readHeader(InputStream inputStream) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, DECODE_UTF8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder headContents = new StringBuilder();
        String sourceTextLine;
        boolean readingHead = false;
        while ((sourceTextLine = bufferedReader.readLine()) != null) {
            int headStart, headEnd;
            if (sourceTextLine.contains(HEAD_START_TAG)) {
                headStart = sourceTextLine.indexOf(">", sourceTextLine.indexOf(HEAD_START_TAG));
                if (headStart + 1 < sourceTextLine.length()) {
                    headContents.append(sourceTextLine.substring(headStart + 1));
                }
                if (sourceTextLine.contains(HEAD_END_TAG)) {
                    return headContents.toString().substring(0, sourceTextLine.indexOf(HEAD_END_TAG));
                } else {
                    readingHead = true;
                }
            } else if (sourceTextLine.contains(HEAD_END_TAG)) {
                sourceTextLine = sourceTextLine.trim();
                headEnd = sourceTextLine.indexOf(HEAD_END_TAG);
                if (headEnd != 0) {
                     return headContents + sourceTextLine.substring(0, headEnd);
                }
                readingHead = false;
            } else if (readingHead) {
                headContents.append(sourceTextLine.trim());
            }
        }
        bufferedReader.close();
        return headContents.toString();
    }

    private OGData parseHeader(String headContents) {
        OGData.Builder ogDataBuilder = new OGData.Builder();
        List<Meta> metaList = new ArrayList<>(parseMetaTags(headContents));
        for (Meta meta : metaList) {
            fillOGFromMeta(ogDataBuilder, meta);
        }
        return ogDataBuilder.build();
    }

    private void fillOGFromMeta(OGData.Builder ogDataBuilder, Meta meta) {
        switch (meta.name) {
            case OG_TITLE:
            case TWITTER_TITLE:
                ogDataBuilder.title(meta.content);
                break;
            case OG_IMAGE:
            case TWITTER_IMAGE:
                ogDataBuilder.image(meta.content);
                break;
            case OG_URL:
            case TWITTER_URL:
                ogDataBuilder.url(meta.content);
                break;
            case OG_DESC:
            case TWITTER_DESC:
                ogDataBuilder.description(meta.content);
                break;
        }
    }

    private List<Meta> parseMetaTags(String headText) {
        List<Meta> metaList = new ArrayList<>();
        int start = headText.indexOf(META_START_TAG);
        int end = headText.indexOf(">", start) + 1;
        int length = headText.length();
        while (end < length) {
            if (start >= 0 && start < length) {
                String metaStr = headText.substring(start, end);
                String propertyName = getPropertyName(metaStr);
                if (propertyName != null) {
                    metaList.add(new Meta(propertyName, getPropertyContent(metaStr)));
                }
            } else {
                return metaList;
            }
            start = headText.indexOf(META_START_TAG, end);
            end = headText.indexOf(">", start) + 1;
        }
        return metaList;
    }

    private String getPropertyContent(String line) {
        int start = line.indexOf(PROPERTY_CONTENT) + PROPERTY_CONTENT.length();
        int end = line.indexOf("\"", start);
        return line.substring(start, end);
    }

    private String getPropertyName(String line) {
        if (line.contains(PROPERTY_NAME)) {
            int start = line.indexOf(PROPERTY_NAME) + PROPERTY_NAME.length();
            int end = line.indexOf("\"", start);
            return line.substring(start, end);
        } else {
            return null;
        }
    }

    private static class Meta {

        private final String name;
        private final String content;

        private Meta(String name, String content) {
            this.name = name;
            this.content = content;
        }
    }

}
