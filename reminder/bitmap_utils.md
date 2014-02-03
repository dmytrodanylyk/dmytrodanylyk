```java
public final class BitmapUtils {

    public static final String TAG = BitmapUtils.class.getSimpleName();

    @Nullable
    public static Bitmap rotateBitmap(@Nullable Bitmap bitmap, @Nullable Uri fileUri) {
        if (bitmap == null || fileUri == null) {
            return null;
        }

        Matrix matrix = new Matrix();
        try {
            ExifInterface exif = new ExifInterface(fileUri.getPath());
            int rotation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(rotation);
            if (rotation != 0f) {
                matrix.preRotate(rotationInDegrees);
            }

        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }

        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    @Nullable
    public static Bitmap cropBitmap(@Nullable Bitmap bitmap, int reqWidth, int reqHeight) {
        if (bitmap == null) {
            return null;
        }

        return ThumbnailUtils.extractThumbnail(bitmap, reqWidth, reqHeight);
    }

    @Nullable
    public static Bitmap loadBitmap(@Nullable Context context, @Nullable Uri fileUri) {
        if (context == null || fileUri == null) {
            return null;
        }

        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = context.getContentResolver().openInputStream(fileUri);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (FileNotFoundException e) {
            Log.d(TAG, e.toString());
        } finally {
            close(is);
        }

        return bitmap;
    }

    @Nullable
    public static Bitmap createFromPath(@NotNull String path, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = null;

        if (reqWidth > 0 || reqHeight > 0) {
            // First decode with inJustDecodeBounds=true to check dimensions
            options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
        }

        return BitmapFactory.decodeFile(path, options);
    }

    @Nullable
    public static Bitmap createFromResources(@NotNull Resources res, int resId, int reqWidth,
            int reqHeight) {
        BitmapFactory.Options options = null;

        if (reqWidth > 0 || reqHeight > 0) {
            // First decode with inJustDecodeBounds=true to check dimensions
            options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(res, resId, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
        }

        return BitmapFactory.decodeResource(res, resId, options);
    }

    @Nullable
    public static Bitmap createFromUrl(@NotNull String url, int reqWidth, int reqHeight) {
        InputStream stream = getStream(url);

        BitmapFactory.Options options = null;

        if (reqWidth > 0 || reqHeight > 0) {
            // First decode with inJustDecodeBounds=true to check dimensions
            options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(stream, null, options);

            stream = getStream(url); // TODO copy stream

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
        }

        return BitmapFactory.decodeStream(stream, null, options);
    }

    public static void saveBitmap(@Nullable Uri fileUri, @Nullable Bitmap bitmap) {
        if (fileUri == null && bitmap == null) {
            return;
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fileUri.getPath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        } finally {
            close(out);
        }
    }

    private static int exifToDegrees(int exifOrientation) {
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
        }
        return 0;
    }

    private static void close(@Nullable Closeable is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                Log.d(TAG, e.toString());
            }
        }
    }

    private static int calculateInSampleSize(
            @NotNull BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private static InputStream getStream(@NotNull String url) {
        InputStream is = null;
        try {
            is = new URL(url).openConnection().getInputStream();
        } catch (MalformedURLException e) {
            Log.d(TAG, e.toString());
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }

        return is;
    }
}
```
