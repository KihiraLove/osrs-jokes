package com.osrsjokes;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JokeDeckTest
{
	@Test
	public void drawsInOrderAndWrapsWithoutSkippingFirstJoke()
	{
		JokeDeck deck = new JokeDeck(List.of("First", "Second"));
		assertEquals("First", deck.draw());
		assertEquals(1, deck.cardNumber());
		assertEquals("Second", deck.draw());
		assertEquals(2, deck.cardNumber());
		assertEquals("First", deck.draw());
		deck.reset();
		assertEquals("First", deck.draw());
	}

	@Test
	public void emptyAndBlankDecksCanBeDrawnRepeatedly()
	{
		JokeDeck deck = new JokeDeck(Arrays.asList("", "  ", null));
		assertEquals(0, deck.size());
		assertTrue(deck.draw().contains("No jokes yet"));
		assertTrue(deck.draw().contains("No jokes yet"));
		assertEquals(0, deck.cardNumber());
	}

	@Test
	public void singleJokeRepeatsAndPreservesParagraphs()
	{
		JokeDeck deck = new JokeDeck(List.of("  Setup\n\nPunchline  "));
		assertEquals("Setup\n\nPunchline", deck.draw());
		assertEquals("Setup\n\nPunchline", deck.draw());
		assertEquals(1, deck.cardNumber());
	}
}
