Sample demonstrates how to display date in format "time ago"

```java
DateUtils.getRelativeTimeSpanString(currentTime - 60 * 1000) // 1 minute ago
DateUtils.getRelativeTimeSpanString(currentTime - 5 * 60 * 1000) // 5 minutes ago
DateUtils.getRelativeTimeSpanString(currentTime - 60 * 60 * 1000) // 1 hour ago
DateUtils.getRelativeTimeSpanString(currentTime - 12 * 60 * 60 * 1000) // 12 hours ago
DateUtils.getRelativeTimeSpanString(currentTime - 24 * 60 * 60 * 1000) // yesterday
```
