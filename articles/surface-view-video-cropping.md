![Screenshot][10]

### Surface View
- [Part 1 - Intro][11]
- [Part 2 - Playing video][12]
- [Part 3 - Video Cropping][13]

### Video Cropping

In this tutorial we are going to create application which will do following:

- display video from assets folder using *TextureView*
- when user click on screen, *TextureView* should be resized and video should be cropped to match new view size

#### Final Results

![video crop 1][2] ![video crop 2][3]
![video crop 3][4] ![video crop 4][5]

#### Step 1 - Preparing

Create android project and target *Android version 4.0*. Make sure you have following lines in your *AndroidManifest.xml* file.
```xml
<uses-sdk
    android:minSdkVersion="14"
    android:targetSdkVersion="14"/>
```
#### Step 2 - XML

Copy video file [big_buck_bunny.mp4][6] to your *assets* folder.

In your *values* folder create *dimen.xml* file and add following lines.
```xml
<dimen name="texture_view_width">320dp</dimen>
<dimen name="texture_view_height">176dp</dimen>

<dimen name="text_size_big">22sp</dimen>
```

In your values folder create *string.xml* file and add following line.

```xml
<string name="Original_Video_Size">Original Video Size</string>
```

In your layout folder create *texture_video_crop.xml* file and add following lines:
```xml
<?xml version="1.0" encoding="utf-8"?>
<!--suppress AndroidLintContentDescription -->
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
             android:layout_width="match_parent"
             android:layout_height="match_parent"
             android:id="@+id/rootView">

    <TextView
            android:layout_width="@dimen/texture_view_width"
            android:layout_height="@dimen/texture_view_height"
            android:gravity="center"
            android:text="@string/Original_Video_Size"
            android:textSize="@dimen/text_size_big"
            android:background="@android:color/darker_gray"/>

    <TextureView
            android:id="@+id/textureView"
            android:layout_width="@dimen/texture_view_width"
            android:layout_height="@dimen/texture_view_height"/>
</FrameLayout>
```
**Note:** *TextView* is only indicator of our video original size.

#### Step 3 - Basic code

Create a new activity class and call it VideoCropActivity. Don’t forget to declare it inside AndroidManifest.xml file.

Imports
```java
import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import java.io.IOException;
```

Our start point will be code from [Playing video from assets tutorial][7], which will just load and play video from *assets* folder.

```java
public class VideoCropActivity extends Activity implements TextureView.SurfaceTextureListener {

    // Log tag
    private static final String TAG = VideoCropActivity.class.getName();

    // Asset video file name
    private static final String FILE_NAME = "big_buck_bunny.mp4";

    // MediaPlayer instance to control playback of video file.
    private MediaPlayer mMediaPlayer;
    private TextureView mTextureView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.texture_video_crop);

        initView();
    }

    private void initView() {
        mTextureView = (TextureView) findViewById(R.id.textureView);
        // SurfaceTexture is available only after the TextureView
        // is attached to a window and onAttachedToWindow() has been invoked.
        // We need to use SurfaceTextureListener to be notified when the SurfaceTexture
        // becomes available.
        mTextureView.setSurfaceTextureListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            // Make sure we stop video and release resources when activity is destroyed.
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        Surface surface = new Surface(surfaceTexture);

        try {
            AssetFileDescriptor afd = getAssets().openFd(FILE_NAME);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer
                    .setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mMediaPlayer.setSurface(surface);
            mMediaPlayer.setLooping(true);

            // don't forget to call MediaPlayer.prepareAsync() method when you use constructor for
            // creating MediaPlayer
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

To calculate video scale factor we need to know original video size. We can do that by using [MediaMetadataRetriever][8] class.

Declare following class variables:
```java
// Original video size, in our case 640px / 360px
private float mVideoWidth;
private float mVideoHeight;
```

Create method which will initialize them:
```java
private void calculateVideoSize() {
    try {
        AssetFileDescriptor afd = getAssets().openFd(FILE_NAME);
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(
                afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        String height = metaRetriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String width = metaRetriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        mVideoHeight = Float.parseFloat(height);
        mVideoWidth = Float.parseFloat(width);

    } catch (IOException e) {
        Log.d(TAG, e.getMessage());
    } catch (NumberFormatException e) {
        Log.d(TAG, e.getMessage());
    }
}
```

Now call this method inside your activity onCreate method:
```java
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.texture_video_crop);

    calculateVideoSize();
    initView();
}
```
#### Step 4 - View resizing

The next part of logic we are going to write is view resizing. When user touch the screen, view *width* and *height* should be updated to appropriate x and y values of touch event.

Inside *initView* method we need to set *touch listener* to our root view, and when *action up* occurs - call method which will update texture view size.
```java
private void initView() {
    mTextureView = (TextureView) findViewById(R.id.textureView);
    // SurfaceTexture is available only after the TextureView
    // is attached to a window and onAttachedToWindow() has been invoked.
    // We need to use SurfaceTextureListener to be notified when the SurfaceTexture
    // becomes available.
    mTextureView.setSurfaceTextureListener(this);

    FrameLayout rootView = (FrameLayout) findViewById(R.id.rootView);
    rootView.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_UP:
                    updateTextureViewSize((int) motionEvent.getX(), (int) motionEvent.getY());
                    break;
            }
            return true;
        }
    });
}

private void updateTextureViewSize(int viewWidth, int viewHeight) {
    mTextureView.setLayoutParams(new FrameLayout.LayoutParams(viewWidth, viewHeight));
}
```

Now you can launch application and check the results. As you can see view size is changed, however video itself is stretched. Next task is to fix this.

![video crop 1][9]

#### Step 5 - Video cropping
Now change *updateTextureViewSize* method. First we need to calculate *scaleX* and *scaleY* factor and set it to *Matrix* object using method *setScale(..)*. Next pass this matrix to *TextureView* by *setTransform(..)* method and you are done.
```java
private void updateTextureViewSize(int viewWidth, int viewHeight) {
    float scaleX = 1.0f;
    float scaleY = 1.0f;

    if (mVideoWidth > viewWidth && mVideoHeight > viewHeight) {
        scaleX = mVideoWidth / viewWidth;
        scaleY = mVideoHeight / viewHeight;
    } else if (mVideoWidth < viewWidth && mVideoHeight < viewHeight) {
        scaleY = viewWidth / mVideoWidth;
        scaleX = viewHeight / mVideoHeight;
    } else if (viewWidth > mVideoWidth) {
        scaleY = (viewWidth / mVideoWidth) / (viewHeight / mVideoHeight);
    } else if (viewHeight > mVideoHeight) {
        scaleX = (viewHeight / mVideoHeight) / (viewWidth / mVideoWidth);
    }

    // Calculate pivot points, in our case crop from center
    int pivotPointX = viewWidth / 2;
    int pivotPointY = viewHeight / 2;

    Matrix matrix = new Matrix();
    matrix.setScale(scaleX, scaleY, pivotPointX, pivotPointY);

    mTextureView.setTransform(matrix);
    mTextureView.setLayoutParams(new FrameLayout.LayoutParams(viewWidth, viewHeight));
}
```

#### Step 6 - Launch

When you launch application you should notice that now video is cropped and correctly displayed. Of course when width to height ratio is too big, video loose it quality as it is scaled to much, this is the same behaviour as in *ImageView.setScaleType(ImageVIew.ScaleType.CENTER_CROP)*;

  [1]: https://github.com/dmytrodanylyk/dmytrodanylyk/blob/gh-pages/videos/articles/big_buck_bunny.mp4
  [2]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/video-crop-1.png
  [3]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/video-crop-2.png
  [4]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/video-crop-3.png
  [5]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/video-crop-4.png
  [6]: https://github.com/dmytrodanylyk/dmytrodanylyk/blob/gh-pages/videos/articles/big_buck_bunny.mp4
  [7]: https://github.com/dmytrodanylyk/dmytrodanylyk/blob/gh-pages/articles/surface-view-play-video.md#playing-video-from-assets-folder
  [8]: http://developer.android.com/reference/android/media/MediaMetadataRetriever.html
  [9]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/video-crop-5.png
  [10]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/surface-view-video-cropping.png
  [11]: https://github.com/dmytrodanylyk/dmytrodanylyk/blob/gh-pages/articles/surface-view-intro.md
  [12]: https://github.com/dmytrodanylyk/dmytrodanylyk/blob/gh-pages/articles/surface-view-play-video.md
  [13]: https://github.com/dmytrodanylyk/dmytrodanylyk/blob/gh-pages/articles/surface-view-video-cropping.md
