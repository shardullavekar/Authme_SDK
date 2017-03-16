/*
 *   Copyright 2017 Authme ID Services Pvt. Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.authme.sdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.authme.sdk.util.AlpSettings;
import io.authme.sdk.util.IEncrypter;
import io.authme.sdk.util.InvalidEncrypterException;
import io.authme.sdk.util.LoadingView;
import io.authme.sdk.util.ResourceUtils;
import io.authme.sdk.util.UI;
import io.authme.sdk.widget.LockPatternUtils;
import io.authme.sdk.widget.LockPatternView;
import io.authme.sdk.widget.LockPatternView.Cell;
import io.authme.sdk.widget.LockPatternView.DisplayMode;

import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static io.authme.sdk.BuildConfig.DEBUG;
import static io.authme.sdk.util.AlpSettings.Display.METADATA_CAPTCHA_WIRED_DOTS;
import static io.authme.sdk.util.AlpSettings.Display.METADATA_MAX_RETRIES;
import static io.authme.sdk.util.AlpSettings.Display.METADATA_MIN_WIRED_DOTS;
import static io.authme.sdk.util.AlpSettings.Display.METADATA_STEALTH_MODE;
import static io.authme.sdk.util.AlpSettings.Security.METADATA_AUTO_SAVE_PATTERN;
import static io.authme.sdk.util.AlpSettings.Security.METADATA_ENCRYPTER_CLASS;


public class LockPatternActivity extends Activity {
    private static final String CLASSNAME = LockPatternActivity.class.getName();

    public static final String ACTION_CREATE_PATTERN = CLASSNAME + ".CREATE_PATTERN";
    public static final String PATTERN_JSON = CLASSNAME + ".PATTERN_JSON";


    public static Intent newIntentToCreatePattern(Context context) {
        Intent result = new Intent(ACTION_CREATE_PATTERN, null, context, LockPatternActivity.class);
        return result;
    }

    public static void startToCreatePattern(Object caller, Context context, int requestCode) {
        call_startActivityForResult(caller, newIntentToCreatePattern(context), requestCode);
    }

    public static void call_startActivityForResult(Object caller, Intent intent, int requestCode) {
        try {
            Method method = caller.getClass().getMethod("startActivityForResult", Intent.class, int.class);
            method.setAccessible(true);
            method.invoke(caller, intent, requestCode);
        } catch (Throwable t) {
            Log.e(CLASSNAME, t.getMessage(), t);
            // Re-throw it
            throw new RuntimeException(t);
        }
    }

    public static final String ACTION_COMPARE_PATTERN = CLASSNAME + ".COMPARE_PATTERN";


    public static Intent newIntentToComparePattern(Context context, char[] pattern) {
        Intent result = new Intent(ACTION_COMPARE_PATTERN, null, context, LockPatternActivity.class);
        if (pattern != null) result.putExtra(EXTRA_PATTERN, pattern);

        return result;
    }

    public static void startToComparePattern(Object caller, Context context, int requestCode, char[] pattern) {
        call_startActivityForResult(caller, newIntentToComparePattern(context, pattern), requestCode);
    }

    public static final String ACTION_VERIFY_CAPTCHA = CLASSNAME + ".VERIFY_CAPTCHA";


    public static Intent newIntentToVerifyCaptcha(Context context) {
        Intent result = new Intent(ACTION_VERIFY_CAPTCHA, null, context, LockPatternActivity.class);
        return result;
    }

    public static void startToVerifyCaptcha(Object caller, Context context, int requestCode) {
        call_startActivityForResult(caller, newIntentToVerifyCaptcha(context), requestCode);
    }

    public static final int RESULT_FAILED = RESULT_FIRST_USER + 1;


    public static final int RESULT_FORGOT_PATTERN = RESULT_FIRST_USER + 2;


    public static final String EXTRA_RETRY_COUNT = CLASSNAME + ".RETRY_COUNT";


    public static final String EXTRA_THEME = CLASSNAME + ".THEME";


    public static final String EXTRA_PATTERN = CLASSNAME + ".PATTERN";


    public static final String EXTRA_RESULT_RECEIVER = CLASSNAME + ".RESULT_RECEIVER";


    public static final String EXTRA_PENDING_INTENT_OK = CLASSNAME + ".PENDING_INTENT_OK";


    public static final String EXTRA_PENDING_INTENT_CANCELLED = CLASSNAME + ".PENDING_INTENT_CANCELLED";


    public static final String EXTRA_PENDING_INTENT_FORGOT_PATTERN = CLASSNAME + ".PENDING_INTENT_FORGOT_PATTERN";


    private enum ButtonOkCommand {
        CONTINUE, FORGOT_PATTERN, DONE
    }// ButtonOkCommand

    /**
     * Delay time to reload the lock pattern view after a wrong pattern.
     */
    private static final long DELAY_TIME_TO_RELOAD_LOCK_PATTERN_VIEW = SECOND_IN_MILLIS;

    /////////
    // FIELDS
    /////////

    private int mMaxRetries, mMinWiredDots, mRetryCount = 0, mCaptchaWiredDots;
    private boolean mAutoSave, mStealthMode;
    private IEncrypter mEncrypter;
    private ButtonOkCommand mBtnOkCmd;
    private Intent mIntentResult;
    private LoadingView<Void, Void, Object> mLoadingView;

    ///////////
    // CONTROLS
    ///////////

    private TextView mTextInfo;
    private LockPatternView mLockPatternView;
    private View mFooter;
    private Button mBtnConfirm, mBtnCancel;
    private View mViewGroupProgressBar;
    private BroadcastReceiver mMessageReceiver;
    private ImageView imageView;
    private String logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        if (getIntent().hasExtra("statusbar")) {

            if (Build.VERSION.SDK_INT >= 21) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(Color.parseColor(getIntent().getStringExtra("statusbar")));
            }

        }

        if (getIntent().hasExtra("titlecolor")) {
            if (Build.VERSION.SDK_INT >= 19) {
                getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(getIntent().getStringExtra("titlecolor"))));
            }
            else {
                getWindow().setTitleColor(Color.parseColor(getIntent().getStringExtra("titlecolor")));
            }
        }

        if (getIntent().hasExtra("titletext")) {
            if ( Build.VERSION.SDK_INT >= 19) {
                getActionBar().setTitle(getIntent().getStringExtra("titletext"));
            }
            else {
                getWindow().setTitle(getIntent().getStringExtra("titletext"));
            }
        }

        if (getIntent().hasExtra("logo")) {
            logo = getIntent().getStringExtra("logo");
        }

        getWindow().getAttributes().windowAnimations = io.authme.sdk.R.style.Fade;
        if (DEBUG) Log.d(CLASSNAME, "onCreate()");

        /**
         * EXTRA_THEME
         */
        if (getIntent().hasExtra(EXTRA_THEME))
            setTheme(getIntent().getIntExtra(EXTRA_THEME, io.authme.sdk.R.style.Alp_42447968_Theme_Dark));

        /**
         * Apply theme resources
         */
        final int resThemeResources = ResourceUtils.resolveAttribute(this, io.authme.sdk.R.attr.alp_42447968_theme_resources);
        if (resThemeResources == 0)
            throw new RuntimeException(
                    "Please provide theme resource via attribute `alp_42447968_theme_resources`."
                            + " For example: <item name=\"alp_42447968_theme_resources\">@style/Alp_42447968" +
                            ".ThemeResources.Light</item>");
        getTheme().applyStyle(resThemeResources, true);

        super.onCreate(savedInstanceState);

        loadSettings();

        mIntentResult = new Intent();
        setResult(RESULT_CANCELED, mIntentResult);

        IntentFilter filter = new IntentFilter();
        filter.addAction("SWIPE_COMPLETE");

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
        registerReceiver(mMessageReceiver, filter);
        initContentView();
    }// onCreate()

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (DEBUG) Log.d(CLASSNAME, "onConfigurationChanged()");

        initContentView();
    }// onConfigurationChanged()

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        /**
         * Use this hook instead of onBackPressed(), because onBackPressed() is not available in API 4.
         */
        if (keyCode == KeyEvent.KEYCODE_BACK && ACTION_COMPARE_PATTERN.equals(getIntent().getAction())) {
            if (mLoadingView != null) mLoadingView.cancel(true);

            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, io.authme.sdk.R.style.LockScreenAlertBox));

            builder.setTitle("Confirm");
            builder.setMessage("Are you sure?");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finishWithNegativeResult(RESULT_CANCELED);
                }

            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }// if

        return super.onKeyDown(keyCode, event);
    }// onKeyDown()


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /**
         * Support canceling dialog on touching outside in APIs < 11.
         *
         * This piece of code is copied from android.view.Window. You can find it by searching for methods
         * shouldCloseOnTouch() and isOutOfBounds().
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB && event.getAction() == MotionEvent.ACTION_DOWN
                && getWindow().peekDecorView() != null) {
            final int x = (int) event.getX();
            final int y = (int) event.getY();
            final int slop = ViewConfiguration.get(this).getScaledWindowTouchSlop();
            final View decorView = getWindow().getDecorView();
            boolean isOutOfBounds = (x < -slop) || (y < -slop) || (x > (decorView.getWidth() + slop))
                    || (y > (decorView.getHeight() + slop));
            if (isOutOfBounds) {
                finishWithNegativeResult(RESULT_CANCELED);
                return true;
            }
        }// if

        return super.onTouchEvent(event);
    }// onTouchEvent()

    @Override
    protected void onDestroy() {
        if (mLoadingView != null) mLoadingView.cancel(true);

        super.onDestroy();
        unregisterReceiver(mMessageReceiver);
    }// onDestroy()

    /**
     * Loads settings, either from manifest or {@link AlpSettings}.
     */
    private void loadSettings() {
        Bundle metaData = null;
        try {
            metaData = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA).metaData;
        } catch (NameNotFoundException e) {
            /**
             * Never catch this.
             */
            e.printStackTrace();
        }

        if (metaData != null && metaData.containsKey(METADATA_MIN_WIRED_DOTS))
            mMinWiredDots = AlpSettings.Display.validateMinWiredDots(this, metaData.getInt(METADATA_MIN_WIRED_DOTS));
        else
            mMinWiredDots = AlpSettings.Display.getMinWiredDots(this);

        if (metaData != null && metaData.containsKey(METADATA_MAX_RETRIES))
            mMaxRetries = AlpSettings.Display.validateMaxRetries(this, metaData.getInt(METADATA_MAX_RETRIES));
        else
            mMaxRetries = AlpSettings.Display.getMaxRetries(this);

        if (metaData != null && metaData.containsKey(METADATA_AUTO_SAVE_PATTERN))
            mAutoSave = metaData.getBoolean(METADATA_AUTO_SAVE_PATTERN);
        else
            mAutoSave = AlpSettings.Security.isAutoSavePattern(this);

        if (metaData != null && metaData.containsKey(METADATA_CAPTCHA_WIRED_DOTS))
            mCaptchaWiredDots = AlpSettings.Display.validateCaptchaWiredDots(
                    this, metaData.getInt(METADATA_CAPTCHA_WIRED_DOTS));
        else
            mCaptchaWiredDots = AlpSettings.Display.getCaptchaWiredDots(this);

        if (metaData != null && metaData.containsKey(METADATA_STEALTH_MODE))
            mStealthMode = metaData.getBoolean(METADATA_STEALTH_MODE);
        else
            mStealthMode = AlpSettings.Display.isStealthMode(this);

        /**
         * Encrypter.
         */
        char[] encrypterClass;
        if (metaData != null && metaData.containsKey(METADATA_ENCRYPTER_CLASS))
            encrypterClass = metaData.getString(METADATA_ENCRYPTER_CLASS).toCharArray();
        else
            encrypterClass = AlpSettings.Security.getEncrypterClass(this);

        if (encrypterClass != null) {
            try {
                mEncrypter = (IEncrypter) Class.forName(new String(encrypterClass), false, getClassLoader())
                        .newInstance();
            } catch (Throwable t) {
                throw new InvalidEncrypterException();
            }
        }
    }// loadSettings()

    /**
     * Initializes UI...
     */
    private void initContentView() {
        /**
         * Save all controls' state to restore later.
         */
        CharSequence infoText = mTextInfo != null ? mTextInfo.getText() : null;
        Boolean btnOkEnabled = mBtnConfirm != null ? mBtnConfirm.isEnabled()
                : null;
        LockPatternView.DisplayMode lastDisplayMode = mLockPatternView != null ? mLockPatternView.getDisplayMode() :
                null;
        List<Cell> lastPattern = mLockPatternView != null ? mLockPatternView.getPattern() : null;

        setContentView(io.authme.sdk.R.layout.alp_42447968_lock_pattern_activity);
        UI.adjustDialogSizeForLargeScreens(getWindow());

        /**
         * MAP CONTROLS
         */

        imageView = (ImageView) findViewById(R.id.authlogo);

        if (!TextUtils.isEmpty(logo)) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(LockPatternActivity.this
                        .openFileInput(logo));
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        mTextInfo = (TextView) findViewById(io.authme.sdk.R.id.alp_42447968_textview_info);
        mLockPatternView = (LockPatternView) findViewById(io.authme.sdk.R.id.alp_42447968_view_lock_pattern);

        mFooter = findViewById(io.authme.sdk.R.id.alp_42447968_viewgroup_footer);
        mBtnCancel = (Button) findViewById(io.authme.sdk.R.id.alp_42447968_button_cancel);
        mBtnConfirm = (Button) findViewById(io.authme.sdk.R.id.alp_42447968_button_confirm);

        mViewGroupProgressBar = findViewById(io.authme.sdk.R.id.alp_42447968_view_group_progress_bar);

        /**
         * SETUP CONTROLS
         */

        mViewGroupProgressBar.setOnClickListener(mViewGroupProgressBarOnClickListener);

        /**
         * LOCK PATTERN VIEW
         */

        switch (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
            case Configuration.SCREENLAYOUT_SIZE_XLARGE: {
                final int size = getResources().getDimensionPixelSize(
                        io.authme.sdk.R.dimen.alp_42447968_lockpatternview_size);
                LayoutParams lp = mLockPatternView.getLayoutParams();
                lp.width = size;
                lp.height = size;
                mLockPatternView.setLayoutParams(lp);

                break;
            }// LARGE / XLARGE
        }

        // Haptic feedback
        boolean hapticFeedbackEnabled = false;
        try {
            /**
             * This call requires permission WRITE_SETTINGS. Since it's not necessary, we don't need to declare that
             * permission in manifest. Don't scare our users  :-D
             */
            hapticFeedbackEnabled = Settings.System.getInt(getContentResolver(),
                    Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) != 0;
        } catch (Throwable t) {
            // Ignore it
            t.printStackTrace();
        }
        mLockPatternView.setTactileFeedbackEnabled(hapticFeedbackEnabled);

        mLockPatternView.setInStealthMode(mStealthMode && !ACTION_VERIFY_CAPTCHA.equals(getIntent().getAction()));
        mLockPatternView.setOnPatternListener(mLockPatternViewListener);
        if (lastPattern != null && lastDisplayMode != null && !ACTION_VERIFY_CAPTCHA.equals(getIntent().getAction()))
            mLockPatternView.setPattern(lastDisplayMode, lastPattern);

        /**
         * COMMAND BUTTONS
         */

        if (ACTION_CREATE_PATTERN.equals(getIntent().getAction())) {
            mBtnCancel.setOnClickListener(mBtnCancelOnClickListener);
            mBtnConfirm.setOnClickListener(mBtnConfirmOnClickListener);

            mBtnCancel.setVisibility(View.VISIBLE);
            mFooter.setVisibility(View.VISIBLE);

            if (infoText != null)
                mTextInfo.setText(infoText);
            else
                mTextInfo.setText(io.authme.sdk.R.string.alp_42447968_msg_draw_an_unlock_pattern);

            /**
             * BUTTON OK
             */
            if (mBtnOkCmd == null) mBtnOkCmd = ButtonOkCommand.CONTINUE;
            switch (mBtnOkCmd) {
                case CONTINUE:
                    mBtnConfirm.setText(io.authme.sdk.R.string.alp_42447968_cmd_continue);
                    break;
                case DONE:
                    mBtnConfirm.setText(io.authme.sdk.R.string.alp_42447968_cmd_confirm);
                    break;
                default:
                    /**
                     * Do nothing.
                     */
                    break;
            }
            if (btnOkEnabled != null) mBtnConfirm.setEnabled(btnOkEnabled);
        }// ACTION_CREATE_PATTERN
        else if (ACTION_COMPARE_PATTERN.equals(getIntent().getAction())) {
            if (TextUtils.isEmpty(infoText))
                mTextInfo.setText(io.authme.sdk.R.string.alp_42447968_msg_draw_pattern_to_unlock);
            else
                mTextInfo.setText(infoText);
            if (getIntent().hasExtra(EXTRA_PENDING_INTENT_FORGOT_PATTERN)) {
                mBtnConfirm.setOnClickListener(mBtnConfirmOnClickListener);
                mBtnConfirm.setText(io.authme.sdk.R.string.alp_42447968_cmd_forgot_pattern);
                mBtnConfirm.setEnabled(true);
                mFooter.setVisibility(View.VISIBLE);
            }
        }// ACTION_COMPARE_PATTERN
        else if (ACTION_VERIFY_CAPTCHA.equals(getIntent().getAction())) {
            mTextInfo.setText(io.authme.sdk.R.string.alp_42447968_msg_redraw_pattern_to_confirm);

            /**
             * NOTE: EXTRA_PATTERN should hold a char[] array. In this case we use it as a temporary variable to hold
             * a list of Cell.
             */

            final ArrayList<Cell> pattern;
            if (getIntent().hasExtra(EXTRA_PATTERN))
                pattern = getIntent().getParcelableArrayListExtra(EXTRA_PATTERN);
            else
                getIntent().putParcelableArrayListExtra(EXTRA_PATTERN,
                        pattern = LockPatternUtils.genCaptchaPattern(mCaptchaWiredDots));

            mLockPatternView.setPattern(DisplayMode.Animate, pattern);
        }// ACTION_VERIFY_CAPTCHA
    }// initContentView()

    /**
     * Compares {@code pattern} to the given pattern ( {@link #ACTION_COMPARE_PATTERN}) or to the generated "CAPTCHA"
     * pattern ( {@link #ACTION_VERIFY_CAPTCHA}). Then finishes the activity if they match.
     *
     * @param pattern    the pattern to be compared.
     * @param recordJson
     */
    private void doComparePattern(final List<Cell> pattern, final JSONObject recordJson) {
        if (pattern == null) return;

        /**
         * Use a LoadingView because decrypting pattern might take time...
         */

        mLoadingView = new LoadingView<Void, Void, Object>(this, mViewGroupProgressBar) {

            @Override
            protected Object doInBackground(Void... params) {
                if (ACTION_COMPARE_PATTERN.equals(getIntent().getAction())) {
                    char[] currentPattern = getIntent().getCharArrayExtra(EXTRA_PATTERN);
                    if (currentPattern == null)
                        currentPattern = AlpSettings.Security.getPattern(LockPatternActivity.this);
                    if (currentPattern != null) {
                        if (mEncrypter != null)
                            return pattern.equals(mEncrypter.decrypt(LockPatternActivity.this, currentPattern));
                        else
                            return Arrays.equals(currentPattern, LockPatternUtils.patternToSha1(pattern).toCharArray());
                    }
                }// ACTION_COMPARE_PATTERN
                else if (ACTION_VERIFY_CAPTCHA.equals(getIntent().getAction())) {
                    return pattern.equals(getIntent().getParcelableArrayListExtra(EXTRA_PATTERN));
                }// ACTION_VERIFY_CAPTCHA

                return false;
            }// doInBackground()

            @Override
            protected void onPostExecute(Object result) {
                super.onPostExecute(result);

                if ((Boolean) result) finishWithResultOk(null, recordJson.toString());
                else {
                    mRetryCount++;
                    mIntentResult.putExtra(EXTRA_RETRY_COUNT, mRetryCount);

                    if (mRetryCount >= mMaxRetries)
                        finishWithNegativeResult(RESULT_FAILED);
                    else {
                        mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                        mTextInfo.setText(io.authme.sdk.R.string.alp_42447968_msg_try_again);
                        mLockPatternView.postDelayed(mLockPatternViewReloader, DELAY_TIME_TO_RELOAD_LOCK_PATTERN_VIEW);
                    }
                }
            }// onPostExecute()

        };

        mLoadingView.execute();
    }// doComparePattern()

    /**
     * Checks and creates the pattern.
     *
     * @param pattern    the current pattern of lock pattern view.
     * @param recordJson
     */
    private void doCheckAndCreatePattern(final List<Cell> pattern, final JSONObject recordJson) {
        if (pattern.size() < mMinWiredDots) {
            mLockPatternView.setDisplayMode(DisplayMode.Wrong);
            mTextInfo.setText(getResources().getQuantityString(
                    io.authme.sdk.R.plurals.alp_42447968_pmsg_connect_x_dots, mMinWiredDots,
                    mMinWiredDots));
            mLockPatternView.postDelayed(mLockPatternViewReloader, DELAY_TIME_TO_RELOAD_LOCK_PATTERN_VIEW);
            return;
        }// if

        if (getIntent().hasExtra(EXTRA_PATTERN)) {
            /**
             * Use a LoadingView because decrypting pattern might take time...
             */
            mLoadingView = new LoadingView<Void, Void, Object>(this, mViewGroupProgressBar) {

                @Override
                protected Object doInBackground(Void... params) {
                    if (mEncrypter != null)
                        return pattern.equals(mEncrypter.decrypt(LockPatternActivity.this, getIntent()
                                .getCharArrayExtra(EXTRA_PATTERN)));
                    else
                        return Arrays.equals(getIntent().getCharArrayExtra(EXTRA_PATTERN),
                                LockPatternUtils.patternToSha1(pattern).toCharArray());
                }// doInBackground()

                @Override
                protected void onPostExecute(Object result) {
                    super.onPostExecute(result);

                    if ((Boolean) result) {
                        mTextInfo.setText(io.authme.sdk.R.string.alp_42447968_msg_your_new_unlock_pattern);
                        mBtnConfirm.setEnabled(true);
                    } else {
                        mTextInfo.setText(io.authme.sdk.R.string.alp_42447968_msg_redraw_pattern_to_confirm);
                        mBtnConfirm.setEnabled(false);
                        mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                        mLockPatternView.postDelayed(mLockPatternViewReloader, DELAY_TIME_TO_RELOAD_LOCK_PATTERN_VIEW);
                    }
                }// onPostExecute()

            };

            mLoadingView.execute();
        } else {
            /**
             * Use a LoadingView because encrypting pattern might take time...
             */
            mLoadingView = new LoadingView<Void, Void, Object>(this, mViewGroupProgressBar) {

                @Override
                protected Object doInBackground(Void... params) {
                    return mEncrypter != null ? mEncrypter.encrypt(LockPatternActivity.this, pattern) :
                            LockPatternUtils.patternToSha1(pattern).toCharArray();
                }// onCancel()

                @Override
                protected void onPostExecute(Object result) {
                    super.onPostExecute(result);

                    getIntent().putExtra(EXTRA_PATTERN, (char[]) result);
                    getIntent().putExtra(PATTERN_JSON, recordJson.toString());
                    mTextInfo.setText(io.authme.sdk.R.string.alp_42447968_msg_pattern_recorded);
                    mBtnConfirm.setEnabled(true);
                }// onPostExecute()

            };

            mLoadingView.execute();
        }
    }// doCheckAndCreatePattern()

    /**
     * Finishes activity with {@link Activity#RESULT_OK}.
     *
     * @param pattern the pattern, if this is in mode creating pattern. In any cases, it can be set to {@code null}.
     */
    private void finishWithResultOk(char[] pattern, String jsonPattern) {
        if (ACTION_CREATE_PATTERN.equals(getIntent().getAction())) {
            mIntentResult.putExtra(EXTRA_PATTERN, pattern);
            mIntentResult.putExtra(PATTERN_JSON, jsonPattern);
        } else {
            /**
             * If the user was "logging in", minimum try count can not be zero.
             */
            mIntentResult.putExtra(EXTRA_RETRY_COUNT, mRetryCount + 1);
            mIntentResult.putExtra(PATTERN_JSON, jsonPattern);
        }

        setResult(RESULT_OK, mIntentResult);

        /**
         * ResultReceiver
         */
        ResultReceiver receiver = getIntent().getParcelableExtra(EXTRA_RESULT_RECEIVER);
        if (receiver != null) {
            Bundle bundle = new Bundle();
            if (ACTION_CREATE_PATTERN.equals(getIntent().getAction()))
                bundle.putCharArray(EXTRA_PATTERN, pattern);
            else {
                /**
                 * If the user was "logging in", minimum try count can not be zero.
                 */
                bundle.putInt(EXTRA_RETRY_COUNT, mRetryCount + 1);
            }
            receiver.send(RESULT_OK, bundle);
        }

        /**
         * PendingIntent
         */
        PendingIntent pi = getIntent().getParcelableExtra(EXTRA_PENDING_INTENT_OK);
        if (pi != null) {
            try {
                pi.send(this, RESULT_OK, mIntentResult);
            } catch (Throwable t) {
                Log.e(CLASSNAME, "Error sending PendingIntent: " + pi, t);
            }
        }//if

        finish();
    }// finishWithResultOk()

    /**
     * Finishes the activity with negative result ( {@link Activity#RESULT_CANCELED}, {@link #RESULT_FAILED} or {@link
     * #RESULT_FORGOT_PATTERN}).
     */
    private void finishWithNegativeResult(int resultCode) {
        if (ACTION_COMPARE_PATTERN.equals(getIntent().getAction()))
            mIntentResult.putExtra(EXTRA_RETRY_COUNT, mRetryCount);

        setResult(resultCode, mIntentResult);

        /**
         * ResultReceiver
         */
        ResultReceiver receiver = getIntent().getParcelableExtra(EXTRA_RESULT_RECEIVER);
        if (receiver != null) {
            Bundle resultBundle = null;
            if (ACTION_COMPARE_PATTERN.equals(getIntent().getAction())) {
                resultBundle = new Bundle();
                resultBundle.putInt(EXTRA_RETRY_COUNT, mRetryCount);
            }
            receiver.send(resultCode, resultBundle);
        }//if

        /**
         * PendingIntent
         */
        PendingIntent pi = getIntent().getParcelableExtra(EXTRA_PENDING_INTENT_CANCELLED);
        if (pi != null) {
            try {
                pi.send(this, resultCode, mIntentResult);
            } catch (Throwable t) {
                Log.e(CLASSNAME, "Error sending PendingIntent: " + pi, t);
            }
        }//if

        finish();
    }// finishWithNegativeResult()

    ////////////
    // LISTENERS
    ////////////

    /**
     * Pattern listener for LockPatternView.
     */
    private final LockPatternView.OnPatternListener mLockPatternViewListener = new LockPatternView.OnPatternListener() {

        @Override
        public void onPatternStart() {
            mLockPatternView.removeCallbacks(mLockPatternViewReloader);
            mLockPatternView.setDisplayMode(DisplayMode.Correct);

            if (ACTION_CREATE_PATTERN.equals(getIntent().getAction())) {
                mTextInfo.setText(io.authme.sdk.R.string.alp_42447968_msg_release_finger_when_done);
                mBtnConfirm.setEnabled(false);
                if (mBtnOkCmd == ButtonOkCommand.CONTINUE) getIntent().removeExtra(EXTRA_PATTERN);
            }// ACTION_CREATE_PATTERN
            else if (ACTION_COMPARE_PATTERN.equals(getIntent().getAction())) {
                mTextInfo.setText(io.authme.sdk.R.string.alp_42447968_msg_draw_pattern_to_unlock);
            }// ACTION_COMPARE_PATTERN
            else if (ACTION_VERIFY_CAPTCHA.equals(getIntent().getAction())) {
                mTextInfo.setText(io.authme.sdk.R.string.alp_42447968_msg_redraw_pattern_to_confirm);
            }// ACTION_VERIFY_CAPTCHA
        }// onPatternStart()

        @Override
        public void onPatternDetected(List<Cell> pattern, JSONObject recordJson) {

            if (ACTION_CREATE_PATTERN.equals(getIntent().getAction())) {
                doCheckAndCreatePattern(pattern, recordJson);
            }// ACTION_CREATE_PATTERN
            else if (ACTION_COMPARE_PATTERN.equals(getIntent().getAction())) {
                doComparePattern(pattern, recordJson);
            }// ACTION_COMPARE_PATTERN
            else if (ACTION_VERIFY_CAPTCHA.equals(getIntent().getAction())) {
                if (!DisplayMode.Animate.equals(mLockPatternView.getDisplayMode()))
                    doComparePattern(pattern, recordJson);
            }// ACTION_VERIFY_CAPTCHA
        }// onPatternDetected()

        @Override
        public void onPatternCleared() {
            mLockPatternView.removeCallbacks(mLockPatternViewReloader);

            if (ACTION_CREATE_PATTERN.equals(getIntent().getAction())) {
                mLockPatternView.setDisplayMode(DisplayMode.Correct);
                mBtnConfirm.setEnabled(false);
                if (mBtnOkCmd == ButtonOkCommand.CONTINUE) {
                    getIntent().removeExtra(EXTRA_PATTERN);
                    mTextInfo.setText(io.authme.sdk.R.string.alp_42447968_msg_draw_an_unlock_pattern);
                } else
                    mTextInfo.setText(io.authme.sdk.R.string.alp_42447968_msg_redraw_pattern_to_confirm);
            }// ACTION_CREATE_PATTERN
            else if (ACTION_COMPARE_PATTERN.equals(getIntent().getAction())) {
                mLockPatternView.setDisplayMode(DisplayMode.Correct);
                mTextInfo.setText(io.authme.sdk.R.string.alp_42447968_msg_draw_pattern_to_unlock);
            }// ACTION_COMPARE_PATTERN
            else if (ACTION_VERIFY_CAPTCHA.equals(getIntent().getAction())) {
                mTextInfo.setText(io.authme.sdk.R.string.alp_42447968_msg_redraw_pattern_to_confirm);
                List<Cell> pattern = getIntent().getParcelableArrayListExtra(EXTRA_PATTERN);
                mLockPatternView.setPattern(DisplayMode.Animate, pattern);
            }// ACTION_VERIFY_CAPTCHA
        }// onPatternCleared()

        @Override
        public void onPatternCellAdded(List<Cell> pattern) {
            // Nothing to do
        }// onPatternCellAdded()

    };// mLockPatternViewListener

    /**
     * Click listener for button Cancel.
     */
    private final View.OnClickListener mBtnCancelOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            finishWithNegativeResult(RESULT_CANCELED);
        }// onClick()

    };// mBtnCancelOnClickListener

    /**
     * Click listener for button Confirm.
     */
    private final View.OnClickListener mBtnConfirmOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (ACTION_CREATE_PATTERN.equals(getIntent().getAction())) {
                if (mBtnOkCmd == ButtonOkCommand.CONTINUE) {
                    mBtnOkCmd = ButtonOkCommand.DONE;
                    mLockPatternView.clearPattern();
                    mTextInfo.setText(io.authme.sdk.R.string.alp_42447968_msg_redraw_pattern_to_confirm);
                    mBtnConfirm.setText(io.authme.sdk.R.string.alp_42447968_cmd_confirm);
                    mBtnConfirm.setEnabled(false);
                } else {
                    final char[] pattern = getIntent().getCharArrayExtra(EXTRA_PATTERN);
                    if (mAutoSave)
                        AlpSettings.Security.setPattern(LockPatternActivity.this, pattern);
                    finishWithResultOk(pattern, "");
                }
            }// ACTION_CREATE_PATTERN
            else if (ACTION_COMPARE_PATTERN.equals(getIntent().getAction())) {
                /**
                 * We don't need to verify the extra. First, this button is only visible if there is this extra in
                 * the intent. Second, it is the responsibility of the caller to make sure the extra is good.
                 */
                PendingIntent pi = null;
                try {
                    pi = getIntent().getParcelableExtra(EXTRA_PENDING_INTENT_FORGOT_PATTERN);
                    if (pi != null) pi.send();
                } catch (Throwable t) {
                    Log.e(CLASSNAME, "Error sending pending intent: " + pi, t);
                }
                finishWithNegativeResult(RESULT_FORGOT_PATTERN);
            }// ACTION_COMPARE_PATTERN
        }// onClick()

    };// mBtnConfirmOnClickListener

    /**
     * This reloads the {@link #mLockPatternView} after a wrong pattern.
     */
    private final Runnable mLockPatternViewReloader = new Runnable() {

        @Override
        public void run() {
            mLockPatternView.clearPattern();
            mLockPatternViewListener.onPatternCleared();
        }// run()

    };// mLockPatternViewReloader

    /**
     * Click listener for view group progress bar.
     */
    private final View.OnClickListener mViewGroupProgressBarOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            /**
             * Do nothing. We just don't want the user to interact with controls behind this view.
             */
        }// onClick()

    };// mViewGroupProgressBarOnClickListener

}
