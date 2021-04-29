#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>


int main() {

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

    return 0;
}