### ANDROID HTTP CLIENT & IMAGE LOADER - VOLLEY

#### Why Volley?
- Simple
- Powerful
- Extendable
- Built-in memory cache
- Built-in disk cache

#### How to use it?

Step 1 - Create request queue
```java
RequestQueue requestQueue = Volley.newRequestQueue(context.getApplicationContext());
```
Step 2 - Create request
```java    
StringRequest request = new StringRequest(
            Request.Method.GET,
            url,
            listener,
            errorListener);
```
Step 3 - Create listeners
```java  
Response.Listener<String> listener = new Response.Listener<String>() {
    @Override
    public void onResponse(String response) {
        L.d("Success Response: " + response.toString());
    }
};

Response.ErrorListener errorListener = new Response.ErrorListener() {
    @Override
    public void onErrorResponse(VolleyError error) {
        if (error.networkResponse != null) {
            L.d("Error Response code: " +  error.networkResponse.statusCode);
        }
    }
};
```

Step 4 - Add request to queue
```java  
requestQueue.add(request);
```

#### Request methods

- Request.Method.GET
- Request.Method.POST
- Request.Method.PUT
- Request.Method.DELETE

#### Request types

![Volley request diagram][1]

Every request listener returns appropriate type.

- String 
- Json Object
- Json Array 
- Bitmap

#### You can create your own type

Example of request which add some cookie.

```java  
public class CookieRequest extends StringRequest {

    private String mCookieValue;
    
        public CookieRequest(String url, String cookieValue,
                Response.Listener<String> listener,
                Response.ErrorListener errorListener) {
            super(Method.GET, url, listener, errorListener);
            mCookieValue = cookieValue;
        }
    
        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> map = new HashMap<String, String>();
            map.put("Cookie", mCookieValue);
            return map;
        }
}
```

#### How to pass post request parameters?

You need to override `getParams()` method. 

```java 
StringRequest request = new StringRequest(
        Request.Method.POST,
        url,
        listener,
        errorListener) {
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {

        Map<String, String> map = new HashMap<String, String>();
        map.put("name", "Jon Doe");
        map.put("age", "21");

        return map;
    }
};
```

#### How to set request retry policy?
```java 
StringRequest request = new StringRequest(
        Request.Method.GET,
        url,
        listener,
        errorListener);
        
request.setRetryPolicy(
    new DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, // 2500
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES, // 1
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)); //1f
```

#### HTTP basic authorization
```java 
StringRequest request = new StringRequest(
        Request.Method.GET,
        url,
        listener,
        errorListener) {

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return createBasicAuthHeader("user", "passwd");
    }
};
```
```java 
Map<String, String> createBasicAuthHeader(String username, String password) {
    Map<String, String> headerMap = new HashMap<String, String>();

    String credentials = username + ":" + password;
    String base64EncodedCredentials =
            Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
    headerMap.put("Authorization", "Basic " + base64EncodedCredentials);

    return headerMap;
}
```

#### How to cancel request?
```java 
StringRequest request1 = new StringRequest(...);
request1.setTag("weather-screen"); // request tag

StringRequest request2 = new StringRequest(...);
request2.setTag("weather-screen"); // request tag

requestQueue.add(request1);
requestQueue.add(request2);
```
To cancel request you just need to remember **request tag** and call *cancelAll(...)* method.
```java 
requestQueue.cancelAll("weather-screen"); // cancel all requests with "weather-screen" tag 
```
#### Application Model

> Do I need to instantiate request queue in each Service or Activity? 

No

> Should I create singleton wrapper?

Yes

> Why ?

 - Request Queue creation is quite expensive 
 - You will be able to access and cancel all your requests 
 - You will be able to access and clear your cache


----------


### Application Model Example
Let's create two class.

**Request Proxy**

- wrapper for request queue
- holds list of all application requests

**Request Manager**

- singleton which hold *Request Proxy*

Put those two classes in the same package.

```java 
public class RequestProxy {

    private RequestQueue mRequestQueue;
    
    // package access constructor
    RequestProxy(Context context) {
    mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }
    public void login() {
        // login request
    }
    
    public void weather() {
        // weather request
    }
 }
```
```java 
public class RequestManager {

    private static RequestManager instance;
    private RequestProxy mRequestProxy;
    
    private RequestManager(Context context) {
        mRequestProxy = new RequestProxy(context);
    }
    public RequestProxy doRequest() {
        return mRequestProxy;
    }
    
    // This method should be called first to do singleton initialization
    public static synchronized RequestManager getInstance(Context context) {
        if (instance == null) {
            instance = new RequestManager(context);
        }
        return instance;
    }
    
    public static synchronized RequestManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(RequestManager.class.getSimpleName() +
                    " is not initialized, call getInstance(..) method first.");
        }
        return instance;
    }
}
```

**Usage**

Initialize `RequestManager` in application class
```java 
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // RequestManager initialization
        RequestManager.getInstance(getApplicationContext());
    }
}
```

Now you can start you request as following.
```java 
RequestManager.getInstance().doRequest().login(..);
RequestManager.getInstance().doRequest().weather(..);
```


----------


### Image Loader

> You need to load image?

We have view for this! 

```xml 
<com.android.volley.toolbox.NetworkImageView
        android:id="@+id/imgAvatar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:scaleType="centerCrop"/>
```

Just set up url and image loader.
```java 
NetworkImageView imgAvatar = (NetworkImageView) findViewById(R.id.imgAvatar);
imageView.setImageUrl(url, imageLoader);
imageView.setDefaultImageResId(..);
imageView.setErrorImageResId(..);
```

> Wait what?.. Image loader?

To create image loader we need two things.

- RequestQueue
- ImageCache

```java 
ImageLoader imageLoader = new ImageLoader(Volley.newRequestQueue(context), imageCache);
```

Memory Cache
```java 
public class BitmapLruCache
extends LruCache<String, Bitmap>
        implements ImageLoader.ImageCache {
        
    public BitmapLruCache() {
        this(getDefaultLruCacheSize());
    }

    public BitmapLruCache(int sizeInKiloBytes) {
        super(sizeInKiloBytes);
    }
    
    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight() / 1024;
    }
    
    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }
    
    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }

    public static int getDefaultLruCacheSize() {
        final int maxMemory =
                (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
    
        return cacheSize;
    }
}
```

Put it all together
```java 
ImageLoader.ImageCache imageCache = new BitmapLruCache();
ImageLoader imageLoader = new ImageLoader(Volley.newRequestQueue(context), imageCache);
```
```xml 
<com.android.volley.toolbox.NetworkImageView
    android:id="@+id/imgAvatar"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:scaleType="centerCrop"/>
```
```java
NetworkImageView imgAvatar = (NetworkImageView) findViewById(R.id.imgAvatar);
imageView.setImageUrl(url, imageLoader);
```

Code above works perfectly but caches images to RAM memory. When you look inside `Volley.newRequestQueue(context)`, you will notice it uses default cache directory which is not a good choice if we have a lot of images.
```java
Volley.newRequestQueue(context)

// inside newRequestQueue method
File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);

// getCacheDir() method description 
You should always have a reasonable maximum, such as 1 MB, for the amount of space you consume with cache files, and prune those files when exceeding that space.
```

We need to use external cache directory to cache images and for this we should create our own request queue.
```java
// Default maximum disk usage in bytes
private static final int DEFAULT_DISK_USAGE_BYTES = 25 * 1024 * 1024;

// Default cache folder name
private static final String DEFAULT_CACHE_DIR = "photos";

// Most code copied from "Volley.newRequestQueue(..)", we only changed cache directory
private static RequestQueue .newRequestQueue()(Context context) {
    // define cache folder
    File rootCache = context.getExternalCacheDir();
    if (rootCache == null) {
        L.w("Can't find External Cache Dir, "
                + "switching to application specific cache directory");
        rootCache = context.getCacheDir();
    }
    
    File cacheDir = new File(rootCache, DEFAULT_CACHE_DIR);
    cacheDir.mkdirs();
    
    HttpStack stack = new HurlStack();
    Network network = new BasicNetwork(stack);
    DiskBasedCache diskBasedCache = new DiskBasedCache(cacheDir, DEFAULT_DISK_USAGE_BYTES);
    RequestQueue queue = new RequestQueue(diskBasedCache, network);
    queue.start();
    
    return queue;
}
```

Replace `Volley.newRequestQueue(context)` with our own method and now you have fully working Image loader with configurable memory and disk cache.
```java 
ImageLoader.ImageCache imageCache = new BitmapLruCache();
ImageLoader imageLoader = new ImageLoader(newRequestQueue(context), imageCache);
```
```xml 
<com.android.volley.toolbox.NetworkImageView
    android:id="@+id/imgAvatar"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:scaleType="centerCrop"/>
```
```java
NetworkImageView imgAvatar = (NetworkImageView) findViewById(R.id.imgAvatar);
imageView.setImageUrl(url, imageLoader);
```

#### I don't want to use network image view, I want bitmap!

Don't panic! Use image request.
```java
new ImageRequest(
        url,
        listener,
        maxWidth,
        maxHeight,
        decodeConfig,
        errorListener);
        
Response.Listener<Bitmap> listener = new Response.Listener<Bitmap>() {
    @Override
    public void onResponse(Bitmap bitmap) {
        // use your bitmap
    }
};
```

**Note:** Volley decides whether to cache response or not based only on headers *"Cache-Control"* and *"Expires"*

  [1]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/volley-diagram.png
