![Application Model][4]

### Volley - Android HTTP client
- [Part 1 - Quickstart][1]
- [Part 2 - Application Model][2]
- [Part 3 - Image Loader][3]

### Part 2 - Application Model

> Do I need to instantiate request queue in each Service or Activity? 

No

> Should I create singleton wrapper?

Yes

> Why ?

 - Request Queue creation is quite expensive 
 - You will be able to access and cancel all your requests 
 - You will be able to access and clear your cache


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


  [1]: https://github.com/dmytrodanylyk/dmytrodanylyk/blob/gh-pages/articles/volley-part-1.md
  [2]: https://github.com/dmytrodanylyk/dmytrodanylyk/blob/gh-pages/articles/volley-part-2.md
  [3]: https://github.com/dmytrodanylyk/dmytrodanylyk/blob/gh-pages/articles/volley-part-3.md
  [4]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/volley-part-2.png
