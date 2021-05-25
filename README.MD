# PARTIE CLIENT  
Cette partie a été réalisée exclusivement en C, son déroulement consiste à avoir un thread principal qui se connecte au gestionnaire et reçoit la liste des diffuseurs disponibles.  
Une  fois la liste reçue, et le diffuseur choisi, on lance 2 threads :  

*  Le thread **lecture_term** s'occupe de se connecter au port TCP du diffuseur, et se met en lecture de commande ( M pour MESS ou L pour LAST) à envoyer.  

*  Le thread **ecriture_term** quant à lui s'occupe de recevoir les messages envoyés par le diffuseur.  

Pour lancer le client, il faut d'abord qu'un gestionnaire soit lancé et ouvrir un terminal qui servira à recevoir les messages du diffuseur, puis taper les commandes :   
1.  **make** client_final.
2.  **./client_final** /dev/pts/x adresse_ip_gestionnaire port_gestionnaire.


# PARTIE DIFFUSEUR  
Le diffuseur est coder en JAVA il run 3 thread principeau pour la diffusion, le seveur tcp  et la connexion gestionnaire.
## Extension : Multi Gestionaire
Une extension implemente qui permet au diffuseur de se connecter avec plusieur gestionnaires . 
## how to run 
`cd Diffuseur/src `
`javac **/*.java`
`javac app.java`
`java app`
# PARTIE GESTIONNAIRE  
Le gestionnaire a été écrit en Java et son fonctionnement est le suivant :
- On écoute les connexions entrantes et on les maintient dans un object Client
- On traite les paquets entrants des clients et on renvoie les réponses associées aux requêtes à celui-ci
- Les clients pourront s'enregistrer en tant que diffuseur.
- Les diffuseurs reçoivent des messages pour vérifier si ils sont toujours en vie du type "RUOK" toutes les 5 secondes auxquels ils doivent répondre par "IMOK", si celui-ci ne répond pas au bout de 20 secondes, il est déconnecté.

Le gestionnaire tournera sur le port 4242 si il n'y a pas de fichier "Config.txt" à la racine. Sinon, il prendra le port situé dans celui-ci.
Le fichier Config.txt doit uniquement contenir le port sur 4 caractères.
## how to run 
`cd Gestionnaire `
`javac *.java`
`java Manager`
