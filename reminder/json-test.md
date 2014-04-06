JSON sample

```json
{
    "title1": "Test title",
    "title2": null,
    "double_value1": 10.0,
    "int_value1": 10
}
```

Java parser sample

```java
JSONObject jsonObject = new JSONObject(string);
jsonObject.optString("title1", null); // "Test title"
jsonObject.optString("title2", null); // "null"
jsonObject.optString("title3", null); // null
jsonObject.optString("title4"); // ""

jsonObject.optDouble("double_value1", 0); // 10.0
jsonObject.optDouble("double_value2", 0); // 0.0
jsonObject.optDouble("double_value3"); // NaN

jsonObject.optInt("int_value1", 0); // 10
jsonObject.optInt("int_value2", 0); // 0
jsonObject.optInt("int_value3"); // 0
```
