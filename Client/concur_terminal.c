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

int main() {

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

    char id[100];

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

    return NULL;

}

void * ecriture_term(void * arg) {

    int fd = open("/dev/pts/3", O_RDWR);

    if(fd==-1) {
        perror("open");
        exit(1);
    }

    char * mess = "Hello\n";
    for(int i=0; i<10; i++) {
        write(fd, mess, strlen(mess));
    }
    

    close(fd);
    return NULL;
}