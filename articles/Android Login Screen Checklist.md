## Android Login Screen Checklist

During my career I wrote a lot of login screen's, but every time forgot at least one more or less important thing. So I decided to write a small article which describe all issues you can face building a new Login Screen.

Quick list of issues:

- Network
- Data validation
    - Lazy
    - Runtime
- Edit Text attributes
- Handle keyboard done button
- Loading indicator
    - Dialog
        - Handle state
        - Handle cancellation
    - Progress bar
- Background intent issue
- Encrypt credentials
- Login Screen is not always MAIN

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

#### Dialog

Since [Activity.showDialog(..)][9] method is now deprecated, we are forced to use [fragment dialogs][10], which brings a lot of issues.

**Handle fragment state**

![enter image description here][11]

This issue could be fixed in two ways.

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

Great article available [here][12].

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
  [11]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/login-checklist-5.png
  [12]: http://www.androiddesignpatterns.com/2013/08/fragment-transaction-commit-state-loss.html
