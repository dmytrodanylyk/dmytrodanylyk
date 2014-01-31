Android helper class to start third part application with intent, like *Send Email*, *Send SMS*, open link in *Browser* or Perform *Call*.

**Source**

```java
public class IntentUtils {
 
        private static final String EXTRA_SMS = "sms_body";
        private static final String INTENT_TYPE_SMS = "vnd.android-dir/mms-sms";
        private static final String INTENT_TYPE_EMAIL = "message/rfc822";
        private static final String TEL = "tel:";
        private static final String CHOOSER_TITLE = "Select application";
 
        public static void openBrowser(Context context, Uri uri) {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(Intent.createChooser(intent, CHOOSER_TITLE));
 
        }
 
        public static void sendEmail(Context context, String email, String subject, String body) {
                sendEmail(context, new String[]{email}, subject, body);
        }
 
        public static void sendEmail(Context context, String[] emails, String subject, String body) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType(INTENT_TYPE_EMAIL);
                intent.putExtra(Intent.EXTRA_EMAIL, emails);
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, body);
 
                context.startActivity(Intent.createChooser(intent, CHOOSER_TITLE));
 
        }
 
        public static void sendSms(Context context, String theText) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setType(INTENT_TYPE_SMS);
                intent.putExtra(EXTRA_SMS, theText);
 
                context.startActivity(Intent.createChooser(intent, CHOOSER_TITLE));
 
        }
 
        public static void startDialPhone(Context context, String thePhone) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(TEL + Html.fromHtml(thePhone)));
 
                context.startActivity(Intent.createChooser(intent, CHOOSER_TITLE));
 
        }
}
```
