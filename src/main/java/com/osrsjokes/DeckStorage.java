package com.osrsjokes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import net.runelite.client.RuneLite;

final class DeckStorage
{
	static final Path FILE = RuneLite.RUNELITE_DIR.toPath().resolve("osrs-jokes").resolve("decks.json");

	private DeckStorage()
	{
	}

	/** Only called by the plugin's I/O worker. Existing files are never rewritten. */
	static List<DeckDefinition> load(Path file) throws IOException
	{
		Files.createDirectories(file.getParent());
		try
		{
			Files.writeString(file, DeckCodec.serialize(List.of(
				new DeckDefinition("general", "General", JokeBook.JOKES),
				new DeckDefinition("osrs", "OSRS", List.of())
			)), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
		}
		catch (FileAlreadyExistsException ignored)
		{
			// Keep hand-written decks (including comments and formatting) untouched.
		}
		return DeckCodec.deserialize(Files.readString(file, StandardCharsets.UTF_8));
	}
}
