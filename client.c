#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <netdb.h>

int main() {

    struct addrinfo *first_info;

    struct addrinfo hints;
    memset(&hints, 0, sizeof(struct addrinfo));

    hints.ai_family = AF_INET;
    hints.ai_socktype=SOCK_STREAM;

    // Recherche de gestionnaire, 5555 numéro temporaire
    int rgai = getaddrinfo("localhost", "5555" ,&hints, &first_info); 

    if(rgai==0 ){
        if(first_info!=NULL){
            struct sockaddr_in *address_in = (struct sockaddr_in *) first_info->ai_addr;
            // Numero 5555 temporaire où le client communique gestionnaire
            address_in->sin_port = htons(5555);
            int sock=socket(PF_INET, SOCK_STREAM, 0);
            int r2 = connect(sock, (struct sockaddr*) address_in, sizeof(struct sockaddr_in));

            if(r2 != -1) {
                char cmdList[5];
                char reponseGest[100000]; // Numero arbitraire, le temps de calculer vraie valeur de la taille de la liste des diffuseurs
                
                strcpy(cmdList, "LIST");
                send(sock, cmdList, strlen(cmdList) * sizeof(char), 0);

                int rec = recv(sock, reponseGest, 100000 * sizeof(char), 0);
                reponseGest[rec] = '\0';

                close(sock);
            }

        }
    }
    return 0;
}