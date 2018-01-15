package uk.co.vurt.hakken.ui.widget;

/**
 * Multiple child listener. Used by the parent to listen for changes.
 */
public interface MultiChildListener {
    /**
     * Child removal request.
     * @param child child requesting removal
     */
    void removeChildRequest(MultiGroupChild child);
}
