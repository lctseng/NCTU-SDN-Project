/**
 *    Copyright 2013, Big Switch Networks, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

package net.floodlightcontroller.topology.web;



import net.floodlightcontroller.routing.IRoutingService;

import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SDNResource extends ServerResource {

    protected static Logger log = LoggerFactory.getLogger(RouteResource.class);

    @Get("json")
    public String retrieve() {
        IRoutingService routing = 
                (IRoutingService)getContext().getAttributes().
                    get(IRoutingService.class.getCanonicalName());
        routing.showTraffic();
        return "Printed in console\n";
        
    }
    @Post
    public void handlePost(String src) {
        log.info("Traffic Cleared");
        IRoutingService routing = 
                (IRoutingService)getContext().getAttributes().
                    get(IRoutingService.class.getCanonicalName());
        routing.resetTraffic();
    }
}
