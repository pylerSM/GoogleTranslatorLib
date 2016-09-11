package com.pyler.googletranslatorlib;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pyler.googletranslator.GoogleTranslation;
import com.pyler.googletranslator.GoogleTranslator;
import com.pyler.googletranslator.GoogleTranslatorResponse;

public class MainActivity extends Activity implements GoogleTranslatorResponse {
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button translate = (Button) findViewById(R.id.btnTranslate);
        final EditText text = (EditText) findViewById(R.id.editText);
        progressDialog = new ProgressDialog(this);

        translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Translating...");
                progressDialog.show();
                GoogleTranslator tr = new GoogleTranslator(MainActivity.this);
                tr.translate(text.getText().toString(), "auto", "sk");
            }
        });
    }

    @Override
    public void onTranslate(GoogleTranslation translation) {
        progressDialog.hide();
        TextView translationView = (TextView) findViewById(R.id.textView);
        translationView.setText(translation.getTranslation());
    }
}
