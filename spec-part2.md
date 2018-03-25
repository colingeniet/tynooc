This is a summary of the game mecanics of Part 2 and how they should be implemented

# New route system
## Complex Routing

A simple system of instructions will be implemented to give orders to a train.
An order is a list of instruction; here are some that should be implemented:

```
  .Goto c: go to the city c
  .Wait t: wait a certain amount of time t
  .Load x n: Loads a certain amount <= n of good x, as much as possible if n is unspecified.
  .Loop: Goes to the beginning of the list.
  (.Wait_for t: wait for a certain date ?)

```

## Connections

They all share the same trait


```
Trait Connection:
  accept(v: Vehicle): Boolean
```

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


```
Class Station / Upgradable / :
  
  accept(v: Vehicle): Boolean
  
  Vars:
  .owner
  .model // For it is upgradable.

```

accept returns yes if the vehicle can go in...
Here is what is contained in the model;


```
.vehicle -> (price/time) (for non owners)
.size
.capacity/vehicle
.capacity/citizen
```

They are Ownable

# Different vehicles type.

This is the most difficult part of the project:


# Goods

Here is a list of possible implementations of goods:
  - Enum: Although, it's bad for a huge number of possible goods
  - An object, coupled with a factory for goods. It's more extensible.

# City consumption & updates

We added price/goods in cities.
They depend on:
  .The number of citizens & the consumption
  .The number of goods available in the city

##City evolution

###Consumption evolution



###Urban evolution

It should be possible to create a factory or a station if there is enough space available. Although creating something from nothing should cost a lot more than buying a previously existing one.

# Factories

They are as follow:

```
Class Factory /Upgradable/: 

  .timeTable ; When does the factory starts/stop producing ?
  .city ; The city it's in.
  .Productions ; This is a set of rules of the form:
    A1/Q1, ...., An/Qn => B/Q
    Where Ai & B are Goods, Qi  is a quantity necessary of good, Q is the quantity produced with that.

```

They shall use the simplex algorithm at each beginning of production to try to maximise their income.

They also are Ownable

# Parsing & Maps

Maps will parse XML.
Many factories will be "hardcoded": name -> factory. We should create a special type of factory for unknown factories, that would produce a special good like "Smurfs" or "Goofs"

# Patterns & Concepts & Hierarchy

An Ownable should implement this :
```
  .owner ; The owner; by defaults most factories will be owned by the State player.
  .available ; Can you buy it ?
  .price ; The price at wich you can buy the factory
```

Should it be different than a Makeable, or should we fuse them ? For now Ownable <=> Makeable...

Makeable:
```
  .pop_needed ; You need at least x citizens to construct something this big (no airport in a village...)
  .space_needed ; The space needed to build it
  .build_price ; The price at wich you can build the factory
```

# Easter eggs

So, i did a typo and wrote goof instead of good, it'd be a fun easter egg to put goof as the "random" good.


