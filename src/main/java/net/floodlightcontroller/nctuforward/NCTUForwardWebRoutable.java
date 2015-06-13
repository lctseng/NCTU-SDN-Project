package net.floodlightcontroller.nctuforward;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import net.floodlightcontroller.restserver.RestletRoutable;

public class NCTUForwardWebRoutable implements RestletRoutable {

    @Override
    public Restlet getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/target_ip/json", NCTUForwardResource.class);
        return router;
    }

    @Override
    public String basePath() {
        return "/wm/nctuforward";
    }

}
