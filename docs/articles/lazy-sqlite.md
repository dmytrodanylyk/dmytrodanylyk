<table bordercolor="#FFFFFF">
   <tr>
    <td style=" background-color: #FFFFFF;">
<img padding=0,5,0,0 src="/images/image-1.gif">
</td>

    <td style=" background-color: #FFFFFF;">
<img padding=0,5,0,0 src="/images/image-2.gif" padding=40px> </td>
   </tr>
</table>

Data appears immediately / Data appears with delay

Lazy loading data from *SQLite* means parsing data from *Cursor* on the fly when you need it (on-demand). You may wonder why you need this? To answer this question let’s see some benchmarks and sample first.

### Task

In our *SQLite* database we have 10 000 news entities, which must be displayed in `RecyclerView`.

```java
new Thread(new Runnable() {
    @Override
    public void run() {
        List<News> userList = NewsDAO.getAll();
        updateAdapterUI(userList);
    }
}).start();
```

### What is wrong?

Loading 10 000 objects takes time, which means your user have to wait until all of them are loaded and this i bad for user experience. What we want - immediately display number of items which fits screen and load other when user scroll.

### Benchmark

Let’s see what actually takes our time. `NewsDAO.getAll()` method consist of two things:

- Query - returns *Cursor* ~1 millisecond
- Parsing data from *Cursor* to *News* object. ~ 999 millisecond

**Note:** query and parsing time depends on device performance, query complexity and number of items in database.

> It’s clear that query itself takes significantly less time than parsing data from cursor.

So why user must wait until all data is parsed, when he only need few latest items? - Because you are to lazy to implement lazy data loading :)

### What we can do?

There is a class - [CursorAdapter](http://developer.android.com/reference/android/widget/CursorAdapter.html) which you can use to implement lazy data loading, but I am sure you all are familiar with it, and ofc this article is not about it. The main problem with `CursorAdapter` is that it doesn’t cache data and it doesn’t work with `RecyclerView`.

Here is our plan. When query return `Cursor` object we will create and return `cursor.getCount()` empty *Proxy* objects. Those proxy object must have getters method to parse data from `Cursor` and cache it.

Let's start by defining simple data object class - `User`.

```java
public class User {

    private long id;
    private int age;
    private String name;
    private String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // other getter and setter
}
```

Base proxy class.

```java
abstract class CursorItemProxy {

    private Cursor mCursor;
    private int mIndex;

    public CursorItemProxy(@NonNull Cursor cursor, int index) {
        mCursor = cursor;
        mIndex = index;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public int getIndex() {
        return mIndex;
    }

}
```

This is where the magic happens. `UserProxy` class extends `CursorItemProxy` and has reference to `Cursor`, index and `User` object. If name inside `User` object is empty - we parse data from `Cursor` and cache it. Next time - cached name from `User` object is returned.

```java
public class UserProxy extends CursorItemProxy {

    private User mUser;

    public UserProxy(@NonNull Cursor cursor, int index) {
        super(cursor, index);
        mUser = new User();
    }

    public String getName() {
        if (mUser.getName() == null) {
            Cursor cursor = getCursor();
            cursor.moveToPosition(getIndex());
            int columnIndex = cursor.getColumnIndex("name");
            mUser.setName(cursor.getString(columnIndex));
        }

        return mUser.getName();
    }

    // other getter
}
```

Now it's time to add select method to `UserDAO` class. Note that `manageProxyCursor()` method creates `UserProxy` objects and passes `Cursor` and cursor position for later use.

```java
public class UserDAO {

    private Database mDatabase;
    private Context mContext;

    public UserDAO(Database database, Context context) {
        mDatabase = database;
        mContext = context;
    }

    public List<UserProxy> selectAllUserProxy() {
        Cursor cursor = mDatabase.rawQuery(mContext.getString(R.string.select_all_users), null);
        return manageProxyCursor(cursor);
    }

    protected List<UserProxy> manageProxyCursor(Cursor cursor) {
        List<UserProxy> dataList = new ArrayList<>();

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                dataList.add(new UserProxy(cursor, cursor.getPosition()));
                cursor.moveToNext();
            }
        }

        if(dataList.isEmpty()) {
            closeCursor(cursor);
        }

        return dataList;
    }

    public void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    // other methods
}
```

Finally you can load and display data. Please keep in mind the following:

- Make sure to close `Cursor` to avoid leaks. See `cleanUpDatabase()` method.
- Since query takes small amount of time it is made in UI thread.

```java
public class UserListActivity extends ListActivity {

    private ArrayAdapter<UserProxy> mAdapter;
    private UserDAO mUserDAO;

    public static void start(@NonNull Activity activity) {
        activity.startActivity(new Intent(activity, UserListActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Database database = DatabaseConnection.instance().open();
        mUserDAO = new UserDAO(database, getApplicationContext());

        List<UserProxy> userProxies = mUserDAO.selectAllUserProxy();
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userProxies);
        setListAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        cleanUpDatabase();
        super.onDestroy();
    }

    private void cleanUpDatabase() {
        if(mAdapter != null && mAdapter.getCount() != 0) {
            Cursor cursor = mAdapter.getItem(0).getCursor();
            mUserDAO.closeCursor(cursor);
        }

        DatabaseConnection.instance().close();
    }
}
```

### Update 3/10/2015

You may noticed that `selectAllUserProxy()` method generates `cursor.count()` empty `UserProxy` and `User` objects. If you have 10k rows in your database it means 20k empty objects will be generated.

To fix this issue we can create custom collection which will create objects only during first reference.

```java
public class LazyList<T> extends ArrayList<T> {

    private final Cursor mCursor;
    private final ItemFactory<T> mCreator;

    public LazyList(Cursor cursor, ItemFactory<T> creator) {
        mCursor = cursor;
        mCreator = creator;
    }

    @Override
    public T get(int index) {
        int size = super.size();
        if (index < size) {
            // find item in the collection
            T item = super.get(index);
            if (item == null) {
                item = mCreator.create(mCursor, index);
                set(index, item);
            }
            return item;
        } else {
            // we have to grow the collection
            for (int i = size; i < index; i++) {
                add(null);
            }
            // create last object, add and return
            T item = mCreator.create(mCursor, index);
            add(item);
            return item;
        }
    }

    @Override
    public int size() {
        return mCursor.getCount();
    }

    public void closeCursor() {
        mCursor.close();
    }

    public interface ItemFactory<T> {
        T create(Cursor cursor, int index);
    }

}

```

Now add another method to `UserDAO` class. Note `LazyList.ItemFactory.create(...)` method is used to define how your object is parsed from `Cursor`.

```java
public LazyList<User> selectAllLazy() {
	Cursor cursor = mDatabase.rawQuery(mContext.getString(R.string.select_all_users), null);
	return new LazyList<>(cursor, new LazyList.ItemFactory<User>() {
		@Override
		public User create(Cursor cursor, int index) {
			User user = new User();
			cursor.moveToPosition(index);
			int columnIndex = cursor.getColumnIndex("name");
			user.setName(cursor.getString(columnIndex));
			// TODO add parsing other data from cursor
			return user;
		}
	});
}
```

Now you can load and display data, similar to the method above.

```java
public class UserListActivity3 extends ListActivity {

    private ArrayAdapter<User> mAdapter;
    private LazyList<User> mUserLazyList;

    public static void start(@NonNull Activity activity) {
        activity.startActivity(new Intent(activity, UserListActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Database database = DatabaseConnection.instance().open();
        UserDAO mUserDAO = new UserDAO(database, getApplicationContext());

        mUserLazyList = mUserDAO.selectAllLazy();
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mUserLazyList);
        setListAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        cleanUpDatabase();
        super.onDestroy();
    }

    private void cleanUpDatabase() {
        if(mAdapter != null && mAdapter.getCount() != 0) {
            mUserLazyList.closeCursor();
        }

        DatabaseConnection.instance().close();
    }
}
```

Source code for this article is available on [GitHub](https://github.com/dmytrodanylyk/lazy-data-loading). Also you can download and check apk file [here](https://github.com/dmytrodanylyk/lazy-data-loading/releases/tag/1).
