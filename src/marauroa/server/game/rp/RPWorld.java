/* $Id: RPWorld.java,v 1.5 2007/02/05 18:07:39 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.rp;

import java.util.HashMap;
import java.util.Iterator;

import marauroa.common.Log4J;
import marauroa.common.game.IRPZone;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObjectInvalidException;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.container.PlayerEntryContainer;

import org.apache.log4j.Logger;

/**
 * This class is a container of RPZones.
 * TODO: Actually it is poorly implemented the relation of neighbourghoud between zones.
 * @author miguel
 */
public class RPWorld implements Iterable<IRPZone> {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(RPWorld.class);

	/** The Singleton instance */
	private static RPWorld instance;

	/** A map containing the zones. */
	HashMap<IRPZone.ID, IRPZone> zones;

	/** The all-mighty player container. */
	PlayerEntryContainer playerContainer;

	protected RPWorld() {
		zones = new HashMap<IRPZone.ID, IRPZone>();
		playerContainer=PlayerEntryContainer.getContainer();
	}
	
	/** 
	 * Returns an unique World method.
	 * @return an instance of RPWorld
	 */
	public static RPWorld get() {
		if (instance == null) {
			instance = new RPWorld();
		}
		return instance;
	}

	/** This method is called when RPWorld is created */
	public void onInit() {
	}

	/** This method is called when server is going to shutdown. */
	public void onFinish() {
	}

	/** Adds a new zone to World */
	public void addRPZone(IRPZone zone) {
		zones.put(zone.getID(), zone);
	}

	/** Returns true if world has such zone */
	public boolean hasRPZone(IRPZone.ID zoneid) {
		return zones.containsKey(zoneid);
	}

	/** Returns the zone or null if it doesn't exists */
	public IRPZone getRPZone(IRPZone.ID zoneid) {
		return zones.get(zoneid);
	}

	/** Returns the zone or null if it doesn't exists */
	public IRPZone getRPZone(RPObject.ID objectid) {
		return zones.get(new IRPZone.ID(objectid.getZoneID()));
	}

	/**
	 * This method adds an object to the zone it points with its zoneid attribute.
	 * And if it is a player, it request also a sync perception. 
	 * 
	 * @param object the object to add 
	 */
	public void add(RPObject object) {
		if (object.has("zoneid")) {
			IRPZone zone = zones.get(new IRPZone.ID(object.get("zoneid")));
			zone.assignRPObjectID(object);

			zone.add(object);

			/** A player object will have always the clientid attribute. */
			if (object.has("clientid")) {
				/** So if object has the attribute, we request a sync perception as we have 
				 *  entered a new zone. */
				PlayerEntry entry=playerContainer.get(object.getID());
				if(entry!=null) {
					entry.requestSync();
				}
			}
		}
	}

	/**
	 * This method returns an object from a zone using it ID<object, zone>
	 * @param id the object's id
	 * @return the object
	 */
	public RPObject get(RPObject.ID id) {
		IRPZone zone = zones.get(new IRPZone.ID(id.getZoneID()));
		return zone.get(id);
	}

	/**
	 * This method returns true if an object exists in a zone using it ID<object, zone>
	 * @param id the object's id
	 * @return true if the object exists
	 */
	public boolean has(RPObject.ID id) {
		IRPZone zone = zones.get(new IRPZone.ID(id.getZoneID()));
		return zone.has(id);
	}

	/**
	 * This method returns an object from a zone using it ID<object, zone> and remove it
	 * @param id the object's id
	 * @return the object or null if it not found.
	 */
	public RPObject remove(RPObject.ID id) {
		IRPZone zone = zones.get(new IRPZone.ID(id.getZoneID()));
		return zone.remove(id);
	}

	/**
	 * This method returns an iterator over all the zones contained.
	 * @return iterator over zones.
	 */
	public Iterator<IRPZone> iterator() {
		return zones.values().iterator();
	}

	/**
	 * This method notify zone that object has been modified. Used in Delta^2 
	 * @param object the object that has been modified.
	 */
	public void modify(RPObject object) {
		IRPZone zone = zones.get(new IRPZone.ID(object.get("zoneid")));
		zone.modify(object);
	}

	/**
	 * This methods make a player/object to change zone.
	 * @param oldzoneid the old zone id
	 * @param newzoneid the new zone id
	 * @param object the object we are going to change zone to.
	 * @throws RPObjectInvalidException
	 */
	public void changeZone(IRPZone.ID oldzoneid, IRPZone.ID newzoneid, RPObject object) {
		try {
			if (newzoneid.equals(oldzoneid)) {
				return;
			}

			IRPZone oldzone = getRPZone(oldzoneid);

			oldzone.remove(object.getID());
			object.put("zoneid", newzoneid.getID());

			add(object);
		} catch (Exception e) {
			logger.error("error changing Zone", e);
			throw new RPObjectInvalidException("zoneid");
		}
	}

	/**
	 * This methods make a player/object to change zone.
	 * @param oldzoneid the old zone id
	 * @param newzoneid the new zone id
	 * @param object the object we are going to change zone to.
	 */
	public void changeZone(String oldzone, String newzone, RPObject object) {
		changeZone(new IRPZone.ID(oldzone), new IRPZone.ID(newzone), object);
	}

	/**
	 * This method make world to move to the next turn, calling each zone nextTurn method.	 *
	 */
	public void nextTurn() {
		for (IRPZone zone : zones.values()) {
			zone.nextTurn();
		}
	}

	/**
	 * This methods return the amount of objects added to world.
	 * @return the amount of objects added to world.
	 */
	public int size() {
		int size = 0;

		for (IRPZone zone : zones.values()) {
			size += zone.size();
		}

		return size;
	}
}