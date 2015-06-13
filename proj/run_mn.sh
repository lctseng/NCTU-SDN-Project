#!/bin/sh
sudo mn --controller=remote,ip=127.0.0.1 --link=tc --custom /home/floodlight/NCTU-SDN-Project/proj/topo_sdn.py --topo=$1
