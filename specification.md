# Les routes

Les routes relient deux villes (le départ et l’arrivée) en 
première instance. Elles ont juste besoin d’une longueur.

En troisième instance, elles pourraient être plus ou moins propices aux accidents
(un accident est provoqué avec une certaine probabilité et détruit une locomotive 
en plus de faire baisser la réputation du joueur).
et abîmer plus ou moins les locomotives et les wagons.

La distance d’une route correspond au nombre de *frames* qu’il faudra pour la 
parcourir avec une vitesse de un.

```
Class Route
   Attributs
      longueur : la longueur de la route
      départ : la ville de départ
      arrivée : la ville de fin
      
   Méthodes 
```

# Les voyages

Un voyage est composé d’un train, d’une liste de route à emprunter, d’une ville,
de la route actuelle et de la distance déjà parcourue sur cette route. 

Il a également un état `ON_ROAD` ou `WAITING`, `WAITING` signifiant qu’il est 
sur une ville et attend des passagers et une liste de billets.

```
Class Voyage
   Attributs
      possesseur : le possesseur du voyage
      train : le train du voyage
      liste_routes : la liste des routes à emprunter
      ville : la ville actuelle
      route : la route actuelle
      distance_parcourue : la distance parcourue sur la route actuelle
      liste_places : liste de places pour ce voyage
      état : ON_ROAD, ARRIVAL ou WAITING      
      
   Méthodes
      def update
         Si ON_ROAD
            distance_parcourue += train.vitesse
            Si distance_parcourure >= route.distance
               distance_parcourure = 0
               état = ARRIVAL
            Fin Si
         Sinon si ARRIVAL
            train.détériorer(route)
            état = WAITING
            liste_routes = tl(liste_routes)
            route = hd(liste_routes)
         Sinon /* état = WAITING */  
            état = ON_ROAD
            ville = route.destination
         Fin Si

      def places_disponibles
          [place de liste_places où place.disponible]
          
      def est_terminé
         liste_routes = []
        
      def destination
         Si est_terminé
            Lever exception
         hd(listes_routes).arrivée
```

# Les PNJs

Un PNJ est juste une donnée et n’est jamais vu. Il a un statut `RICHE`, `PAUVRE`, 
`AISÉ` et un état, `WAITING`, `ON_ROAD`, `SETTLED`, qui permet de savoir quelle
action il peut entreprendre et une ville correspondant à celle où il est.

Un PNJ peut compter comme un groupe de plusieurs personnes (cela permettra avec 
1000 PNJ par exemple d'en simuler 1 000 000).

## Comment choisit-il sa destination

Un PNJ a une probabilité de migrer qui dépend de la note de sa ville. Il migre 
dans la ville voisine qui a la meilleure note. En seconde instance, il pourrait 
également ne pas choisir d’aller dans une ville voisine, mais d’aller plus loin 
encore chose facilitée par les plans de route (voir partie sur les joueurs).

## Comment choisit-il son train

La destination du joueur étant fixée, il regarde tous les voyages vers cette
destination, et choisit la place suivant son statut (`RICHE` => confortable, 
`PAUVRE` => le moins cher, `AISÉ` => le premier). En seconde instance, la 
réputation du joueur pourrait être prise en compte.

S'il n'y a aucun train allant là où il veut, il attend le tour suivant.

## Comment consomme-t-il (s’il le fait) ?

Un joueur consomme en fonction de son statut (et de la réputation du wagon en 
troisième instance). Le système n’a pas à être très élaboré. Un wagon a par 
exemple 100 trucs qui coûtent chacun 10 euros, à chaque tour, le PNJ a une 
probabilité d’en acheter un. Cela rend également le système d’achat/vente plus 
simple (s’il est implémenté). Le joueur achète simplement des trucs (on pourrait 
prévoir 3 trucs, un à 10, un à 30 et un à 100), et choisit le prix de vente qu’il
fixe par wagon ou par train.  

```
class PNJ
   Attributs 
      statut : RICHE, PAUVRE ou AISÉ
      état : WAITING, ON_ROAD ou SETTLED
      ville : la ville où il est
      probabilité : la probabilité de bouger
      destination : la destination
      place : la place qu’il a acheté
      
   Méthodes
      def veut_migrer
         ville.note < probabilité
         
      def chercher_destination
         destination = ville.voisins.maxPar(note)
         
      def essayer_migrer
         Si veut_migrer
            chercher_destination
            état = WAITING
         Fin Si
         
      def chercher_voyage
         voyages = monde.voyages.filtrer(voyage.état = WAITING et voyage.ville = ville)
         Retourner Si pas de voyages 
         places = voyages.flatMap (voyage => voyage.places_disponibles)
         place = match statut with
            RICH   => places.maxPar(confort)
            PAUVRE => places.minPar(Prix)
            AISÉ   => hd(places)
          place.acheter
          place = place
          état = ON_ROAD
          ville.supprimer_habitant
          
      def voyager 
         Si place.voyage = ARRIVAL et place.voyage.ville = destination
            place.libérer
            ville = destination
            ville.ajouterHabitant
            état = SETTLED
            place = null
            destination = null
         Sinon   
            /* Consommer quand ce sera là */ 
         
      def update
         match état with 
            WAITING => chercher_voyage
            SETTLED => essayer_migrer
            ON_ROAD => voyager     
```

# Les places

Une place est associé à un voyage et à un wagon et a un prix. Une place représente 
en fait plusieurs places et a un attribut places. On peut acheter un 
place, savoir s’il est disponible, etc.

```
Class Place
   Attributs 
      voyage : le voyage de la place
      prix : le prix de la place
      wagon : le wagon auquel il est associé
      places : le nombre de place disponible
      
   Méthodes
      def est_disponible
         places > 0
         
      def acheter
         if non disponible
            Lever exception
         places = places - 1
         Augmenter argent du possesseur du train du voyage
        
      def libérer
         places = places + 1  

      def niveau_de_confort
         wagon.niveau_de_confort
         
``` 

# Le monde 

Le monde est composé de villes, de PNJs, de joueurs, et de voyages. 

```
Class Monde
   Attributs
      liste_villes : la liste des villes du monde      
      liste_voyages : la liste des voyages en cours
      liste_PNJs : la liste des PNJs
      liste_joueurs : la liste des joueurs
      population_totale : le nombre de PNJS
      
   Méthodes
      def ajouter_ville(ville)
          liste_villes = ville :: liste_villes

      def ajouter_voyage(voyage)
          liste_voyages = voyage :: liste_voyages
   
      def update(ville)
         Pour chaque voyage de liste_voyages
            voyage.update
         Fin Pour
         Pour chaque PNJ de liste_PNJs
             PNJ.update
         Fin Pour
         Pour chaque joueur de liste_joueurs
            joueur.update
         Fin Pour
         liste_voyages = liste_voyage.filtrer(voyage.est_terminé)               
```  
 
# Les villes

Une ville a une position, un niveau d’accueil, et des routes. Elle a un 
certain nombre d’habitants qui varie suivant les déplacements des PNJs. Peut-être
ajouter une « capacité ». 

Elle a également une note dépendant de sa population et de son niveau d’accueil.

```
Class Ville
   Attributs
      x : sa coordonnée x dans le monde 
      y : sa coordonnée y dans le monde
      liste_routes : la liste des routes
      population : la population de cette ville
      niveau_accueil : le niveau d’accueil de cette ville /* Entre 0 et 1 */
      
   Méthodes
      def ajouterHabitant
         population = population + 1
         
      def supprimer_habitant
         population = population - 1
         
      def ajouter_route(route)
          liste_routes = route :: liste_routes
          
      def voisins
         liste_routes.map (route => route.destination)
         
      def note /* Entre 0 et 1 */
         niveau_accueil * population / monde.population_totale
```



# Les locomotives

Une locomotive a un poids, une puissance qui lui permet de tirer un certain poids. Il 
a aussi une vitesse, une consommation, et un réservoir d’une certaine capacité.

Finalement, il a des points de vie, un état (dans un train, ou libre) et une 
éventuelle amélioration.

```
class Locomotive
   Attributs 
      poids : le poids de la locomotive
      puissance : la puissance de la locomotive
      vitesse : la vitesse de la locomotive
      capacité_essence : la capacité du réservoir
      consommation : la consommation de la locomotive
      état : TRAIN ou FREE
      PV : les points de vie de la locomotive
      modèle : le modèle de la locomotive 
      PV_initial : les points de vie initiaux /* Peut être obtenu avec le modèle */
      amélioration (optionnelle) : l’amélioration de la locomotive
      
   Méthode
      def améliorer 
         Si amélioration et FREE
            améliorer
            /* Soit changer les caractéristiques, soit 
               supprimer ce locomotive et en renvoyer un nouveau
            */
      
      def détériorer(arg)
         PV = max (0, PV - arg)
         
      def réparer
         points_de_vie = PV
         
```

## Les wagons

Un wagon a une capacité (nombre de personnes qu’il peut prendre), un poids et 
une « note de confort ». En seconde instance, un wagon pourrait aussi avoir des
choses à vendre (nourriture, etc.) qui produisent du bénéfice supplémentaire.

Tout comme la locomotive, il a des points de vie, un état et une éventuelle 
amélioration (son confort actuel dépend de ses PVs).

```
Class Wagon
   Attributs
      poids : le poids du wagon
      capacité : la capacité du wagon
      confort : le confort du wagon
      amélioration (optionnelle) : l’amélioration du wagon
      PV : les points de vie de la locomotive
      modèle : le modèle de wagon
      PV_initial : les points de vie initiaux /* Peut être obtenu avec le modèle */
      confort_initial : la note de confort du wagon /* Entre 0 et 1 */
      état : TRAIN ou FREE
      
    Méthodes
       def améliorer
          Si amélioration et FREE
             améliorer
             /* Soit changer les caractéristiques, soit 
                supprimer ce locomotive et en renvoyer un nouveau
             */
       
       def détériorer(arg)
          PV = max (0, PV - arg)
         
       def restaurer
          points_de_vie = PV
          
       def confort 
          confort_initial * PV / PV_initial
```

# Les trains

Un train est composé d’un locomotive et de plusieurs wagons. Ceux-ci peuvent être 
dans plusieurs états (voir comment cela sera représenté dans le jeu, 2 états 
peuvent suffire). 

1. **Non utilisé** , ce qui signifie qu’ils peuvent être utilisés pour créer un 
nouveau train (**assemblage de train**), ou qu’ils peuvent être modifiés 
(voir plus bas).
2. **Utilisé**, ce qui signifie qu’ils sont actuellement une partie d’un train.
Si on veut les modifier ou les affecter à un autre train, il faudra désassembler
le train (voir s’il est nécessaire de tout désassembler ou si un train peut être
traité comme une pile).

Un train peut lui aussi être dans deux états. 

1. **Sur la route**, ce qui signifie qu’ils est sur la route. On ne peut bien 
sûr pas désassembler un train qui est en train de rouler.
2. **En cours de modification**,  auquel cas ils ne peuvent pas être utilisé
pour un nouveau train jusqu’à la fin de la modification.

Ces éléments ont une composante **état**, une locomotive en trop mauvais état ne 
pouvant pas fonctionner.

```
Class Train
    Méthodes
        SetOnRails(roadPlan) : 
        
    
    Attributs
        État : OnRoad, NotOnRoad
        Position : 
        Locomotive : 
        Wagons : List of wagons
```




## Modification de trains

On peut modifier un train, un wagon ou une locomotive de plusieurs manières.

1. Améliorer de la puissance d’une locomotive (bien sûr, il y a une puissance
limite)
2. Réparer d’une locomotive.
3. Rendre un wagon plus confortable.
4. Restaurer un wagon.
5. Assembler un train (nécessite une locomotive).
6. Désassembler un train.
7. Ajouter ou retirer le dernier wagon d’un train ?

# Les joueurs

Un joueur dispose de ses locomotives, de ses wagons, de sa compagnie et d’argent.
Il peut acheter des wagons et des locomotives, les améliorer (voir plus haut), etc.
Pour cela, il peut voir quelles wagons/locomotives sont libres, etc. 

Il décide également du prix que coûte les billets suivant le wagon, la destination,
etc. Suivant cela, sa « côte de popularité » peut varier, ce qui emmène les gens à 
acheter plus ou moins ses billets.

Il planifie chaque voyage de train. En première instance, les voyages ne sont 
que des directs d’une ville à son voisin, mais on peut ensuite imaginer
des voyages qui vont d’une ville à une autre en passant par d’autres villes (en 
s’arrêtant ou pas) => plan de route.

Il gère son argent et l’état de sa compagnie et de ses trains, son but étant 
de rester dans le positif (et de couler les autres joueurs). Les frais qu’il aura à payer sont les suivants.

- Achat, réparation et amélioration de trains.
- Personnel ; on fait simple, pour un voyage dans un train, le prix du personnel
est `f(puissance_de_la_locomotive)` (on considère que plus la locomotive est puissante, 
plus il faut un pilote compétent) plus le salaire des hôtes.
- Carburant (dépendant de la distance parcourue et de la puissance de la locomotive).

```
Class Joueur


```



# L’IA

L’IA n’a accès qu’à l’interface de `Joueur` et au monde (comme le vrai joueur en fait),
et n’utilise **que** l’interface de `Joueur`. Toutes les méthodes nécessaires à 
ce qu’elle fait y sont.

En première instance, l’IA essaie juste de ne pas couler, elle améliore rarement ses
trains et vend ses places un peu plus cher que ce que va lui coûter le voyage (
consommation, personnel, etc.) histoire de faire du bénéfice. Elle décide de faire
partir des trains des lieux où il y a beaucoup de personnes.

# Jeu général

Le jeu général se déroule de la manière suivante. À chaque tour, on récupère
les actions de l'utilisateur, puis celles des IA et on les exécute. Puis, on met à jour
tout ce qui doit être mis à jour. La boucle principale de jeu sera
alors la suivante.


```
Tant qu'on joue
    Actions joueur
    Actions IA
    monde.update
    Mettre à jour l'affichage
```


# Gestion des déplacements

La longueur d’une route correspond au nombre de *frames* qu’il faudra à un train 
de vitesse 1 pour faire le voyage. Ainsi, une longueur de 500 correspondra à une
dizaine de secondes pour un train de vitesse 1. 

Pour simplifier les choses, les données seront représentées avec cette unité
même si autre chose pourra être affiché. Ainsi, des vitesses entre 1 et 3 et 
des longueurs autour de 80 seront bien (=> voyages autour de la dizaine
de seconde). 


# Gestion des ressources

Le carburant est géré au début du voyage (l’argent est pris sur le compte du 
joueur correspondant), les points de vie diminuent à la fin de chaque étape du 
voyage.
