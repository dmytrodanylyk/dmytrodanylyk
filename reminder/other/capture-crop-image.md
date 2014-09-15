```java
public static final String CAMERA_FACING = "android.intent.extras.CAMERA_FACING";
private static final int CODE_CAMERA_CAPTURE = 1000;
private static final int CODE_CROP = 2000;
private Uri mFileUri;

// TODO call onTakePictureClicked();

private void onTakePictureClicked() {
    mFileUri = createOutputMediaFileUri();
    if (getFileUri() == null) {
        Toast.makeText(this, "Failed to create photo directory", Toast.LENGTH_LONG).show();
    } else {
        launchCamera(mFileUri);
    }
}

private void launchCamera(Uri fileUri) {
    try {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        // if you want to launch front facing camera
        // intent.putExtra(CAMERA_FACING, Camera.CameraInfo.CAMERA_FACING_FRONT);
        startActivityForResult(intent, CODE_CAMERA_CAPTURE);
    } catch (ActivityNotFoundException e) {
        String errorMessage = "Your device doesn't support capturing images!";
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }
}

@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
        if (requestCode == CODE_CAMERA_CAPTURE) {
            Uri picUri = data.getData();
            performCrop(picUri);
        } else if (requestCode == CODE_CROP) {
            // get the returned data
            Bundle extras = data.getExtras();
            // get the cropped bitmap
            Bitmap bitmap = extras.getParcelable("data");
            // TODO update image view
        }
    } else {
        mFileUri = null;
    }
}

private void performCrop(Uri picUri) {
    try {
        // call the standard crop action intent (the user device may not support it)
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        // indicate image type and Uri
        cropIntent.setDataAndType(picUri, "image/*");
        // set crop properties
        // cropIntent.putExtra("crop", "true");
        // indicate aspect of desired crop
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        // indicate output X and Y
        cropIntent.putExtra("outputX", 256);
        cropIntent.putExtra("outputY", 256);
        // retrieve data on return
        cropIntent.putExtra("return-data", true);
        // cropIntent.putExtra("scale", true);
        // start the activity - we handle returning in onActivityResult
        startActivityForResult(cropIntent, CODE_CROP);
    } catch (ActivityNotFoundException e) {
        String errorMessage = "Whoops - your device doesn't support the crop action!";
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }
}

@Nullable
public Uri getFileUri() {
    return mFileUri;
}

@Nullable
private Uri createOutputMediaFileUri() {
    File outputMediaFile = createOutputMediaFile();
    if (outputMediaFile != null) {
        return Uri.fromFile(outputMediaFile);
    }

    return null;
}

@Nullable
private File createOutputMediaFile() {
    // To be safe, you should check that the SDCard is mounted
    // using Environment.getExternalStorageState() before doing this.

    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), "DIRECTORY_NAME");
    // This location works best if you want the created images to be shared
    // between applications and persist after your app has been uninstalled.

    // Create the storage directory if it does not exist
    if (!mediaStorageDir.exists()) {
        if (!mediaStorageDir.mkdirs()) {
            L.w("Failed to create photo directory");
            return null;
        }
    }

    // Create a media file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    File mediaFile =
            new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

    return mediaFile;
}
```
