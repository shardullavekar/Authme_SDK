package io.authme.sdk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;



public class MainActivity extends AppCompatActivity {
    private static final int REQ_CREATE_PATTERN = 1;
    private static final int REQ_ENTER_PATTERN = 2;
    private static final String PREFERENCES = "savedpreferences";

    Button patternbutton;
    SharedPreferences savedPattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Config config = new Config();
        config.setApi_key("YOUR_API_KEY_HERE");

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
//        savedPattern = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
//        String stringArray = savedPattern.getString("myByteArray", null);
//        if (stringArray != null) {
//            char[] charArray = stringArray.toCharArray();
//            Intent intent = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null,
//                    MainActivity.this, LockPatternActivity.class);
//            intent.putExtra(LockPatternActivity.EXTRA_PATTERN, charArray);
//            startActivityForResult(intent, REQ_ENTER_PATTERN);
//        } else {
//            Intent intent = new Intent(LockPatternActivity.ACTION_CREATE_PATTERN, null, MainActivity.this, LockPatternActivity.class);
//            startActivityForResult(intent, REQ_CREATE_PATTERN);
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case REQ_CREATE_PATTERN: {
//                switch (resultCode) {
//                    case RESULT_OK:
//                        Toast.makeText(MainActivity.this, "Pattern Created", Toast.LENGTH_LONG).show();
//                        char[] pattern = data.getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN);
//                        savePattern(pattern);
//                        break;
//                    case RESULT_CANCELED:
//                        Toast.makeText(MainActivity.this, "User Canceled", Toast.LENGTH_SHORT).show();
//                        break;
//                    default:
//                        break;
//                }
//
//            } break;
//
//            case REQ_ENTER_PATTERN: {
//                switch (resultCode) {
//                    case RESULT_OK:
//                        Toast.makeText(MainActivity.this, "User Matched", Toast.LENGTH_LONG).show();
//                        break;
//                    case RESULT_CANCELED:
//                        Toast.makeText(MainActivity.this, "User Canceled", Toast.LENGTH_SHORT).show();
//                        break;
//                    case LockPatternActivity.RESULT_FAILED:
//                        Toast.makeText(MainActivity.this, "Failed to identify", Toast.LENGTH_SHORT).show();
//                        break;
//                    case LockPatternActivity.RESULT_FORGOT_PATTERN:
//                        Toast.makeText(MainActivity.this, "Forgot pattern", Toast.LENGTH_SHORT).show();
//                        break;
//                    default:
//                        break;
//                }
//            } break;
//
//            default:
//                break;
//
//        }
    }

}
