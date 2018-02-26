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

# Les villes

Une ville a une position, un niveau d’accueil, et des routes. Elle a un 
certain nombre d’habitants qui varie suivant les déplacements des PNJs. Peut-être
ajouter une « capacité ». 

Elle a également une note dépendant de sa population et de son niveau d’accueil.

## Génération de PNJs 

À chaque mise à jour, un ville génère pour chacun de ses voisins un nombre
de personnes qui veulent y aller. Ce nombre dépend de la note de la ville 
et de celle du voisin en question.

En seconde instance, cela pourrait ne pas être que les villes voisines mais 
toutes les villes.


```
Class Ville
   Attributs
      x : sa coordonnée x dans le monde 
      y : sa coordonnée y dans le monde
      liste_routes : la liste des routes
      population : la population de cette ville
      nbRiches : le nombres de riches /* 1/4 de population par défaut */
      nbPauvres : le nombre de pauvres /* 1/4 de population par défaut */
      nbAisés : le nombre d’aisé /* 1/2 de population par défaut */
      niveau_accueil : le niveau d’accueil de cette ville /* Entre 0 et 1 */
      id : l’identifiant de la ville
      
   Méthodes
      def ajouterRiches(nb)
         nbRiches = nb + nbRiches
         population = population + nb
         
      def ajouterPauvres(nb)
         nbPauvres = nbPauvres + nb
         population = population + nb
         
      def ajouterAisés(nb)
         nbAisés = nbAisés + nb
         population = population + nb
      
      def supprimerRiches(nb)
         Si nb > nbRiches
            Lever exception
         nbRiches = nb + nbRiches
         population = population + nb
         
      def supprimerPauvres(nb)
         Si nb > nbPauvres
            Lever exception
         nbPauvres = nbPauvres + nb
         population = population + nb
         
      def supprimerAisés(nb)
         Si nb > nbAisés
            Lever exception
         nbAisés = nbAisés + nb
         population = population + nb
         
      def ajouter_route(route)
          liste_routes = route :: liste_routes
          
      def voisins
         liste_routes.map (route => route.destination)
         
      def note /* Entre 0 et 1 */
         niveau_accueil * population / monde.population_totale
      
      def générerVoyageurs(destination)
          return Math.max(0, population * (destination.note - note)) 
      
      def update 
         destinations_possibles = voisins.trier(par_note)
         Pour chaque destination dans destinations_possibles
            nbVoyageurs = générerVoyageurs(destination)
            nbVoyageursRiches = nbVoyageurs * (nbRiches / population)
            nbVoyageursPauvres = nbVoyageurs * (nbPauvres / population)
            nbVoyageursAisés = nbVoyageurs - nbVoyageursRiches - nbVoyageursPauvres
            Voyageur.essayer_voyager(self, destination, nbVoyageursRiches, 
                                     nbVoyageursPauvres, nbVoyageursAisés)
```

Note : en gérant le nombre de riches, de pauvres et d’aisés comme un tableau, 
il est possible d’avoir une seule méthode `ajouterHabitants` qui prend en paramètre
le nombre d’habitants à ajouter et leur statut. De plus, cela permet de rajouter
d’autres catégories de personnes facilement.

# Les Voyageur (anciennement PNJ)

Un voyageur est juste une destination et un statut (riche, etc.). Il n’existe 
dans le jeu que par le nombre de personnes dans un voyage ; ils sont alors
regroupés par wagon, par statut et par destination (voir la spécification de 
Voyage).
  
À chaque mise à jour, une ville génère des voyageurs qui veulent migrer. Ceux 
qui trouvent un train le font ce qui fait baisser la population de la ville et
augmente le nombre de voyageurs du train en question. 

Un voyageur peut compter comme un groupe de plusieurs personnes (cela permettra 
avec 1000 voyageur par exemple d'en simuler 1 000 000).

Tout ceci montre qu’il n’y a pas besoin de classe pour le Voyageur 
L’objet Voyageur sert juste à faire des calculs pour savoir comment un voyageur 
en question choisit son train et consomme (modulo implémentation de la 
consommation).
 
## Comment choisit-il son train

La destination du voyageur étant fixée, il regarde tous les voyages vers cette
destination, et choisit la place suivant son statut (`RICHE` => confortable, 
`PAUVRE` => le moins cher, `AISÉ` => le premier (ou le meilleur rapport qualité 
prix). En seconde instance, la réputation du joueur pourrait être prise en compte.

## Comment consomme-t-il (s’il le fait) ?

Un joueur consomme en fonction de son statut (et de la réputation du wagon en 
troisième instance). Le système n’a pas à être très élaboré. Un wagon a par 
exemple 100 trucs qui coûtent chacun 10 euros, à chaque tour, le PNJ a une 
probabilité d’en acheter un. Cela rend également le système d’achat/vente plus 
simple (s’il est implémenté). Le joueur achète simplement des trucs (on pourrait 
prévoir 3 trucs, un à 10, un à 30 et un à 100), et choisit le prix de vente qu’il
fixe par wagon ou par train.

```
object Voyageur
   Attributs 
      places = []
      
   Méthodes
      def essayerVoyager(départ, destination, nbRiches, nbPauvres, nbAisés)
         voyages = monde.voyages.filtrer(voyage.état = WAITING et
                                         voyage.ville = ville et 
                                         voyage.destination = destination)
         places = voyages.flatMap ( _.places_disponibles)     
         places.trierPar(confort)
         prendre_place(places, nbRiches, RICHE, départ.supprimerRiches)
         places.trierPar(prix)
         prendre_place(places, nbPauvres, PAUVRE, départ.supprimerPauvres)        
         places.trierPar(confort / prix)
         prendre_place(places, nbAisés, AISÉ, départ.supprimerAisés)
         
      def prendre_place(places, nb_voyageurs, statut, destination 
                        fonctionSuppressionHabitants)
         nbPlacesAchetées = 0
         Tant que nbPlaceAchetées > 0 && !places.isEmpty
            place = places.head
            nbPlaceAchetées = Math.max(nb_voyageurs, place.nbPlaces)
            match statut 
               RICHE  => place.ajouterRiches(nbPlaceAchetées, destination)
               PAUVRE => place.ajouterPauvres(nbPlaceAchetées, destination)
               AISÉ   => place.ajouterAisés(nbPlaceAchetées, destination)
            Si !place.estDisponible
               places = places.tail
         fonctionSuppressionHabitants(nb_place_achetées)
```

Note : Voyageur pas nécessairement utile, placer méthodes dans Monde ?

Note 2 : là encore, gérer le nombre de riches, de pauvres et d’aisés comme un 
tableau pourrait simplifier les choses.

Note 3 : On se rend compte qu’en fait, une place correspond plus à un wagon en 
route (voir comment bien nommer cette classe, peut-être `Salle`). On pourrait
tout simplement rajouter à la classe Wagon un attribut `prix_place`, mais l’idée
n’est pas plaisante car cela n’a de sens que quand le wagon est dans un voyage.

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
      nbPlaces : le nombre de place disponible
      riches : un tableau du nombre de riches par destination
      pauvres : un tableau du nombre de pauvres par destination
      aisés : un tableau du nombre d’aisés par destination
      
   Méthodes
      def estDisponible
         places > 0
         
      def nbVoyageurs
         riches.somme + pauvres.somme + aisés.somme
      
      def nbRichesTo(destination)
         riches[destination]
         
      def nbPauvresTo(destination)
         pauvres[destination]
         
      def nbAisésTo(destination)
         aisés[destination]
      
      def ajouterRiches(nbPlaceAchetées, destination)
         Si nbPlacesAchetées > nbPlaces
            Lever exception
         nbPlaces = nbPlaces - nbPlaceAchetées
         riches[destination] += nbPlacesAchetées
         voyage.possesseur.ajouterArgent(nbPlaceAchetées * prix)
         
      def ajouterPauvres(nbPlaceAchetées, destination)
         Si nbPlacesAchetées > nbPlaces
            Lever exception
         nbPlaces = nbPlaces - nbPlaceAchetées
         pauvres[destination] += nbPlacesAchetées
         voyage.possesseur.ajouterArgent(nbPlaceAchetées * prix)
      
      def ajouterAisés(nbPlaceAchetées, destination)
         Si nbPlacesAchetées > nbPlaces
            Lever exception
         nbPlaces = nbPlaces - nbPlaceAchetées
         aisés[destination] += nbPlacesAchetées
         voyage.possesseur.ajouterArgent(nbPlaceAchetées * prix)

      def retirerRiches(nbPlacesLibérés, destination)
         /* Vérifier que c’est plus petit que la capacité du wagon */
         nbPlaces = nbPlaces + nbPlacesLibérés
         riches[destination] -= nbPlacesLibérés
      
      def retirerPauvres(nbPlacesLibérés, destination)
         /* Vérifier que c’est plus petit que la capacité du wagon */
         nbPlaces = nbPlaces + nbPlacesLibérés
         pauvres[destination] -= nbPlacesLibérés
         
      def retirerAisés(nbPlacesLibérés, destination)
         /* Vérifier que c’est plus petit que la capacité du wagon */
         nbPlaces = nbPlaces + nbPlacesLibérés
         aisés[destination] -= nbPlacesLibérés

      def niveau_de_confort
         wagon.niveau_de_confort
``` 

# Les voyages

Un voyage est composé d’un train, d’une liste de route à emprunter, d’une ville,
de la route actuelle, de ses voyageurs et de la distance déjà parcourue sur 
cette route. 

Il a également un état `ON_ROAD` ou `WAITING`, `WAITING` signifiant qu’il est 
sur une ville et attend des passagers et une liste de billets.

```
Class Voyage
   Attributs
      possesseur : le possesseur du voyage
      train : le train du voyage
      listeRoutes : la liste des routes à emprunter
      ville : la ville actuelle
      route : la route actuelle
      distanceParcourue : la distance parcourue sur la route actuelle
      listePlaces : liste de places pour ce voyage
      état : ON_ROAD, ARRIVAL ou WAITING      
      
   Méthodes
      def débarquerVoyageurs
         Pour chaque place de listePlaces
            ville.ajouterRiches(place.nbRichesTo(ville))
            place.retirerRiches(place.nbRichesTo(ville), ville)
            ville.ajouterPauvres(place.nbPauvresTo(ville))
            place.retirerPauvres(place.nbPauvresTo(ville), ville)
            ville.ajouterAisés(place.nbAisésTo(ville))
            place.retirerAises(place.nbAisésTo(ville), ville)
         
      def nbVoyageurs
         somme(listesPlaces.nbVoyageurs)
         
      def update(dt)
         Si ON_ROAD
            distanceParcourue += train.vitesse * Game.world.real_time_to_game(dt)
            Si distanceParcourue >= route.distance
               distanceParcourue = 0
               état = ARRIVAL
            Fin Si
         Sinon si ARRIVAL
            train.détériorer(route)
            état = WAITING
            listeRoutes = tl(listeRoutes)
            route = hd(listeRoutes)
            débarquerVoyageurs
         Sinon /* état = WAITING */  
            état = ON_ROAD
            ville = route.destination
         Fin Si

      def places_disponibles
          [place de listePlaces où place.disponible]
          
      def est_terminé
         liste_routes = []
        
      def destination
         Si est_terminé
            Lever exception
         hd(listes_routes).arrivée
         
      def destination_finale
         listesRoutes.last.arrivée
```

# Le monde 

Le monde est composé de villes, de PNJs, de joueurs, et de voyages. 

```
Class Monde
   Attributs
      listeVilles : la liste des villes du monde      
      listeVoyages : la liste des voyages en cours
      population : la population du monde
      
   Méthodes
      def ajouterVille(ville)
          listeVilles = ville :: listeVilles
          population = population + ville.population

      def ajouter_voyage(voyage)
          liste_voyages = voyage :: liste_voyages
   
      def update(ville)
         Pour chaque voyage de liste_voyages
            voyage.update
         Pour chaque ville de listeVilles
            ville.update
         liste_voyages = liste_voyage.filtrer(voyage.est_terminé)               
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

# Gestion du temps

Une unité arbitraire est utilisée pour les longueurs et les temps. Une fonction
du monde se charge de faire les conversions entre temps réels et temps du jeu.

La distance représente le nombre de secondes qu’il faudra en temps réel pour
faire le trajet à une vitesse 1.

# Gestion des ressources

Le carburant est géré au début du voyage (l’argent est pris sur le compte du 
joueur correspondant), les points de vie diminuent à la fin de chaque étape du 
voyage.
