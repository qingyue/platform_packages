/**
 * 
 */
package com.onyx.android.launcher.dialog;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.DataSetObserver;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.onyx.android.launcher.OnyxBaseActivity;
import com.onyx.android.launcher.R;
import com.onyx.android.launcher.adapter.SelectionAdapter;
import com.onyx.android.launcher.data.GridItemManager;
import com.onyx.android.launcher.view.OnyxDialogBase;
import com.onyx.android.sdk.data.cms.OnyxMetadata;
import com.onyx.android.sdk.data.sys.OnyxAppPreference;
import com.onyx.android.sdk.data.sys.OnyxAppPreferenceCenter;
import com.onyx.android.sdk.data.util.FileUtil;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.BookItemData;
import com.onyx.android.sdk.ui.data.FileItemData;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager.UpdateMode;

/**
 * @author joy
 *
 */
public class DialogFileProperty extends OnyxDialogBase
{
	private ViewSwitcher mSwitcher = null;
	private Button mButtonProperty = null;
	private Button mButtonOpenWith = null;
	private Button mButtonNextPage = null;
	private Button mButtonPreviousPage = null;
	private OnyxBaseActivity mActivity = null;
	private OnyxGridView mGridView = null;
	private TextView mTextViewProgress = null;
	private TextView mTextViewUnableOpenFile = null;
	private LinearLayout mLayout = null;
	private File mFile = null;
	private SelectionAdapter mAdapter = null;
	private List<ResolveInfo> mListInfos = null;
	private String mExt= null;
	private int mUnselection = -1;

	public DialogFileProperty(Context context, FileItemData fileItem)
	{
		super(context);

		mActivity = (OnyxBaseActivity)context;
		mFile = GridItemManager.getFileFromURI(fileItem.getURI());

		this.setContentView(R.layout.dialog_file_property);

		ImageView imageview_cover = (ImageView)this.findViewById(R.id.imageview_cover);
		EditText edittext_name = (EditText)this.findViewById(R.id.edittext_name);
		EditText edittext_title = (EditText)this.findViewById(R.id.edittext_title);
		EditText edittext_authors = (EditText)this.findViewById(R.id.edittext_authors);
//		EditText edittext_rating = (EditText)this.findViewById(R.id.edittext_rating);
//		EditText edittext_publisher = (EditText)this.findViewById(R.id.edittext_publisher);
//		EditText edittext_tags = (EditText)this.findViewById(R.id.edittext_tags);
//		EditText edittext_series = (EditText)this.findViewById(R.id.edittext_series);
//		EditText edittext_isbn = (EditText)this.findViewById(R.id.edittext_isbn);
//		EditText edittext_date = (EditText)this.findViewById(R.id.edittext_date);
//		EditText edittext_published = (EditText)this.findViewById(R.id.edittext_published);

		EditText edittext_description = (EditText)this.findViewById(R.id.edittext_description);
		mTextViewUnableOpenFile = (TextView)this.findViewById(R.id.textview_unable_open_file);
		mSwitcher = (ViewSwitcher)this.findViewById(R.id.view_switcher);
		mGridView = (OnyxGridView)this.findViewById(R.id.gridview_open_with);
		mButtonProperty = (Button)this.findViewById(R.id.button_directory_details);
		mButtonOpenWith = (Button)this.findViewById(R.id.button_open_with);
		mButtonPreviousPage = (Button)this.findViewById(R.id.button_previous);
		mButtonNextPage = (Button)this.findViewById(R.id.button_next);
		mTextViewProgress = (TextView)this.findViewById(R.id.textview_paged);
		mLayout = (LinearLayout)this.findViewById(R.id.layout_paged);

		mSwitcher.setDisplayedChild(0);
		mButtonProperty.setBackgroundResource(R.drawable.button_background_reverse);

		mButtonProperty.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ScreenUpdateManager.invalidate(mSwitcher, UpdateMode.GU);

				if (mSwitcher.getDisplayedChild() != 0) {
					mSwitcher.setDisplayedChild(0);

					mButtonProperty.setBackgroundResource(R.drawable.button_background_reverse);
					mButtonOpenWith.setBackgroundResource(R.drawable.button_background);
				}
			}
		});
		mButtonOpenWith.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ScreenUpdateManager.invalidate(mSwitcher, UpdateMode.GU);

				if (mSwitcher.getDisplayedChild() != 1) {
					mSwitcher.setDisplayedChild(1);

					mButtonOpenWith.setBackgroundResource(R.drawable.button_background_reverse);
					mButtonProperty.setBackgroundResource(R.drawable.button_background);
				}
			}
		});

		Button button_cancel = (Button)this.findViewById(R.id.button_cancel);
		button_cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				DialogFileProperty.this.dismiss();
			}
		});
		Button button_set= (Button)this.findViewById(R.id.button_set);
		button_set.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				DialogFileProperty.this.dismiss();

				int selection = mAdapter.getSelection();
				if (selection >= 0) {
					ResolveInfo resolve_info = mListInfos.get(selection);
					String name = resolve_info.activityInfo.applicationInfo.loadLabel(mActivity.getPackageManager()).toString();
					String pkg = resolve_info.activityInfo.packageName;
					String cls = resolve_info.activityInfo.name;

					if (!OnyxAppPreferenceCenter.setAppPreference(mActivity, mExt, name, pkg, cls)) {
						Toast.makeText(mActivity, "Fail set", Toast.LENGTH_SHORT).show();
					}
				}
				else {
					if (!OnyxAppPreferenceCenter.removeAppPreference(mActivity, mExt)) {
						Toast.makeText(mActivity, "Fail remove", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

		mGridView.registerOnAdapterChangedListener(new OnyxGridView.OnAdapterChangedListener()
		{

			@Override
			public void onAdapterChanged()
			{
				mAdapter = (SelectionAdapter) mGridView.getAdapter();
				DialogFileProperty.this.setSelection(mAdapter.getSelection());

				mAdapter.registerDataSetObserver(new DataSetObserver()
				{
					@Override
					public void onChanged()
					{
						ScreenUpdateManager.invalidate(mSwitcher, UpdateMode.GU);
						DialogFileProperty.this.updateTextViewProgress();
					}

					@Override
					public void onInvalidated()
					{
						ScreenUpdateManager.invalidate(mSwitcher, UpdateMode.GU);
						DialogFileProperty.this.updateTextViewProgress();
					}
				});
			}
		});

		mGridView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				int selection = (Integer) view.getTag();
				int idx = mGridView.getPagedAdapter().getPaginator().getAbsoluteIndex(position);

				if (selection == idx) {
					DialogFileProperty.this.setSelection(mUnselection);
				}
				else {
					DialogFileProperty.this.setSelection(idx);
				}
			}
		});

		mButtonPreviousPage.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (mGridView.getPagedAdapter().getPaginator().canPrevPage()) {
					mGridView.getPagedAdapter().getPaginator().prevPage();
				}
			}
		});

		mButtonNextPage.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (mGridView.getPagedAdapter().getPaginator().canNextPage()) {
					mGridView.getPagedAdapter().getPaginator().nextPage();
				}
			}
		});

		if (fileItem instanceof BookItemData) {
			BookItemData book = (BookItemData)fileItem;

			if (book.getMetadata() != null) {
				imageview_cover.setImageBitmap(book.getBitmap());
				edittext_name.setText(book.getMetadata().getName());
				edittext_title.setText(book.getMetadata().getTitle());
				edittext_authors.setText(book.getMetadata().getAuthors() == null ? "" : 
					OnyxMetadata.convertAuthorsToString(book.getMetadata().getAuthors()));
				edittext_description.setText(book.getMetadata().getDescription());
			}
		}
		else {
			imageview_cover.setImageResource(fileItem.getImageResourceId());
			edittext_name.setText(fileItem.getURI().getName());
		}

		if (mFile.isFile()) {
			final Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(android.content.Intent.ACTION_VIEW);

			mExt = FileUtil.getFileExtension(mFile.getName());

			String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mExt);
			File file = new File("mnt/sdcard/dummy."+ mExt);
			if (type != null) {
				intent.setDataAndType(Uri.fromFile(file), type);
			}
			else {
				intent.setData(Uri.fromFile(file));
			}

			mListInfos = mActivity.getPackageManager().queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);

			if (mListInfos.size() <= 0) {
				if (mTextViewUnableOpenFile.getVisibility() == View.GONE) {
					mTextViewUnableOpenFile.setVisibility(View.VISIBLE);
				}

				mGridView.setVisibility(View.GONE);
				mLayout.setVisibility(View.GONE);
			}
			else {
				String defaultAppName = null;
				OnyxAppPreference p = OnyxAppPreferenceCenter.getApplicationPreference(mFile);
				if (p != null) {
					defaultAppName = p.getAppName();
				}
				else {
					defaultAppName = mActivity.getString(R.string.default_application);
				}

				String[] app_names = new String[mListInfos.size()];
				int selection = -1;
				for (int i = 0; i < mListInfos.size(); i++) {
					if (defaultAppName.equals(mListInfos.get(i).activityInfo.applicationInfo.loadLabel(mActivity.getPackageManager()))) {
						selection = i;
					}
					app_names[i] = mListInfos.get(i).activityInfo.applicationInfo.loadLabel(mActivity.getPackageManager()).toString();
				}

				mAdapter = new SelectionAdapter(mActivity, mGridView, app_names, selection);
				mGridView.setAdapter(mAdapter);

				mAdapter.getPaginator().setPageSize(mListInfos.size());
			}
		}
		else {
			if (mTextViewUnableOpenFile.getVisibility() == View.GONE) {
				mTextViewUnableOpenFile.setVisibility(View.VISIBLE);
			}

			mGridView.setVisibility(View.GONE);
			mLayout.setVisibility(View.GONE);
		}

		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager manager = getWindow().getWindowManager();
		manager.getDefaultDisplay().getMetrics(metrics);
		LayoutParams params = getWindow().getAttributes();
		if (metrics.widthPixels > metrics.heightPixels) {
			params.width = (int) (metrics.heightPixels * 0.9);
		}
		else {
			params.width = (int) (metrics.widthPixels * 0.9);
		}
	}

	private void setSelection(int position)
	{
		mAdapter.setSelection(position);
		mAdapter.notifyDataSetChanged();
	}

	private void updateTextViewProgress()
	{
		final int current_page = mGridView.getPagedAdapter().getPaginator().getPageIndex() + 1;
		final int page_count = (mGridView.getPagedAdapter().getPaginator().getPageCount() != 0) ? 
				mGridView.getPagedAdapter().getPaginator().getPageCount() : 1;

		mTextViewProgress.setText(String.valueOf(current_page) + "/" + String.valueOf(page_count));
	}
}