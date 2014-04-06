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

| Note      | Method | Result  |
| :-------- | :--------| :-- |
| Key exist and has correct value  | `jsonObject.optString("title1")`       |  `"Test title"`   |
| Key exist but value is null      | `jsonObject.optString("title2", null)` |  `"null"`  |
| Key doesn't exist                | `jsonObject.optString("title4")`       | `""`  |
| Key doesn't exist                | `jsonObject.optString("title4", null)` | `null`  |

| Note      | Method | Result  |
| :-------- | :--------| :-- |
| Key exist and has correct value  | `jsonObject.optDouble("double_value1")`    |  `10.0`   |
| Key exist but value is null      | `jsonObject.optDouble("double_value2")`    |  `NaN`  |
| Key exist but value is null      | `jsonObject.optDouble("double_value2", -1)`| `-1.0`  |
| Key doesn't exist                | `jsonObject.optDouble("double_value3")`    | `NaN`  |
| Key doesn't exist                | `jsonObject.optDouble("double_value3", -1)`| `-1.0`  |

| Note      | Method | Result  |
| :-------- | :--------| :-- |
| Key exist and has correct value  | `jsonObject.optInt("int_value1")`    |  `10`   |
| Key exist but value is null      | `jsonObject.optInt("int_value2")`    |  `0`  |
| Key exist but value is null      | `jsonObject.optInt("int_value2", -1)`| `-1`  |
| Key doesn't exist                | `jsonObject.optInt("int_value3")`    | `0`  |
| Key doesn't exist                | `jsonObject.optInt("int_value3", -1)`| `-1`  |

**Сonclusion**

When using `optValue` method of `JSONObject` always set `fallback` parameters. For parsing `String` it is prefereable to use following methods, which ensure that you will never have `"null"` value or empty string `""`.

```java
String optString(JSONObject jsonObject, String key) {
    return jsonObject.isNull(key) ? null : jsonObject.optString(key, null);
}
```

| Note      | Method | Result  |
| :-------- | :--------| :-- |
| Key exist and has correct value  | `optString(jsonObject, "title1")` | `null`  |
| Key exist but value is null      | `optString(jsonObject, "title2")` | `null`  |
| Key doesn't exist                | `optString(jsonObject, "title3")` | `null`  |
