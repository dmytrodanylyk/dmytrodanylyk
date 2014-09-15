```java
InputStream inputStream = ...;
RootElement root = new RootElement("root_element");
Element dict = root.getChild("child_element");

dict.setEndTextElementListener(new EndTextElementListener() {
	@Override
	public void end(String text) {
		// TODO parse text
	}
});

try {
	Xml.parse(inputStream, Xml.Encoding.UTF_8, root.getContentHandler());
} catch (IOException e) {
	L.d(e.toString());
} catch (SAXException e) {
	L.d(e.toString());
}
```
