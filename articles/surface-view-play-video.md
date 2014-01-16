![Screenshot][5]

### Surface View - Playing video

#### Playing video from assets folder

This tutorial describes how to use [TextureView][1] to load and play video from *assets* folder. For this you need video sample file which you can get [here][6].

#### Step 1 - Preparing

Create android project and target android version 4.0. Remember that *TextureView* is available since [API level 14][2]

Make sure you have following lines in your *AndroidManifest.xml* file.
```xml
<uses-sdk
    android:minSdkVersion="14"
    android:targetSdkVersion="14"/>
```

#### Step 2 - XML
Copy video file *big_buck_bunny.mp4* to your *assets* folder.

In your *values* folder create *dimen.xml* file and add following lines.
```xml
<dimen name="texture_view_width">320dp</dimen>
<dimen name="texture_view_height">176dp</dimen>
```

**Why 320dp and 176dp ?**  
> Video file *big_buck_bunny.mp4* has 320px width
> and 176px height. We are using the same values but in *dp* to stretch
> view and keep proportions. In other words video will look bigger.

In your *layout* folder create *texture_video_simple.xml* file and add following lines.
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
                android:layout_width="match_parent"
                android:layout_height="match_parent">

    <TextureView
            android:id="@+id/textureView"
            android:layout_width="@dimen/texture_view_width"
            android:layout_height="@dimen/texture_view_height"
            android:layout_centerInParent="true"/>
</RelativeLayout>
```

#### Step 3 - Code

Create a new activity class and call it *VideoAssetActivity*. Don’t forget to declare it inside *AndroidManifest.xml* file. Here comes the main part.

Imports
```java
import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.io.IOException;
```

Here how our activity structure will look like.

![UML diagram][3]

Code

```java
public class VideoAssetActivity extends Activity implements TextureView.SurfaceTextureListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.texture_video_simple);

        initView();
    }

    private void initView() { }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }
}
```
Now we need to fill it with logic. Declare three class member variables.
```java
// Log tag.
private static final String TAG = VideoAssetActivity.class.getName();

// Asset video file name.
private static final String FILE_NAME = "big_buck_bunny.mp4";

// MediaPlayer instance to control playback of video file.
private MediaPlayer mMediaPlayer;
```
Start point is *initView()* method. To display video we need to get - *SurfaceTexture* - which is available only after the *TextureView* is attached to a window and *onAttachedToWindow()* has been invoked. So we are using *SurfaceTextureListener* to be notified when the *SurfaceTexture* becomes available.
```java
private void initView() {
    TextureView textureView = (TextureView) findViewById(R.id.textureView);
    textureView.setSurfaceTextureListener(this);
}
```

When *SurfaceTexture* is available *onSurfaceTextureAvailable* method is automatically called.
```java
@Override
public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
    Surface surface = new Surface(surfaceTexture);

    try {
        AssetFileDescriptor afd = getAssets().openFd(FILE_NAME);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        mMediaPlayer.setSurface(surface);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.prepareAsync();

        // Play video when the media source is ready for playback.
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });

    } catch (IllegalArgumentException e) {
        Log.d(TAG, e.getMessage());
    } catch (SecurityException e) {
        Log.d(TAG, e.getMessage());
    } catch (IllegalStateException e) {
        Log.d(TAG, e.getMessage());
    } catch (IOException e) {
        Log.d(TAG, e.getMessage());
    }
}
```
Some explanation about what is happening here. We are using *AssetFileDescriptor* to open video file from assets folder. *MediaPlayer* object is used to control video file, so we are packaging things like surface and data source inside. We also set looping flag to true, to make video automatically restarts when it is over. The last part is to set *onPreparedListener* and call *MediaPlayer.prepareAsync()* method which fire *onPrepared* event when we can start video playback.

**Tip**
> Don't forget to call *MediaPlayer.prepareAsync()* method when you use constructor for creating *MediaPlayer* object and if you > use *MediaPlayer.create(...)* factory method *MediaPlayer.prepareAsync()* is called automatically.

#### Step 4 - Memory cleanup

Keep in mind that we are working with resources which requires a lot of memory. To avoid memory leaks make sure you stop video and release resources when activity is destroyed.
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    if (mMediaPlayer != null) {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }
}
```

#### Step 5 - Launch
Now you can launch application. Remember you need Emulator or device with **Android version 4.0 or higher**. Here is screenshot of how it should look like. Video is centered, starts playing automatically and loop. When activity is destroyed video is stopped and resources are released.

![Screenshot][4]


----------


#### Playing video from url

This tutorial shows how to use TextureView to load and play video from url.

**Important:** almost all code is the same as in **Playing video from assets tutorial**, so you need to complete it first and here I will describe which lines of code you need to modify.

#### Step 1 - Preparing

As we are going to play video from internet we need to add internet permission to *AndroidManifest.xml file*.

```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

#### Step 2 - XML
The same as in **Playing video from assets** tutorial except video coping part.

#### Step 3 - Code

The same as in **Playing video from assets** tutorial plus additional changes.

Our file url variable now should point to video url file.
```java
private static final String FILE_URL = "http://www.w3schools.com/html/mov_bbb.mp4";
```
Next change is inside *onSurfaceTextureAvailable* listener. Delete here two line which are responsible for playing asset video, and add one line which set data source to video url.
```java
@Override
public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
    Surface surface = new Surface(surfaceTexture);

    try {
        AssetFileDescriptor afd = getAssets().openFd(FILE_NAME);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(FILE_URL));
        mMediaPlayer.setSurface(surface);
        mMediaPlayer.setLooping(true);

        mMediaPlayer.prepareAsync();

        // Play video when the media source is ready for playback.
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });

    } catch (IllegalArgumentException e) {
        Log.d(TAG, e.getMessage());
    } catch (SecurityException e) {
        Log.d(TAG, e.getMessage());
    } catch (IllegalStateException e) {
        Log.d(TAG, e.getMessage());
    } catch (IOException e) {
        Log.d(TAG, e.getMessage());
    }
}
```

#### Step 4 - Memory cleanup

The same as in **Playing video from assets** tutorial.

#### Step 5 - Launch

The same as in Playing video from assets tutorial, but keep in mind you need internet connection. Also note we didn’t put any code which check network state. In real application you should first check if internet connection is available.



  [1]: http://developer.android.com/reference/android/view/TextureView.html
  [2]: http://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels
  [3]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/surface-view-uml-1.png
  [4]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/surface-view-screenshot-1.png
  [5]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/surface-view-play-video.png
  [6]: https://github.com/dmytrodanylyk/dmytrodanylyk/blob/gh-pages/videos/articles/big_buck_bunny.mp4
