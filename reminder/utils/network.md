Android helper class to check network connection.

**Example**

````java
// check if any network is available
boolean isNetworkAvailable = NetworkUtils.isOn(getApplicationContext());

// only check if WiFi is available
boolean isNetworkAvailable = NetworkUtils.isWIFIOn(getApplicationContext());

// only check if mobile network is available
boolean isNetworkAvailable = NetworkUtils.isMobileOn(getApplicationContext());
```

**Source**

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
