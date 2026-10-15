package com.osrsjokes;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(OsrsJokesConfig.GROUP)
public interface OsrsJokesConfig extends Config
{
	String GROUP = "osrs-jokes";

	@ConfigItem(
		keyName = "showDeck",
		name = "Show deck",
		description = "Show the joke deck. Choose the current deck in the OSRS Jokes sidebar settings."
	)
	default boolean showDeck()
	{
		return false;
	}

	@ConfigItem(
		keyName = "selectedDeck",
		name = "Current deck",
		description = "The deck selected in the OSRS Jokes sidebar dropdown",
		hidden = true
	)
	default String selectedDeck()
	{
		return "general";
	}
}
