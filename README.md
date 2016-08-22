1. Prerequisite
-------------------

API KEY is a prerequisite - please get in touch with us before you start integration.

2. Set Your API KEY
-------------------
        Config config = new Config();
        config.setApi_key("YOUR_API_KEY_HERE");
3. Set Email Id of the user
---------------------------
        config.setEmailId("USER_EMAIL_ID");
4. Call LockPatternActivity
---------------------------

If the pattern is set already, call the Activity to compare the set pattern.

Else, call the activity to create a user pattern.

![Creating user pattern](https://github.com/shardullavekar/Authme_SDK/blob/master/1.png)

- - - - - - - - - - - - - - - - - - - - -
        savedPattern = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        String stringArray = savedPattern.getString("myByteArray", null);
        if (stringArray != null) {
            //Pattern is already set!!
            //Call LockPatternActivity to unlock
            char[] charArray = stringArray.toCharArray();
            Intent intent = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null,
                    MainActivity.this, LockPatternActivity.class);
            intent.putExtra(LockPatternActivity.EXTRA_PATTERN, charArray);
            startActivityForResult(intent, REQ_ENTER_PATTERN);
        } else {
            //Pattern is not set
            //Call LockPatternActivity to set the pattern
            Intent intent = new Intent(LockPatternActivity.ACTION_CREATE_PATTERN, null, MainActivity.this, LockPatternActivity.class);
            startActivityForResult(intent, REQ_CREATE_PATTERN);
        }
 
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
                case REQ_CREATE_PATTERN: {
                  switch (resultCode) {
                    case RESULT_OK:
                        Toast.makeText(MainActivity.this, "Pattern Created", Toast.LENGTH_LONG).show();
                        char[] pattern = data.getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN);
                        savePattern(pattern);
                        break;
                    case RESULT_CANCELED:
                        Toast.makeText(MainActivity.this, "User Canceled", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }

            } break;
            case REQ_ENTER_PATTERN: {
                switch (resultCode) {
                    case RESULT_OK:
                        Toast.makeText(MainActivity.this, "User Matched", Toast.LENGTH_LONG).show();
                        break;
                    case RESULT_CANCELED:
                        Toast.makeText(MainActivity.this, "User Canceled", Toast.LENGTH_SHORT).show();
                        break;
                    case LockPatternActivity.RESULT_FAILED:
                        Toast.makeText(MainActivity.this, "Failed to identify", Toast.LENGTH_SHORT).show();
                        break;
                    case LockPatternActivity.RESULT_FORGOT_PATTERN:
                        Toast.makeText(MainActivity.this, "Forgot pattern", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            } break;

            default:
                break;

          }
        }
