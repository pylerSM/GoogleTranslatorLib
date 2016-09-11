package com.pyler.googletranslator;

public class GoogleTranslation {
    private int mTranslationId;
    private String mTranslation;
    private int mErrorCode;

    public static final String AUTO_DETECT_LANGUAGE = "auto";
    public static final int NO_TRANSLATION_ID = -1;
    public static final int NO_ERROR = 0;
    public static final int ERROR_INVALID_INPUT_DATA = 1;
    public static final int ERROR_CONNECTION = 2;
    public static final int ERROR_INVALID_OUTPUT_DATA = 3;

    public GoogleTranslation(int translationId, String translation, int errorCode) {
        mTranslationId = translationId;
        mTranslation = translation;
        mErrorCode = errorCode;
    }

    public int getTranslationId() {
        return mTranslationId;
    }

    public String getTranslation() {
        return mTranslation;
    }

    public int getErrorCode() {
        return mErrorCode;
    }
}
