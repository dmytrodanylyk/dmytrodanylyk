```java
public final class DeviceUtils {
	
	public static Display getDisplay(Context context) {
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		return display;
	}

	public static int getDisplayWidth(Context context) {
		Display display = getDisplay(context);
		Point size = new Point();
		display.getSize(size);

		int width = size.x;
		return width;
	}

	public static int getDisplayHeight(Context context) {
		Display display = getDisplay(context);
		Point size = new Point();
		display.getSize(size);

		int height = size.y;
		return height;
	}
	
	public static int getAvailableMemory(Context context) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		int availableMemory = activityManager.getMemoryClass();
		return availableMemory;
	}
	
	public static int getActionBarHeight(Context context) {
		int actionbarHeight = (int) context.getResources().getDimension(R.dimen.actionbar_height); // 48dp
		return actionbarHeight;
	}

	public static int getStatusBarHeight(Context context) {
		int statusBarHeightMDPI = 25;
		int statusBarHeight = (int) Math.ceil(statusBarHeightMDPI * context.getResources().getDisplayMetrics().density);
		return statusBarHeight;
	}
}
```
