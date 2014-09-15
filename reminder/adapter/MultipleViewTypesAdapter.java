public class MultipleViewTypesAdapter extends BaseAdapter {

    public interface ItemType {
		public static final int TYPE_TITLE = 0;
		public static final int TYPE_DESCRIPTION = 1;
		public int getType();
	}

	public class Title implements ItemType {
		// data
		public int getType() {
			return TYPE_TITLE;
		}
	}

	public class Description implements ItemType {
		// data
		public int getType() {
			return TYPE_DESCRIPTION;
		}
	}

	private final LayoutInflater mInflater;
	private final List<ItemType> mItemList;

	public ListViewAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		mItemList = new ArrayList<ItemType>();
	}

	public void add(List<ItemType> itemList){
		if(itemList == null){
			return;
		}
		mItemList.addAll(itemList);
	}

	public void add(ItemType item){
		if(item == null){
			return;
		}
		mItemList.add(item);
	}

	public void clear(){
		if(mItemList == null){
			return;
		}
		mItemList.clear();
	}

	@Override
	public int getItemViewType(int position) {
		return getItem(position).getType();
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getCount() {
		return mItemList.size();
	}

	@Override
	public ItemType getItem(int position) {
		return mItemList.get(position);
	}

	@Override
	public long getItemId(int thePosition) {
		return thePosition;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View resultView = convertView;
		if(getItemViewType(position) == ItemType.TYPE_TITLE) {
			// init title view
		} else {
			// init description view
		}

		return resultView;
	}

}
