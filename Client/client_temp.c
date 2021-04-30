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

void * lecture_term(void * arg);
void * ecriture_term(void * arg);

char id[100];

int main() {

    printf("Rentrez votre pseudo :\n");

    int r=0;

    while(1) {
        r = read(0, id, 100); // Retourne taille de l'input + 1
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

    int rep1 = pthread_create(&th1, NULL, lecture_term, NULL);
    int rep2 = pthread_create(&th2, NULL, ecriture_term, NULL);
    pthread_join(th1, NULL);
    pthread_join(th2, NULL);


    if(rep1 != 0 || rep2 != 0) {
        printf("Problème création thread\n");
        exit(0);
    }

    return 0;
}

void * lecture_term(void * arg) {

    char cmd[100];

    char rep[5];

    struct sockaddr_in adress_sock;
    adress_sock.sin_family = AF_INET;
    adress_sock.sin_port = htons(4242);
    inet_aton("127.0.0.1", &adress_sock.sin_addr);

    int sock = socket(PF_INET, SOCK_STREAM, 0);

    int r = connect(sock, (struct sockaddr *) &adress_sock, sizeof(struct sockaddr_in));

    if(r != -1) {
        while(1) {
            read(0, cmd, 100);
            printf("You're %s and you want to transmit this : %s\n", id, cmd);

            send(sock, cmd, strlen(cmd)*sizeof(char), 0);
            int size_rec = recv(sock, rep, 5*sizeof(char), 0);
            rep[size_rec] = '\0';
        }

        close(sock);
    }

    return NULL;

}

void * ecriture_term(void * arg) {

    int fd = open("/dev/pts/3", O_RDWR);

    if(fd==-1) {
        perror("open");
        exit(1);
    }

    int sock=socket(PF_INET,SOCK_DGRAM,0);

    int ok=1;
    int r=setsockopt(sock, SOL_SOCKET, SO_REUSEPORT, &ok, sizeof(ok)); // PORT REUTILISABLE, DESFOIS ON MET SO_REUSEADDR ( pour le projet notamment )

    struct sockaddr_in address_sock;
    address_sock.sin_family=AF_INET;
    address_sock.sin_port=htons(9999);
    address_sock.sin_addr.s_addr=htonl(INADDR_ANY);

    r=bind(sock, (struct sockaddr *)&address_sock, 
        sizeof(struct sockaddr_in));

    struct ip_mreq mreq;
    mreq.imr_multiaddr.s_addr=inet_addr("225.1.2.4");
    mreq.imr_interface.s_addr=htonl(INADDR_ANY);

    r=setsockopt(sock, IPPROTO_IP, 
        IP_ADD_MEMBERSHIP, &mreq, sizeof(mreq)); // On abonne la socket

    if (r==0) {
        char tampon[100];
        
        while(1){
            int rec=recv(sock, tampon, 100, 0);
            tampon[rec]='\0';
            write(fd, tampon,strlen(tampon));
        }   
    }
    

    close(fd);
    return NULL;
}