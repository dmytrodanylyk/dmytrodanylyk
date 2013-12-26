### Volley - Android HTTP client

- [Part 1 - Quickstart][1]
- [Part 2 - Application Model][2]
- [Part 3 - Image Loader][3]
- [Part 4 - Common Questions][4]

### Part 4 - Common Questions

#### How to set request pool size?

Sometimes you don't need to run all your requests simultaneously or want to restrict maximum number of requests, for this you need to create your own `RequestQueue`.

Here is sample which demonstrates how to make all your requests execute in the same order, in which they were added to the queue. Since it uses single thread execution, next request will not be executed, until previous is finished.
```java 
    // copied from Volley.newRequestQueue(..); source code
    public static RequestQueue newRequestQueue(Context context, HttpStack stack) {
        File cacheDir = new File(context.getCacheDir(), "def_cahce_dir");
        
        String userAgent = "volley/0";
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            userAgent = packageName + "/" + info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
        }
        
        if (stack == null) {
            if (Build.VERSION.SDK_INT >= 9) {
                stack = new HurlStack();
            } else {
                // Prior to Gingerbread, HttpUrlConnection was unreliable.
                // See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
                stack = new HttpClientStack(AndroidHttpClient.newInstance(userAgent));
            }
        }
        
        // important part
        int threadPoolSize = 1; // number of network dispatcher threads to create 
        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network, threadPoolSize);
        queue.start();
        
        return queue;
    }
```

#### How to make event listeners trigger not in UI thread?

When you are executing new request, you pass two listeners: success and error. By default Volley trigger them in UI thread. In one hand, you can immediately show error dialog or update view without extra code of switching to UI thread. In other hand mostly you have to parse XML or JSON response which may take time, and doing this in UI thread is not a best practice.
```java 
    Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            // UI thread, need to parse response in separate thread :(
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // do parsing     
                }
            });
        }
    };
```   
    
If you want to fix this, and make all your request listeners trigger in non UI thread - we need to create `RequestQueue` and pass `Executor` to constructor of `ResponseDelivery` object, then pass this `ResponseDelivery` object as a 4th parameter for `RequestQueue` constructor
```java 
    // copied from Volley.newRequestQueue(..); source code
    public static RequestQueue newRequestQueue(Context context, HttpStack stack) {
        File cacheDir = new File(context.getCacheDir(), "def_cahce_dir");
        
        String userAgent = "volley/0";
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            userAgent = packageName + "/" + info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
        }
        
        if (stack == null) {
            if (Build.VERSION.SDK_INT >= 9) {
                stack = new HurlStack();
            } else {
                // Prior to Gingerbread, HttpUrlConnection was unreliable.
                // See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
                stack = new HttpClientStack(AndroidHttpClient.newInstance(userAgent));
            }
        }
        
        // important part
        int threadPoolSize = 10; // number of network dispatcher threads to create
        // pass Executor to constructor of ResponseDelivery object
        ResponseDelivery delivery = new ExecutorDelivery(Executors.newFixedThreadPool(threadPoolSize));
        // pass ResponseDelivery object as a 4th parameter for RequestQueue constructor
        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network, threadPoolSize, delivery);
        queue.start();
        
        return queue;
    }
```

Don't forget to switch to UI thread when parsing is done.
```java     
    Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            // not UI thread, do parsing
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // update view
                }
            });
        }
    };
```
  [1]: https://github.com/dmytrodanylyk/dmytrodanylyk/blob/gh-pages/articles/volley-part-1.md
  [2]: https://github.com/dmytrodanylyk/dmytrodanylyk/blob/gh-pages/articles/volley-part-2.md
  [3]: https://github.com/dmytrodanylyk/dmytrodanylyk/blob/gh-pages/articles/volley-part-3.md
  [4]: https://github.com/dmytrodanylyk/dmytrodanylyk/blob/gh-pages/articles/volley-part-4.md
