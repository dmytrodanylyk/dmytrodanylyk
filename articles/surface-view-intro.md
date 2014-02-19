![Quickstart][5]

### Surface View
- [Part 1 - Intro][6]
- [Part 2 - Playing video][7]
- [Part 3 - Video Cropping][8]

### Intro

**Before Android 4.0** we were forced to use [SurfaceView][1] to deal with camera, [GLSurfaceView][2] to display *OpenGL* rendering and [VideoView][3] to play video.Two last are direct child of [SurfaceView][4]. Thats fine until you find out that surface view creates a new window, placed behind your application’s window, to manage content.

**I am in trouble?** Only if you want to move, scale, transform, animate or use *SurfaceView* inside scrollable container such as item of *ListView* or *ViewPager* - *SurfaceView* shows it’s dark side. It will stretch, jump, fly away, squeeze, starts blinking and as a result you will get epileptic attack faster then you find out how to fix all those stuff.

**Problems**

- unpredictable behavior inside scrollable container
- unpredictable behavior with animations
- no way to crop content

#### Why Texture View is amazing?

Unlike *SurfaceView*, *TextureView* does not create a separate window but behaves as a regular view. This key difference allows a texture view to be moved, transformed, animated, etc.

*TextureView* is simple and powerful way to work with *Video*, *Camera* and *OpenGL*. Because it uses hardware accelerated 2D rendering - it is so fast and efficient. 

**Problems**

- TextureView is available only since API level 14

----------
Found a mistake or have a question? Create new [issue](https://github.com/dmytrodanylyk/dmytrodanylyk/issues).

  [1]: http://developer.android.com/reference/android/view/SurfaceView.html
  [2]: http://developer.android.com/reference/android/opengl/GLSurfaceView.html
  [3]: http://developer.android.com/reference/android/widget/VideoView.html
  [4]: http://developer.android.com/reference/android/view/SurfaceView.html
  [5]: https://raw.github.com/dmytrodanylyk/dmytrodanylyk/gh-pages/images/articles/surface-view-intro.png
  [6]: https://github.com/dmytrodanylyk/dmytrodanylyk/blob/gh-pages/articles/surface-view-intro.md
  [7]: https://github.com/dmytrodanylyk/dmytrodanylyk/blob/gh-pages/articles/surface-view-play-video.md
  [8]: https://github.com/dmytrodanylyk/dmytrodanylyk/blob/gh-pages/articles/surface-view-video-cropping.md
