Android Patterns to validate Email, Phone, Web Url

```java
Patterns.PHONE.matcher("+1-111-111-111").matches(); // true
Patterns.PHONE.matcher("+1_111_111_111").matches(); // false
 
Patterns.EMAIL_ADDRESS.matcher("example@gmail.com").matches(); // true
Patterns.EMAIL_ADDRESS.matcher("example@gmail_com").matches(); // false
 
Patterns.WEB_URL.matcher("www.google.com").matches(); // true
Patterns.WEB_URL.matcher("www.goo_gle.com").matches(); // false
````