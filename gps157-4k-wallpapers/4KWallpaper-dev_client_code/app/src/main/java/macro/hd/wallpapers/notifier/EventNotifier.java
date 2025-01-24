package macro.hd.wallpapers.notifier;

import java.util.Vector;


/**
 * This class defines methods to register, unregister listeners and call event notify of all
 * registered listeners according to priority.
 * 
 * 
 */
public class EventNotifier {
    
    private Vector< ListenerObject > _registeredListener = null;
    private int _eventCategory;
    
    /**
     * Parameterized constructor. Sets category of event notifier.
     * 
     * @param category
     *            integer containing category of notifier. Categories are defined in
     *            {@link NotifierFactory}.
     */
    public EventNotifier( int category ) {
        
        _registeredListener = new Vector< ListenerObject >( );
        _eventCategory = category;
    }


    /**
     * This is a template class created for adding Listener and its information in the queue
     *
     */
    public class ListenerObject {
        private IEventListener _eventListener;
        private int _priority;

        /**
         * Parameterized constructor.
         *
         * @param eventListener
         *            Listener object to which events need to be notified
         * @param priority
         *            priority from {@link ListenerPriority} indicating priority of listener
         */
        public ListenerObject( IEventListener eventListener, int priority ) {
            _eventListener = eventListener;
            _priority = priority;
        }

        /**
         *
         * @return the _eventListener
         */
        public IEventListener getIEventListener( ) {
            return _eventListener;
        }

        /**
         *
         * @return the _priority
         */
        public int getPriority( ) {
            return _priority;
        }
    }


    /**
     * @return category of event notifier
     */
    public int getEventCategory( ) {
        return _eventCategory;
    }
    
    /**
     * This method is called by all listeners to register for an required event. All listeners get
     * added in queue according to descending order of their priority.
     * 
     * @param eventListener
     *            {@link IEventListener} object to be registered.
     * @param listenerPriority
     *            {@link ListenerPriority}
     */
    public void registerListener( IEventListener eventListener, int listenerPriority ) {
        
        try {
            /**
             * check if listener is already registered. Two IEventListener can have same priority.
             */
            if ( _checkAlreadyRegistered( eventListener, listenerPriority ) ) {
                return;
            }
            
            ListenerObject listenerObj = new ListenerObject( eventListener, listenerPriority );
            int length = _registeredListener.size( );
            
            /**
             * if listeners queue is empty then add new listener in list at 1st position
             */
            if ( length == 0 ) {
                _registeredListener.addElement( listenerObj );
                return;
            }
            
            /**
             * Listeners are added in DESCENDING order of priority.
             */
            for ( int index = 0; index < length; index++ ) {
                
                ListenerObject tempObj1 = (ListenerObject) _registeredListener.elementAt( index );
                if ( listenerPriority <= tempObj1.getPriority( ) ) {
                    _registeredListener.insertElementAt( listenerObj, index );
                    return;
                }
            }
            
            // _registeredListener.insertElementAt(listenerObj, 0);
            _registeredListener.addElement( listenerObj );
            return;
        } catch ( Exception e ) {
            
            /*
             * MlLog.error( Category.CAT_NOTIFICATION,
             * "EventNotifier: registerListener: Exception: " + e.getMessage());
             */
        }
    }
    

    
    /**
     * This function scans listeners queue and notifies listeners according to their priority. If
     * {@link EventState} returns as CONSUMED, then it stops notifying further listeners otherwise
     * continues till queue gets finished.
     * 
     * @param eventType
     *            Type of event to be notified. One of the constants from {@link EventTypes}.
     * @param eventObject
     *            Extra information regarding event.
     * @return One of the Event state EVENT_CONSUMED/ EVENT_PROCESSED/ EVENT_IGNORED from
     *         {@link EventState}
     */
    public int eventNotify( int eventType, Object eventObject ) {
        int eventState = EventState.EVENT_PROCESSED;
        try {
            int length = _registeredListener.size( );
            if ( length == 0 ) {
                return EventState.EVENT_IGNORED;
            }
            /*
             * if ( eventType == EventTypes.EVENT_REFRESH_PLAYLIST_SONGLIST ) { Log.d( "VME",
             * "EventNotifier.eventNotify() listeners length is:" + length ); }
             */
            for ( int index = _registeredListener.size( ) - 1; index >= 0; index-- ) {
                IEventListener listenerObj =
                        (IEventListener) ( (ListenerObject) _registeredListener.elementAt( index ) )
                                .getIEventListener( );
                if ( listenerObj == null )
                    continue;
                /*
                 * if ( eventType == EventTypes.EVENT_REFRESH_PLAYLIST_SONGLIST ) { Log.d( "VME",
                 * "EventNotifier.eventNotify() listener type is:" + listenerObj.getClass(
                 * ).getCanonicalName( )); }
                 */

                eventState = listenerObj.eventNotify( eventType, eventObject );
                if ( eventState == EventState.EVENT_CONSUMED ) {
                    return EventState.EVENT_CONSUMED;
                }
            }
        } catch ( Exception e ) {
            e.printStackTrace( );
        }
        return eventState;
    }
    
    /**
     * This function checks whether specified listener is already registered or not.
     * 
     * @param eventListener
     *            {@link IEventListener} listener to be registered.
     * @param priority
     *            {@link ListenerPriority}
     * 
     * @return true if listener is already registered, false otherwise.
     */
    private boolean _checkAlreadyRegistered( IEventListener eventListener, int priority ) {
        /**
         * check if listener is already registered if so, return true; otherwise false;
         */
        try {
            int length = _registeredListener.size( );
            
            for ( int index = 0; index < length; index++ ) {
                IEventListener listener =
                        (IEventListener) ( (ListenerObject) _registeredListener.elementAt( index ) )
                                .getIEventListener( );
                /**
                 * compare only objects; do not check for priority. two IEventListener objects can
                 * have same priority
                 */
                
                if ( eventListener.equals( listener ) ) {
                    return true;
                }
            }
        } catch ( Exception e ) {
            
        }
        return false;
    }

    /**
     * This is to unregister listener. Listener gets removed from queue.
     *
     * @param eventListener
     *            {@link IEventListener} to be unregistered.
     */
    public void unRegisterListener( IEventListener eventListener ) {
        try {
            int length = _registeredListener.size( );
            for ( int index = 0; index < length; index++ ) {
                ListenerObject listenerObj = (ListenerObject) _registeredListener.elementAt( index );
                IEventListener iEventListener = listenerObj.getIEventListener( );

                /**
                 * do not compare priorities because 2 IEventListener can have same priorities,
                 * compare IEventListener Objects
                 */
                if ( iEventListener.equals( eventListener ) ) {
                    _registeredListener.removeElementAt( index );
                    return;
                }
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            /*
             * MlLog.error( Category.CAT_NOTIFICATION,
             * "EventNotifier: unRegisterListener: Exception: " + e.getMessage());
             */

        }
    }

}