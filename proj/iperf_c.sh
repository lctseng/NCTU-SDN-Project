#!/bin/sh
iperf -c $1 -t 20 -i 1
