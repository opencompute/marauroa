/* $Id: CreateAccountHandler.java,v 1.1 2010/05/09 19:42:51 nhnb Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.messagehandler;

import marauroa.common.Log4J;
import marauroa.common.game.AccountResult;
import marauroa.common.game.Result;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SCreateAccount;
import marauroa.common.net.message.MessageS2CCreateAccountACK;
import marauroa.common.net.message.MessageS2CCreateAccountNACK;
import marauroa.server.game.GameServerManager;

/**
 * This is a create account request. It just create the
 * account, it doesn't login us into it.
 */
class CreateAccountHandler extends MessageHandler {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(GameServerManager.class);


	/**
	 * This message is used to create a game account. It may fail if the player
	 * already exists or if any of the fields are empty. This method does not
	 * make room for the player at the server.
	 *
	 * @param msg
	 *            The create account message.
	 */
	@Override
	public void process(Message message) {
		MessageC2SCreateAccount msg = (MessageC2SCreateAccount) message;
		try {
			/*
			 * We request RP Manager to create an account for our player. This
			 * will return a <b>result</b> of the operation.
			 */
			AccountResult val = rpMan.createAccount(msg.getUsername(), msg.getPassword(), msg.getEmail(), msg.getAddress().getHostAddress());
			Result result = val.getResult();

			if (result == Result.OK_CREATED) {
				/*
				 * If result is OK then the account was created and we notify
				 * player about that.
				 */
				logger.debug("Account (" + msg.getUsername() + ") created.");
				MessageS2CCreateAccountACK msgCreateAccountACK = new MessageS2CCreateAccountACK(msg
				        .getSocketChannel(), val.getUsername());
				netMan.sendMessage(msgCreateAccountACK);
			} else {
				/*
				 * Account creation may also fail. So expose the reasons of the
				 * failure.
				 */
				MessageS2CCreateAccountNACK msgCreateAccountNACK = new MessageS2CCreateAccountNACK(
				        msg.getSocketChannel(), result);
				netMan.sendMessage(msgCreateAccountNACK);
			}
		} catch (Exception e) {
			logger.error("Unable to create an account", e);
		}
	}

}