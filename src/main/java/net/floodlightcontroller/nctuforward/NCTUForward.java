package net.floodlightcontroller.nctuforward;

import java.io.IOException;
import java.util.ArrayList;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openflow.protocol.OFBarrierRequest;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionDataLayerDestination;
import org.openflow.protocol.action.OFActionNetworkLayerDestination;
import org.openflow.protocol.action.OFActionOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Route;


import net.floodlightcontroller.learningswitch.LearningSwitch;
import net.floodlightcontroller.packet.Ethernet;

import net.floodlightcontroller.packet.IPv4;import net.floodlightcontroller.packet.UDP;

import net.floodlightcontroller.topology.NodePortTuple;
import net.floodlightcontroller.util.MACAddress;
import net.floodlightcontroller.util.OFMessageDamper;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.devicemanager.SwitchPort;
import net.floodlightcontroller.packet.ARP;


public class NCTUForward implements IFloodlightModule, IOFMessageListener, INCTUForwardService {

	
	public final int DEFAULT_CACHE_SIZE = 10; 
	private IFloodlightProviderService floodlightProvider;
	private IDeviceService deviceManager;
	private IRoutingService routingEngine; 
	private OFMessageDamper messageDamper;
	private String targetIPstring;
	private int targetIP;
	
	protected IRestApiService restApi;
	protected static Logger log = LoggerFactory.getLogger(LearningSwitch.class);
	
    protected static short FLOWMOD_DEFAULT_IDLE_TIMEOUT = 7; // sec
    protected static short FLOWMOD_DEFAULT_HARD_TIMEOUT = 0; // infinite
    
	@Override
	public String getName() {
		return "NCTUForward";
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		return (type.equals(OFType.PACKET_IN) &&
				(name.equals("topology") ||
				 name.equals("devicemanager"))); 
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		return (type.equals(OFType.PACKET_IN) && name.equals("forwarding"));
	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,IFloodlightProviderService.CONTEXT_PI_PAYLOAD);//取得link layer封包
		OFPacketIn pin = (OFPacketIn) msg;
		if (eth.getEtherType() == Ethernet.TYPE_ARP){
			ARP arp = (ARP)eth.getPayload();
			// we will modify ARP request
			// test on when sender is not targetIP
			// and the question is also not targetIP
			if( arp.getOpCode() == ARP.OP_REQUEST 
			        && IPv4.toIPv4Address(arp.getTargetProtocolAddress()) != targetIP
			        && IPv4.toIPv4Address(arp.getSenderProtocolAddress()) != targetIP
			        )
			{
			    // change and send double arp only if target is not in ARP frame
			    // original ARP needed to be send
	            pushPacket(sw,pin,false,(short)-5,cntx,true);
	            // let's copy ARP to find target host
	            arp.setTargetProtocolAddress(targetIP);
	            arp.setTargetHardwareAddress(MACAddress.valueOf("00:00:00:00:00:00").toBytes());
	            eth.setPayload(arp);
	            pin.setPacketData(eth.serialize());
			    // now the packet-in next is aimed to find targetIP
			}
			
		}
		else if (eth.getEtherType() == Ethernet.TYPE_IPv4){
			 //System.out.println("$$$ IPv4 Detected");
			 IPv4 ipPkt = (IPv4)eth.getPayload();
			// source device
			 IDevice srcDev = IDeviceService.fcStore.
                     get(cntx, IDeviceService.CONTEXT_SRC_DEVICE);
			 SwitchPort[] pts = srcDev.getAttachmentPoints();
			 SwitchPort srcSW = null;
			 if(pts.length > 0){
			     srcSW = pts[0];
			 }
			 
			 //System.out.println(ipPkt.getSourceAddress());
			 //&& ipPkt.getDestinationAddress() != targetIP
             if(srcSW != null && ipPkt.getProtocol() == IPv4.PROTOCOL_UDP && srcSW.getSwitchDPID() == sw.getId() ){
            	 UDP udpPkt = (UDP)ipPkt.getPayload();
            	 if(udpPkt.getDestinationPort() == 5134){
        			 // find target info : MAC and IP
        			 byte[] dstMAC;
        			 int dstIP = targetIP;
        			 // output port for this switch
        			 short outport;
        			 
        			 
            		 // find target device
        			 IDevice dstDev = null;
        			 short idleTimeout;
        			 Route routeIn = null;
        			 List<NodePortTuple> path = null;
            		 Iterator<? extends IDevice> it = deviceManager.queryDevices(null, null,targetIP ,null,null);
            		 if (it.hasNext()) {   
            			 dstDev = it.next();
            			 dstMAC = MACAddress.valueOf( dstDev.getMACAddress()).toBytes();
            			 // route!
            			 
            			 SwitchPort dstSW = dstDev.getAttachmentPoints()[0];
            			 routeIn= routingEngine.getRoute(srcSW.getSwitchDPID(), 
            			         (short)srcSW.getPort(), dstSW.getSwitchDPID(), 
            			         (short)dstSW.getPort(),0);
            			 path = routeIn.getPath();
            			 NodePortTuple swPort = path.get(1);
            			 outport = swPort.getPortId();
            			 idleTimeout = 0; // infinite
            		 }
            		 else{ // no mac ? use broadcast address
            			 dstMAC = MACAddress.valueOf("FF:FF:FF:FF:FF:FF").toBytes();
            			 outport = OFPort.OFPP_FLOOD.getValue();
            			 idleTimeout = FLOWMOD_DEFAULT_IDLE_TIMEOUT;
            		 }
            		 System.out.println("$$$ Target MAC is ");
            		 System.out.println(MACAddress.valueOf(dstMAC).toString());
            		 // set match for further use
            		 
        			 OFMatch match = new OFMatch();
        			 match.loadFromPacket(pin.getPacketData(),pin.getInPort()); //從PACKET_IN 所帶的封包設定match
            		 // now , THIS packet-in will redirect
            		 // Change your Dest IP!
            		 ipPkt.setDestinationAddress(dstIP);
        			 ipPkt.resetChecksum();
        			 eth.setPayload(ipPkt);
        			 eth.setDestinationMACAddress(dstMAC);
        			 pin.setPacketData(eth.serialize()); //packed!
        			 
        			 
        			 // Next step, make a action to change header
        			 List<OFAction> actions = new ArrayList<OFAction>();
    				 
    				 
    				 // we will change the MAC and IP !
        			 int extraLength = 0;
            		 OFActionDataLayerDestination destMacAct= new OFActionDataLayerDestination(dstMAC);
            		 OFActionNetworkLayerDestination destIPAct= new OFActionNetworkLayerDestination(dstIP);
            		 
            		 actions.add(destMacAct);
            		 actions.add(destIPAct);
            		 extraLength += OFActionDataLayerDestination.MINIMUM_LENGTH
         			 			+OFActionNetworkLayerDestination.MINIMUM_LENGTH;

            		 OFActionOutput out = new OFActionOutput(outport);
            	     out.setMaxLength((short)0xffff);//設定傳送封包大小
            	     
            	     actions.add(out);

        			 OFFlowMod fm= (OFFlowMod) floodlightProvider
        			         .getOFMessageFactory()
        			         .getMessage(OFType.FLOW_MOD);
        			 fm.setIdleTimeout(idleTimeout)
        			 	.setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
        			 	.setBufferId(OFPacketOut.BUFFER_ID_NONE)
        			 	.setCookie((long) 1001)
        			 	.setPriority((short)1024)
        			 	.setCommand(OFFlowMod.OFPFC_ADD)
        			 	.setMatch(match)
        			 	.setActions(actions)
        			 	.setLengthU(OFFlowMod.MINIMUM_LENGTH
        			 			+OFActionOutput.MINIMUM_LENGTH
        			 			+extraLength);
        			 Integer wildcard_hints= ((Integer)sw.getAttribute(IOFSwitch.PROP_FASTWILDCARDS)).intValue()
        					 	& ~OFMatch.OFPFW_IN_PORT
        					 	& ~OFMatch.OFPFW_NW_SRC_MASK
        					 	& ~OFMatch.OFPFW_NW_DST_MASK
        					 	& ~OFMatch.OFPFW_TP_DST
        					 	& ~OFMatch.OFPFW_NW_PROTO
        					 	& ~OFMatch.OFPFW_DL_TYPE;
        			 fm.setMatch(wildcard(match, sw, wildcard_hints));
        			 
        			 try {
						messageDamper.write(sw, fm, cntx);
						sw.flush();
					 } catch (IOException e) {
					     System.out.println("$$$ FAIL ON WRITE FM to ingress SW!");
					 }
        			 
        			 // Now for all other switches on path
        			 if (path != null){
        			     System.out.println("$$$ write for path SW...");
            			 for(int i=3;i<path.size();i+=2){
            			     
            			     NodePortTuple node = path.get(i);
                             IOFSwitch pathSW = floodlightProvider.getSwitch(node.getNodeId());
                             if (pathSW != null){
                                 System.out.println("$$$ On SW:");
                                 System.out.println(node.getNodeId());
                                 // switch exist!
                                 // set match : match new!
                                 OFMatch matchPath = match.clone();
                                 matchPath.setNetworkDestination(targetIP);
                                 actions = new ArrayList<OFAction>();
                                 out = new OFActionOutput(node.getPortId());
                                 //out = new OFActionOutput(OFPort.OFPP_FLOOD.getValue());
                                 out.setMaxLength((short)0xffff);//設定傳送封包大小
                                 actions.add(out);
                                 // flow modify
                                 extraLength = 0;
                                 fm= (OFFlowMod) floodlightProvider
                                         .getOFMessageFactory()
                                         .getMessage(OFType.FLOW_MOD);
                                 fm.setIdleTimeout(idleTimeout)
                                    .setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
                                    .setBufferId(OFPacketOut.BUFFER_ID_NONE)
                                    .setCookie((long) 1002)
                                    .setPriority((short)1024)
                                    .setCommand(OFFlowMod.OFPFC_ADD)
                                    .setMatch(matchPath)
                                    .setActions(actions)
                                    .setLengthU(OFFlowMod.MINIMUM_LENGTH
                                            +OFActionOutput.MINIMUM_LENGTH
                                            +extraLength);
                                 wildcard_hints= ((Integer)sw.getAttribute(IOFSwitch.PROP_FASTWILDCARDS)).intValue()
                                            & ~OFMatch.OFPFW_IN_PORT
                                            & ~OFMatch.OFPFW_NW_DST_MASK
                                            & ~OFMatch.OFPFW_TP_DST
                                            & ~OFMatch.OFPFW_NW_PROTO
                                            & ~OFMatch.OFPFW_DL_TYPE;
                                 fm.setMatch(wildcard(matchPath, sw, wildcard_hints));
                                 
                                 try {
                                     messageDamper.write(pathSW, fm, cntx);
              
                                     
                                     OFBarrierRequest br = (OFBarrierRequest) floodlightProvider
                                             .getOFMessageFactory()
                                             .getMessage(OFType.BARRIER_REQUEST);
                                     messageDamper.write(pathSW, br, cntx);
                                     
                                     pathSW.flush();
                                  } catch (IOException e) {
                                      System.out.println("$$$ Error On SW:");
                                      System.out.println(node.getNodeId());
                                      System.out.println("$$$ FAIL ON WRITE FM to path SW!");
                                  }
                                 
                                  
                                  
                                  
                             } //if sw
            			     
            			 } // for each sw
            			 // push pkt to last sw
            			 NodePortTuple node = path.get(path.size()-1);
            			 IOFSwitch pathSW = floodlightProvider.getSwitch(node.getNodeId());
            			 pushPacket(pathSW,pin,false,node.getPortId(),cntx,true);
            			 System.out.println("$$$ STOPPING $$$");
            			 return Command.STOP;
        			 } // if path
			         
        			 // 
        			 //
        			 // 
        			 
        			 //pushPacket(sw,pin,false,path.get(i).getPortId(),cntx);
        			 
            		 /*
            		 // This packet needs to be redirect
            		 
            		 // get all device
            		 
            		 // set source and target device/switch info for routing
            		 IDevice srcDev = IDeviceService.fcStore.
                             get(cntx, IDeviceService.CONTEXT_SRC_DEVICE);
            		 System.out.println("$$$ Src dev:");
            		 System.out.println(srcDev);
            		 IDevice dstDev = null;
            		 // find target device
            		 Iterator<? extends IDevice> it = deviceManager.queryDevices(null, null,IPv4.toIPv4Address("10.0.0.1") ,null,null);
            		 if (it.hasNext()) {   
            			 dstDev = it.next();
            		 }
            		 // get switchPort
            		 SwitchPort srcSW,dstSW;
            		 System.out.println("$$$ Finding Destination...:");
            		 if(dstDev != null){
            			 System.out.println("$$$ Src device:");
            			 System.out.println(srcDev);
                		 System.out.println("$$$ Target Device");
            			 System.out.println(dstDev);
            			 // get switch
            			 srcSW = srcDev.getAttachmentPoints()[0];
            			 dstSW = dstDev.getAttachmentPoints()[0];
            			 // get Route
            			 Route routeIn= routingEngine.getRoute(srcSW.getSwitchDPID(), (short)srcSW.getPort(), dstSW.getSwitchDPID(), (short)dstSW.getPort(),0);
            			 
            			 // destination info
            			 byte[] dstMAC = MACAddress.valueOf( dstDev.getMACAddress()).toBytes();
            			 int dstIP = IPv4.toIPv4Address("10.0.0.1");
            			 
    
            			 OFPacketIn pin = (OFPacketIn) msg;
            			 
            			 eth.setDestinationMACAddress(dstMAC);

            			 ipPkt.setDestinationAddress(dstIP);
            			 ipPkt.resetChecksum();
            			 eth.setPayload(ipPkt);
            			 
            			 
            			 pin.setPacketData(eth.serialize());
            			 

            			 
            			 
            			 OFMatch matchOri = new OFMatch();
            			 matchOri.loadFromPacket(pin.getPacketData(),pin.getInPort()); //從PACKET_IN 所帶的封包設定match
            			 // for each switch in path
            			 List<NodePortTuple> path = routeIn.getPath();
            			 System.out.println("$$$ Start SW:");
            			 System.out.println(path.size());
            			 for(int i=1;i<path.size();i+=2){
            			 //for(int i=0;i<1;i++){	 
            				 int extraLength = 0;
            				 NodePortTuple node = path.get(i);
            				 IOFSwitch sw = floodlightProvider.getSwitch(node.getNodeId());
            				 System.out.println("$$$ On SW:");
            				 System.out.println(node.getNodeId());
            				 
            				 System.out.println(sw.getStringId());
                			 // set match
                			 OFMatch match = matchOri.clone();
            				 // actions
            				 List<OFAction> actions = new ArrayList<OFAction>();
            				 
            				 OFActionOutput out = new OFActionOutput(node.getPortId());
            				 out.setMaxLength((short)0xffff);//設定傳送封包大小
            				 if(i > 1){
            					 // need to match new IP and MAC
            					 match.setDataLayerDestination(dstMAC);
            					 match.setNetworkDestination(dstIP);
            					 

            				 }
            				 else{
            					 // first switch !
            					 // we will change the MAC and IP !
                    			 OFActionDataLayerDestination destMacAct= new OFActionDataLayerDestination(dstMAC);
                    			 OFActionNetworkLayerDestination destIPAct= new OFActionNetworkLayerDestination(dstIP);
                    			 actions.add(destMacAct);
                    			 actions.add(destIPAct);
                    			 extraLength += OFActionDataLayerDestination.MINIMUM_LENGTH
                 			 			+OFActionNetworkLayerDestination.MINIMUM_LENGTH;

            				 }

            				 actions.add(out);

                			 OFFlowMod fm= (OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);
                			 fm.setIdleTimeout(FLOWMOD_DEFAULT_IDLE_TIMEOUT)
                			 	.setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
                			 	.setBufferId(OFPacketOut.BUFFER_ID_NONE)
                			 	.setCookie((long) 0)
                			 	.setPriority((short)1024)
                			 	.setCommand(OFFlowMod.OFPFC_ADD)
                			 	.setMatch(match)
                			 	.setActions(actions)
                			 	.setLengthU(OFFlowMod.MINIMUM_LENGTH
                			 			+OFActionOutput.MINIMUM_LENGTH
                			 			+extraLength);
                			 Integer wildcard_hints= ((Integer)sw.getAttribute(IOFSwitch.PROP_FASTWILDCARDS)).intValue()
                					 	& ~OFMatch.OFPFW_IN_PORT
                					 	& ~OFMatch.OFPFW_NW_SRC_MASK
                					 	& ~OFMatch.OFPFW_NW_DST_MASK
                					 	& ~OFMatch.OFPFW_TP_DST
                					 	& ~OFMatch.OFPFW_NW_PROTO
                					 	& ~OFMatch.OFPFW_DL_TYPE;
                			 fm.setMatch(wildcard(match, sw, wildcard_hints));
                			 
                			 
                			 try {
								sw.write(fm, cntx);
							} catch (IOException e) {
							
								e.printStackTrace();
							}

                			 
                			 if (i == 1){
                				
                				 pushPacket(sw,pin,false,path.get(i).getPortId(),cntx);
                				 
                			 }
                			 
            			 } // end of for switch
            			 return Command.STOP;
            		 } // end if dst device
            		 */
            	 } // end port
              } // end UDP
              
		} // end IPv4
		return Command.CONTINUE;
	}
    protected OFMatch wildcard(OFMatch match, IOFSwitch sw,
            Integer wildcard_hints)
    {
		if (wildcard_hints != null) {
			return match.clone().setWildcards(wildcard_hints.intValue());
		}
		return match.clone();
		
    }


    protected void pushPacket(IOFSwitch sw, OFPacketIn pi,
            boolean useBufferId,
            short outport, FloodlightContext cntx,boolean doFlush) {

			if (pi == null) {
			return;
			}
			
			// The assumption here is (sw) is the switch that generated the
			// packet-in. If the input port is the same as output port, then
			// the packet-out should be ignored.


			
			OFPacketOut po =
			 (OFPacketOut) floodlightProvider.getOFMessageFactory()
			                                 .getMessage(OFType.PACKET_OUT);
			
			// set actions
			List<OFAction> actions = new ArrayList<OFAction>();
			actions.add(new OFActionOutput(outport, (short) 0xffff));
			
			po.setActions(actions)
			.setActionsLength((short) OFActionOutput.MINIMUM_LENGTH);
			short poLength =
			 (short) (po.getActionsLength() + OFPacketOut.MINIMUM_LENGTH);
			
			if (useBufferId) {
			po.setBufferId(pi.getBufferId());
			} else {
			po.setBufferId(OFPacketOut.BUFFER_ID_NONE);
			}
			
			if (po.getBufferId() == OFPacketOut.BUFFER_ID_NONE) {
			byte[] packetData = pi.getPacketData();
			poLength += packetData.length;
			po.setPacketData(packetData);
			}
			
			po.setInPort(pi.getInPort());
			po.setLength(poLength);
			
			try {
				messageDamper.write(sw, po, cntx);
				if(doFlush){
					sw.flush();
				}
			
			} catch (IOException e) {
			}
			}    
    
    
    
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
	    Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
	    l.add(INCTUForwardService.class);
	    return l;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
	    Map<Class<? extends IFloodlightService>, IFloodlightService> m = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
	    m.put(INCTUForwardService.class, this);
	    return m;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IDeviceService.class);
		l.add(IRoutingService.class);
		l.add(IRestApiService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		floodlightProvider = 
				context.getServiceImpl(IFloodlightProviderService.class);
		deviceManager = 
				context.getServiceImpl(IDeviceService.class);
		routingEngine = 
				context.getServiceImpl(IRoutingService.class);
        messageDamper = new OFMessageDamper(10000,
                EnumSet.of(OFType.FLOW_MOD),
                25);
        restApi = 
                context.getServiceImpl(IRestApiService.class);
        
        targetIPstring = "10.0.0.1";
        onIpChanged();

	}
	
	private void cleanFlowEntries(){
	    Map<Long, IOFSwitch> switches = floodlightProvider.getAllSwitchMap();
	    System.out.printf("$$$ Cleaning Flow entry..\n");
	    OFMatch match = new OFMatch();
	    match.setWildcards(OFMatch.OFPFW_ALL);
	    for (Entry<Long, IOFSwitch> entry : switches.entrySet()){
	        
	        IOFSwitch sw = entry.getValue();
	        System.out.printf("$$$ Work on SW %s\n", sw.getId());
	        for (int cookie=1001;cookie<=1002;cookie++){
	            // send flow_mod delete
	            OFFlowMod fm = (OFFlowMod) floodlightProvider
	               .getOFMessageFactory().getMessage(OFType.FLOW_MOD);
	            
	            fm.setCommand(OFFlowMod.OFPFC_DELETE); 
	            fm.setCookie((long)cookie);
	            fm.setMatch(match);
                fm.setLengthU(OFFlowMod.MINIMUM_LENGTH);
	            try {
	                System.out.printf("$$$ clear cookie %d for %s\n", cookie,sw.getId());
                    messageDamper.write(sw,fm,null,true);
                }
                catch (IOException e) {
                    System.out.printf("$$$ FAIL to clear cookie %d for %s\n", cookie,sw.getId());
                }
	        }
            
	    }
	    
	    
	}
	
	private void onIpChanged(){
	    targetIP = IPv4.toIPv4Address(targetIPstring);
	    cleanFlowEntries();
	}

	@Override
	public void startUp(FloodlightModuleContext context)
			throws FloodlightModuleException {
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this); 
		restApi.addRestletRoutable(new NCTUForwardWebRoutable());
		
	}

    @Override
    public String setTargetIP(String target) {
        String ret = new String(targetIPstring);
        if(!target.equals(targetIPstring)){
            targetIPstring = target;
            System.out.printf("$$$ IP Change to %s\n",target);
            onIpChanged();
        }
        else{
            System.out.printf("$$$ IP Unchanged\n");
        }
        
        
        return ret;
    }
    
    @Override
    public String getTargetIP() {
        return targetIPstring;
    }

}
