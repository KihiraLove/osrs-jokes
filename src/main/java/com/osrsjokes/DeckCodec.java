package com.osrsjokes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** A schema-specific text codec: JSON strings/arrays/objects, comments and trailing commas. */
final class DeckCodec
{
	private DeckCodec()
	{
	}

	static List<DeckDefinition> deserialize(String text)
	{
		return new Parser(text).read();
	}

	static String serialize(List<DeckDefinition> decks)
	{
		StringBuilder out = new StringBuilder("{\n  \"decks\": [");
		Set<String> ids = new HashSet<>();
		for (int i = 0; i < decks.size(); i++)
		{
			DeckDefinition deck = decks.get(i);
			if (!ids.add(deck.getId()))
			{
				throw new IllegalArgumentException("Duplicate deck id: " + deck.getId());
			}
			out.append(i == 0 ? "\n" : ",\n").append("    {\n      \"id\": ");
			quote(out, deck.getId());
			out.append(",\n      \"name\": ");
			quote(out, deck.getName());
			out.append(",\n      \"jokes\": [");
			for (int j = 0; j < deck.getJokes().size(); j++)
			{
				out.append(j == 0 ? "\n        " : ",\n        ");
				quote(out, deck.getJokes().get(j));
			}
			if (!deck.getJokes().isEmpty())
			{
				out.append("\n      ");
			}
			out.append("]\n    }");
		}
		return out.append("\n  ]\n}\n").toString();
	}

	private static void quote(StringBuilder out, String value)
	{
		out.append('"');
		for (int i = 0; i < value.length(); i++)
		{
			char c = value.charAt(i);
			switch (c)
			{
				case '"': out.append("\\\""); break;
				case '\\': out.append("\\\\"); break;
				case '\n': out.append("\\n"); break;
				case '\r': out.append("\\r"); break;
				case '\t': out.append("\\t"); break;
				case '\b': out.append("\\b"); break;
				case '\f': out.append("\\f"); break;
				default:
					if (c < 0x20 || Character.isSurrogate(c))
					{
						out.append(String.format("\\u%04x", (int) c));
					}
					else
					{
						out.append(c);
					}
			}
		}
		out.append('"');
	}

	private static final class Parser
	{
		private final String text;
		private int pos;

		private Parser(String text)
		{
			this.text = text;
			pos = text.startsWith("\uFEFF") ? 1 : 0;
		}

		private List<DeckDefinition> read()
		{
			expect('{');
			if (!"decks".equals(string()))
			{
				throw error("Expected the 'decks' property");
			}
			expect(':');
			expect('[');
			List<DeckDefinition> decks = new ArrayList<>();
			Set<String> ids = new HashSet<>();
			if (!take(']'))
			{
				do
				{
					DeckDefinition deck = deck();
					if (!ids.add(deck.getId()))
					{
						throw error("Duplicate deck id: " + deck.getId());
					}
					decks.add(deck);
				}
				while (more(']'));
			}
			take(',');
			expect('}');
			space();
			if (pos != text.length())
			{
				throw error("Unexpected text after the document");
			}
			return List.copyOf(decks);
		}

		private DeckDefinition deck()
		{
			expect('{');
			String id = null;
			String name = null;
			List<String> jokes = null;
			Set<String> keys = new HashSet<>();
			if (!take('}'))
			{
				do
				{
					String key = string();
					if (!keys.add(key))
					{
						throw error("Duplicate property: " + key);
					}
					expect(':');
					switch (key)
					{
						case "id": id = string(); break;
						case "name": name = string(); break;
						case "jokes": jokes = jokes(); break;
						default: throw error("Unknown deck property: " + key);
					}
				}
				while (more('}'));
			}
			if (id == null || id.trim().isEmpty() || name == null || name.trim().isEmpty() || jokes == null)
			{
				throw error("Every deck needs a non-blank id, name and a jokes array");
			}
			return new DeckDefinition(id, name, jokes);
		}

		private List<String> jokes()
		{
			expect('[');
			List<String> jokes = new ArrayList<>();
			if (!take(']'))
			{
				do
				{
					jokes.add(string());
				}
				while (more(']'));
			}
			return jokes;
		}

		private boolean more(char end)
		{
			if (take(end))
			{
				return false;
			}
			expect(',');
			return !take(end);
		}

		private String string()
		{
			expect('"');
			StringBuilder out = new StringBuilder();
			while (pos < text.length())
			{
				char c = text.charAt(pos++);
				if (c == '"')
				{
					return out.toString();
				}
				if (c < 0x20)
				{
					throw error("Use escaped line breaks (\\n) inside strings");
				}
				if (c != '\\')
				{
					out.append(c);
					continue;
				}
				if (pos == text.length())
				{
					throw error("Incomplete escape");
				}
				char escaped = text.charAt(pos++);
				switch (escaped)
				{
					case '"': case '\\': case '/': out.append(escaped); break;
					case 'n': out.append('\n'); break;
					case 'r': out.append('\r'); break;
					case 't': out.append('\t'); break;
					case 'b': out.append('\b'); break;
					case 'f': out.append('\f'); break;
					case 'u':
						int value = 0;
						for (int i = 0; i < 4; i++)
						{
							int digit = pos < text.length() ? Character.digit(text.charAt(pos++), 16) : -1;
							if (digit < 0)
							{
								throw error("Expected four hex digits after \\u");
							}
							value = value * 16 + digit;
						}
						out.append((char) value);
						break;
					default: throw error("Unknown escape: \\" + escaped);
				}
			}
			throw error("Unterminated string");
		}

		private void expect(char c)
		{
			if (!take(c))
			{
				throw error("Expected '" + c + "'");
			}
		}

		private boolean take(char c)
		{
			space();
			if (pos < text.length() && text.charAt(pos) == c)
			{
				pos++;
				return true;
			}
			return false;
		}

		private void space()
		{
			while (pos < text.length())
			{
				char c = text.charAt(pos);
				if (c == ' ' || c == '\r' || c == '\n' || c == '\t')
				{
					pos++;
				}
				else if (text.startsWith("//", pos))
				{
					while (pos < text.length() && text.charAt(pos) != '\n' && text.charAt(pos) != '\r')
					{
						pos++;
					}
				}
				else if (text.startsWith("/*", pos))
				{
					int end = text.indexOf("*/", pos + 2);
					if (end < 0)
					{
						throw error("Unterminated comment");
					}
					pos = end + 2;
				}
				else
				{
					return;
				}
			}
		}

		private IllegalArgumentException error(String message)
		{
			int line = 1;
			int column = 1;
			for (int i = 0; i < pos; i++)
			{
				if (text.charAt(i) == '\n')
				{
					line++;
					column = 1;
				}
				else
				{
					column++;
				}
			}
			return new IllegalArgumentException(message + " (line " + line + ", column " + column + ")");
		}
	}
}
