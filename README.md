1. Prerequisite
-------------------

API KEY is a prerequisite - get your API keys from https://authme.io/merchant

2. Set Your API KEY
-------------------
        Config config = new Config();
        config.setApi_key("YOUR_API_KEY_HERE");
3. Set Email Id of the user
---------------------------
        config.setEmailId("USER_EMAIL_ID");
4. Call AuthScreen
---------------------------

Call AuthScreen.class as follows

- - - - - - - - - - - - - - - - - - - - -
        Intent intent = new Intent(MainActivity.this, AuthScreen.class);
        startActivityForResult(intent, RESULT);
 
5. Callback  
------------
You will recieve a callback in onActivityResult method

  1. If you called the activity for creating pattern, you would recieve the requestCode as REQ_CREATE_PATTERN. Following results are expected:

      a. User set the pattern successfully - in this case the resultCode would RESULT_OK
      
      b. User cancelled the activity - in this case the resultCode would be RESULT_CANCELED

  If you received RESULT_OK, then you are expected to save the pattern

  2. If you called the activity for comparing pattern, you would recieve the requestCode as REQ_ENTER_PATTERN. Following results are expected in this case:
 
      a. Pattern Matched - in this case the resultCode would be RESULT_OK
      
      b. User cancelled the activity - in this case the resultCode would be RESULT_CANCELED
      
      c. Failed to identify the user - in this case the resultCode would be RESULT_FAILED
      
      d. User forgot the pattern - in this case the resultCode would be RESULT_FORGOT_PATTERN


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