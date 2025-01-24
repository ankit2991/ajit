package macro.hd.wallpapers.NetworkManager;


import macro.hd.wallpapers.Model.IModel;

public interface IWebServiceCallback< T extends IModel> {
    public void onSuccess(T response, int operationCode);
    
    public void onError(WebServiceError error);

    public void onStartLoading();
}
