Sample demonstrates how to display date in format "time ago" using android [DateUtils](http://developer.android.com/reference/android/text/format/DateUtils.html)

```java
long currentTime = System.currentTimeMillis();

DateUtils.getRelativeTimeSpanString(currentTime - TimeUnit.MINUTES.toMillis(1)) // 1 minute ago
DateUtils.getRelativeTimeSpanString(currentTime - TimeUnit.MINUTES.toMillis(5)) // 5 minutes ago
DateUtils.getRelativeTimeSpanString(currentTime - TimeUnit.HOURS.toMillis(1))   // 1 hour ago
DateUtils.getRelativeTimeSpanString(currentTime - TimeUnit.HOURS.toMillis(12))  // 12 hours ago
DateUtils.getRelativeTimeSpanString(currentTime - TimeUnit.DAYS.toMillis(1))    // yesterday

```
