package com.osrsjokes;

import java.util.List;

/** Legacy jokes seed the General deck only when decks.json is first created. */
public final class JokeBook
{
	public static final List<String> JOKES = List.of(
		// Add each joke as a quoted Java string, separated by commas.
		// Use \n\n between the setup and punchline. For example:
		// "Your setup here\n\nYour punchline here",
		// "Your next joke here"
			"Things you can say in the bed and in osrs\n\nOh no I missed a flick",
			"Things you can say in the bed and in osrs\n\nI'm so dry",
			"Things you can say in the bed and in osrs\n\nI think you put it on crush for this one",
			"Things you can say in the bed and in osrs\n\nThe electricity temporarily paralyses you"
	);

	private JokeBook()
	{
	}
}
