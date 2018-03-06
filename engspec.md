Disclaimer: This specification is not totally implemented. It was used as a guide during coding.
So there are ideas that could be use to ameliorate the game. They'll be implemented or not depending on the 2nd part.

# Roads

Roads join two cities (from and to). They just need a length.
They are oriented for genericity and simplicity purpose.
Then they could be more or less prone to accidents (an accident has a certain probability and destroys a train on top
of decreasing the reputation of a player), plus it will damage engines and carriages linked to this train.

The distance of a road corresponds to the number of frames needed to cover it with a speed of 1.

```
Class Route
   Attributs
      longueur : la longueur de la route
      départ : la ville de départ
      arrivée : la ville de fin
      
   Méthodes 
```

# The cities

They have a position, a welcoming level and roads.
A certain number of inhabitants, varying according to the PNJ's movement.
We might add a max capacity.

It has a note depending on it's population and welcoming level.

## Generating PNJs

Every update, a city generates for every neighbour a number of persons wanting to go there.
We use a gaussian. This number depends on the note of the city and the note of the neighbour.
We might (easily) add the possibility of wanting to go anywhere.

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

Note: by managing the number of rich, poor, and average people as an array, it's possible to have
only one method 'addInhabitants' that would take the nb of habitants to add and their status.
Plus that would allow to add new status easily.

# Travellers (used to be PNJs)

A traveller is just a destination and a status. He is only caracterized in the game by the number of people
in a certain trip. They are grouped by carriage, status and destination. (See the trip specification)

Every update, a city generates travellers that want to migrate.
Those who find a train do, wich decrease the city's population and  increase the number of migrants in that train.
  
A traveller can count as a group of person. (It allows to simulate 1 000 000 persons with 1000 travellers)

This shows that there is no need for a Traveller class.
The traveller object is just used to do calculations on how a traveller chooses his train and consume.
products in the train (if products are added)

## How do they choose their train?

The destination of a traveller being fixed, he looks at all the available trip to that destination and choose a place
depending on his status following this pattern:
RiCH => Most comfy, POOR => Cheapest, AVERAGE => first or best (quality/price)
The reputation of the player might be taken into account.

## How will they consume (if they do) ?

Travellers consume according to their status (and maybe to the carriage's reputation)
The system need not be elaborate. A carriage can have 100 stuff that cost 10 each, every turn, the traveller has
a probability of buying one.
This makes a buy/sell system easy simple (if we make one)
The player simply buy stuff (for exe, we can make 3 stuff, that would cost 10, 30, 100) and fix a price per carriage or train.

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

# Tickets

A ticket is associated to a trip and a carriage, and has a price.
A ticket object is actually a "pack" of tickets, you can buy one, check if there are still available etc...

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

# Trips

A trip is made of a train, a list of road to go on, a city, the current road, the travellers and the distance already
done on that road.
It also has a state `ON_ROAD` or `WAITING`, `WAITING` meaning the train is in a city and is waiting for passengers
and a ticket list.

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

# The World 

It's made of cities, players and trips.

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

# Train Engines.

A train engine has a model, ie a weight, a power that allows it to carry a certain weight, a speed, a consumation of fuel, a
storage of a certain capacity (to store fuel).

It also has life points, a state (in a train or free) and possibly an upgrade.

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

## Carriages

A carriage has a model, that is a capacity (number of person it can take), a weight, and a comfort note.
We could add stuff to sell to make more profit.
It has a life points, a state.
Upgrades are done by modyfing the model.

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

Those 2 classes can be in two states:

1. **Not used** , they then can be used to create a new train, or upgraded.

2. **Used**, their a part of the train. If the user want to modify them he will have to dismantle the train.
(A train is a stack so you can easily change the last carriage, but you'll have to dimsantle some parts
if you want to edit one in the middle)
(If you want to edit the engine you'll have to dismantle the entire train)

# Trains

A train is made of an engine and multiple carriages.
They can be in multiple states:

1. **On the road**, you cannot dissasemble a train on the road
2. **Not on road**, you can do what you want with it.


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




## Change a train?

There are multiple ways to modify stuff:

1. Upgrade the a model
2. Repair something.
3. Create or dismantle a train (nécessite une locomotive).
4. Pop or push a carriage on an unused train.

# Players (/Companies)

A player owns engines, carriages, and money.
He can buy things, upgrade them (if they are not in use) etc..

He also decide on the price of tickets according to the carriage, the destination etc...
(Popularity might be added)

He plans every trip. For now trips are simply path from a city to another, but we can easily add more complex trips
like: A -> C ; wait 10 -> B

His objective his to be the richest and ruin the other players.
He look over his company and his money.

He has to:
- Buy/repair/upgrade trains...
- Pay for things: fuel/workers etc.. (It's done automatically)

```
Class Joueur

```

# L’AI

The AI only has access to the interface of the player (they are treated the same way as the player)
They are different types of AI

YOAN YOU
HAVE
TO
WRITE
WHAT
THE AI
ARE SUPPOSED
TO
DO


# Game

Every tick, the game handles the player input then the AI input.
Then theer is the logical update.
And then the drawing is done.


```
Tant qu'on joue
    Actions joueur
    Actions IA
    monde.update
    Mettre à jour l'affichage
```

# Time

There is a bijection between real time and game time. Handled in an object.

# Ressources

The fuel is dealt with at the beginning of the trip. The life points decrease at the end of every trip.
