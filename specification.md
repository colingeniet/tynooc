# Les trains

Un train est composé d’une locomotive et de plusieurs wagons. Ceux-ci peuvent 
être dans plusieurs états (voir comment cela sera représenté dans le jeu, 2 états 
peuvent suffire).

1. **Non utilisé** , ce qui signifie qu’ils peuvent être utilisés pour créer un 
nouveau train (**assemblage de train**), ou qu’ils peuvent être modifiés 
(voir plus bas).
2. **Utilisé**, ce qui signifie qu’ils sont actuellement une partie d’un train.
Si on veut les modifier ou les affecter à un autre train, il faudra désassembler
le train (voir s’il est nécessaire de tout désassembler ou si un train peut être
traité comme une pile).
3. **Sur la route**, ce qui signifie qu’ils sont sur la route. On ne peut bien 
sûr pas désassembler un train qui est en train de rouler.
4. **En cours de modification**,  auquel cas ils ne peuvent pas être utilisé
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

## Les locomotives

Une locomotive a une puissance qui lui permet de tirer plus ou moins de wagons.
Si le nombre de wagons est OK, le train peut rouler, sa vitesse est déterminée 
à l’aide de sa puissance (formule à fixer).

Une locomotive a aussi une consommation qui dépend de sa puissance (seulement ?).
Plus elle est puissante, plus elle consomme.

```
Class Locomotive
    Méthodes
        Améliorer()
        
        
    Attributs
        Puissance :
        État : NonUsed, Used, OnRoad
```

## Les wagons

Un wagon a une capacité (nombre de personnes qu’il peut prendre) et une « note
de confort », ce qui permettra au joueur de fixer son prix (c’est bien sûr le 
joueur qui décide, mais par exemple vendre cher un wagon peu confortable sera 
une mauvaise idée). Bien sûr, on n’est pas obligé de remplir un wagon ; on peut
imaginer un wagon-affaire très confortable et où on ne met pas trop de personnes.
Il y a donc la capacité du wagon, mais aussi le nombre de places fixées par le
joueur.

En seconde instance, un wagon pourrait aussi avoir des choses à vendre 
(nourriture, etc.), et qui produisent du bénéfice supplémentaire (ou pas...).

```
Class Wagon
    Méthodes
        
    
    Attributs
        Capacité : 20 - 50 - 100
        Prix : 
        Capacité_utilisable :
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

# Les villes

Une ville est caractérisé par sa taille, sa position et ses voisins. Elle a un 
certain nombre d’habitants qui varie suivant les déplacements des PNJs. Peut-être
ajouter une « capacité », c’est-à-dire le nombre maximum de personnes qu’il peut 
y avoir dans une ville (elle est liée à la taille de la ville). Cette capacité 
permettrait aux PNJs de savoir comment voyager. 

En seconde instance, une ville pourrait être plus ou moins accueillante, ce qui 
pousserait également les gens à y aller ou à s’en aller. 

# Les PNJs

Les PNJs ne sont jamais vus, ni visibles. On ne les voit qu’à travers leurs déplacements
(et leurs éventuels achats). Sinon, un PNJ est caractérisé par une certaine 
quantité d’argent représentée grossièrement (ce n’est pas ce qui nous intéresse ici),
peut-être juste par un statut `RICHE`, `PAUVRE`, `AISÉ`.

## Comment choisit-il sa destination

Un PNJ a ue probabilité de migrer qui dépend du nombre de personnes dans sa ville.
Il migre alors dans une ville voisine où il y a moins de personnes (en seconde 
instance, ces deux choses pourraient dépendre du niveau d’accueil de la ville).

En seconde instance, il pourrait également ne pas choisir d’aller dans une ville 
voisine, mais d’aller plus loin encore chose facilitée par les plans de route
(voir partie sur les joueurs).

## Comment choisit-il son train

Un joueur choisit son train en fonction de la destination déjà (ouf). Si plusieurs
trains vont dans la destination souhaitée, il choisit un train avec un wagon 
correspond à son statut (wagon-affaire par exemple). S’il y en a toujours plusieurs,
il choisit le moins cher/au hasard/celui avec la plus bonne réputation.

On va considérer que l’heure d’arrivée ne lui importe pas. 
 
NOTE : On peut décider que le PNJ a une caractéristique représentant son envie
de migrer (celle liée au niveau d'accueil et on nombre de personne dans la ville),
et qu’une fois qu’il a choisit de migrer, il le fait immédiatement en choisissant 
la ville la plus propice parmi toutes les directions actuellement possible, ou qu’il 
choisit sa destination, puis attend un train qui y mène.

## Comment consomme-t-il (s’il le fait) ?

Un joueur consomme en fonction de la réputation du wagon et de son statut. Le système 
n’a pas à être très élaboré. Un wagon a par exemple 100 trucs qui coûtent chacun 
10 euros, à chaque tour, le PNJ a une probabilité d’en acheter un. Cela rend également
le système d’achat/vente plus simple (s’il est implémenté). Le joueur achète simplement
des trucs (on pourrait prévoir 3 trucs, un à 10, un à 30 et un à 100), et choisit 
le prix de vente qu’il fixe par wagon ou par train.  

# Les routes

Les routes sont considérées comme n’ayant pas de sens, et relient deux villes en 
première instance. Elles ont juste besoin d’une longueur.

En troisième instance, elles pourraient être plus ou moins propices aux accidents
(un accident est provoqué avec une certaine probabilité et détruit une locomotive 
en plus de faire baisser la réputation du joueur).
et abîmer plus ou moins les locomotives et les wagons.

# L’IA

L’IA n’a accès qu’à l’interface de `Joueur` et au monde (comme le vrai joueur en fait),
et n’utilise **que** l’interface de `Joueur`. Toutes les méthodes nécessaires à 
ce qu’elle fait y sont.

En première instance, l’IA essaie juste de ne pas couler, elle améliore rarement ses
trains et vend ses tickets un peu plus cher que ce que va lui coûter le voyage (
consommation, personnel, etc.) histoire de faire du bénéfice. Elle décide de faire
partir des trains des lieux où il y a beaucoup de personnes. 
 
