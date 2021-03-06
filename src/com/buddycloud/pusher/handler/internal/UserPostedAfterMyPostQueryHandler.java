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
package com.buddycloud.pusher.handler.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dom4j.Element;
import org.xmpp.packet.IQ;

import com.buddycloud.pusher.NotificationSettings;
import com.buddycloud.pusher.Pusher;
import com.buddycloud.pusher.Pusher.Event;
import com.buddycloud.pusher.Pushers;
import com.buddycloud.pusher.db.DataSource;
import com.buddycloud.pusher.handler.AbstractQueryHandler;
import com.buddycloud.pusher.utils.NotificationUtils;
import com.buddycloud.pusher.utils.XMPPUtils;

/**
 * @author Abmar
 *
 */
public class UserPostedAfterMyPostQueryHandler extends AbstractQueryHandler {
	
	private static final String NAMESPACE = "http://buddycloud.com/pusher/userposted-aftermypost";
	
	/**
	 * @param namespace
	 * @param properties
	 */
	public UserPostedAfterMyPostQueryHandler(Properties properties, DataSource dataSource) {
		super(NAMESPACE, properties, dataSource);
	}

	/* (non-Javadoc)
	 * @see com.buddycloud.pusher.handler.AbstractQueryHandler#handleQuery(org.xmpp.packet.IQ)
	 */
	@Override
	protected IQ handleQuery(IQ iq) {
		Element queryElement = iq.getElement().element("query");
		Element authorElement = queryElement.element("authorJid");
		Element referencedElement = queryElement.element("referencedJid");
		Element channelElement = queryElement.element("channel");
		Element postContentElement = queryElement.element("postContent");
		
		if (authorElement == null || channelElement == null || referencedElement == null) {
			return XMPPUtils.error(iq,
					"You must provide the authorJid, the channel and the referencedJid");
		}
		
		String authorJid = authorElement.getText();
		String referencedJid = referencedElement.getText();
		String channelJid = channelElement.getText();
		String postContent = postContentElement.getText();
		
		Map<String, String> tokens = new HashMap<String, String>();
		tokens.put("AUTHOR_JID", authorJid);
		tokens.put("REFERENCED_JID", referencedJid);
		tokens.put("CHANNEL_JID", channelJid);
		tokens.put("CONTENT", postContent);
		
		List<NotificationSettings> allNotificationSettings = NotificationUtils.getNotificationSettings(
				referencedJid, getDataSource());
		
		for (NotificationSettings notificationSettings : allNotificationSettings) {
			if (!notificationSettings.getPostAfterMe()) {
				getLogger().warn("User " + referencedJid + " won't receive comment notifications.");
				continue;
			}
			
			if (notificationSettings.getTarget() == null) {
				getLogger().warn("User " + referencedJid + " has no target registered.");
				continue;
			}

			Pusher pusher = Pushers.getInstance(getProperties()).get(notificationSettings.getType());
			pusher.push(notificationSettings.getTarget(), Event.POST_AFTER_MY_POST, tokens);
		}
		
		return createResponse(iq, "User [" + authorJid + "] has posted on channel [" + 
				channelJid + "] after [" + referencedJid + "] post.");
	}
}
