Android helper class to check network connection.

*Note:* all methods are `public statis`.

````java
public final class NetworkUtils {
	
	public static boolean isOn(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 
		return (networkInfo != null && networkInfo.isConnected());
	}
 
	public static boolean isWIFIOn(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 
		return (networkInfo != null && networkInfo.isConnected());
	}
 
	public static boolean isMobileOn(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
 
		return (networkInfo != null && networkInfo.isConnected());
	}
}
````
