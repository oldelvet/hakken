package uk.co.vurt.hakken.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.IOException;

import uk.co.vurt.hakken.Constants;
import uk.co.vurt.hakken.providers.Dataitem;
import uk.co.vurt.hakken.providers.Job;
import uk.co.vurt.hakken.providers.Task;
import uk.co.vurt.hakken.providers.TaskProvider;
import uk.co.vurt.hakken.syncadapter.SyncAdapter;
import uk.co.vurt.hakken.LogoutState;

/**
 * Logout processing fragment.
 *
 * Used to maintain state during screen orientation changes.
 */
public class LogoutProcessorFragment extends Fragment implements AccountManagerCallback<Boolean> {
    private static final String TAG = "Logout";

    /** Attached logout listener. */
    private OnLogoutInteractionListener mListener;
    /** Main thread handler. */
    private Handler mHandler;
    /** Sync status change monitor handle. */
    private Object mChangeHandle;
    /** Application context attached content resolver. */
    private ContentResolver mResolver;
    /** Application context attached account manager. */
    private AccountManager mAccountManager;

    /** Current logout processing state. */
    private LogoutState status = LogoutState.INITIALISING;
    /** Initial sync sequence at start of processing. */
    private Integer initialSyncSequence;

    /**
     * Required empty public constructor.
     */
    public LogoutProcessorFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LogoutProcessorFragment.
     */
    public static LogoutProcessorFragment newInstance() {
        LogoutProcessorFragment fragment = new LogoutProcessorFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        // Remember the main thread so we can run things that depend on it later.
        mHandler = new Handler();
        Context appContext = getContext().getApplicationContext();
        mResolver = appContext.getContentResolver();
        mAccountManager = AccountManager.get(appContext);
        mChangeHandle = ContentResolver.addStatusChangeListener(
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE
                        | ContentResolver.SYNC_OBSERVER_TYPE_SETTINGS
                        | ContentResolver.SYNC_OBSERVER_TYPE_PENDING,
                new SyncStatusObserver() {
                    @Override
                    public void onStatusChanged(int which) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // Any update just update the sync status
                                updateSyncStatus();
                            }
                        });
                    }
                });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (LogoutState.INITIALISING.equals(status)) {
            // Just starting out so make things happen
            startLogout();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChangeHandle != null) {
            ContentResolver.removeStatusChangeListener(mChangeHandle);
            mChangeHandle = null;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLogoutInteractionListener) {
            mListener = (OnLogoutInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLogoutInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * @return the current status
     */
    public LogoutState getStatus() {
        return status;
    }

    /**
     * Start the logout processing.
     */
    public void startLogout() {
        if (hasItemsToSync()) {
            status = LogoutState.DOING_SYNC;
            updateListeners();
            synchronise();
        } else {
            status = LogoutState.PREPARING_REMOVE;
            updateListeners();
            doFinalCleanup();
        }
    }

    /**
     * Check with the content provider whether there are items to synchronise.
     * @return true if there are changed items to synchronised
     */
    private boolean hasItemsToSync() {
        boolean needSync = false;
        Cursor jobCursor = mResolver.query(Job.Definitions.CONTENT_URI,
                new String[]{Job.Definitions._ID, Job.Definitions.REMOTE_ID, Job.Definitions.TASK_DEFINITION_ID},
                Job.Definitions.STATUS + " = ?",
                new String[]{"COMPLETED"}, null);
        try {
            needSync = jobCursor.moveToNext();
        } finally {
            jobCursor.close();
        }
        return needSync;
    }

    /**
     * Request a new synchronisation.
     */
    private void synchronise() {
        initialSyncSequence = SyncAdapter.readSyncSequence();
        Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE); //retrieve all Hakken accounts
        if (accounts.length < 1) {
            // No account to remove should never happen - so fail here.
            status = LogoutState.FAILED;
            updateListeners();
            return;
        }
        Account account = accounts[0];

        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(account, TaskProvider.AUTHORITY, bundle);
    }

    private void updateSyncStatus() {
        if (initialSyncSequence != null) {
            boolean needSync = hasItemsToSync();
            int currentSyncSequence = SyncAdapter.readSyncSequence();
            if ((currentSyncSequence - initialSyncSequence) > 0) {
                switch (status) {
                    case DOING_SYNC:
                        if (needSync) {
                            // Need to try sync again
                            status = LogoutState.DOING_SYNC_AGAIN;
                            synchronise();
                        } else {
                            status = LogoutState.REMOVING_DATA_ITEMS;
                            initialSyncSequence = null;
                            doFinalCleanup();
                        }
                        break;
                    case DOING_SYNC_AGAIN:
                        initialSyncSequence = null;
                        if (needSync) {
                            // Retry failed so no more we can do
                            status = LogoutState.FAILED;
                            updateListeners();
                        } else {
                            status = LogoutState.REMOVING_DATA_ITEMS;
                            doFinalCleanup();
                        }
                        break;
                    default:
                        // Not interesting to us. Just ignore
                        // But we should never get to here
                        break;
                }
            }
        }
        updateListeners();
    }

    /**
     * Inform the attached listeners of logout progress.
     */
    public void updateListeners() {
        if (mListener != null) {
            mListener.onStatusUpdate(status);
        }
    }

    /**
     * Start the final cleanup to remove data.
     */
    private void doFinalCleanup() {
        new CleanupDataTask().execute(true);
    }

    @Override
    public void run(AccountManagerFuture<Boolean> future) {
        LogoutState result;
        try {
            if (Boolean.TRUE.equals(future.getResult())) {
                result = LogoutState.COMPLETED;
            } else {
                Log.i(TAG, "Account remove failed");
                result = LogoutState.FAILED;
            }
        } catch (OperationCanceledException e) {
            Log.i(TAG, "Account remove operation cancelled", e);
            result = LogoutState.FAILED;
        } catch (IOException e) {
            Log.i(TAG, "IOException during account remove", e);
            result = LogoutState.FAILED;
        } catch (AuthenticatorException e) {
            Log.i(TAG, "AuthenticationException during account remove", e);
            result = LogoutState.FAILED;
        }
        status = result;
        updateListeners();
    }

    /**
     * Logout progress status interaction.
     */
    public interface OnLogoutInteractionListener {
        /**
         * Logout status update.
         * @param status
         */
        void onStatusUpdate(LogoutState status);
    }

    private class CleanupDataTask extends AsyncTask<Boolean, LogoutState, LogoutState> {

        @Override
        protected LogoutState doInBackground(Boolean... booleans) {
            LogoutState result;
            try {
                result = doWork();
            } catch (Exception e) {
                Log.i(TAG, "Failure processing cleanup", e);
                result = LogoutState.FAILED;
            }
            return result;
        }

        private LogoutState doWork() {
            ContentResolver provider = mResolver;
            this.publishProgress(LogoutState.REMOVING_DATA_ITEMS);
            provider.delete(Dataitem.Definitions.CONTENT_URI, null, null);
            this.publishProgress(LogoutState.REMOVING_TASK_ITEMS);
            provider.delete(Job.Definitions.CONTENT_URI, null, null);
            this.publishProgress(LogoutState.REMOVING_JOB_ITEMS);
            provider.delete(Task.Definitions.CONTENT_URI, null, null);
            this.publishProgress(LogoutState.REMOVING_ACCOUNT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE); //retrieve all Hakken accounts
                if (accounts.length >= 0) {
                    for (Account account : accounts) {
                        mAccountManager.removeAccountExplicitly(account);
                    }
                }
            } else {
                // The actual remove will be done during post execute
                return LogoutState.REMOVING_ACCOUNT;
            }
            return LogoutState.COMPLETED;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(LogoutState value) {
            super.onPostExecute(value);
            status = value;
            updateListeners();
            if (LogoutState.REMOVING_ACCOUNT.equals(status)) {
                Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE); //retrieve all Hakken accounts
                if (accounts.length >= 0) {
                    for (Account account : accounts) {
                        mAccountManager.removeAccount(account, LogoutProcessorFragment.this, mHandler);
                    }
                }
            }
        }

        @Override
        protected void onProgressUpdate(LogoutState... values) {
            super.onProgressUpdate(values);
            status = values[0];
            updateListeners();
        }

        @Override
        protected void onCancelled(LogoutState value) {
            super.onCancelled(value);
            status = value;
            updateListeners();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}
