/* object Game()
{

	private var last: Double
	private var players: List[Player] = [new Hero(), new IA("Dumb")]
	private var world: World

	def update():
	{
		val dt = time.getTime() - last
		logic(dt)
		draw(dt)
	}

	def logic(dt: Double):
	{
		//Trains
		players.foreach(p: Player => p.update(dt))

		//Update Cities
		world.update(dt)

	}

	def draw(dt: Double):
	{
		world.draw()
	}
}

//World.update

update(dt: Double)
{
	cities.foreach(c:City => c.update(dt))
}

//Player.update

update(dt: Double)
{
	trains.foreach(t: Train => t.update(dt))
}*/
