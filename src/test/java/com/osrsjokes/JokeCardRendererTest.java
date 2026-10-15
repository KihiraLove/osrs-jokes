package com.osrsjokes;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JokeCardRendererTest
{
	@Test
	public void wrappingPreservesParagraphsAndNeverLosesLongWords()
	{
		Graphics2D g = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
		try
		{
			g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
			FontMetrics metrics = g.getFontMetrics();
			String text = "Short setup\n\nAverylongunbrokensentencewithnospaces at all";
			List<String> lines = JokeCardRenderer.wrap(text, metrics, 90);
			assertTrue(lines.contains(""));
			assertEquals(text.replaceAll("\\s", ""), String.join("", lines).replace(" ", ""));
			for (String line : lines)
			{
				assertTrue(line, metrics.stringWidth(line) <= 90);
			}
		}
		finally
		{
			g.dispose();
		}
	}

	@Test
	public void recognizesWindowsLineBreaksAndPageBoundaries()
	{
		Graphics2D g = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
		try
		{
			assertEquals(List.of("Setup", "", "Punchline"),
				JokeCardRenderer.wrap("Setup\r\n\r\nPunchline", g.getFontMetrics(), 180));
			assertEquals(1, JokeCardRenderer.pageCount(Collections.nCopies(10, "line")));
			assertEquals(2, JokeCardRenderer.pageCount(Collections.nCopies(11, "line")));
		}
		finally
		{
			g.dispose();
		}
	}
}
