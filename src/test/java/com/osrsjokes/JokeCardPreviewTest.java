package com.osrsjokes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Offline Java2D preview; never creates or interacts with a game client. */
public class JokeCardPreviewTest
{
	@Test
	public void rendersWithBundledRunescapeFont() throws Exception
	{
		List<String> lines = JokeCardRenderer.lines("Your joke setup goes here.\n\nYour punchline goes here.");
		BufferedImage card = JokeCardRenderer.card(lines, 0, 1, 3);
		assertEquals(228, card.getWidth());
		assertEquals(304, card.getHeight());
		assertTrue((card.getRGB(112, 150) >>> 24) > 0);
		BufferedImage preview = new BufferedImage(396, 344, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = preview.createGraphics();
		g.setColor(new Color(0x20201E));
		g.fillRect(0, 0, preview.getWidth(), preview.getHeight());
		g.drawImage(JokeCardRenderer.deck(false), 20, 20, null);
		g.drawImage(card, 148, 20, null);
		g.dispose();
		File output = new File("build/previews/joke-cards.png");
		Files.createDirectories(output.toPath().getParent());
		ImageIO.write(preview, "png", output);
	}
}
