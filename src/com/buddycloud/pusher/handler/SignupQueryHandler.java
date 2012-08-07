/*
 * Copyright 2011 buddycloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.buddycloud.pusher.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.dom4j.Element;
import org.xmpp.packet.IQ;

import com.buddycloud.pusher.PusherSubmitter;
import com.buddycloud.pusher.email.EmailPusher;
import com.buddycloud.pusher.utils.XMPPUtils;

/**
 * @author Abmar
 *
 */
public class SignupQueryHandler extends AbstractQueryHandler {

	private static final String NAMESPACE = "http://buddycloud.com/pusher/signup";
	private static final String WELCOME_TEMPLATE = "welcome.tpl";
	
	/**
	 * @param namespace
	 * @param properties
	 */
	public SignupQueryHandler(Properties properties, PusherSubmitter pusherSubmitter) {
		super(NAMESPACE, properties, pusherSubmitter);
	}

	/* (non-Javadoc)
	 * @see com.buddycloud.pusher.handler.AbstractQueryHandler#handleQuery(org.xmpp.packet.IQ)
	 */
	@Override
	protected IQ handleQuery(IQ iq) {
		Element queryElement = iq.getElement().element("query");
		Element jidElement = queryElement.element("jid");
		Element emailElement = queryElement.element("email");
		
		if (jidElement == null || emailElement == null) {
			return XMPPUtils.error(iq,
					"You must provide the jid and the email", getLogger());
		}
		
		String jid = jidElement.getText();
		String email = emailElement.getText();
		
		Map<String, String> tokens = new HashMap<String, String>();
		tokens.put("FIRST_PART_JID", jid.split("@")[0]);
		tokens.put("EMAIL", email);
		
		EmailPusher pusher = new EmailPusher(getProperties(), tokens, WELCOME_TEMPLATE);
		getPusherSubmitter().submitPusher(pusher);
		
		return createResponse(iq, "User [" + jid + "] was signed up.");
	}

}
