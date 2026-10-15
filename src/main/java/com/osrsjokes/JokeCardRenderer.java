package com.osrsjokes;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import net.runelite.client.ui.FontManager;

final class JokeCardRenderer
{
	static final int CARD_WIDTH = 224;
	static final int CARD_HEIGHT = 300;
	static final int DECK_WIDTH = 116;
	static final int DECK_HEIGHT = 160;
	static final int LINES_PER_PAGE = 10;
	private static final Color GOLD = new Color(0xCFB778);
	private static final Color INK = new Color(0xF0ECE2);
	private static final Color MUTED = new Color(0xB4B0A6);

	private JokeCardRenderer()
	{
	}

	static BufferedImage deck(boolean hovered)
	{
		BufferedImage image = image(DECK_WIDTH + 4, DECK_HEIGHT + 4);
		Graphics2D g = graphics(image);
		for (int offset = 12; offset >= 0; offset -= 6)
		{
			frame(g, offset, offset, 104, 148, hovered && offset == 0);
		}
		g.setFont(FontManager.getRunescapeBoldFont());
		center(g, "OSRS", 52, 34, GOLD);
		center(g, "JOKES", 52, 53, GOLD);
		g.setColor(GOLD);
		g.drawPolygon(new int[]{52, 69, 52, 35}, new int[]{64, 81, 98, 81}, 4);
		g.drawPolygon(new int[]{52, 63, 52, 41}, new int[]{70, 81, 92, 81}, 4);
		g.setFont(FontManager.getRunescapeSmallFont());
		center(g, "Click to draw", 52, 124, INK);
		g.dispose();
		return image;
	}

	static BufferedImage cardBack()
	{
		BufferedImage image = image(CARD_WIDTH + 4, CARD_HEIGHT + 4);
		Graphics2D g = graphics(image);
		frame(g, 0, 0, CARD_WIDTH, CARD_HEIGHT, false);
		g.setFont(FontManager.getRunescapeBoldFont());
		center(g, "OSRS", CARD_WIDTH / 2, 110, GOLD);
		center(g, "JOKES", CARD_WIDTH / 2, 132, GOLD);
		g.setColor(GOLD);
		g.drawPolygon(new int[]{112, 136, 112, 88}, new int[]{152, 176, 200, 176}, 4);
		g.drawPolygon(new int[]{112, 129, 112, 95}, new int[]{159, 176, 193, 176}, 4);
		g.dispose();
		return image;
	}

	static List<String> lines(String text)
	{
		BufferedImage image = image(1, 1);
		Graphics2D g = graphics(image);
		g.setFont(FontManager.getRunescapeFont());
		List<String> lines = wrap(text, g.getFontMetrics(), CARD_WIDTH - 44);
		g.dispose();
		return lines;
	}

	// Preserve explicit paragraph breaks and split unusually long words without clipping.
	static List<String> wrap(String text, FontMetrics metrics, int width)
	{
		List<String> result = new ArrayList<>();
		for (String paragraph : text.split("\\R", -1))
		{
			String line = "";
			for (String word : paragraph.trim().split("\\s+"))
			{
				if (word.isEmpty())
				{
					continue;
				}
				String candidate = line.isEmpty() ? word : line + " " + word;
				if (!line.isEmpty() && metrics.stringWidth(candidate) > width)
				{
					result.add(line);
					line = "";
				}
				while (metrics.stringWidth(word) > width)
				{
					int end = word.offsetByCodePoints(0, 1);
					while (end < word.length())
					{
						int next = word.offsetByCodePoints(end, 1);
						if (metrics.stringWidth(word.substring(0, next)) > width)
						{
							break;
						}
						end = next;
					}
					result.add(word.substring(0, end));
					word = word.substring(end);
				}
				line = line.isEmpty() ? word : line + " " + word;
			}
			result.add(line);
		}
		return result;
	}

	static int pageCount(List<String> lines)
	{
		return Math.max(1, (lines.size() + LINES_PER_PAGE - 1) / LINES_PER_PAGE);
	}

	static BufferedImage card(List<String> lines, int page, int number, int total)
	{
		BufferedImage image = image(CARD_WIDTH + 4, CARD_HEIGHT + 4);
		Graphics2D g = graphics(image);
		frame(g, 0, 0, CARD_WIDTH, CARD_HEIGHT, false);
		g.setFont(FontManager.getRunescapeBoldFont());
		center(g, "OSRS JOKES", CARD_WIDTH / 2, 29, GOLD);
		g.setColor(MUTED);
		g.drawLine(CARD_WIDTH - 24, 17, CARD_WIDTH - 16, 25);
		g.drawLine(CARD_WIDTH - 16, 17, CARD_WIDTH - 24, 25);
		g.setColor(new Color(0x777164));
		g.drawLine(24, 41, CARD_WIDTH - 24, 41);
		g.drawLine(24, CARD_HEIGHT - 42, CARD_WIDTH - 24, CARD_HEIGHT - 42);
		g.setFont(FontManager.getRunescapeFont());
		int from = page * LINES_PER_PAGE;
		int to = Math.min(lines.size(), from + LINES_PER_PAGE);
		int lineHeight = 20;
		int y = 53 + (194 - (to - from) * lineHeight) / 2 + g.getFontMetrics().getAscent();
		for (int i = from; i < to; i++)
		{
			center(g, lines.get(i), CARD_WIDTH / 2, y, INK);
			y += lineHeight;
		}
		g.setFont(FontManager.getRunescapeSmallFont());
		int pages = pageCount(lines);
		String footer = pages > 1 ? "Page " + (page + 1) + "/" + pages + " - click card"
			: total == 0 ? "An empty deck" : "Card " + number + " / " + total;
		center(g, footer, CARD_WIDTH / 2, CARD_HEIGHT - 20, MUTED);
		g.dispose();
		return image;
	}

	private static BufferedImage image(int width, int height)
	{
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	private static Graphics2D graphics(BufferedImage image)
	{
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		return g;
	}

	private static void frame(Graphics2D g, int x, int y, int width, int height, boolean hovered)
	{
		g.setColor(new Color(0, 0, 0, 100));
		g.fillRoundRect(x + 3, y + 3, width, height, 12, 12);
		g.setPaint(new GradientPaint(x, y, new Color(0x56544F), x, y + height, new Color(0x34332F)));
		g.fillRoundRect(x, y, width, height, 12, 12);
		g.setColor(new Color(0x211F1B));
		g.drawRoundRect(x, y, width - 1, height - 1, 12, 12);
		g.setColor(hovered ? GOLD : new Color(0x8C887D));
		g.drawRoundRect(x + 2, y + 2, width - 5, height - 5, 10, 10);
		g.setColor(new Color(0x262520));
		g.drawRoundRect(x + 7, y + 7, width - 15, height - 15, 6, 6);
		g.setColor(new Color(0x777164));
		for (int cornerX : new int[]{x + 11, x + width - 12})
		{
			for (int cornerY : new int[]{y + 11, y + height - 12})
			{
				g.fillRect(cornerX, cornerY, 2, 2);
			}
		}
	}

	private static void center(Graphics2D g, String text, int centerX, int baseline, Color color)
	{
		int x = centerX - g.getFontMetrics().stringWidth(text) / 2;
		g.setColor(Color.BLACK);
		g.drawString(text, x + 1, baseline + 1);
		g.setColor(color);
		g.drawString(text, x, baseline);
	}
}
