
/*

Multiple types of missions ?

1- Commission,
    WAIT FOR N CARGOS + deliver them to X.

2- Fret
    WAIT FOR full load + deliver them to X.

3- Gathering
    Transport Cargos from X1,...,Xn to Gathering point.

4- Redistribution
    Transport n Cargos from X to X1,...,Xn


*/

/*

class Mission(val bonus: Double) extends Serializable {
}


class GatheringMission(val bonus: Double) extends Mission {
}

class FretMission(val bonus: Double) extends Mission {
}

class RedistributionMission(val bonus: Double) extends Mission {
}

class CommissionMission(val bonus: Double) extends Mission {
}
*/

/**

Rapport discussion Yoann:

Les villes génèrent des missions. On voudrait que le joueur a 2 a 3 missions proposées max par jour.
Refuser = no prob. Accepter et echouer = baisse repu.
Reputation influe sur proba d'être choisi pour une mission. 50% Big Brother, 50% players avec reputation (par exemple on prend reputation/(2*sommerepu))...

Quand génère t'on des missions ?

On regarde au début les villes qu'on classe par population. On en en prend 1/7 dans X. Regarde ensuite toute


**/
