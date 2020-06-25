package im.threads.internal.opengraph;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public final class OGDataConverterFactory extends Converter.Factory {

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {

        if (type.equals(OGData.class)) {
            return new OGDataConverter();
        } else {
            return retrofit.nextResponseBodyConverter(this, type, annotations);
        }
    }

    public static class OGDataConverter implements Converter<ResponseBody, OGData> {

        private final OGParser ogParser = new OGParser();

        @Override
        public OGData convert(ResponseBody value) throws IOException {
            return ogParser.parse(value.byteStream());
        }
    }
}
