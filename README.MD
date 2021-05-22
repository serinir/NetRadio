# PARTIE CLIENT  
Cette partie a été réalisée exclusivement en C, son déroulement consiste à avoir un thread principal qui se connecte au gestionnaire et reçoit la liste des diffuseurs disponibles.  
Une  fois la liste reçue, et le diffuseur choisi, on lance 2 threads :  

*  Le thread **lecture_term** s'occupe de se connecter au port TCP du diffuseur, et se met en lecture de commande ( M pour MESS ou L pour LAST) à envoyer.  

*  Le thread **ecriture_term** quant à lui s'occupe de recevoir les messages envoyés par le diffuseur.  


# PARTIE DIFFUSEUR  
Je suis une joli diffuseur écrit en JAVA.
# PARTIE GESTIONNAIRE  
Je suis un joli gestionnaire écrit en JAVA.