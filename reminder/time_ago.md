Sample demonstrates how to display date in format "time ago" using android [DateUtils](http://developer.android.com/reference/android/text/format/DateUtils.html)

```java
DateUtils.getRelativeTimeSpanString(currentTime - 60 * 1000) // 1 minute ago
DateUtils.getRelativeTimeSpanString(currentTime - 5 * 60 * 1000) // 5 minutes ago
DateUtils.getRelativeTimeSpanString(currentTime - 60 * 60 * 1000) // 1 hour ago
DateUtils.getRelativeTimeSpanString(currentTime - 12 * 60 * 60 * 1000) // 12 hours ago
DateUtils.getRelativeTimeSpanString(currentTime - 24 * 60 * 60 * 1000) // yesterday

long currentTime = System.currentTimeMillis();
DateUtils.getRelativeTimeSpanString(currentTime - TimeUnit.MINUTES.toMillis(1)) // 1 minute ago
DateUtils.getRelativeTimeSpanString(currentTime - TimeUnit.MINUTES.toMillis(5)) // 5 minutes ago
DateUtils.getRelativeTimeSpanString(currentTime - TimeUnit.HOURS.toMillis(1))   // 1 hour ago
DateUtils.getRelativeTimeSpanString(currentTime - TimeUnit.HOURS.toMillis(12))  // 12 hours ago
DateUtils.getRelativeTimeSpanString(currentTime - TimeUnit.DAYS.toMillis(1))    // yesterday

```
