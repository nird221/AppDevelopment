package com.example.youkoldemo;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    // Defining xml Attributes:
    private Button btnLogin;

    // Defining the EXTRA_MESSAGE to send with the startActivity action
    public static final String EXTRA_MESSAGE = "com.example.youkoldemo.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Defining the xml page button
        btnLogin = findViewById(R.id.btnLogin);

        // Set the button On Click Function
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
                EditText editText = (EditText) findViewById(R.id.etxtLoginEmail);
                String message = editText.getText().toString();
                intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);
            }
        });
    }
}