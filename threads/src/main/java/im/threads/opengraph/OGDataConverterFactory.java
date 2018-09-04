package im.threads.opengraph;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class OGDataConverterFactory extends Converter.Factory {

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {

        if (type.equals(OGData.class)) {
            return new OGDataConverter();
        } else {
            return retrofit.nextResponseBodyConverter(this, type, annotations);
        }
    }

    public static class OGDataConverter implements Converter<ResponseBody, OGData> {

        @Override
        public OGData convert(ResponseBody value) throws IOException {
            return OGParser.parse(value.byteStream());
        }
    }
}
