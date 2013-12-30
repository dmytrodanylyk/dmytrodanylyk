I want to demonstrate Android *"Raw database model"*. The basic concept is simple - use only raw queries which are stored inside `res/values/queries.xml`.

### Base package classes
```java
    com.sample.db.model
```
Let first start with base classes which can be reused on different projects.

**DatabaseManager** - singletone, contains `DatabaseProxy`. 

Responsibility

- provide access to `DatabaseProxy`
- provide `openDatabase` and `closeDatabase` methods to deal with database concurrency issues
```java
    public class DatabaseManager {
        private AtomicInteger mOpenCounter = new AtomicInteger();
    
        private static DatabaseManager instance;
        private static SQLiteOpenHelper mDatabaseHelper;
        private static Context mContext;
    
        private DatabaseProxy mDatabaseProxy;
    
        public static synchronized void initializeInstance(SQLiteOpenHelper helper, Context context) {
            if (instance == null) {
                instance = new DatabaseManager();
                mDatabaseHelper = helper;
                mContext = context.getApplicationContext();
            }
        }
    
        public static synchronized DatabaseManager getInstance() {
            if (instance == null) {
                throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
                        " is not initialized, call initializeInstance(..) method first.");
            }
    
            return instance;
        }
    
        public DatabaseProxy openDatabase() {
            if (mOpenCounter.incrementAndGet() == 1) {
                mDatabaseProxy = new DatabaseProxy(mDatabaseHelper.getWritableDatabase(), mContext);
            }
            L.d("Database open counter: " + mOpenCounter.get());
            return mDatabaseProxy;
        }
    
        public void closeDatabase() {
            if (mOpenCounter.decrementAndGet() == 0) {
                mDatabaseProxy.close();
            }
            
            L.d("Database open counter: " + mOpenCounter.get());
        }
    }
```

**DatabaseProxy** - wrapper for `SQLiteDatabase` with package access constructor, to prevent creating object outside.

Responsibility

- provide methods for execution raw queries, e.g: `rawQuery`, `execSQL`
- contains package access `close` database method, to prevent closing database outside.
```java
public class DatabaseProxy {

    private SQLiteDatabase mDatabase;
    private Context mContext;

    DatabaseProxy(SQLiteDatabase database, Context context) {
        mDatabase = database;
        mContext = context;
    }

    void close() {
        mDatabase.close();
    }

    public void transactionSuccessful() {
        mDatabase.setTransactionSuccessful();
    }

    public void transactionBegin() {
        mDatabase.beginTransaction();
    }

    public void transactionEnd() {
        mDatabase.endTransaction();
    }

    public Cursor rawQuery(int sql) {
        return rawQuery(sql, null);
    }

    public Cursor rawQuery(int sql, String[] selectionArgs) {
        return mDatabase.rawQuery(mContext.getString(sql), selectionArgs);
    }

    public void execSQL(int sql) {
        mDatabase.execSQL(mContext.getString(sql));
    }

    public void execSQL(int sql, String[] bindArgs) {
        mDatabase.execSQL(mContext.getString(sql), bindArgs);
    }
}
```

**AbstractDAO** - not necessary abstract generic class. All DAO classes will extend it.

Responsibility

- provide simplified `openDatabase` and `closeDatabase`
- provide helper methods, like `manageCursor`, `closeCursor`

```java
public abstract class AbstractDAO<T> {

    protected DatabaseProxy openDatabase() {
        return DatabaseManager.getInstance().openDatabase();
    }

    protected void closeDatabase() {
        DatabaseManager.getInstance().closeDatabase();
    }

    protected void closeCursor(@Nullable Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    @NotNull
    protected List<T> manageCursor(Cursor cursor) {
        List<T> dataList = new ArrayList<T>();

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                T user = cursorToData(cursor);
                dataList.add(user);
                cursor.moveToNext();
            }
        }
        return dataList;
    }

    protected abstract T cursorToData(Cursor cursor);
}
```

### Concrete classes
```java
    com.sample.db.model.concrete
```

**User** - data object.
```java
public class User {

    private long mId;
    private int mAge;
    private String mName;

    public int getAge() {
        return mAge;
    }

    public void setAge(int age) {
        mAge = age;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }
}
```

First version of `UserDAO` and `queries.xml` will contain `create` and `delete` table queries.

**UserDAO** (incomplete) - data access object for User object.

Responsibility

- provide all database access related to `User` object, e.g: `insert`, `select`, `delete`

```java
public class UserDAO extends AbstractDAO<User> {

    interface Table {

        String COLUMN_ID = "id";
        String COLUMN_NAME = "name";
        String COLUMN_AGE = "age";
    }

    public static String getCreateTable(Context context) {
        return context.getString(R.string.create_table_user);
    }

    public static String getDropTable(Context context) {
        return context.getString(R.string.drop_table_users);
    }
    
    @Override
    protected User cursorToData(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(Table.COLUMN_ID);
        int nameIndex = cursor.getColumnIndex(Table.COLUMN_NAME);
        int ageIndex = cursor.getColumnIndex(Table.COLUMN_AGE);

        User user = new User();
        user.setId(cursor.getLong(idIndex));
        user.setAge(cursor.getInt(ageIndex));
        user.setName(cursor.getString(nameIndex));

        return user;
    }
}
```

**queries.xml** (incomplete) - contains all queries.

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <!--Language=SQLite-->
    <string name="drop_table_users">
        DROP TABLE IF EXISTS users;
    </string>
    
    <!--Language=SQLite-->
    <string name="create_table_user">
        CREATE TABLE IF NOT EXISTS users (
            id   INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT,
            age  INTEGER
        );
    </string>
</resources>
```

**DatabaseHelper** - helper class to manage database creation and version management.

```java
public class DatabaseHelper extends SQLiteOpenHelper {


    public static final String DATABASE_NAME = "sample_database";
    public static final int DATABASE_VERSION = 1;
    private Context mContext;

    public DatabaseHelper(@NotNull Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // create all tables
        sqLiteDatabase.execSQL(UserDAO.getCreateTable(mContext));
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            // drop all tables
            sqLiteDatabase.execSQL(UserDAO.getDropTable(mContext));
            //re-create all tables
            onCreate(sqLiteDatabase);
        }
    }
}
```

#### Put it all together

```java
// initialization, should only be done once, preferable inside application class
DatabaseManager.initializeInstance(new DatabaseHelper(getContext()), getContext());
```

Now we need to add queries to `UserDAO` and `queries.xml`

**Delete query**

queries.xml
```xml
<!--Language=SQLite-->
<string name="delete_all_users">
    DELETE FROM users;
</string>
```
UserDAO.class

```java
public void deleteAll() {
    DatabaseProxy databaseProxy = openDatabase();
    databaseProxy.execSQL(R.string.delete_all_users);
    closeDatabase();
}
```

Usage

```java
UserDAO dao = new UserDAO();
dao.deleteAll();
```

**Insert query**

queries.xml

```xml
<!--Language=SQLite-->
<string name="insert_user">
    INSERT INTO users (name, age) VALUES (?, ?);
</string>
```

UserDAO.class

```java
public void insert(List<User> userList) {
    DatabaseProxy databaseProxy = openDatabase();

    for (User user : userList) {
        String[] bindArgs = {
                user.getName(),
                String.valueOf(user.getAge())
        };
        databaseProxy.execSQL(R.string.insert_user, bindArgs);
    }

    closeDatabase();
}

public void insert(User user) {
    DatabaseProxy databaseProxy = openDatabase();
    String[] bindArgs = {
            user.getName(),
            String.valueOf(user.getAge())
    };
    databaseProxy.execSQL(R.string.insert_user, bindArgs);
    closeDatabase();
}
```

Usage

```java
// insert single user
User user = new User();
user.setAge(100);
user.setName("Jon Doe");

UserDAO dao = new UserDAO();
dao.insert(user);

// insert user list
UserDAO dao = new UserDAO();
dao.insert(generateDummyUserList(10));


private List<User> generateDummyUserList(int itemsCount) {
    List<User> userList = new ArrayList<User>();
    for (int i = 0; i < itemsCount; i++) {
        User user = new User();
        user.setAge(i);
        user.setName("Jon Doe");
        userList.add(user);
    }
    return userList;
}
```

**Update query**

queries.xml

```xml
<!--Language=SQLite-->
<string name="update_user_name_by_age">
    UPDATE users SET name = ?
    WHERE age = ?;
</string>
```

UserDAO.class

```java
public void updateNameByAge(String name, int age) {
    DatabaseProxy databaseProxy = openDatabase();
    String[] bindArgs = {
            name,
            String.valueOf(age)
    };
    databaseProxy.execSQL(R.string.update_user_name_by_age, bindArgs);
    closeDatabase();
}
```

Usage

```java
User user = new User();
user.setAge(18);
user.setName("Jon Doe");

UserDAO dao = new UserDAO();
dao.insert(user);

dao.updateNameByAge("Will Smith", 18);
```

**Select query**

queries.xml

```xml
<!--Language=SQLite-->
<string name="select_users_by_age">
    SELECT
        *
    FROM users
    WHERE age = ?;
</string>

<!--Language=SQLite-->
<string name="select_all_users">
    SELECT
        *
    FROM users;
</string>
```

UserDAO.class

```java
public List<User> selectByAge(int age) {
    DatabaseProxy databaseProxy = openDatabase();
    String[] selectionArgs = {
            String.valueOf(age)
    };
    Cursor cursor = databaseProxy.rawQuery(R.string.select_users_by_age, selectionArgs);

    List<User> dataList = manageCursor(cursor);

    closeCursor(cursor);
    closeDatabase();

    return dataList;
}

public List<User> selectAll() {
    DatabaseProxy databaseProxy = openDatabase();
    Cursor cursor = databaseProxy.rawQuery(R.string.select_all_users);

    List<User> dataList = manageCursor(cursor);

    closeCursor(cursor);
    closeDatabase();

    return dataList;
}
```

Usage

```java
// select all users
UserDAO dao = new UserDAO();
List<User> listFromDB = dao.selectAll();

// select all users where age=18
List<User> listFromDB = dao.selectByAge(18);
```
