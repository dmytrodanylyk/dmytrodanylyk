#### Endless ListView

Idea is very simple, set `OnScrollListener` on `ListView`

```java
listView.setOnScrollListener(this);
```

Then inside `onScrollStateChanged` method, check if last item of `ListView` is visible, if so, load more data.

```java
@Override
public void onScrollStateChanged(AbsListView view, int scrollState) {
	if (scrollState == SCROLL_STATE_IDLE) {
		if (listView.getLastVisiblePosition() >= listView.getCount() - 1) {
			loadDate();
		}
	}
}
```
