

class Travel(t: Train, l : List[Route])
{
	/*
	time et percent, 1 des 2 est redondant
	*/
	val train: Train = t
	val stops: List[Route] = l
	val time: Double = 0 //Time since beginning of journey
	val percent: Double = 0 //What percent has been done on the road i
	private val current: int //The road you're taking

	def timeRemaining =
	{
		d = stops.takeRight(stops.length-i+1).foldLeft[Double](0) { (acc, r) => acc + r.length }
		d += stops[i].length*percent
		return max(0, d/(t.engine.model.speed)
	}

	def next =
	{
		return stops[i]
	}
}

class Player() {

	var trains: List[Train] = List()
	var carriages: List[Carriage] = List()
	var engines: List[Engine] = List()
	var travels: List[Travel] = List()
	

	var money: int

	def buyEngine (name: String) = {
		c = EngineModel(name)
		if (c.price >= money) {
			money -= c.price
			carriages = (new Engine(c))::engines
		}
	}

	def buyCarriage (name: String) = {
		c = CarriageModel(name)
		if (c.price >= money) {
			money -= c.price
			carriages = (new Carriage(c))::carriages
		}
	}

	def assembleTrain (e: Engine, c: List[Carriage])
	{
		engines = engines diff List(e)
		c.foreach { carriages = carriages diff List(c)} 
		trains = (new Train(e, c))::trains
	}

	def launchTravel(train:Train, to:Town)
	{
		(new Travel(train, train.where.FindPath(to)))::travels
	}
}