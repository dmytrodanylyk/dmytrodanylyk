```java
@NotNull
public String toJson() {
    Gson gson = new Gson();
    return gson.toJson(this);
}

public static $TYPE$ fromJson(@NotNull String json) {
    Gson gson = new Gson();
    return gson.fromJson(json, $TYPE$.class);
}

public static $TYPE$ fromJson(@NotNull JSONObject json) {
    return fromJson(json.toString());
}
```
