#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <netdb.h>

#define LEN_ITEM 55
#define LEN_LINB 7
#define LEN_OLDM 25

void * chat_diffuseur(void *arg);
void * chat_gestionnaire(void *arg);

struct info_client {
    int sock;
    char * id;
};

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
                char nb_diff[3];
                
                strncpy(reponseGest+4, nb_diff, 2);
                nb_diff[2] = '\0';

                // TABLEAU DIFF
                char * tab_diff[strlen(reponseGest)/LEN_ITEM + 2];
                for(int i=0; i<atoi(nb_diff); i++) {
                    tab_diff[i] = strndup(reponseGest+LEN_LINB+LEN_ITEM*i, LEN_ITEM);
                }

                // FIN TABLEAU DIFF
                

                memset(nb_diff, 0, 3);

                printf("Quel diffuseur voulez-vous écouter ?\n");
                read(0, nb_diff, 2); // Lecture du numéro du diffuseur à écouter

                int sock_diff=socket(PF_INET, SOCK_DGRAM, 0);

                int ok=1;
                int r=setsockopt(sock_diff, SOL_SOCKET, SO_REUSEPORT, &ok, sizeof(ok)); // PORT REUTILISABLE, DESFOIS ON MET SO_REUSEADDR ( pour le projet notamment )

                struct sockaddr_in address_sock;
                address_sock.sin_family = AF_INET;
                address_sock.sin_port = htons(9999);
                address_sock.sin_addr.s_addr = htonl(INADDR_ANY);

                r=bind(sock_diff, (struct sockaddr *)&address_sock, 
                    sizeof(struct sockaddr_in));

                struct ip_mreq mreq;
                mreq.imr_multiaddr.s_addr=inet_addr("225.1.2.4");
                mreq.imr_interface.s_addr=htonl(INADDR_ANY);

                r = setsockopt(sock_diff, IPPROTO_IP, 
                    IP_ADD_MEMBERSHIP, &mreq, sizeof(mreq)); // On abonne la socket

                if (r==0) {
                    char tampon[100];
                    
                    while(1){
                        int rec = recv(sock_diff, tampon, 100, 0);
                        tampon[rec] = '\0';
                        printf("Message recu : %s\n",tampon);
                    }   
                }



                close(sock);
            }

        }
    }
    return 0;
}

void * chat_diffuseur(void * arg){
    struct info_client * cli = (struct info_client *)arg;

    int so = cli->sock;
    char * id = cli->id;

    struct sockaddr_in adress_sock;
    adress_sock.sin_family = AF_INET;
    adress_sock.sin_port = htons(4242);
    inet_aton("127.0.0.1",&adress_sock.sin_addr);

    int r=connect(so, (struct sockaddr *)&adress_sock,
                sizeof(struct sockaddr_in));

    if(r!=-1){
        char choice[3];
        char buffMess[160];
        char Mess[140];

        printf("Press S to send a message and R ro receive the last ones\n");
        read(0, choice, 3);
        if(strcmp(choice, "S")==0) {
            sprintf(buffMess, "MESS %s", id);

            int tailleMess = read(0, Mess, 140);
            Mess[tailleMess] = '\0';

            strcat(buffMess, Mess);
            strcat(buffMess, "\r\n");

            send(so, buffMess, strlen(buffMess)*sizeof(char), 0);
            memset(buffMess, 0, strlen(buffMess));

            int size_rec=recv(so, buffMess, 6*sizeof(char),0);
            buffMess[size_rec]='\0';

            printf("Message : %s\n",buffMess);
        } else {
            if(strcmp(choice, "R")==0) {
                memset(choice, 0, strlen(choice));

                printf("Choose a number between 0 et 999\n");
                read(0, choice, 3);

                int nb_mess = atoi(choice);
                sprintf(buffMess, "LAST %d\r\n", nb_mess);

                memset(buffMess, 0, strlen(buffMess));

                int size_rec=recv(so, buffMess, nb_mess*LEN_OLDM*sizeof(char),0);
                buffMess[size_rec]='\0';
                printf("Les derniers messages sont: %s\n",buffMess);

                memset(buffMess, 0, strlen(buffMess));

                int size_rec=recv(so, buffMess, 6*sizeof(char),0);
                buffMess[size_rec]='\0';

                printf("Message : %s\n",buffMess);
            }
        }

    }
    
    free(arg);
    close(so);

    return NULL;
}