package com.osrsjokes;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

final class JokeDeck
{
	private final List<String> jokes;
	private int index = -1;

	JokeDeck(List<String> jokes)
	{
		this.jokes = jokes.stream()
			.filter(Objects::nonNull)
			.map(String::trim)
			.filter(joke -> !joke.isEmpty())
			.collect(Collectors.toList());
	}

	String draw()
	{
		if (jokes.isEmpty())
		{
			return "No jokes yet.\n\nThe deck is waiting for its first joke.";
		}
		index = (index + 1) % jokes.size();
		return jokes.get(index);
	}

	int size()
	{
		return jokes.size();
	}

	int cardNumber()
	{
		return index + 1;
	}

	void reset()
	{
		index = -1;
	}
}
