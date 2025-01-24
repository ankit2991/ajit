package macro.hd.wallpapers.NetworkManager;

import java.util.Observable;

public interface MutablePriorityElement extends PriorityElement {
    public void setPriority(int priority);
    
    public Observable getPriorityObservable();
}
