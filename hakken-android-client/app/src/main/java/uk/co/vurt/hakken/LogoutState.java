package uk.co.vurt.hakken;

/**
 * Logout processing state.
 */
public enum LogoutState {
    INACTIVE(false, "Inactive"),
    INITIALISING(true, "Preparing to logout"),
    DOING_SYNC(true, "Performing data synchronisation"),
    DOING_SYNC_AGAIN(true, "Retrying data synchronisation"),
    PREPARING_REMOVE(true, "Preparing to remove data"),
    REMOVING_DATA_ITEMS(true, "Removing data items"),
    REMOVING_TASK_ITEMS(true, "Removing tasks"),
    REMOVING_JOB_ITEMS(true, "Removing definitions"),
    REMOVING_ACCOUNT(true, "Removing account"),
    COMPLETED(false, "Logout complete"),
    FAILED(false, "Logout failed"),
    CANCELLED(false, "Logout cancelled");

    /** If true logout is active. */
    private final boolean active;
    /** Progress message for this state. */
    private final String msg;

    private LogoutState(final boolean active, final String msg) {
        this.active = active;
        this.msg = msg;
    }

    public boolean isActive() {
        return active;
    }

    public String getMsg() {
        return msg;
    }
}
