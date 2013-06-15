package ca.rmen.android.scrumchatter.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import ca.rmen.android.scrumchatter.Constants;
import ca.rmen.android.scrumchatter.R;
import ca.rmen.android.scrumchatter.adapter.MembersCursorAdapter;
import ca.rmen.android.scrumchatter.adapter.MembersCursorAdapter.MemberItemCache;
import ca.rmen.android.scrumchatter.provider.MeetingMemberColumns;
import ca.rmen.android.scrumchatter.provider.MemberColumns;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MembersListFragment extends SherlockListFragment implements
		LoaderCallbacks<Cursor> {

	private static final String TAG = Constants.TAG + "/"
			+ MembersListFragment.class.getSimpleName();

	private static final int URL_LOADER = 0;

	private MembersCursorAdapter mAdapter;

	public MembersListFragment() {
		super();
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.member_list, null);
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mAdapter = new MembersCursorAdapter(activity, mOnClickListener);
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(URL_LOADER, null, this);
	}

	@Override
	public void onPause() {
		Log.v(TAG, "onPause");
		super.onPause();
	}

	@Override
	public void onResume() {
		Log.v(TAG, "onResume");
		super.onResume();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		String[] projection = new String[] { MemberColumns._ID,
				MemberColumns.NAME, MeetingMemberColumns.SUM_DURATION,
				MeetingMemberColumns.AVG_DURATION };
		CursorLoader loader = new CursorLoader(getActivity(),
				MeetingMemberColumns.CONTENT_URI, projection, null, null,
				MemberColumns.NAME);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.changeCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.members_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_new_member) {
			final Activity activity = getActivity();
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			final EditText input = new EditText(activity);
			builder.setView(input);
			builder.setTitle(R.string.action_new_member);
			builder.setMessage(R.string.dialog_message_new_member);
			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog,
								int whichButton) {
							String memberName = input.getText().toString()
									.trim();

							if (!TextUtils.isEmpty(memberName)) {
								ContentValues values = new ContentValues();
								values.put(MemberColumns.NAME, memberName);
								activity.getContentResolver().insert(
										MemberColumns.CONTENT_URI, values);
							}
						}
					});
			builder.setNegativeButton(android.R.string.cancel, null);
			final AlertDialog dialog = builder.create();
			input.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {
					validateMemberName();
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					validateMemberName();
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					validateMemberName();
				}

				private void validateMemberName() {
					String memberName = input.getText().toString().trim();
					Cursor existingMemberCountCursor = activity
							.getContentResolver().query(
									MemberColumns.CONTENT_URI,
									new String[] { "count(*)" },
									MemberColumns.NAME + "=?",
									new String[] { memberName }, null);
					input.setError(null);
					Button okButton = dialog
							.getButton(AlertDialog.BUTTON_POSITIVE);
					okButton.setEnabled(true);
					if (existingMemberCountCursor != null) {
						existingMemberCountCursor.moveToFirst();
						int existingMemberCount = existingMemberCountCursor
								.getInt(0);
						existingMemberCountCursor.close();
						if (existingMemberCount > 0) {
							input.setError(activity.getString(
									R.string.error_member_exists, memberName));
							okButton.setEnabled(false);
						}
					}
				}
			});
			dialog.show();

			return true;
		}
		return true;
	}

	private final OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_delete:
				if (v.getTag() instanceof MemberItemCache) {
					final MemberItemCache cache = (MemberItemCache) v.getTag();
					final Activity activity = getActivity();
					AlertDialog.Builder builder = new AlertDialog.Builder(
							activity);
					builder.setTitle(R.string.action_delete_member);
					builder.setMessage(activity.getString(
							R.string.dialog_message_delete_member_confirm,
							cache.name));
					builder.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int whichButton) {
									// TODO do on a background thread.
									activity.getContentResolver().delete(
											MemberColumns.CONTENT_URI,
											MemberColumns._ID + "=?",
											new String[] { String
													.valueOf(cache.id) });
								}
							});
					builder.setNegativeButton(android.R.string.cancel, null);
					builder.create().show();
				}
				break;
			default:
				break;
			}
		}
	};
}