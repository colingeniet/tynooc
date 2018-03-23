This is a summary of the game mecanics of Part 2 and how they should be implemented

# New route system
## Complex Routing

TODO

## Connections

They all share the same trait

-----------------
Trait Connection:
  accept(v: Vehicle): Boolean
----------------

accept is a function that return yes if the vehicle can go on the path.
There a multiple factors that are taken into account in the multiple implementations:
.Nature: A plane cannot go on rails
.Size: A boat can be too large to take a certain stream.
.Capacity: There can be too much trains on certain rails

Exemples of connections:
- Rails
- Sea
- Air

# Stations

They are like this

------------------------
Class Station / Upgradable / :
  
  accept(v: Vehicle): Boolean
  
  Vars:
  .owner
  .model // For it is upgradable.
-------------------------

accept returns yes if the vehicle can go in...
Here is what is contained in the model;

---------------
.vehicle -> (price/time) (for non owners)
.size
.capacity/vehicle
.capacity/citizen
--------------

# Different vehicles type.

This is the most difficult part of the project:



# Goods

# City consumption & updates

We added price/goods in cities.
They depend on:
  .The number of citizens & the consumption
  .The number of goods available in the city

# Factories

They are as follow:

---------------
Class Factory /Upgradable/: 

  .timeTable ; When does the factory starts/stop producing ?
  .city ; The city it's in.
  .Productions ; This is a set of rules of the form:
    A1/Q1, ...., An/Qn => B/Q
    Where Ai & B are Goods, Qi  is a quantity necessary of good, Q is the quantity produced with that.

---------------

They shall use the simplex algorithm at each beginning of production to try to maximise their income.

# Parsing & Maps





# Easter eggs

So, i did a typo and wrote goof instead of good, it'd be a fun easter egg to put goof as the "random" good.
