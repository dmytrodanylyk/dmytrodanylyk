![Image Loader](/assets/images/articles/volley-part-3.png)

### Volley - Android HTTP client
- [Part 1 - Quickstart](/articles/volley-part-1.md)
- [Part 2 - Application Model](/articles/volley-part-2.md)
- [Part 3 - Image Loader](/articles/volley-part-3.md)
- [Part 4 - Common Questions](/articles/volley-part-4.md)

### Part 3 - Image Loader

> You need to load image?

We have view for this!

```xml
<com.android.volley.toolbox.NetworkImageView
        android:id="@+id/imageView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:scaleType="centerCrop"/>
```

Just set up url and image loader.
```java
NetworkImageView imageView = (NetworkImageView) findViewById(R.id.imageView);
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
    android:id="@+id/imageView"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:scaleType="centerCrop"/>
```
```java
NetworkImageView imageView = (NetworkImageView) findViewById(R.id.imageView);
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
private static RequestQueue newRequestQueue(Context context) {
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
    android:id="@+id/imageView"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:scaleType="centerCrop"/>
```
```java
NetworkImageView imageView = (NetworkImageView) findViewById(R.id.imageView);
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
