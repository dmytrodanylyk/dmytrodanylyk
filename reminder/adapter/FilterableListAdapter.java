public class FilterableAdapter extends BaseAdapter {

        private static final class ViewHolder {
    	TextView textLabel;
	}

	private final LayoutInflater mInflater;
	private final List<String> mItemList;
	private final List<String> mFilteredList;

	public FilterableAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		mItemList = new ArrayList<String>();
		mFilteredList = new ArrayList<String>();
	}

	public void add(List<String> itemList) {
		if (itemList == null) {
			return;
		}
		mItemList.addAll(itemList);
		mFilteredList.addAll(itemList);
	}

	public void add(String item) {
		if (item == null) {
			return;
		}
		mItemList.add(item);
		mFilteredList.add(item);
	}

	public void clear() {
		mItemList.clear();
		mFilteredList.clear();
	}

	@Override
	public int getCount() {
		return mFilteredList.size();
	}

	@Override
	public String getItem(int position) {
		return mFilteredList.get(position);
	}

	@Override
	public long getItemId(int thePosition) {
		return thePosition;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
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

		final String item = getItem(position);
		holder.textLabel.setText(item);

		return resultView;
	}

	public void filter(CharSequence constraint) {
		if (constraint == null || mItemList.isEmpty()) {
			return;
		}

		ArrayList<String> resultList = new ArrayList<String>();
		for (String item : mItemList) {
			if (item.contains(constraint)) { // filter condition
				resultList.add(item);
			}
		}
		
		mFilteredList.clear();
		mFilteredList.addAll(resultList);
		notifyDataSetChanged();
	}
}
