This is a summary of game mechanics.

# Towns

Towns have position, a welcoming level, roads towards other towns
and a number of inhabitants that varies with passengers movements.

It has a 'quality' note, based on welcoming level and population.

# Roads

Roads join two cities, and have a length and a damage value (see train damage).
Roads are oriented for simplicity.

# World

A world consist of towns and roads between those towns.
The `World` class is also responsible for population simulation,
and travels simulation.

## Passengers generation.

Every simulation tick, each town generates a number of passengers toward
a neighbor town. Passenger generation approximately follows a binomial law,
with parameters depending on the town population, and the respective town
notes.

We may later add passengers toward any town (not only immediate neighbors).

Passengers are awaiting for departure are registered in the `Town` class.
They will try to take any train going to their destination.
Passengers that board a train are recorded in the corresponding `Travel` object.


# Travels

Class `Travel` defines the state of a train on a travel.
It consist of a train, a travel plan as a list of routes, the current position,
and a list of passengers.

At each update step, the train advance of the appropriate distance.
Upon reaching a town, passengers leave the train (if appropriate).
The train stays stopped for one game tick at each town
to allow other passengers to board.


# Vehicles

Vehicles regroup both train engines and carriages.
Vehicle characteristics are defined by a `model` attribute.

For all vehicles, the model defines the vehicle weight, price, resistance
(health points) and possible upgrades.

In addition to model characteristics, vehicles have a current damage status
(health points), and can or not be part of a train.

When not in a train, vehicles can be repaired and upgraded.

## Engines

In addition to vehicle characteristics, engines have a power, a speed and
a fuel consumption. The engine power determines how heavy a train it can carry.
Fuel consumption is only simulated as an additional cost by distance.

## Carriages

In addition to vehicle characteristics, passengers carriages have a capacity
(number of seats) and a comfort level.

# Trains

A train is made of an engine and several carriages.
They can either be on route or stored. When on route, most actions
are impossible (assembling, disassembling).


# Companies (players)

A player owns engines, carriages, trains and money.
He can buy vehicles, upgrade them (if they are not in use),
assemble (and disassemble) trains, and send trains on travels.
