package net.floodlightcontroller.nctuforward;

import net.floodlightcontroller.core.module.IFloodlightService;

public interface INCTUForwardService extends IFloodlightService {
    public String setTargetIP(String target);
    public String getTargetIP();
}
