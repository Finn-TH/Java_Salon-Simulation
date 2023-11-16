import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Comb class represents a comb resource used by stylists in the salon.
 * It utilizes an AtomicBoolean to track its usage status.
 */
public class Comb {
    // AtomicBoolean to indicate whether the comb is currently in use by a stylist
    private final AtomicBoolean inUse = new AtomicBoolean(false);

    /**
     * Acquires the comb for use by a stylist.
     *
     * @return true if the comb was successfully acquired, false if it was already in use
     */
    public boolean acquire() {
        // Attempt to set inUse to true, returns true if the operation was successful (comb was available)
        return inUse.compareAndSet(false, true);
    }

    public void release() {
        // Set inUse to false, indicating that the comb is now available
        inUse.set(false);
    }
}

