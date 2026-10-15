package com.osrsjokes;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DeckCodecTest
{
	@Test
	public void readsCommentsTrailingCommasBomAndPropertiesInAnyOrder()
	{
		String input = "\uFEFF{ // decks\n\"decks\": [ /* first */ {\"jokes\": [\"A\\n\\nB\",],"
			+ "\"name\": \"My deck\", \"id\": \"mine\",},],}";
		List<DeckDefinition> decks = DeckCodec.deserialize(input);
		assertEquals(1, decks.size());
		assertEquals("mine", decks.get(0).getId());
		assertEquals("My deck", decks.get(0).getName());
		assertEquals(List.of("A\n\nB"), decks.get(0).getJokes());
	}

	@Test
	public void roundTripsEscapesUnicodeAndCommentLikeText()
	{
		List<String> jokes = List.of("Quotes: \"hello\"; slash: \\; // /* */",
			"\n\r\t\b\f\u0001", "Æ ø é \uD83D\uDE00", "", " padded ");
		List<DeckDefinition> original = List.of(new DeckDefinition("id", "A \"name\"", jokes));
		DeckDefinition decoded = DeckCodec.deserialize(DeckCodec.serialize(original)).get(0);
		assertEquals(original.get(0).getId(), decoded.getId());
		assertEquals(original.get(0).getName(), decoded.getName());
		assertEquals(jokes, decoded.getJokes());
		assertEquals("A/", DeckCodec.deserialize("{\"decks\":[{\"id\":\"a\",\"name\":\"A\","
			+ "\"jokes\":[\"\\u0041\\/\"]}]}").get(0).getJokes().get(0));
	}

	@Test
	public void handlesEmptyCollectionsAndManyDecksWithoutFixedSlots()
	{
		assertTrue(DeckCodec.deserialize("{\"decks\":[]}").isEmpty());
		List<DeckDefinition> many = new ArrayList<>();
		for (int i = 0; i < 1000; i++)
		{
			many.add(new DeckDefinition("id-" + i, "Deck " + i, List.of()));
		}
		List<DeckDefinition> decoded = DeckCodec.deserialize(DeckCodec.serialize(many));
		assertEquals(1000, decoded.size());
		assertEquals("id-999", decoded.get(999).getId());
		assertTrue(decoded.get(999).getJokes().isEmpty());
	}

	@Test
	public void rejectsMalformedInputWithLocationInsteadOfReturningPartialDecks()
	{
		String valid = "{\"id\":\"a\",\"name\":\"A\",\"jokes\":[\"one\"]}";
		for (String malformed : List.of(
			"{\"decks\":[" + valid + "," + valid + "]}",
			"{\"decks\":[{\"id\":\"a\",\"id\":\"b\",\"name\":\"A\",\"jokes\":[]}]}",
			"{\"decks\":[{\"name\":\"A\",\"jokes\":[]}]}",
			"{\"decks\":[{\"id\":\" \",\"name\":\"A\",\"jokes\":[]}]}",
			"{\"decks\":[{\"id\":\"a\",\"name\":\"A\",\"jokes\":[null]}]}",
			"{\"decks\":[{\"id\":\"a\",\"name\":\"A\",\"jokes\":[],\"typo\":[]}]}",
			"{\"decks\":[]} garbage", "{\"decks\":[]} /* unfinished",
			"{\"decks\":[{\"id\":\"a\\q\"}]}", "{\"decks\":[{\"id\":\"a\\u0XX0\"}]}",
			"{\"decks\":[{\"id\":\"a\nline\"}]}", "{\"decks\":[{\"id\":\"unfinished",
			"{\"decks\":[,]}", "{\"decks\":[],\"decks\":[]}"))
		{
			try
			{
				DeckCodec.deserialize(malformed);
				fail("Accepted invalid document: " + malformed);
			}
			catch (IllegalArgumentException expected)
			{
				assertTrue(expected.getMessage(), expected.getMessage().contains("line "));
				assertTrue(expected.getMessage(), expected.getMessage().contains("column "));
			}
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void serializerRejectsDuplicateIdsToo()
	{
		DeckDefinition deck = new DeckDefinition("duplicate", "Deck", List.of());
		DeckCodec.serialize(List.of(deck, deck));
	}
}
