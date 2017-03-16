package io.authme.sample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

import io.authme.sdk.AuthScreen;
import io.authme.sdk.server.Config;


public class MainActivity extends AppCompatActivity {

    Button patternbutton;
    private static final int RESULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Config config = new Config(MainActivity.this);

        config.setEnvironment(Config.SANDBOX); //Change this to Config.PRODUCTION when you are ready

        config.setAPIKey("YOUR_API_KEY_HERE"); //Remember that the keys are different for sandbox and production

        config.setEmailId("USER_EMAIL_ID");

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

                    case Config.RESET_PATTERN: {
                        Toast.makeText(getApplicationContext(), "Reset Pattern", Toast.LENGTH_LONG)
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

    private Intent putLogo(Intent intent) {
        String fileName = "mylogo";
        Bitmap bitmap = BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.nestaway_bird_logo);
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE); // MODE_PRIAVE so that no one else can access this file
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
        intent.putExtra("logo", fileName);
        return intent;
    }

}
