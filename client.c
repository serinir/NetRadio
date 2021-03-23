#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <netdb.h>

int main() {

  int sock=socket(PF_INET, SOCK_DGRAM, 0);

  struct sockaddr_in address_sock;
  address_sock.sin_family=AF_INET;
  // Numero 5555 temporaire où le client écoute reponse du gestionnaire
  address_sock.sin_port=htons(5555); 
  address_sock.sin_addr.s_addr=htonl(INADDR_ANY);

  int rbind =bind(sock,(struct sockaddr *)&address_sock,sizeof(struct 
                                sockaddr_in));
  struct sockaddr_in emet;
  socklen_t a=sizeof(emet);

  struct addrinfo *first_info;

  struct addrinfo hints;
  memset(&hints, 0, sizeof(struct addrinfo));

  hints.ai_family = AF_INET;
  hints.ai_socktype=SOCK_DGRAM;

  int rgai = getaddrinfo("localhost", "5555" ,&hints,&first_info); // Recherche de gestionnaire

  if(rgai==0 && rbind == 0){
    if(first_info!=NULL){
        struct sockaddr *saddr=first_info->ai_addr;
        char cmdList[5];
        char reponseGest[100000]; // Numero arbitraire, le temps de calculer vraie valeur de la taille de la liste des diffuseurs
        strcpy(cmdList, "LIST");
        sendto(sock, cmdList, strlen(cmdList), 0, saddr, 
                    (socklen_t) sizeof(struct sockaddr_in));

        int rec=recvfrom(sock, reponseGest, 100000, 0, (struct sockaddr *)&emet, &a);
        reponseGest[rec]='\0';

    }
  }
  return 0;
}