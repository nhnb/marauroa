package marauroa.test;

import marauroa.common.game.IRPZone;
import marauroa.server.game.rp.MarauroaRPZone;
import marauroa.server.game.rp.RPWorld;

public class MockRPWorld extends RPWorld{

	private static MockRPWorld world;
	
	private MockRPWorld() {
		super();
		
		populate();		
	}
	
	protected void populate() {
		IRPZone zone=new MarauroaRPZone("test");
		addRPZone(zone);
	}
	/**
	 * This method MUST be implemented in other for marauroa to be able to load this World implementation.
	 * There is no way of enforcing static methods on a Interface, so just keep this in mind when 
	 * writting your own game.
	 *  
	 * @return an unique instance of world.
	 */
	public static RPWorld get() {
		if(world==null) {
			world = new MockRPWorld();
		}
		
		return world;
	}
}