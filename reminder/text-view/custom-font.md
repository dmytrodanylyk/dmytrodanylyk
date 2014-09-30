Copy font to `main/assets/fonts` folder.

FontTextView.java

```java
public class FontTextView extends TextView {
 
    public FontTextView(Context context) {
        super(context);
    }
 
    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyAttributes(attrs);
    }
 
    public FontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyAttributes(attrs);
    }
 
    private void applyAttributes(AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.FontTextView);
        String font = array.getString(R.styleable.FontTextView_customFont);
        if (font != null) {
            setTypeface(font);
        }
        array.recycle();
    }
 
    public void setTypeface(final String theName) {
        this.setTypeface(TypefaceHolder.getTypeface(this.getContext(), theName));
    }
 
    public void setTypeface(final String theName, int theStyle) {
        this.setTypeface(TypefaceHolder.getTypeface(this.getContext(), theName), theStyle);
    }
 
    public void setTypeface(int theNameResId) {
        this.setTypeface(TypefaceHolder.getTypeface(this.getContext(), theNameResId));
    }
 
    public void setTypeface(int theNameResId, int theStyle) {
        this.setTypeface(TypefaceHolder.getTypeface(this.getContext(), theNameResId), theStyle);
    }
}
```

TypefaceHolder.java

```java
public class TypefaceHolder {

    private final static Map<String, Typeface> sFontsMap = new HashMap<String, Typeface>();

    static String getFontPath(String fontName) {
        return "fonts/" + fontName;
    }

    public static Typeface getTypeface(Context context, String fontName) {
        Typeface typeface;
        typeface = sFontsMap.get(fontName);
        if (typeface == null) {
            typeface = Typeface.createFromAsset(context.getAssets(), getFontPath(fontName));
            sFontsMap.put(fontName, typeface);
        }

        return typeface;
    }

    public static Typeface getTypeface(Context context, int fontNameId) {
        return getTypeface(context, context.getResources().getString(fontNameId));
    }
}
```

res/values/attrs.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <declare-styleable name="FontTextView">
        <attr name="customFont" format="string" />
    </declare-styleable>
</resources>
```

res/values/fonts.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="Roboto_Bold">Roboto-Bold.ttf</string>
    <string name="Roboto_Thin">Roboto-Thin.ttf</string>
    <string name="Roboto_Light">Roboto-Light.ttf</string>
    <string name="Roboto_Regular">Roboto-Regular.ttf</string>
</resources>
```

usage.xml

```xml
<com.dd.ui.views.FontTextView
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="@string/Sign_up_via"
    app:customFont="@string/Roboto_Light" />
```
