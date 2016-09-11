package com.pyler.googletranslator;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class GoogleTranslator {
    private Context mContext;

    public GoogleTranslator(Context context) {
        mContext = context;
    }

    public void translate(String sourceText, String targetLanguage) {
        translate(sourceText, GoogleTranslation.AUTO_DETECT_LANGUAGE, targetLanguage);
    }

    public void translate(String sourceText, String sourceLanguage, String targetLanguage) {
        translate(GoogleTranslation.NO_TRANSLATION_ID, sourceText, sourceLanguage, targetLanguage);
    }

    public void translate(int translationId, String sourceText, String sourceLanguage, String targetLanguage) {
        String userAgent = new WebView(mContext).getSettings().getUserAgentString();
        String id = String.valueOf(translationId);
        new GoogleTranslatorImpl(mContext).execute(id, sourceText, sourceLanguage, targetLanguage, userAgent);
    }

    public void translateViaApp(String sourceText, String targetLanguage) {
        translateViaApp(sourceText, GoogleTranslation.AUTO_DETECT_LANGUAGE, targetLanguage);
    }

    public boolean translateViaApp(String sourceText, String sourceLanguage, String targetLanguage) {
        Intent intent;
        intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setPackage("com.google.android.apps.translate");

        Uri uri = new Uri.Builder()
                .scheme("https")
                .authority("translate.google.com")
                .path("/m/translate")
                .appendQueryParameter("q", sourceText)
                .appendQueryParameter("sl", sourceLanguage)
                .appendQueryParameter("tl", targetLanguage)
                .build();

        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            return false;
        }
        return true;
    }

    private class GoogleTranslatorImpl extends AsyncTask<String, String, GoogleTranslation> {
        private Context mContext;
        private GoogleTranslatorResponse mResponseHandler = null;

        private GoogleTranslatorImpl(Context context) {
            mContext = context;
            mResponseHandler = (GoogleTranslatorResponse) context;
        }

        @Override
        protected GoogleTranslation doInBackground(String... params) {

            int translationId = Integer.valueOf(params[0]);
            String sourceText = params[1];
            String sourceLanguage = params[2];
            String targetLanguage = params[3];
            String userAgent = params[4];

            if (sourceText == null || sourceLanguage == null || targetLanguage == null) {
                return new GoogleTranslation(translationId, null, GoogleTranslation.ERROR_INVALID_INPUT_DATA);
            }

            if (sourceText.isEmpty()) {
                return new GoogleTranslation(translationId, sourceText, GoogleTranslation.NO_ERROR);
            }

            return translateViaApi(translationId, sourceText, sourceLanguage, targetLanguage, userAgent);
        }

        @Override
        protected void onPostExecute(GoogleTranslation result) {
            mResponseHandler.onTranslate(result);
        }

        private GoogleTranslation translateViaApi(int translationId, String sourceText, String sourceLanguage, String targetLanguage, String userAgent) {

            if (sourceLanguage.isEmpty()) {
                sourceLanguage = GoogleTranslation.AUTO_DETECT_LANGUAGE;
            }

            URL url = null;
            try {
                url = new URL("https://translate.googleapis.com/translate_a/t?client=gtx&text=" + Uri.encode(sourceText) + "&sl=" + sourceLanguage + "&tl=" + targetLanguage);
            } catch (MalformedURLException e) {
                return new GoogleTranslation(translationId, null, GoogleTranslation.ERROR_INVALID_INPUT_DATA);
            }

            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", userAgent);
            } catch (IOException e) {
                return new GoogleTranslation(translationId, null, GoogleTranslation.ERROR_CONNECTION);
            }

            try {
                connection.setRequestMethod("GET");
            } catch (ProtocolException e) {
                return new GoogleTranslation(translationId, null, GoogleTranslation.ERROR_CONNECTION);
            }
            try {
                connection.connect();
            } catch (IOException e) {
                return new GoogleTranslation(translationId, null, GoogleTranslation.ERROR_CONNECTION);
            }

            try {
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    return translateViaWebpage(translationId, sourceText, sourceLanguage, targetLanguage, userAgent);
                } else {
                    BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                    StringBuilder sb = new StringBuilder();
                    String output;
                    while ((output = br.readLine()) != null) {
                        sb.append(output);
                    }

                    String response = sb.toString();

                    String translation = null;

                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        translation = (String) jsonArray.get(0);
                    } catch (JSONException e) {
                        translation = response.replace("\"", "");
                    }

                    return new GoogleTranslation(translationId, translation, GoogleTranslation.NO_ERROR);
                }

            } catch (IOException e) {
                return new GoogleTranslation(translationId, null, GoogleTranslation.ERROR_CONNECTION);

            }
        }

        private GoogleTranslation translateViaWebpage(int translationId, String sourceText, String sourceLanguage, String targetLanguage, String userAgent) {
            if (sourceLanguage.isEmpty()) {
                sourceLanguage = GoogleTranslation.AUTO_DETECT_LANGUAGE;
            }

            URL url = null;
            try {
                url = new URL("https://translate.google.com/m?hl=sk&sl=" + sourceLanguage + "&tl=" + targetLanguage + "&ie=UTF-8&prev=_m&q=" + Uri.encode(sourceText));
            } catch (MalformedURLException e) {
                return new GoogleTranslation(translationId, null, GoogleTranslation.ERROR_INVALID_INPUT_DATA);
            }

            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", userAgent);
            } catch (IOException e) {
                return new GoogleTranslation(translationId, null, GoogleTranslation.ERROR_CONNECTION);
            }

            try {
                connection.setRequestMethod("GET");
            } catch (ProtocolException e) {
                return new GoogleTranslation(translationId, null, GoogleTranslation.ERROR_CONNECTION);
            }
            try {
                connection.connect();
            } catch (IOException e) {
                return new GoogleTranslation(translationId, null, GoogleTranslation.ERROR_CONNECTION);
            }

            try {
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    return new GoogleTranslation(translationId, null, GoogleTranslation.ERROR_CONNECTION);
                } else {
                    BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                    StringBuilder sb = new StringBuilder();
                    String output;
                    while ((output = br.readLine()) != null) {
                        sb.append(output);
                    }

                    String response = sb.toString();

                    int divStart = response.indexOf("<div dir=\"ltr\" class=\"t0\">") + 26;

                    if (divStart <= 0) {
                        return new GoogleTranslation(translationId, null, GoogleTranslation.ERROR_INVALID_OUTPUT_DATA);
                    }

                    response = response.substring(divStart, response.length());
                    int divEnd = response.indexOf("</div>");
                    String translation = response.substring(0, divEnd);
                    return new GoogleTranslation(translationId, translation, GoogleTranslation.NO_ERROR);
                }

            } catch (IOException e) {
                return new GoogleTranslation(translationId, null, GoogleTranslation.ERROR_CONNECTION);

            }
        }


    }
}
