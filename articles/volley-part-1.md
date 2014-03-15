![Quickstart][/images/articles/volley-part-1.png]

### Volley - Android HTTP client
- [Part 1 - Quickstart][/articles/volley-part-1.md]
- [Part 2 - Application Model][/articles/volley-part-2.md]
- [Part 3 - Image Loader][/articles/volley-part-3.md]
- [Part 4 - Common Questions][/articles/volley-part-4.md]

### Part 1 - Quickstart

### Were I can get it?

Download volley library and import it as a library project or make jar file.
```
git clone https://android.googlesource.com/platform/frameworks/volley
```
#### Why Volley?
- Simple
- Powerful
- Extendable
- Built-in memory cache
- Built-in disk cache

#### How to use it?

Step 1 - Create request queue
```java
RequestQueue requestQueue = Volley.newRequestQueue(context.getApplicationContext());
```
Step 2 - Create request
```java    
StringRequest request = new StringRequest(
            Request.Method.GET,
            url,
            listener,
            errorListener);
```
Step 3 - Create listeners
```java  
Response.Listener<String> listener = new Response.Listener<String>() {
    @Override
    public void onResponse(String response) {
        L.d("Success Response: " + response.toString());
    }
};

Response.ErrorListener errorListener = new Response.ErrorListener() {
    @Override
    public void onErrorResponse(VolleyError error) {
        if (error.networkResponse != null) {
            L.d("Error Response code: " +  error.networkResponse.statusCode);
        }
    }
};
```

Step 4 - Add request to queue
```java  
requestQueue.add(request);
```

#### Request methods

- Request.Method.GET
- Request.Method.POST
- Request.Method.PUT
- Request.Method.DELETE

#### Request types

![Volley request diagram][/images/articles/volley-diagram.png]

Every request listener returns appropriate type.

- String 
- Json Object
- Json Array 
- Bitmap

#### You can create your own type

Example of request which add some cookie.

```java  
public class CookieRequest extends StringRequest {

    private String mCookieValue;
    
        public CookieRequest(String url, String cookieValue,
                Response.Listener<String> listener,
                Response.ErrorListener errorListener) {
            super(Method.GET, url, listener, errorListener);
            mCookieValue = cookieValue;
        }
    
        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> map = new HashMap<String, String>();
            map.put("Cookie", mCookieValue);
            return map;
        }
}
```

#### How to pass post request parameters?

You need to override `getParams()` method. 

```java 
StringRequest request = new StringRequest(
        Request.Method.POST,
        url,
        listener,
        errorListener) {
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {

        Map<String, String> map = new HashMap<String, String>();
        map.put("name", "Jon Doe");
        map.put("age", "21");

        return map;
    }
};
```

#### How to set request retry policy?
```java 
StringRequest request = new StringRequest(
        Request.Method.GET,
        url,
        listener,
        errorListener);
        
request.setRetryPolicy(
    new DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, // 2500
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES, // 1
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)); //1f
```

#### HTTP basic authorization
```java 
StringRequest request = new StringRequest(
        Request.Method.GET,
        url,
        listener,
        errorListener) {

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return createBasicAuthHeader("user", "passwd");
    }
};
```
```java 
Map<String, String> createBasicAuthHeader(String username, String password) {
    Map<String, String> headerMap = new HashMap<String, String>();

    String credentials = username + ":" + password;
    String base64EncodedCredentials =
            Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
    headerMap.put("Authorization", "Basic " + base64EncodedCredentials);

    return headerMap;
}
```

#### How to cancel request?
```java 
StringRequest request1 = new StringRequest(...);
request1.setTag("weather-screen"); // request tag

StringRequest request2 = new StringRequest(...);
request2.setTag("weather-screen"); // request tag

requestQueue.add(request1);
requestQueue.add(request2);
```
To cancel request you just need to remember **request tag** and call *cancelAll(...)* method.
```java 
requestQueue.cancelAll("weather-screen"); // cancel all requests with "weather-screen" tag 
```

----------
Found a mistake or have a question? Create new [issue](https://github.com/dmytrodanylyk/dmytrodanylyk/issues).
