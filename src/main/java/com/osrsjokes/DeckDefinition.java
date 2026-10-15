package com.osrsjokes;

import java.util.List;
import java.util.Objects;

final class DeckDefinition
{
	private final String id;
	private final String name;
	private final List<String> jokes;

	DeckDefinition(String id, String name, List<String> jokes)
	{
		this.id = Objects.requireNonNull(id).trim();
		this.name = Objects.requireNonNull(name).trim();
		this.jokes = List.copyOf(jokes);
		if (this.id.isEmpty() || this.name.isEmpty())
		{
			throw new IllegalArgumentException("Deck id and name must not be blank");
		}
	}

	String getId()
	{
		return id;
	}

	String getName()
	{
		return name;
	}

	List<String> getJokes()
	{
		return jokes;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
