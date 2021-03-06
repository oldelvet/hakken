package uk.co.vurt.hakken.fragments;

import uk.co.vurt.hakken.R;
import uk.co.vurt.hakken.domain.job.JobDomainAdapter;
import uk.co.vurt.hakken.providers.Job;
import uk.co.vurt.hakken.providers.Task;
import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class JobListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = "JobListFragment";

	private OnJobSelectedListener listener;

	private final static int JOB_LOADER = 0;

	private JobDomainAdapter adapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate called");
		// Create an empty adapter we will use to display the loaded data.
		// We pass null for the cursor, then update it in onLoadFinished()
		// Used to map task definition entries from the database to views
//		adapter = new JobDomainAdapter(super.getActivity().getApplicationContext(),
//				R.layout.selectjob_list_item, null, new String[] {
//						Job.Definitions.NAME, Job.Definitions.DUE,
//						Job.Definitions.STATUS, Job.Definitions.GROUP,
//						Job.Definitions.NOTES}, new int[] {
//						R.id.joblist_entry_name, R.id.joblist_entry_duedate,
//						R.id.joblist_entry_completed, R.id.joblist_section_header,
//						R.id.joblist_entry_notes},
//						CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		Log.d(TAG, "onCreateView called");
		View root = inflater.inflate(R.layout.fragment_job_list, null);
		adapter = new JobDomainAdapter(super.getActivity().getApplicationContext(),
				R.layout.selectjob_list_item, null, new String[] {
						Job.Definitions.NAME, Job.Definitions.DUE,
						Job.Definitions.STATUS, Job.Definitions.GROUP,
						Job.Definitions.DESCRIPTION}, new int[] {
						R.id.joblist_entry_name, R.id.joblist_entry_duedate,
						R.id.joblist_entry_completed, R.id.joblist_section_header,
						R.id.joblist_entry_notes},
						CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		setListAdapter(adapter);
		return root;
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated called");
		// Create an empty adapter we will use to display the loaded data.
		// We pass null for the cursor, then update it in onLoadFinished()
		// Used to map task definition entries from the database to views
//		adapter = new JobDomainAdapter(super.getActivity().getApplicationContext(),
//				R.layout.selectjob_list_item, null, new String[] {
//						Job.Definitions.NAME, Job.Definitions.DUE,
//						Job.Definitions.STATUS, Job.Definitions.GROUP,
//						Job.Definitions.NOTES}, new int[] {
//						R.id.joblist_entry_name, R.id.joblist_entry_duedate,
//						R.id.joblist_entry_completed, R.id.joblist_section_header,
//						R.id.joblist_entry_notes},
//						CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		setListAdapter(adapter);
		
		getLoaderManager().initLoader(JOB_LOADER, null, this);
	}

	

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "Attempting to create cursor loader");
		return new CursorLoader(getActivity(), Job.Definitions.CONTENT_URI,
				Job.Definitions.ALL, null, null, Job.Definitions.DEFAULT_SORT_ORDER);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d(TAG, "onLoadFinished called: " + cursor);
		adapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d(TAG, "onLoaderReset called.");
		adapter.swapCursor(null);
	}

	@Override
	public void onListItemClick(ListView list, View view, int position, long id) {
		Uri jobUri = ContentUris
				.withAppendedId(Job.Definitions.CONTENT_URI, id);
		listener.onJobSelected(jobUri);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnJobSelectedListener) activity;
		} catch (ClassCastException cce) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnJobSelectedListener");
		}
	}

	public interface OnJobSelectedListener {
		public void onJobSelected(Uri jobUri);
	}
}
