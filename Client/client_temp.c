#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <sys/socket.h>
#include <pthread.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#define LEN_OLDM 159
#define LEN_ITEM 55

void * chat_gestionnaire(void * arg);
void * lecture_term(void * arg);
void * ecriture_term(void * arg);

char ip1[17];
char port1[7];

char ip2[17];
char port2[7];

char id[12];

int main() {

    printf("Rentrez votre pseudo :\n");

    int r=0;

    while(1) {
        r = read(0, id, 12); // Retourne taille de l'input + 1
        if(r<=9) break;
    }

    id[r-1] = '\0'; 
    
    int tailleId = strlen(id);
    printf("Taille ID %d\n", tailleId);
    if(tailleId < 8) {
        for(int i=0; i< 8 - tailleId; i++) {
            strcat(id, "#");
        }
    }

    printf("Votre pseudo est : %s\n", id);



    pthread_t th1, th2;

    int rep1 = pthread_create(&th1, NULL, lecture_term, NULL); // Lancement de thread qui s'occupe de la communication avec le diffuseur
    int rep2 = pthread_create(&th2, NULL, ecriture_term, NULL); // Lancement du thread qui s'occupe d'écouter le diffuseur
    pthread_join(th1, NULL);
    pthread_join(th2, NULL);


    if(rep1 != 0 || rep2 != 0) {
        printf("Problème création thread\n");
        exit(0);
    }

    return 0;
}

void * lecture_term(void * arg) {

    char * mess = malloc(sizeof(char) * 150);
    char * nb_mess = malloc(sizeof(char) * 5);
    char * cmdToSend = malloc(sizeof(char) * 170);

    char rep[5];

    // CREATION SOCKET GESTIONNAIRE
    struct sockaddr_in adress_sock_gest;
    adress_sock_gest.sin_family = AF_INET;
    adress_sock_gest.sin_port = htons(5055);
    inet_aton("127.0.0.1", &adress_sock_gest.sin_addr);

    int sock_gest = socket(PF_INET, SOCK_STREAM, 0);

    int r_gest = connect(sock_gest, (struct sockaddr *) &adress_sock_gest, sizeof(struct sockaddr_in));

    if(r_gest != -1) {
        char * currentItem = malloc(LEN_ITEM * sizeof(char)+4);
        char * linb = malloc(15*sizeof(char));

        sprintf(cmdToSend, "LIST\r\n");

        send(sock_gest, cmdToSend, strlen(cmdToSend)*sizeof(char), 0);

        int size_rec_gest = 0;

        size_rec_gest = recv(sock_gest, linb, 12*sizeof(char), 0);
        printf("%s\n", linb);

        do {
            size_rec_gest = recv(sock_gest, currentItem, LEN_ITEM*sizeof(char)+15, 0); // Reception des OLD MESSAGES
            currentItem[size_rec_gest] = '\0';
            strncpy(ip1, currentItem+14, 15);
            strncpy(port1, currentItem+30, 4);
            strncpy(ip2, currentItem+35, 15);
            strncpy(port2, currentItem+51, 4);
            printf("%s\n", currentItem);
        } while(size_rec_gest != 0);

    }

    int sock_diff = socket(PF_INET, SOCK_STREAM, 0);

    // CREATION SOCKET DIFFUSEUR
    struct sockaddr_in adress_sock_diff;
    adress_sock_diff.sin_family = AF_INET;
    adress_sock_diff.sin_port = htons(atoi(port2));
    inet_aton(ip2, &adress_sock_diff.sin_addr);


    int r_diff = connect(sock_diff, (struct sockaddr *) &adress_sock_diff, sizeof(struct sockaddr_in));

    if(r_diff != -1) {

        int r=0;

        while(1) {
            while(1) {
                r = getchar();
                if(r=='M' || r=='L') break;
            }

            printf("---> Commande choisie : %c\n", r);

            if(r == 'M') { // Case M

                while(1) {
                    
                    r = read(0, mess, 150); // Retourne taille de l'input + 1
                    if(r<=141) break;
                }

                mess[r-1] = '\0'; 
                
                int tailleMess = strlen(mess);
                printf("Taille message %d\n", tailleMess);
                if(tailleMess < 140) {
                    for(int i=0; i< 140 - tailleMess; i++) {
                        strcat(mess, "#");
                    }
                }
                
    
                sprintf(cmdToSend, "MESS %s %s\r\n", id, mess);
                
                send(sock_diff, cmdToSend, strlen(cmdToSend)*sizeof(char), 0); // Envoie de MESS id message
                int size_rec = recv(sock_diff, rep, 5*sizeof(char), 0); // Reception de ACKM
                rep[size_rec] = '\0';
                printf("%s\n", rep);

            } else { // Case L  
                while(1) {
                    r = read(0, nb_mess, 5); // Retourne taille de l'input + a
                    if(r<=4) break;
                }

                nb_mess[r-1] = '\0';

                char temp;

                int tailleNbMess = strlen(nb_mess);
                if(tailleNbMess < 3) {
                    for(int i=0; i< 3 - tailleNbMess; i++) {
                        strcat(nb_mess, "0");
                    }

                    temp = nb_mess[0];
                    nb_mess[0] = nb_mess[2];
                    nb_mess[2] = temp;
                }


                char * currentOldM = malloc(LEN_OLDM * sizeof(char)+4);

                
                printf("Vous allez recevoir au plus %d messages\n", atoi(nb_mess));

                sprintf(cmdToSend, "LAST %s\r\n", nb_mess);

                send(sock_diff, cmdToSend, strlen(cmdToSend)*sizeof(char), 0);

                int size_rec = 0;

                do {
                    size_rec = recv(sock_diff, currentOldM, LEN_OLDM*sizeof(char)+15, 0); // Reception des OLD MESSAGES
                    currentOldM[size_rec] = '\0';
                    printf("%s\n", currentOldM);
                } while(size_rec != 0);

                
            }
        }
        close(sock_diff);
        close(sock_gest);
    }
            
    return NULL;
}


void * ecriture_term(void * arg) {

    int fd = open("/dev/pts/4", O_RDWR);

    if(fd==-1) {
        perror("open");
        exit(1);
    }

    int sock=socket(PF_INET, SOCK_DGRAM, 0);

    int ok=1;
    int r=setsockopt(sock, SOL_SOCKET, SO_REUSEPORT, &ok, sizeof(ok)); // PORT REUTILISABLE, DESFOIS ON MET SO_REUSEADDR ( pour le projet notamment )

    struct sockaddr_in address_sock;
    address_sock.sin_family=AF_INET;
    address_sock.sin_port=htons(5000); // port de multidiffusion
    address_sock.sin_addr.s_addr=htonl(INADDR_ANY);

    r=bind(sock, (struct sockaddr *)&address_sock, 
        sizeof(struct sockaddr_in));

    struct ip_mreq mreq;
    mreq.imr_multiaddr.s_addr=inet_addr("225.10.20.30"); // Adresse de multidiffusion
    mreq.imr_interface.s_addr=htonl(INADDR_ANY);

    r=setsockopt(sock, IPPROTO_IP, 
        IP_ADD_MEMBERSHIP, &mreq, sizeof(mreq)); // On abonne la socket

    if (r==0) {
        char tampon[100];
        
        while(1){
            int rec=recv(sock, tampon, 100, 0);
            tampon[rec]='\0';
            write(fd, tampon,strlen(tampon));
            write(fd, "\n", 2);
        }   
    }
    

    close(fd);
    return NULL;
}