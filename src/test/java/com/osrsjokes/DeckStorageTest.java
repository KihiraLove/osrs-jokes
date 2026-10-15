package com.osrsjokes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DeckStorageTest
{
	@Rule
	public TemporaryFolder temporary = new TemporaryFolder();

	@Test
	public void bundledStarterFileIsReadable() throws Exception
	{
		List<DeckDefinition> decks = DeckCodec.deserialize(Files.readString(Paths.get("examples/decks.json")));
		assertEquals(2, decks.size());
		assertEquals("general", decks.get(0).getId());
		assertTrue(decks.get(0).getJokes().isEmpty());
		assertTrue(decks.get(1).getJokes().isEmpty());
	}

	@Test
	public void firstLoadCreatesFileAndSubdirectory() throws Exception
	{
		Path file = temporary.getRoot().toPath().resolve("osrs-jokes/decks.json");
		List<DeckDefinition> decks = DeckStorage.load(file);
		assertTrue(Files.isRegularFile(file));
		assertEquals(2, decks.size());
		assertEquals("general", decks.get(0).getId());
		assertEquals(decks.size(), DeckCodec.deserialize(Files.readString(file)).size());
	}

	@Test
	public void reloadPreservesHandWrittenFileAndReadsEdits() throws Exception
	{
		Path file = temporary.newFile("decks.json").toPath();
		String original = "// my comment\n{\"decks\":[{\"id\":\"custom\",\"name\":\"Custom\",\"jokes\":[\"First\",],},]}";
		Files.writeString(file, original);
		assertEquals("First", DeckStorage.load(file).get(0).getJokes().get(0));
		assertEquals(original, Files.readString(file));
		String edited = original.replace("First", "Edited");
		Files.writeString(file, edited);
		assertEquals("Edited", DeckStorage.load(file).get(0).getJokes().get(0));
		assertEquals(edited, Files.readString(file));
	}

	@Test
	public void invalidFilesAreNeverOverwritten() throws Exception
	{
		Path file = temporary.newFile("decks.json").toPath();
		String invalid = "{my unfinished edits";
		Files.writeString(file, invalid);
		try
		{
			DeckStorage.load(file);
			fail("Should report a parse error");
		}
		catch (IllegalArgumentException expected)
		{
			assertEquals(invalid, Files.readString(file));
		}
	}
}
