Sample demonstrates some notes about parsing JSON object.

**JSON sample**

```json
{
    "title1": "Test title",
    "title2": null,
    "double_value1": 10.0,
    "double_value2": null,
    "int_value1": 10,
    "int_value2": null
}
```

**Java parser sample**

```java
JSONObject jsonObject = new JSONObject(string);

// key exist and has correct value
jsonObject.optString("title1"); // "Test title"
// key exist but value is null
jsonObject.optString("title2", null); // "null"
// key doesn't exist
jsonObject.optString("title4"); // ""
// key doesn't exist
jsonObject.optString("title3", null); // null

// key exist and has correct value
jsonObject.optDouble("double_value1"); // 10.0
// key exist but value is null
jsonObject.optDouble("double_value2"); // NaN
// key exist but value is null
jsonObject.optDouble("double_value2", -1); // -1.0
// key doesn't exist
jsonObject.optDouble("double_value3"); // NaN
// key doesn't exist
jsonObject.optDouble("double_value3", -1); // -1.0

// key exist and has correct value
jsonObject.optInt("int_value1"); // 10
// key exist but value is null
jsonObject.optInt("int_value2"); // 0
// key exist but value is null
jsonObject.optInt("int_value2", -1); // -1
// key doesn't exist
jsonObject.optInt("int_value3"); // 0
// key doesn't exist
jsonObject.optInt("int_value3", -1); // -1
```

When using `optValue` method of `JSONObject` always set `fallback` parameters. For parsing `String` it is prefereable to use following methods, which ensure that you will never have `"null"` value or empty string `""`.

```java
String optString(JSONObject jsonObject, String key) {
    return jsonObject.isNull(key) ? null : jsonObject.optString(key, null);
}
```

**Java parser sample**

```java
// key exist and has correct value
jsonObject.optString("title1"); // "Test title"
// key exist but value is null
jsonObject.optString("title2", null); // null
// key doesn't exist
jsonObject.optString("title4"); // null
// key doesn't exist
jsonObject.optString("title3", null); // null
```
