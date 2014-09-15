public class ViewPagerAdapter extends PagerAdapter {

    private LayoutInflater mInflater;

	public ViewPagerAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return 0; // TODO items count
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View view = mInflater.inflate(android.R.layout.select_dialog_item, null); // TODO layout
		container.addView(view, 0);
		return view;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public boolean isViewFromObject(View view, Object obj) {
		return view == obj;
	}

	@Override
	public Parcelable saveState() {
		return null;
	}
}
