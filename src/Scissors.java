import java.util.concurrent.atomic.AtomicBoolean;

public class Scissors {
    // AtomicBoolean to indicate whether the scissors are currently in use by a stylist
    private final AtomicBoolean inUse = new AtomicBoolean(false);

    public boolean acquire() {
        // Attempt to set inUse to true, returns true if the operation was successful (scissors were available)
        return inUse.compareAndSet(false, true);
    }

    /**
     * Releases the scissors, marking them as available for use by another stylist.
     */
    public void release() {
        // Set inUse to false, indicating that the scissors are now available
        inUse.set(false);
    }
}
