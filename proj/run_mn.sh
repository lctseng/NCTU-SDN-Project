#!/bin/sh
sudo mn --controller=remote,ip=192.168.234.134 --link=tc --custom /home/floodlight/mininet/custom/topo_sdn.py --topo=$1
