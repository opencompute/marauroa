package marauroa.common.game.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import marauroa.common.game.Perception;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import marauroa.server.game.rp.MarauroaRPZone;

import org.junit.Test;


public class TestRPObject {
	@Test
	public void testRPObject() {
		RPObject obj=new RPObject();

		obj.put("a",1);
		obj.put("b","1");
		obj.put("c",2.0);
		obj.put("d","string of text");

		obj.addSlot("lhand");
		obj.addSlot("rhand");

		obj.addEvent("chat", "Hi there!");
		obj.addEvent("chat", "Does this work?");

		assertTrue(obj.has("a"));
		assertEquals(1, obj.getInt("a"));
		assertTrue(obj.has("b"));
		assertEquals("1", obj.get("b"));
		assertTrue(obj.has("c"));
		assertEquals(2.0, obj.getDouble("c"));
		assertFalse(obj.has("e"));

		assertTrue(obj.hasSlot("lhand"));
		assertTrue(obj.hasSlot("rhand"));

		for(Iterator<RPEvent> it=obj.eventsIterator(); it.hasNext();) {
			RPEvent event=it.next();
			assertEquals("chat", event.getName());
		}
	}

	@Test
	public void testRPSlots() {
		RPObject obj=new RPObject();

		obj.put("a",1);
		obj.put("b","1");
		obj.put("c",2.0);
		obj.put("d","string of text");

		obj.addSlot("lhand");
		obj.addSlot("rhand");

		RPSlot lhand=obj.getSlot("lhand");
		assertNotNull(lhand);

		RPObject pocket=new RPObject();
		pocket.put("size", 1);
		pocket.addSlot("container");
		lhand.add(pocket);

		RPSlot container=pocket.getSlot("container");

		RPObject coin=new RPObject();
		coin.put("euro", 100);
		coin.put("value", 100);

		int assignedid=container.add(coin);

		assertEquals(coin, obj.getSlot("lhand").getFirst().getSlot("container").getFirst());

		RPObject expected=obj.getSlot("lhand").getFirst().getSlot("container").getFirst();
		assertEquals(1,expected.getInt("id"));
		assertEquals(1,assignedid);
	}

	@Test
	public void testRPEvents() {
		RPObject obj=new RPObject();

		obj.put("a",1);
		obj.put("b","1");
		obj.put("c",2.0);
		obj.put("d","string of text");

		obj.addSlot("lhand");
		obj.addSlot("rhand");

		obj.addEvent("test", "work!");
		obj.addEvent("test", "it MUST work!");
		obj.addEvent("test", "OMG! It works!");

		RPSlot lhand=obj.getSlot("lhand");
		assertNotNull(lhand);

		RPObject coin=new RPObject();
		coin.setID(new RPObject.ID(1,""));
		coin.put("euro", 100);
		coin.put("value", 100);

		lhand.add(coin);

		obj.clearVisible();

		assertTrue(obj.isEmpty());
	}


	@Test
	public void testSerialization() throws IOException, ClassNotFoundException {
		RPObject obj=new RPObject();

		obj.put("a",1);
		obj.put("b","1");
		obj.put("c",2.0);
		obj.put("d","string of text");

		obj.addSlot("lhand");
		obj.addSlot("rhand");

		RPSlot lhand=obj.getSlot("lhand");

		RPObject pocket=new RPObject();
		pocket.put("size", 1);
		pocket.addSlot("container");
		lhand.add(pocket);

		RPSlot container=pocket.getSlot("container");

		RPObject coin=new RPObject();
		coin.put("euro", 100);
		coin.put("value", 100);
		container.add(coin);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputSerializer os = new OutputSerializer(out);

		os.write(obj);

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		InputSerializer is = new InputSerializer(in);

		RPObject result=(RPObject) is.readObject(new RPObject());

		assertEquals(obj, result);
	}

	@Test
	public void testBaseContainer() {
		RPObject obj=new RPObject();

		obj.put("a",1);
		obj.put("b","1");
		obj.put("c",2.0);
		obj.put("d","string of text");

		obj.addSlot("lhand");
		obj.addSlot("rhand");

		RPSlot lhand=obj.getSlot("lhand");

		RPObject pocket=new RPObject();
		pocket.put("size", 1);
		pocket.addSlot("container");
		lhand.add(pocket);

		RPSlot container=pocket.getSlot("container");

		RPObject coin=new RPObject();
		coin.put("euro", 100);
		coin.put("value", 100);
		container.add(coin);
		
		assertEquals(obj,coin.getBaseContainer());
	}

		
	@Test
	public void testDelta2() {
		RPObject obj=new RPObject();

		obj.put("a",1);
		obj.put("b","1");
		obj.put("c",2.0);
		obj.put("d","string of text");

		obj.addSlot("lhand");
		obj.addSlot("rhand");

		RPSlot lhand=obj.getSlot("lhand");

		RPObject pocket=new RPObject();
		pocket.put("size", 1);
		pocket.addSlot("container");
		lhand.add(pocket);

		RPSlot container=pocket.getSlot("container");

		RPObject coin=new RPObject();
		coin.put("euro", 100);
		coin.put("value", 100);
		container.add(coin);
		
		MarauroaRPZone zone=new MarauroaRPZone("test") {
			public void onInit() throws Exception {				
			}
			
			public void onFinish() throws Exception {				
			}
		};

		/* 
		 * Add object to zone
		 * Test it has correct attributes values.
		 */
		zone.assignRPObjectID(obj);
		assertTrue(obj.has("id"));
		assertTrue(obj.has("zoneid"));
		assertEquals("test",obj.get("zoneid"));
		
		/*
		 * Test if first time modification works as expected. 
		 */
		zone.add(obj);
		obj.put("b",9);
		zone.modify(obj);
		
		Perception expected=zone.getPerception(obj.getID(), Perception.DELTA);
		assertFalse(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
		
		/*
		 * Next turn.
		 * We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		/*
		 * Test Delta^2 on attribute object modification.
		 */
		obj.put("b",19);
		
		zone.modify(obj);

		expected=zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
		
		/*
		 * Next turn.
		 * We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		/*
		 * Test Delta^2 on attribute object addition.
		 */
		obj.put("bg","house red");
		
		zone.modify(obj);

		expected=zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
		

		/*
		 * Next turn.
		 * We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		/*
		 * Test Delta^2 on attribute object removal.
		 */
		obj.remove("b");
		
		zone.modify(obj);

		expected=zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertFalse(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
		
		/*
		 * Next turn.
		 * We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		/*
		 * Test Delta^2 on slot object modification.
		 */
		RPObject slotcoin=obj.getSlot("lhand").getFirst().getSlot("container").getFirst();
		assertEquals(coin,slotcoin);
		
		slotcoin.put("value",200);

		assertEquals(coin,slotcoin);

		zone.modify(coin.getBaseContainer());

		expected=zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
		
	}

}