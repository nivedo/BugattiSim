package tyrantlib.live;

import tyrantlib.model.*;

/**
 * Created by Jay on 9/26/2014.
 */
public class LiveAPI {

    CardHandler handler;

    // Get instance to search for cards
    public static LiveAPI instance = null;
    public static LiveAPI getInstance() {
        if (instance == null) {
            instance = new LiveAPI();
        }
        return instance;
    }

    public LiveAPI() {
        handler = CardHandler.getInstance();
    }

    public void requestAPI(String message) {

    }
}
