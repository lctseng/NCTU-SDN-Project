cmd_/home/floodlight/floodlight/openvswitch-2.3.0/datapath/linux/flow_dissector.o := gcc -Wp,-MD,/home/floodlight/floodlight/openvswitch-2.3.0/datapath/linux/.flow_dissector.o.d  -nostdinc -isystem /usr/lib/gcc/x86_64-linux-gnu/4.8/include -I/home/floodlight/floodlight/openvswitch-2.3.0/include -I/home/floodlight/floodlight/openvswitch-2.3.0/datapath/linux/compat -I/home/floodlight/floodlight/openvswitch-2.3.0/datapath/linux/compat/include  -I/usr/src/linux-headers-3.11.0-12-generic/arch/x86/include -Iarch/x86/include/generated  -Iinclude -I/usr/src/linux-headers-3.11.0-12-generic/arch/x86/include/uapi -Iarch/x86/include/generated/uapi -I/usr/src/linux-headers-3.11.0-12-generic/include/uapi -Iinclude/generated/uapi -include /usr/src/linux-headers-3.11.0-12-generic/include/linux/kconfig.h -Iubuntu/include  -D__KERNEL__ -Wall -Wundef -Wstrict-prototypes -Wno-trigraphs -fno-strict-aliasing -fno-common -Werror-implicit-function-declaration -Wno-format-security -fno-delete-null-pointer-checks -O2 -m64 -mno-sse -mpreferred-stack-boundary=3 -mtune=generic -mno-red-zone -mcmodel=kernel -funit-at-a-time -maccumulate-outgoing-args -fstack-protector -DCONFIG_X86_X32_ABI -DCONFIG_AS_CFI=1 -DCONFIG_AS_CFI_SIGNAL_FRAME=1 -DCONFIG_AS_CFI_SECTIONS=1 -DCONFIG_AS_FXSAVEQ=1 -DCONFIG_AS_AVX=1 -DCONFIG_AS_AVX2=1 -pipe -Wno-sign-compare -fno-asynchronous-unwind-tables -mno-sse -mno-mmx -mno-sse2 -mno-3dnow -mno-avx -Wframe-larger-than=1024 -Wno-unused-but-set-variable -fno-omit-frame-pointer -fno-optimize-sibling-calls -pg -mfentry -DCC_USING_FENTRY -Wdeclaration-after-statement -Wno-pointer-sign -fno-strict-overflow -fconserve-stack -DCC_HAVE_ASM_GOTO -DVERSION=\"2.3.0\" -I/home/floodlight/floodlight/openvswitch-2.3.0/datapath/linux/.. -I/home/floodlight/floodlight/openvswitch-2.3.0/datapath/linux/.. -g -include /home/floodlight/floodlight/openvswitch-2.3.0/datapath/linux/kcompat.h  -DMODULE  -D"KBUILD_STR(s)=\#s" -D"KBUILD_BASENAME=KBUILD_STR(flow_dissector)"  -D"KBUILD_MODNAME=KBUILD_STR(openvswitch)" -c -o /home/floodlight/floodlight/openvswitch-2.3.0/datapath/linux/.tmp_flow_dissector.o /home/floodlight/floodlight/openvswitch-2.3.0/datapath/linux/flow_dissector.c

source_/home/floodlight/floodlight/openvswitch-2.3.0/datapath/linux/flow_dissector.o := /home/floodlight/floodlight/openvswitch-2.3.0/datapath/linux/flow_dissector.c

deps_/home/floodlight/floodlight/openvswitch-2.3.0/datapath/linux/flow_dissector.o := \
  /home/floodlight/floodlight/openvswitch-2.3.0/datapath/linux/kcompat.h \
  include/generated/uapi/linux/version.h \

/home/floodlight/floodlight/openvswitch-2.3.0/datapath/linux/flow_dissector.o: $(deps_/home/floodlight/floodlight/openvswitch-2.3.0/datapath/linux/flow_dissector.o)

$(deps_/home/floodlight/floodlight/openvswitch-2.3.0/datapath/linux/flow_dissector.o):
