public class ExpandableListViewAdapter extends BaseExpandableListAdapter {

    private static final class ViewHolder {
    	TextView textLabel;
	}

	private final List<ParentItem> mItemList;
	private final LayoutInflater mInflater;

	public ExpandableListViewAdapter(Context context, List<ParentItem> mItemList) {
		this.mInflater = LayoutInflater.from(context);
		this.mItemList = mItemList;
	}

	public void add(List<ParentItem> itemList) {
		if (itemList == null) {
			return;
		}
		itemList.addAll(itemList);
	}

	public void add(ParentItem item) {
		if (item == null) {
			return;
		}
		mItemList.add(item);
	}

	public void clear() {
		if (mItemList == null) {
			return;
		}
		mItemList.clear();
	}

	@Override
	public ChildItem getChild(int groupPosition, int childPosition) {

		return mItemList.get(groupPosition).getChildItemList().get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mItemList.get(groupPosition).getChildItemList().size();
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
	                         final ViewGroup parent) {
		View resultView = convertView;
		ViewHolder holder;


		if (resultView == null) {

			resultView = mInflater.inflate(android.R.layout.test_list_item, null); //TODO change layout id
			holder = new ViewHolder();
			holder.textLabel = (TextView) resultView.findViewById(android.R.id.title); //TODO change view id
			resultView.setTag(holder);
		} else {
			holder = (ViewHolder) resultView.getTag();
		}

		ChildItem item = getChild(groupPosition, childPosition);

		holder.textLabel.setText(item.toString());

		return resultView;
	}

	@Override
	public ParentItem getGroup(int groupPosition) {
		return mItemList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mItemList.size();
	}

	@Override
	public long getGroupId(final int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View theConvertView, ViewGroup parent) {
		View resultView = theConvertView;
		ViewHolder holder;

		if (resultView == null) {
			resultView = mInflater.inflate(android.R.layout.test_list_item, null); //TODO change layout id
			holder = new ViewHolder();
			holder.textLabel = (TextView) resultView.findViewById(android.R.id.title); //TODO change view id
			resultView.setTag(holder);
		} else {
			holder = (ViewHolder) resultView.getTag();
		}

		ParentItem item = getGroup(groupPosition);

		holder.textLabel.setText(item.toString());

		return resultView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
