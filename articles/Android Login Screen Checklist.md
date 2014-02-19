![enter image description here][16]

## Android Login Screen Checklist

During my career I wrote a lot of login screen's, but every time forgot at least one more or less important thing. So I decided to write a small article which describe all issues you can face building a new Login Screen.

----------

Quick list of issues:

1. [Network] (http://goo.gl/OLvC7X)
2. [Data validation](http://goo.gl/e5HfXn)
    - [Lazy](http://goo.gl/g5wr9d)
    - [Runtime](http://goo.gl/y3EXVR)
3. [Edit Text attributes](http://goo.gl/LpNa4S)
4. [Handle keyboard done button](http://goo.gl/3Wx3LN)
5. [Loading indicator](http://goo.gl/v3vOYa)
    - [Dialog](http://goo.gl/7EcaYf)
        - Handle state
        - Handle cancellation
6. [Encrypt credentials](http://goo.gl/PU12sS)
7. [Login Screen is not always MAIN](http://goo.gl/4Y33MK)

### Network
Every time user press *Login* button first you need to check if Network is available. 

```java
private void onLoginClicked() {
    if(!isNetworkOn(getBaseContext())) {
        Toast.makeText(getBaseContext(), 
                "No network connection", Toast.LENGTH_SHORT).show();
    } else {
        // do login
    }
}

public boolean isNetworkOn(@NotNull Context context) {
    ConnectivityManager connMgr =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

    return (networkInfo != null && networkInfo.isConnected());
}
```

### Data validation
Before making sign in request to server, do simple validation of *login* and *password*. For example you can check if those values are not empty or if *login* - is user *email*, you can check if it match [email pattern][1]. Here two possible solutions available. 

#### Lazy

Occurs only when user click on *sign in* / *login* button.

```java
private void onLoginClicked() {
    if(!isDataValid()) {
        Toast.makeText(getBaseContext(),
                "Login or password is incorrect", Toast.LENGTH_SHORT).show();
    } else {
        // do login request
    }
}

public boolean isDataValid() {
    boolean isEmailValid = Patterns.EMAIL_ADDRESS.matcher(getEmail()).matches();
    boolean isPasswordValid = !getPassword().isEmpty();
    return isEmailValid && isPasswordValid;
}

public String getPassword() {
    return mEditPassword.getText().toString().trim(); // mEditPassword - EditText
}

public String getEmail() {
    return mEditEmail.getText().toString().trim(); // mEditEmail - EditText
}
```

#### Runtime

Occurs whenever text inside login and password input fields changed. Login button stays disabled until input data is valid.

```java
private EditText mEditEmail;
private EditText mEditPassword;
private Button mBtnLogin;
private boolean isEmailValid;
private boolean isPasswordValid;

private void initView() {
    mEditEmail = (EditText) findViewById(R.id.editEmail);
    mEditPassword = (EditText) findViewById(R.id.editPassword);

    mEditEmail.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            validateEmail(s.toString());
            updateLoginButtonState();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    });

    mEditPassword.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            validatePassword(s.toString());
            updateLoginButtonState();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    });

    mBtnLogin = (Button) findViewById(R.id.btnLogin);
    mBtnLogin.setEnabled(false); // default state should be disabled
    mBtnLogin.setOnClickListener(this);
}

private void validatePassword(String text) {
    isPasswordValid = !text.isEmpty();
}

private void validateEmail(String text) {
    isEmailValid = Patterns.EMAIL_ADDRESS.matcher(text).matches();
}

private void updateLoginButtonState() {
    if(isEmailValid && isPasswordValid) {
        mBtnLogin.setEnabled(true);
    } else {
        mBtnLogin.setEnabled(false);
    }
}
```

### Edit Text attributes

[android:hint][2] - hint text to display when the text is empty.
[android:singleLine][3] - constrains the text to a single horizontally scrolling line instead of letting it wrap onto multiple lines.
[android:inputType][4] - the type of data being placed in a text field, used to help an input method decide how to let the user enter text. Use `textPassword` for password input field and `textEmailAddress` or `textNoSuggestions` for login input field.

Sample without `inputType` attributes.

![enter image description here][5] ![enter image description here][6]

Sample with `inputType="textEmailAddress"` and `inputType="textPassword"` attributes. 

![enter image description here][7] ![enter image description here][8]

**Notice**, here we have two additional buttons `@` and `.com` to make email typing for user a lot easier, after typing `@` symbol autocomplete shows three commonly used email domains: `gmail`, `hotmal`, `yahoo`. Also password field is now hidden and didn't suggest any autocompletion.

### Handle keyboard done button 

Default behavior when user press keyboard's `Done` button - is to close keyboard. Good practice is to handle this click and duplicate your login button click logic.

```java
mEditPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

        boolean isValidKey = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
        boolean isValidAction = actionId == EditorInfo.IME_ACTION_DONE;

        if (isValidKey || isValidAction) {
            // do login request
        }
        return false;
    }
});
```

### Loading indicator

Display loading dialog, when login request is performed, is commonly used in lot of applications, however in my opinion this is a bad practice. I would rather replace login button with cancel, and disable user input.

![enter image description here][13] ![enter image description here][14]

#### Dialog

Since [Activity.showDialog(..)][9] method is now deprecated, we are forced to use [fragment dialogs][10], which brings a lot of issues.

![enter image description here][13] ![enter image description here][15]

**Handle fragment state**

Whenever you try to open/close fragment dialog when activity is invisible you will got crash:

`java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState`

This may happen when user press login button, you show dialog, and then user press home button, so request is completed and dialog is dismissed when application is in background.

How to fix this issue?

- Instead of using `dialogFragment.dismiss()` method use `dialogFragment.dismissAllowingStateLoss()`
- Always close dialog in `onPause` method, and restore state in `onResume`

```java
private boolean isDialogVisible;

private void showLoadingDialog() {
    isDialogVisible = true;
    // show dialog
}

private void closeLoadingDialog() {
    isDialogVisible = false;
    // close dialog
}

@Override
protected void onPause() {
    super.onPause();

    closeLoadingDialog(); // prevent IllegalStateException
}

@Override
protected void onResume() {
    super.onResume();

    if(isDialogVisible) {
        showLoadingDialog();
    }
}
```

Great article available [here][11].

**Handle cancellation**

Whenever user press back button whether loading dialog is visible, you should close it and cancel request. Fragment dialog doesn't have dismiss listener, so you need to create your own.

```java
public class LoadingDialogFragment extends DialogFragment {

    public static LoadingDialogFragment newInstance() {
        return new LoadingDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.Loading));
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                triggerActivityListener();
            }
        });

        return dialog;
    }

    private void triggerActivityListener() {
        if(getActivity() instanceof OnDialogClosedListener) {
            OnDialogClosedListener listener = (OnDialogClosedListener) getActivity();
            listener.onDialogClosed();
        }
    }

    public interface OnDialogClosedListener {
        public void onDialogClosed();
    }

}
```

In your activity implement `OnDialogClosedListener` and cancel request.

```java
public class LoginActivity extends Activity implements
        LoadingDialogFragment.OnDialogClosedListener {
        
    @Override
    public void onDialogClosed() {
        // cancel request
    }    
}
```

### Encrypt credentials

Almost all applications require entering login and password only during first time. Next time user launch application auto-sign in is performed. Does this mean application save your credentials to preferences? - Not necessary.

If you server side made correctly, after success login request it return you *access token* or *cookie*, which you can use until expiration date. How you use it? Usually adding *access token* as a header to all further requests.

In case your *poor* server requires adding credentials to every request, you need to save them to preferences. It is not safe to save them in open form, since anyone with root level access to the device will be able to see them. 

There is a great article which describe [how to store credentials safely][12].

### Login Screen is not always MAIN
When you have a login screen in your application it doesn't mean it should be your *main launcher* screen.

```xml
<activity
    android:name=".LoginActivity">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

**Wrong**

- Launch *Login* activity
- Check if *access token* is valid
- If *access token* valid launch *Dashboard* activity

**Correct**

- Launch *Dashboard* activity
- Check if *access token* is valid
- If *access token* is not valid launch *Login* activity

If you think about it, user only sign in once and then you save *access token* or *user credentials* and do auto-sign in, until access token is expired. So you can check if *access token* is valid in your main screen, e.g. *Dashboard*, inside `onCreate` method before view is visible. 

This will prevent your application from unnecessary launching *Login* screen, and speed up application launching.

```java
public class DashboardActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if user is not signed in, finish current activity
        // and launch login screen
        if (!isUserSignedIn()) {
            finish();
            startLoginActivity();
            return;
        }
        setContentView(R.layout.ac_main);
        // do initialization
    }
    
    // retrieve access token from preferences
    public boolean isUserSignedIn() {
        return PreferencesManager.getInstance().getAccessToken() != null;
    }
    
    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
```

----------
Found a mistake or have a question? Post it [here](https://github.com/dmytrodanylyk/dmytrodanylyk/issues).

  [1]: http://developer.android.com/reference/android/util/Patterns.html#EMAIL_ADDRESS
  [2]: http://developer.android.com/reference/android/widget/TextView.html#attr_android:hint
  [3]: http://developer.android.com/reference/android/widget/TextView.html#attr_android:singleLine
  [4]: http://developer.android.com/reference/android/R.attr.html#inputType
  [5]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/login-checklist-1.png
  [6]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/login-checklist-2.png
  [7]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/login-checklist-3.png
  [8]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/login-checklist-4.png
  [9]: http://developer.android.com/reference/android/app/Activity.html#showDialog%28int%29
  [10]: http://developer.android.com/reference/android/app/DialogFragment.html
  [11]: http://www.androiddesignpatterns.com/2013/08/fragment-transaction-commit-state-loss.html
  [12]: http://android-developers.blogspot.com/2013/02/using-cryptography-to-store-credentials.html
  [13]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/login-checklist-5.png
  [14]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/login-checklist-6.png
  [15]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/login-checklist-7.png
  [16]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/checklist-login-screen.png
