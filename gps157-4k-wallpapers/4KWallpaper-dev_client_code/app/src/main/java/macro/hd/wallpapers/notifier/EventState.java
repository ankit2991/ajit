package macro.hd.wallpapers.notifier;

/**
 * This class declares all possible states of event returned by Event listeners.
 * 
 */
public class EventState {
    /**
     * Event is utilized so should not pass on to next listener.
     */
    public static final int EVENT_CONSUMED = 1;
    /**
     * Event is processed and utilized but can be pass on to next listener.
     */
    public static final int EVENT_PROCESSED = 2;
    /**
     * Event is not utilized so pass it to next listener.
     */
    public static final int EVENT_IGNORED = 3;
    /**
     * There was an error in processing the event.
     */
    public static final int EVENT_ERROR = 4;
}