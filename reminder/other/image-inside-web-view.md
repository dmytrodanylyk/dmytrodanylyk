Android Image inside WebView for Pinch to Zoom support.

```java
public class ImagePreviewActivity extends Activity {

    public static final String EXTRAS_IMAGE_PATH = "EXTRAS_IMAGE_PATH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String imagePath = getIntent().getExtras().getString(EXTRAS_IMAGE_PATH);

        L.d("imagePath " + imagePath);

        WebView webView = new WebView(this);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        boolean isUrl = Patterns.WEB_URL.matcher(imagePath).matches();
        if (isUrl) {
            webView.loadUrl(imagePath);
        } else {
            if (imagePath.startsWith("/")) {
                imagePath = imagePath.replaceFirst("/", "");
            }
            String path = "file:///" + imagePath;
            String html = "<html>"
                    + "<head>"
                    + "<meta name=\"viewport\" content=\"width=device-width,height=device-height,target-densityDpi=device-dpi,minimum-scale=1\" />"
                    + "</head>"
                    + "<body>"
                    + "<img src=\"" + path + "\">"
                    + "</body>"
                    + "</html>";
            webView.loadDataWithBaseURL("", html, "text/html", "utf-8", "");
        }

        setContentView(webView);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
```
