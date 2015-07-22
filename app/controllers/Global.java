package controllers;

import play.*;

/**
 * Created by manisha on 7/6/2015.
 */
public class Global extends GlobalSettings {

    public void onStart(Application app)
    {
        Logger.info("Hello App");
    }

    public void onStop(Application app)
    {
        Logger.info("end.....");
    }

}
