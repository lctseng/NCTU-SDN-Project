

package net.floodlightcontroller.topology;

public class MatchTrafficInfo {

    public int priority;
    public int old_p;
    public MatchTrafficInfo(int p,int old_p) {
        this.priority = p;
        this.old_p = old_p;
    }
    public boolean fresh(){
        return priority == old_p;
    }
}
