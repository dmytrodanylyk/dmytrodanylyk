Android helper class to finish any activity by class name.

**Usage**

Finish all activities except `ActivityC`.

```java
Class[] activityArr = null;
Class[] excludeArr = {ActivityC.class};
ActivityManager.finishActivities(getBaseContext(), activityArr, excludeArr);
```

Finish `ActivityA` and `ActivityB`.

```java
Class[] activityArr = {ActivityA.class, ActivityB.class};
ActivityManager.finishActivities(getBaseContext(), activityArr);
```

Finish activity `ActivityB`, exclude from finish `ActivityA` and `ActivityC`

```java
Class[] activityArr = {ActivityB.class};
Class[] excludeArr = {ActivityA.class, ActivityC.class};
ActivityManager.finishActivities(getBaseContext(), activityArr, excludeArr);
```

**Why we need this?**

This is typically used when an application can be launched on to another task (such as from an `ACTION_VIEW` of a content type it understands).

![](/assets/images/reminders/finish-activity-diagram.png)

**How it works?**

Inside `ActivityManager` class we have `registerExitReceiver()` method which register broadcast receiver for current activity and listen when `ActivityManager.ACTION_FINISH` occurs. 

**Setup**

Copy `ActivityManager` class to your sources. Create `BaseActivity` class. Make other activities extend your `BaseActivity` class.

```java
public class BaseActivity extends Activity {

    private ActivityManager mActivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityManager = new ActivityManager(this);
        mActivityManager.registerExitReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityManager.unregisterReceiver();
    }
}

public class ActivityA extends BaseActivity { }

public class ActivityB extends BaseActivity { }

public class ActivityC extends BaseActivity { }

```

**Source**

```java
public class ActivityManager {

    public static final boolean LOGS_ON = true;

    private static final String ACTION_FINISH = "ACTION_FINISH";
    private static final String EXTRAS_ACTIVITIES_FINISH = "EXTRAS_ACTIVITIES_FINISH";
    private static final String EXTRAS_ACTIVITIES_EXCLUDE = "EXTRAS_ACTIVITIES_EXCLUDE";

    private Activity mActivity;

    public ActivityManager(Activity activity) {
        mActivity = activity;
    }

    private BroadcastReceiver mExitReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (!TextUtils.equals(intent.getAction(), ACTION_FINISH) || extras == null) {
                return;
            }

            boolean haveExcludeActivity = intent.hasExtra(EXTRAS_ACTIVITIES_EXCLUDE);
            boolean haveFinishActivity = intent.hasExtra(EXTRAS_ACTIVITIES_FINISH);

            if (!haveFinishActivity && !haveExcludeActivity) {
                return;
            }

            String[] excludeActivityArr;

            if (haveExcludeActivity) {
                excludeActivityArr = extras.getStringArray(EXTRAS_ACTIVITIES_EXCLUDE);

                if (excludeActivityArr != null) {
                    String name = mActivity.getClass().getName();
                    int index = Arrays.binarySearch(excludeActivityArr, name);
                    if (index >= 0) {
                        log("Excluding activity from finish: " + mActivity.getClass().getName());
                        return;
                    } else if(!haveFinishActivity) {
                        log("Finishing activity: " + mActivity.getClass().getName());
                        mActivity.finish();
                    }
                }
            }

            String[] finishActivityArr;

            if (haveFinishActivity) {
                finishActivityArr = extras.getStringArray(EXTRAS_ACTIVITIES_FINISH);

                if (finishActivityArr != null) {
                    String name = mActivity.getClass().getName();
                    int index = Arrays.binarySearch(finishActivityArr, name);
                    if (index >= 0) {
                        log("Finishing activity: " + mActivity.getClass().getName());
                        mActivity.finish();
                    }
                }
            }
        }
    };

    public void registerExitReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_FINISH);
        mActivity.registerReceiver(mExitReceive, filter);
    }

    public void unregisterReceiver() {
        mActivity.unregisterReceiver(mExitReceive);
    }

    public static void finishActivities(Context context, Class[] activityArr) {
        finishActivities(context, activityArr, null);
    }

    public static void finishActivities(Context context, Class[] activityArr, Class[] excludeArr) {
        Intent intent = new Intent(ACTION_FINISH);

        if (activityArr != null) {
            intent.putExtra(EXTRAS_ACTIVITIES_FINISH, toNameArr(activityArr));
        }

        if (excludeArr != null) {
            intent.putExtra(EXTRAS_ACTIVITIES_EXCLUDE, toNameArr(excludeArr));
        }
        context.sendBroadcast(intent);
    }

    private static String[] toNameArr(Class[] activityArr) {
        String[] activityNameArr = new String[activityArr.length];
        for (int i = 0; i < activityArr.length; i++) {
            Class value = activityArr[i];
            activityNameArr[i] = value.getName();
        }
        return activityNameArr;
    }

    private static void log(String message) {
        if (LOGS_ON) {
            Log.d(ActivityManager.class.getSimpleName(), message);
        }
    }
}
```
