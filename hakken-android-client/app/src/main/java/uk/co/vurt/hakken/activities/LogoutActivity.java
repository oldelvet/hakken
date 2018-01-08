package uk.co.vurt.hakken.activities;

import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Toast;

import uk.co.vurt.hakken.R;
import uk.co.vurt.hakken.fragments.LogoutProcessorFragment;
import uk.co.vurt.hakken.LogoutState;

public class LogoutActivity extends FragmentActivity
implements View.OnClickListener, DialogInterface.OnCancelListener,
        LogoutProcessorFragment.OnLogoutInteractionListener {

    /** Logout processor fragment tag. */
    private static final String TAG_LOGOUT_FRAGMENT = "LogoutProcessorFragment";

    /** Processor fragment. Only set if processing has begun. */
    private LogoutProcessorFragment mLogoutFrag;
    /** Logout status progress dialog. */
    private AlertDialog logoutProgressDialog;
    /** Boolean notifying when the display is in the foreground - no need to save/restore this. */
    private boolean mInForeground;

    private AccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);
        findViewById(R.id.buttonLogout).setOnClickListener(this);

        accountManager = AccountManager.get(this);

        // Look for any existing in progress logout
        FragmentManager fm = getSupportFragmentManager();
        mLogoutFrag =  (LogoutProcessorFragment)fm.findFragmentByTag(TAG_LOGOUT_FRAGMENT);
    }

    @Override
    public void onStatusUpdate(LogoutState status) {
        updateLogoutProgress(status, LogoutState.COMPLETED.equals(status));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mInForeground = true;
        if (mLogoutFrag != null) {
            LogoutState status = mLogoutFrag.getStatus();
            updateLogoutProgress(status, LogoutState.COMPLETED.equals(status));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mInForeground = false;
        if (logoutProgressDialog != null) {
            dismissLogoutProgress();
        }
    }

    @Override
    public void finish() {
        super.finish();
        Intent intent = new Intent(this, DispatcherActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonLogout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.logout_confirm);
                builder.setMessage(R.string.logout_are_you_sure);
                builder.setPositiveButton(R.string.logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startLogout();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(R.string.cancel_button_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                break;
            default:
                break;
        }
    }

    private void startLogout() {
        if (mLogoutFrag == null) {
            FragmentManager fm = getSupportFragmentManager();
            mLogoutFrag = LogoutProcessorFragment.newInstance();
            fm.beginTransaction().add(mLogoutFrag, TAG_LOGOUT_FRAGMENT).commit();
        }
    }

    /* (non-Javadoc)
     * @see android.content.DialogInterface.OnCancelListener#onCancel(android.content.DialogInterface)
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        // currently cancel is not enabled
    }

    private LogoutState getStatus() {
        LogoutProcessorFragment frag = mLogoutFrag;
        LogoutState result = LogoutState.INACTIVE;
        if (frag != null) {
            result = frag.getStatus();
        }
        return result;
    }

    /**
     * Update logout progress dialog
     * @param current current status
     * @param success true if the reload succeeded
     */
    public void updateLogoutProgress(final LogoutState current,
                                            final boolean success) {
        if (!mInForeground) {
            // Not in foreground so we should not update things.
            return;
        }
        final boolean dismissRequired;
        if (current.isActive()) {
            if (logoutProgressDialog == null) {
                ProgressDialog.Builder builder = new ProgressDialog.Builder(this);
                builder.setTitle(R.string.performing_logout);
                builder.setMessage(current.getMsg());
                builder.setCancelable(false);
                builder.setOnCancelListener(this);
                logoutProgressDialog = builder.show();
            } else {
                logoutProgressDialog.setMessage(current.getMsg());
            }
            dismissRequired = false;
        } else {
            dismissRequired = (logoutProgressDialog != null) || (mLogoutFrag != null);
        }
        if (dismissRequired) {
            dismissLogoutProgress();
            Toast.makeText(this,
                    success ? R.string.logout_complete : R.string.logout_failed,
                    Toast.LENGTH_LONG).show();
            // Remove fragment here
            if (mLogoutFrag != null) {
                FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction().remove(mLogoutFrag).commit();
                mLogoutFrag = null;
            }
            if (success) {
                finish();
            }
        }
    }

    /**
     * Dismiss reload profile.
     */
    private void dismissLogoutProgress() {
        if (logoutProgressDialog != null) {
            logoutProgressDialog.dismiss();
            logoutProgressDialog = null;
        }
    }
}
