/***  UDP Server(udpserver.c)
 *
 * 利用 socket 介面設計網路應用程式
 * 程式啟動後等待 client 端連線，連線後印出對方之 IP 位址
 * 並顯示對方所傳遞之訊息，並回送給 Client 端。
 *
 */
#include <stdio.h>
#include <stdlib.h>
#include <strings.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <sys/errno.h>
#include <arpa/inet.h>

#define SERV_PORT 5134

#define MAXNAME 1024

extern int errno;

int main()
{
    int socket_fd;   /* file description into transport */
    //int recfd; /* file descriptor to accept        */
    socklen_t length; /* length of address structure      */
    int nbytes; /* the number of read **/
    char buf[BUFSIZ];
    struct sockaddr_in myaddr; /* address of this service */
    struct sockaddr_in client_addr; /* address of client    */
    /*
     *      Get a socket into UDP/IP
     */
    if ((socket_fd = socket(AF_INET, SOCK_DGRAM, 0)) <0)
    {
        perror ("socket failed");
        exit(1);
    }
    /*
     *    Set up our address
     */
    bzero ((char *)&myaddr, sizeof(myaddr));
    myaddr.sin_family = AF_INET;
    myaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    myaddr.sin_port = htons(SERV_PORT);

    /*
     *     Bind to the address to which the service will be offered
     */
    if (bind(socket_fd, (struct sockaddr *)&myaddr, sizeof(myaddr)) <0)
    {
        perror ("bind failed\n");
        exit(1);
    }

    /*
     * Loop continuously, waiting for datagrams
     * and response a message
     */
    length = sizeof(client_addr);
    printf("Server is ready to receive !!\n");
    printf("Can strike Cntrl-c to stop Server >>\n");

    while (1)
    {
        nbytes = recvfrom(socket_fd, &buf, MAXNAME, 0, (struct sockaddr*)&client_addr, &length);
        if (nbytes <0)
        {
            perror ("could not read datagram!!");
            continue;
        }

        printf("Received data form %s : %d\n", inet_ntoa(client_addr.sin_addr), htons(client_addr.sin_port));
        printf("%s\n", buf);

       
        /* return to client */
        /*
        if (sendto(socket_fd, &buf, nbytes, 0, (struct sockaddr*)&client_addr, length) < 0)
        {
            perror("Could not send datagram!!\n");
            continue;
        }
        */
        printf("Can Strike Crtl-c to stop Server >>\n");
    }
    return 0;
}
