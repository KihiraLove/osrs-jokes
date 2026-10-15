package com.osrsjokes;

import java.awt.event.MouseEvent;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.input.MouseAdapter;

@Singleton
class JokeMouseListener extends MouseAdapter
{
	private final JokeDeckOverlay overlay;
	private JokeDeckOverlay.Target pressed = JokeDeckOverlay.Target.NONE;
	private boolean consumeClick;

	@Inject
	JokeMouseListener(JokeDeckOverlay overlay)
	{
		this.overlay = overlay;
	}

	@Override
	public synchronized MouseEvent mousePressed(MouseEvent event)
	{
		if (event.getButton() != MouseEvent.BUTTON1)
		{
			return event;
		}
		reset();
		// RuneLite's Alt-drag overlay management must keep receiving these events.
		if (!event.isConsumed() && !event.isAltDown())
		{
			pressed = overlay.targetAt(event.getPoint());
			if (pressed != JokeDeckOverlay.Target.NONE)
			{
				event.consume();
			}
		}
		return event;
	}

	@Override
	public synchronized MouseEvent mouseReleased(MouseEvent event)
	{
		if (event.getButton() == MouseEvent.BUTTON1 && pressed != JokeDeckOverlay.Target.NONE)
		{
			if (!event.isAltDown() && overlay.targetAt(event.getPoint()) == pressed)
			{
				overlay.activate(pressed);
			}
			pressed = JokeDeckOverlay.Target.NONE;
			consumeClick = true;
			event.consume();
		}
		return event;
	}

	@Override
	public synchronized MouseEvent mouseClicked(MouseEvent event)
	{
		if (event.getButton() == MouseEvent.BUTTON1 && consumeClick)
		{
			consumeClick = false;
			event.consume();
		}
		return event;
	}

	@Override
	public synchronized MouseEvent mouseDragged(MouseEvent event)
	{
		if (pressed != JokeDeckOverlay.Target.NONE)
		{
			event.consume();
		}
		return event;
	}

	synchronized void reset()
	{
		pressed = JokeDeckOverlay.Target.NONE;
		consumeClick = false;
	}
}
