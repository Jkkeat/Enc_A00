package com.example.user.myapplication;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class PasswordDialog extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_dialog);
    }

    public void PassWordButtonClicked(View view)
    {
        EditText editText = (EditText)findViewById(R.id.password);
        String Password = editText.getText().toString();

        Intent intent = new Intent();
        intent.putExtra("Userpassword",Password);
        setResult(RESULT_OK,intent);
        finish();
    }
}
