package im.threads.internal.utils;

import android.content.Context;

import im.threads.internal.model.FileDescription;

public abstract class DualFilePoster {
    private static final String TAG = "DualFilePoster ";
    private FileDescription fileDescription;
    private FileDescription qoteFileDescription;

    public DualFilePoster(final FileDescription fileDescription, final FileDescription quoteFileDescription, final Context ctx) {
        this.fileDescription = fileDescription;
        this.qoteFileDescription = quoteFileDescription;
        ThreadsLogger.i(TAG, "filePath = " + fileDescription + " quoteFilePath = " + quoteFileDescription);
        final DualFilePoster poster = this;
        if (fileDescription != null) {
            new FilePoster(fileDescription, ctx).post(new Callback<String, Throwable>() {
                @Override
                public void onSuccess(final String mfmsPath) {
                    if (quoteFileDescription == null) {
                        poster.onResult(mfmsPath, null);
                    } else {
                        new FilePoster(quoteFileDescription, ctx).post(new Callback<String, Throwable>() {
                            @Override
                            public void onSuccess(String quoteMfmsPath) {
                                poster.onResult(mfmsPath, quoteMfmsPath);
                            }

                            @Override
                            public void onError(Throwable error) {
                                poster.onError(error);
                            }
                        });
                    }
                }

                @Override
                public void onError(Throwable error) {
                    poster.onError(error);
                }
            });
        } else if (quoteFileDescription != null) {
            new FilePoster(quoteFileDescription, ctx).post(new Callback<String, Throwable>() {
                @Override
                public void onSuccess(String result) {
                    poster.onResult(null, result);
                }

                @Override
                public void onError(Throwable error) {
                    poster.onError(error);
                }
            });
        }
    }

    @Override
    public String toString() {
        return "DualFilePoster{" +
                "fileDescription=" + fileDescription +
                ", qoteFileDescription=" + qoteFileDescription +
                '}';
    }

    public abstract void onResult(String mfmsFilePath, String mfmsQuoteFilePath);

    public abstract void onError(Throwable e);
}
