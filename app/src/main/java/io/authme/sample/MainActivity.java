package io.authme.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import io.authme.sdk.AuthScreen;
import io.authme.sdk.server.Config;

import static io.authme.sdk.server.Config.STORED_VALUES;


public class MainActivity extends AppCompatActivity {

    Button patternbutton;
    private static final int RESULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainActivity.this.getSharedPreferences(STORED_VALUES, 0).edit().clear().commit();

        Config config = new Config(MainActivity.this);

        config.setEnvironment(Config.PRODUCTION);

        config.setAPIKey("YOUR_API_KEY_HERE");

        config.setEmailId("USER_EMAIL_ID_HERE");

        patternbutton = (Button) this.findViewById(R.id.pattern_button);

        patternbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callPatternActivity();
            }
        });
    }

    private void callPatternActivity() {
        Intent intent = new Intent(MainActivity.this, AuthScreen.class);
        startActivityForResult(intent, RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT : {
                switch (resultCode) {
                    case Config.SIGNUP_PATTERN : {
                        Toast.makeText(getApplicationContext(), "Sign up successfull", Toast.LENGTH_LONG)
                                .show();
                    } break;

                    case Config.LOGIN_PATTERN : {
                        Toast.makeText(getApplicationContext(), data.getStringExtra("response"), Toast.LENGTH_LONG)
                                .show();
                    } break;

                    case Config.FORGOT_PATTERN : {
                        Toast.makeText(getApplicationContext(), "Forgot Pattern", Toast.LENGTH_LONG)
                                .show();
                    } break;

                    case Config.RESULT_FAILED : {
                        Toast.makeText(getApplicationContext(), "Failed To Identify", Toast.LENGTH_LONG)
                                .show();
                        if (data.hasExtra("response")) {
                            Toast.makeText(getApplicationContext(), data.getStringExtra("response"), Toast.LENGTH_LONG)
                                    .show();
                        }
                    } break;

                    default: break;
                }

            } break;

            default: break;

      }
    }

}
