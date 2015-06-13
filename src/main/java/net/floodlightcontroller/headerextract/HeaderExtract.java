package net.floodlightcontroller.headerextract;

import java.util.*;

import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFType;
import org.openflow.util.HexString;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.IPv4;

public class HeaderExtract implements IOFMessageListener, IFloodlightModule {

	
	public final int DEFAULT_CACHE_SIZE = 10; 
	private IFloodlightProviderService floodlightProvider;
	
	@Override
	public String getName() {
		return "HeaderExtract";
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		return false;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		return l;

	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		floodlightProvider = 
		context.getServiceImpl(IFloodlightProviderService.class); 
	}

	@Override
	public void startUp(FloodlightModuleContext context)
			throws FloodlightModuleException {
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this); 

	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		OFPacketIn pin = (OFPacketIn) msg; 
		OFMatch match = new OFMatch(); 
		match.loadFromPacket(pin.getPacketData(), pin.getInPort()); 
		System.out.println("$$$$$-Get the Destination IP Address-$$$$$");      
		System.out.println(IPv4.fromIPv4Address(match.getNetworkDestination())); 
		System.out.println("$$$$$-Mac Address Destination-$$$$$$"); ; 
		System.out.println(HexString.toHexString(match.getDataLayerDestination()));
		System.out.println("$$$$$-PacketIn ARRAY-$$$$$"); 
		System.out.println(Arrays.asList(match)); 
		return Command.CONTINUE;
	}

}
