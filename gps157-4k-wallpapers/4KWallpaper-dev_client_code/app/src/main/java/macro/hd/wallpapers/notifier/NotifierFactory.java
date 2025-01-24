package macro.hd.wallpapers.notifier;

import java.util.Vector;

/**
 * This is a factory class for {@link EventNotifier}. It maintains queue of notifiers required by
 * application. Checks whether already present, otherwise creates new and returns it.
 * 
 */
public class NotifierFactory {
    
    /**
     * This is a vector for maintaining all {@link EventNotifier} required by application.
     */
    private static Vector< EventNotifier > _eventNotifiers = null;
    
    private static NotifierFactory _notifierFactoryInstance = null;
    
    /**
     * Session events.
     */
    public static final int EVENT_NOTIFIER_LOCATION_UPDATE = 2;

    public static final int EVENT_NOTIFIER_USER_INFO = 3;
    public static final int EVENT_NOTIFIER_DOWNLOAD_INFO = 4;

    public static final int EVENT_NOTIFIER_AD_STATUS = 5;
    public static final int EVENT_NOTIFIER_UPDATE_WALL = 6;

    public static final int EVENT_NOTIFIER_UPDATE_FOURK_WALL = 7;

    public static final int EVENT_NOTIFIER_STATUS_SAVER = 8;

    /**
     * Private constructor.
     */
    private NotifierFactory( ) {
        _eventNotifiers = new Vector< EventNotifier >( );
    }
    
    /**
     * This method is used to maintain the NotifierFactory class singleton.
     * 
     * @return initialized object of type {@link NotifierFactory}.
     */
    public static NotifierFactory getInstance( ) {
        if ( _notifierFactoryInstance == null ) {
            _notifierFactoryInstance = new NotifierFactory( );
        }
        return _notifierFactoryInstance;
    }
    
    /**
     * This function is called to get {@link EventNotifier} for specified event category. New
     * {@link EventNotifier} is created if not present in queue, otherwise returns notifier from
     * queue.
     * 
     * @param eventCategory
     *            integer constant indicating category of required {@link EventNotifier}.
     * @return {@link EventNotifier} for required category.
     */
    public EventNotifier getNotifier( int eventCategory ) {
        
        /**
         * Check whether present in queue
         */
        EventNotifier eventNotifier = findNotifier( eventCategory );
        /**
         * If present, return EventNotifier from queue.
         */
        if ( eventNotifier != null ) {
            
            return eventNotifier;
        }
        
        /**
         * Required EventNotifier is not present in queue, so create new, add it to the queue and
         * return it
         */
        EventNotifier objEventNotifier = new EventNotifier( eventCategory );
        _eventNotifiers.addElement( objEventNotifier );
        
        return objEventNotifier;
    }
    
    /**
     * This function checks whether EventNotifier for specified category is already created or not
     * depending on specified category.
     * 
     * @param eventCategory
     *            Integer indicating constant for require category.
     * 
     * @return {@link EventNotifier} if present, otherwise null.
     */
    private static EventNotifier findNotifier( int eventCategory ) {
        
        EventNotifier eventNotifierObject = null;
        
        int length = _eventNotifiers.size( );
        
        for ( int index = 0; index < length; index++ ) {
            
            eventNotifierObject = (EventNotifier) _eventNotifiers.elementAt( index );
            int category = eventNotifierObject.getEventCategory( );
            
            if ( eventCategory == category ) {
                
                return eventNotifierObject;
            }
            eventNotifierObject = null;
        }
        return eventNotifierObject;
    }
}