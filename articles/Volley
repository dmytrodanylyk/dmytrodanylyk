How to use it?

STEP 1 - Create request queue
```java
RequestQueue requestQueue =
Volley.newRequestQueue(context.getApplicationContext());
```
STEP 2 - Create request
```java    
StringRequest request = new StringRequest(
            Request.Method.GET,
            url,
            listener,
            errorListener);
```
STEP 3 - Create listeners
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

STEP 4 - Add request to queue
```java  
requestQueue.add(request);
```
