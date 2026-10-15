package com.osrsjokes;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
class JokeDeckOverlay extends Overlay
{
	enum Target { NONE, DECK, CARD, CLOSE, BLOCKED }

	private static final int CARD_X = 128;
	private final Client client;
	private JokeDeck deck = new JokeDeck(Collections.emptyList());
	private boolean visible;
	private Rectangle deckBounds = new Rectangle();
	private Rectangle cardBounds = new Rectangle();
	private Rectangle closeBounds = new Rectangle();
	private BufferedImage deckImage;
	private BufferedImage hoveredDeckImage;
	private BufferedImage cardImage;
	private BufferedImage cardBackImage;
	private long drawStarted;
	private boolean drawing;
	private List<String> lines = Collections.emptyList();
	private int page;

	@Inject
	JokeDeckOverlay(Client client, OsrsJokesPlugin plugin)
	{
		super(plugin);
		this.client = client;
		setPosition(OverlayPosition.TOP_LEFT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	synchronized void setVisible(boolean visible)
	{
		this.visible = visible;
		if (!visible)
		{
			drawing = false;
		}
		clearBounds();
	}

	synchronized void useDeck(List<String> jokes)
	{
		deck = new JokeDeck(jokes);
		reset();
	}

	synchronized void reset()
	{
		deck.reset();
		cardImage = null;
		cardBackImage = null;
		drawing = false;
		deckImage = null;
		hoveredDeckImage = null;
		lines = Collections.emptyList();
		page = 0;
		clearBounds();
	}

	synchronized Target targetAt(Point point)
	{
		if (!visible)
		{
			return Target.NONE;
		}
		if (drawing && (deckBounds.contains(point) || cardBounds.contains(point)))
		{
			return Target.BLOCKED;
		}
		if (closeBounds.contains(point))
		{
			return Target.CLOSE;
		}
		if (cardBounds.contains(point))
		{
			return Target.CARD;
		}
		return deckBounds.contains(point) ? Target.DECK : Target.NONE;
	}

	synchronized void activate(Target target)
	{
		if (!visible || drawing)
		{
			return;
		}
		switch (target)
		{
			case DECK:
				lines = JokeCardRenderer.lines(deck.draw());
				page = 0;
				updateCard();
				drawStarted = System.nanoTime();
				drawing = true;
				// The prior card's controls are no longer valid while a new card is drawn.
				cardBounds = new Rectangle();
				closeBounds = new Rectangle();
				break;
			case CARD:
				if (cardImage != null && JokeCardRenderer.pageCount(lines) > 1)
				{
					page = (page + 1) % JokeCardRenderer.pageCount(lines);
					updateCard();
				}
				break;
			case CLOSE:
				cardImage = null;
				cardBounds = new Rectangle();
				closeBounds = new Rectangle();
				break;
			default:
				break;
		}
	}

	private void updateCard()
	{
		cardImage = JokeCardRenderer.card(lines, page, deck.cardNumber(), deck.size());
	}

	@Override
	public synchronized Dimension render(Graphics2D graphics)
	{
		if (!visible || client.getGameState() != GameState.LOGGED_IN)
		{
			clearBounds();
			return null;
		}
		if (deckImage == null)
		{
			deckImage = JokeCardRenderer.deck(false);
			hoveredDeckImage = JokeCardRenderer.deck(true);
			cardBackImage = JokeCardRenderer.cardBack();
		}
		int x = getBounds().x;
		int y = getBounds().y;
		deckBounds = new Rectangle(x, y, JokeCardRenderer.DECK_WIDTH, JokeCardRenderer.DECK_HEIGHT);
		cardBounds = new Rectangle();
		closeBounds = new Rectangle();
		boolean hovered = deckBounds.contains(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY());
		graphics.drawImage(hovered ? hoveredDeckImage : deckImage, 0, 0, null);
		if (cardImage != null)
		{
			double progress = drawing ? DrawAnimation.progress(System.nanoTime() - drawStarted) : 1;
			if (progress >= 1)
			{
				drawing = false;
				graphics.drawImage(cardImage, CARD_X, 0, null);
				cardBounds = new Rectangle(x + CARD_X, y, JokeCardRenderer.CARD_WIDTH, JokeCardRenderer.CARD_HEIGHT);
				closeBounds = new Rectangle(x + CARD_X + JokeCardRenderer.CARD_WIDTH - 32, y + 9, 24, 24);
			}
			else
			{
				drawAnimated(graphics, progress, x, y);
			}
		}
		// Let an open game menu receive its own clicks, even if it covers the deck.
		if (client.isMenuOpen())
		{
			clearBounds();
		}
		return cardImage == null ? new Dimension(deckImage.getWidth(), deckImage.getHeight())
			: new Dimension(CARD_X + cardImage.getWidth(), cardImage.getHeight());
	}

	private void drawAnimated(Graphics2D graphics, double progress, int originX, int originY)
	{
		double eased = DrawAnimation.ease(progress);
		int fullWidth = (int) Math.round(104 + (JokeCardRenderer.CARD_WIDTH - 104) * eased);
		int height = (int) Math.round(148 + (JokeCardRenderer.CARD_HEIGHT - 148) * eased);
		int width = Math.max(3, (int) Math.round(fullWidth * Math.abs(Math.cos(Math.PI * progress))));
		int x = (int) Math.round(CARD_X * eased) + (fullWidth - width) / 2;
		int y = (int) Math.round(8 * Math.sin(Math.PI * progress));
		Graphics2D g = (Graphics2D) graphics.create();
		try
		{
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			BufferedImage image = progress < 0.5 ? cardBackImage : cardImage;
			g.drawImage(image, x, y, width, height, null);
		}
		finally
		{
			g.dispose();
		}
		// Block clicks on the moving card as well as the deck; neither advances mid-animation.
		cardBounds = new Rectangle(originX + x, originY + y, width, height);
	}

	private void clearBounds()
	{
		deckBounds = new Rectangle();
		cardBounds = new Rectangle();
		closeBounds = new Rectangle();
	}
}
