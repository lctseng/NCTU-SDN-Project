#!/usr/bin/env python

"""Custom topology example

Two directly connected switches plus a host for each switch:

   host --- switch --- switch --- host

Adding the 'topos' dict with a key/value pair to generate our newly defined
topology enables one to pass in '--topo=mytopo' from the command line.
"""

from mininet.topo import Topo
from mininet.net import Mininet
from mininet.link import TCLink
from mininet.util import dumpNodeConnections
from mininet.log import setLogLevel
from mininet.node import RemoteController

class FatTreeTopo( Topo ):
    CORE_LOSS = 5
    "Simple topology example."


    def __init__( self ):
        "Create custom topo."

        # Initialize topology
        Topo.__init__( self )

        sw_core = dict()
        # Create core switches
        for sid in range(1001,1004+1):
            name = str(sid)
            sw_core[name] = self.addSwitch(name)

        # Hosts
        host_index = 1
        hosts = [None]
        # For each pod
        pods = dict()
        for pid in range(0,3+1):
            pods[pid] = dict()
            
            # Lv2 switch
            pods[pid]['sw_l2'] = dict()
            for l2_id in range(pid*2+1,pid*2+2+1):
                name = "2%03d" % l2_id
                pods[pid]['sw_l2'][name] = self.addSwitch(name)
            # connect L2 to Core
            # left
            name = "2%03d" % (pid*2+1)
            sw = pods[pid]['sw_l2'][name]
            self.addLink(sw,sw_core['1001'],bw=1000,loss=self.CORE_LOSS)
            self.addLink(sw,sw_core['1002'],bw=1000,loss=self.CORE_LOSS)
            # right
            name = "2%03d" % (pid*2+2)
            sw = pods[pid]['sw_l2'][name]
            self.addLink(sw,sw_core['1003'],bw=1000,loss=self.CORE_LOSS)
            self.addLink(sw,sw_core['1004'],bw=1000,loss=self.CORE_LOSS)

            # Lv3 switch
            pods[pid]['sw_l3'] = dict()
            for l3_id in range(pid*2+1,pid*2+2+1):
                name = "3%03d" % l3_id
                new_sw = pods[pid]['sw_l3'][name] = self.addSwitch(name)
                # connect L3 to L2
                for l2_sw in pods[pid]['sw_l2'].values():
                    self.addLink(new_sw,l2_sw,bw=100)
                # Two hosts for each L3
                for i in range(0,2):
                    h_name = "h%d" % host_index
                    host_index += 1
                    h = self.addHost(h_name)
                    hosts.append(h)
                    # Link it
                    self.addLink(h,new_sw,bw=100)


BW_NEAR=100
BW_CORE=100
BW_LOWER=70

class ProjTopo1( Topo ):

    def __init__( self ):
        "Create custom topo."

        # Initialize topology
        Topo.__init__( self )

        # switches
        s1 = self.addSwitch("s1")
        s2 = self.addSwitch("s2")
        s3 = self.addSwitch("s3")

        # Hosts
        h1 = self.addHost("h1")
        h2 = self.addHost("h2")

        # Links
        self.addLink(h1,s1,bw=BW_NEAR)
        self.addLink(h2,s3,bw=BW_NEAR)

        
        self.addLink(s1,s2,bw=BW_CORE)
        self.addLink(s2,s3,bw=BW_CORE)
class ProjTopo2( Topo ):

    def __init__( self ):
        "Create custom topo."

        # Initialize topology
        Topo.__init__( self )

        # switches
        s1 = self.addSwitch("s1")
        s2 = self.addSwitch("s2")
        s3 = self.addSwitch("s3")
        s4 = self.addSwitch("s4")

        # Hosts
        h1 = self.addHost("h1")
        h2 = self.addHost("h2")
        h3 = self.addHost("h3")

        # Links
        self.addLink(h1,s1,bw=BW_NEAR)
        self.addLink(h2,s1,bw=BW_NEAR)
        
        # Path 2
        self.addLink(s1,s4,bw=BW_CORE)
        self.addLink(s4,h3,bw=BW_CORE)
        # Path1
        self.addLink(s1,s2,bw=BW_CORE)
        self.addLink(s2,s3,bw=BW_CORE)
        self.addLink(s3,h3,bw=BW_CORE)
        
class ProjTopo3( Topo ):

    def __init__( self ):
        "Create custom topo."

        # Initialize topology
        Topo.__init__( self )

        # switches
        s1 = self.addSwitch("s1")
        s2 = self.addSwitch("s2")
        s3 = self.addSwitch("s3")

        # Hosts
        h1 = self.addHost("h1")
        h2 = self.addHost("h2")
        h3 = self.addHost("h3")
        h4 = self.addHost("h4")

        # Links
        self.addLink(h1,s1,bw=BW_NEAR)
        self.addLink(h2,s1,bw=BW_NEAR)
        self.addLink(h3,s3,bw=BW_NEAR)
        self.addLink(h4,s3,bw=BW_NEAR)
        
        # Path1
        self.addLink(s1,s2,bw=BW_LOWER)
        self.addLink(s2,s3,bw=BW_LOWER)
        # Path 2
        self.addLink(s1,s3,bw=BW_CORE)
        


class ProjTopo4( Topo ):

    def __init__( self ):
        "Create custom topo."

        # Initialize topology
        Topo.__init__( self )

        # switches
        s1 = self.addSwitch("s1")
        s2 = self.addSwitch("s2")
        s3 = self.addSwitch("s3")
        s4 = self.addSwitch("s4")

        # Hosts
        h1 = self.addHost("h1")
        h2 = self.addHost("h2")
        h3 = self.addHost("h3")
        h4 = self.addHost("h4")

        # Links
        self.addLink(h1,s1,bw=BW_NEAR)
        self.addLink(h2,s1,bw=BW_NEAR)
        self.addLink(h3,s4,bw=BW_NEAR)
        self.addLink(h4,s4,bw=BW_NEAR)
        
        # Path1
        self.addLink(s1,s2,bw=BW_CORE,loss=0)
        self.addLink(s2,s3,bw=BW_CORE,loss=0)
        self.addLink(s3,s4,bw=BW_CORE,loss=0)
        # Path 2
        self.addLink(s1,s3,bw=BW_CORE,loss=0)
        self.addLink(s3,s4,bw=BW_CORE,loss=0)


class ProjTopo5( Topo ):

    def __init__( self ):
        "Create custom topo."

        # Initialize topology
        Topo.__init__( self )

        # switches
        s1 = self.addSwitch("s1")
        s2 = self.addSwitch("s2")
        s3 = self.addSwitch("s3")

        # Hosts
        h1 = self.addHost("h1")
        h2 = self.addHost("h2")
        h3 = self.addHost("h3")
        h4 = self.addHost("h4")
        h5 = self.addHost("h5")
        h6 = self.addHost("h6")
        h7 = self.addHost("h7")
        h8 = self.addHost("h8")

        # Links
        self.addLink(h1,s1,bw=BW_NEAR)
        self.addLink(h2,s1,bw=BW_NEAR)
        self.addLink(h3,s1,bw=BW_NEAR)
        self.addLink(h4,s1,bw=BW_NEAR)
        self.addLink(h5,s3,bw=BW_NEAR)
        self.addLink(h6,s3,bw=BW_NEAR)
        self.addLink(h7,s3,bw=BW_NEAR)
        self.addLink(h8,s3,bw=BW_NEAR)
        
        # Path1
        self.addLink(s1,s2,bw=BW_LOWER)
        self.addLink(s2,s3,bw=BW_LOWER)
        # Path 2
        self.addLink(s1,s3,bw=BW_CORE)

topos = { 'ftree': ( lambda: FatTreeTopo() ), 
          'prj1': ( lambda: ProjTopo1() ), 
          'prj2': ( lambda: ProjTopo2() ), 
          'prj3': ( lambda: ProjTopo3() ),
          'prj4': ( lambda: ProjTopo4() ),
          'prj5': ( lambda: ProjTopo5() ) 
        }
"""
def perfTest():
    topo = MyTopo()
    net = Mininet(topo=topo,link=TCLink,controller=None)
    net.addController('c0',controller=RemoteController,ip='127.0.0.1')
    net.start()

    dumpNodeConnections(net.hosts)
    
    
    h1, h2, h16 = net.get('h1','h2','h16')
    
    #net.pingFull(hosts=[h1,h2,h16])
    net.pingAllFull()

    h1.popen('iperf -s -u -i 1',shell=True)
    h16.popen('iperf -s -u -i 1',shell=True)
    h2.cmdPrint("iperf -c %s -u -t 10 -i 1 -b 100m" % h1.IP())
    h2.cmdPrint("iperf -c %s -u -t 10 -i 1 -b 100m" % h16.IP())

    net.stop()
H




if __name__ == '__main__':
    setLogLevel('info')
    perfTest()

"""


