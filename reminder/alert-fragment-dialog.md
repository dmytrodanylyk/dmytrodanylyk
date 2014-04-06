Android helper class which extends `DialogFragment` and add additional methods to create different dialogs.

**Example**

```java
public class SampleActivity extends Activity
        implements AlertFragmentDialog.AlertDialogListener {
    
    private static final int DIALOG_ID_SIGN_OUT = 1000;
    private static final int DIALOG_ID_QUIT = 2000;
    
    // "Yes / No" dialog sample, id is used in listener for identification
    private void showSignOutDialog() {
        String title = "Alert";
        String message = "Do you want to sign out?";
        AlertFragmentDialog.newInstance(title, message, AlertFragmentDialog.STATE_YES_NO, DIALOG_ID_SIGN_OUT)
                .showAllowStateLoss(getFragmentManager()); // prevents IllegalStateException crash
    }
    
    // "Ok / Cancel" dialog sample, id is used in listener for identification
    private void showQuitDialog() {
        String title = "Alert";
        String message = "Do you really wish to quit?";
        AlertFragmentDialog.newInstance(title, message, AlertFragmentDialog.STATE_OK_CANCEL, DIALOG_ID_QUIT)
                .showAllowStateLoss(getFragmentManager()); // prevents IllegalStateException crash
    }
    
    // "Ok" dialog sample, id is ignored
    private void showMessageDialog() {
        String title = "Alert";
        String message = "Your message was sent";
        AlertFragmentDialog.newInstance(title, message, AlertFragmentDialog.STATE_OK)
                .showAllowStateLoss(getFragmentManager()); // prevents IllegalStateException crash
    }
    
    @Override
    public void onPositiveButtonClicked(int dialogId) {
       switch (dialogId) {
            case DIALOG_ID_SIGN_OUT:
                // sign out dialog yes button was pressed
                break;
           case DIALOG_ID_QUIT:
                // quit dialog ok button was pressed
                break;
        } 
    }

    @Override
    public void onNegativeButtonClicked(int dialogId) {
        switch (dialogId) {
            case DIALOG_ID_SIGN_OUT:
                // sign out dialog no button was pressed
                break;
            case DIALOG_ID_QUIT:
                // quit dialog cancel button was pressed
                break;
        }
    }

}
```

**Source**

```java
public class AlertFragmentDialog extends DialogFragment {

    public static final int STATE_OK_CANCEL = 0;
    public static final int STATE_YES_NO = 1;
    public static final int STATE_OK = 2;
    public static final int DEFAULT_ID = -1;

    private static final String EXTRAS_TITLE = "EXTRAS_TITLE";
    private static final String EXTRAS_MESSAGE = "EXTRAS_MESSAGE";
    private static final String EXTRAS_STATE = "EXTRAS_STATE";
    private static final String EXTRAS_ID = "EXTRAS_ID";

    public static AlertFragmentDialog newInstance(String title, String message, int state, int id) {
        AlertFragmentDialog dialog = new AlertFragmentDialog();

        Bundle bundle = new Bundle();
        bundle.putString(EXTRAS_TITLE, title);
        bundle.putString(EXTRAS_MESSAGE, message);
        bundle.putInt(EXTRAS_STATE, state);
        bundle.putInt(EXTRAS_ID, id);

        dialog.setArguments(bundle);

        return dialog;
    }

    public static AlertFragmentDialog newInstance(String title, String message, int state) {
        return newInstance(title, message, state, DEFAULT_ID);
    }

    public void showAllowStateLoss(FragmentManager manager, String tag) {
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.add(this, tag);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void showAllowStateLoss(FragmentManager manager) {
        showAllowStateLoss(manager, null);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());

        String positiveText = null;
        String negativeText = null;

        int state = getArguments().getInt(EXTRAS_STATE, STATE_OK_CANCEL);
        switch (state) {
            case STATE_YES_NO:
                positiveText = getString(R.string.Yes);
                negativeText = getString(R.string.No);
                break;
            case STATE_OK_CANCEL:
                positiveText = getString(android.R.string.ok);
                negativeText = getString(android.R.string.cancel);
                break;
            case STATE_OK:
                positiveText = getString(android.R.string.ok);
                break;
        }

        dialog.setTitle(getArguments().getString(EXTRAS_TITLE));
        dialog.setMessage(getArguments().getString(EXTRAS_MESSAGE));
        setPositiveListener(dialog, positiveText);

        if (negativeText != null) {
            setNegativeListener(dialog, negativeText);
        }

        setCancelable(false);

        return dialog.create();
    }

    private void setPositiveListener(AlertDialog.Builder dialog, CharSequence positive) {
        final int id = getArguments().getInt(EXTRAS_ID, DEFAULT_ID);

        dialog.setPositiveButton(positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                triggerPositiveActivityListener(id);
            }
        });
    }

    private void setNegativeListener(AlertDialog.Builder dialog, CharSequence negative) {
        final int id = getArguments().getInt(EXTRAS_ID, DEFAULT_ID);

        dialog.setNegativeButton(negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                triggerNegativeActivityListener(id);
            }
        });
    }

    private void triggerPositiveActivityListener(int dialogId) {
        if (getActivity() instanceof AlertDialogListener) {
            Activity activity = getActivity();
            AlertDialogListener listener = (AlertDialogListener) activity;
            listener.onPositiveButtonClicked(dialogId);
        }
    }

    private void triggerNegativeActivityListener(int dialogId) {
        if (getActivity() instanceof AlertDialogListener) {
            Activity activity = getActivity();
            AlertDialogListener listener = (AlertDialogListener) activity;
            listener.onNegativeButtonClicked(dialogId);
        }
    }

    public interface AlertDialogListener {

        public void onPositiveButtonClicked(int dialogId);

        public void onNegativeButtonClicked(int dialogId);
    }
}
```
