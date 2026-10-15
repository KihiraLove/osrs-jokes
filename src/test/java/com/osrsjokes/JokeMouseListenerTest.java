package com.osrsjokes;

import java.awt.Canvas;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JokeMouseListenerTest
{
	private final StubOverlay overlay = new StubOverlay();
	private final JokeMouseListener listener = new JokeMouseListener(overlay);
	private final Canvas canvas = new Canvas();

	@Test
	public void drawConsumesWholeClickAndActivatesOnceOnRelease()
	{
		assertTrue(listener.mousePressed(event(MouseEvent.MOUSE_PRESSED, 20, 0, 1)).isConsumed());
		assertEquals(0, overlay.activations);
		assertTrue(listener.mouseReleased(event(MouseEvent.MOUSE_RELEASED, 20, 0, 1)).isConsumed());
		assertTrue(listener.mouseClicked(event(MouseEvent.MOUSE_CLICKED, 20, 0, 1)).isConsumed());
		assertEquals(1, overlay.activations);
	}

	@Test
	public void draggingAwayCancelsDrawButStillConsumesRelease()
	{
		listener.mousePressed(event(MouseEvent.MOUSE_PRESSED, 20, 0, 1));
		assertTrue(listener.mouseDragged(event(MouseEvent.MOUSE_DRAGGED, 200, 0, 0)).isConsumed());
		assertTrue(listener.mouseReleased(event(MouseEvent.MOUSE_RELEASED, 200, 0, 1)).isConsumed());
		assertEquals(0, overlay.activations);
	}

	@Test
	public void altDragRightClicksAndOutsideClicksPassThrough()
	{
		assertFalse(listener.mousePressed(event(MouseEvent.MOUSE_PRESSED, 20, InputEvent.ALT_DOWN_MASK, 1)).isConsumed());
		assertFalse(listener.mousePressed(event(MouseEvent.MOUSE_PRESSED, 20, 0, 3)).isConsumed());
		assertFalse(listener.mousePressed(event(MouseEvent.MOUSE_PRESSED, 200, 0, 1)).isConsumed());
		assertFalse(listener.mouseReleased(event(MouseEvent.MOUSE_RELEASED, 20, 0, 1)).isConsumed());
		assertEquals(0, overlay.activations);
	}

	@Test
	public void pressDuringAnimationCannotDrawWhenAnimationFinishesBeforeRelease()
	{
		overlay.drawing = true;
		assertTrue(listener.mousePressed(event(MouseEvent.MOUSE_PRESSED, 20, 0, 1)).isConsumed());
		overlay.drawing = false;
		assertTrue(listener.mouseReleased(event(MouseEvent.MOUSE_RELEASED, 20, 0, 1)).isConsumed());
		assertEquals(0, overlay.activations);
	}

	@Test
	public void hidingBetweenPressAndReleaseCancelsDraw()
	{
		listener.mousePressed(event(MouseEvent.MOUSE_PRESSED, 20, 0, 1));
		overlay.hidden = true;
		assertTrue(listener.mouseReleased(event(MouseEvent.MOUSE_RELEASED, 20, 0, 1)).isConsumed());
		assertEquals(0, overlay.activations);
		assertFalse(listener.mousePressed(event(MouseEvent.MOUSE_PRESSED, 20, 0, 1)).isConsumed());
	}

	private MouseEvent event(int id, int x, int modifiers, int button)
	{
		return new MouseEvent(canvas, id, 0, modifiers, x, 20, 1, false, button);
	}

	private static class StubOverlay extends JokeDeckOverlay
	{
		private int activations;
		private boolean hidden;
		private boolean drawing;

		StubOverlay()
		{
			super(null, null);
		}

		@Override
		synchronized Target targetAt(Point point)
		{
			return !hidden && point.x < 100 ? (drawing ? Target.BLOCKED : Target.DECK) : Target.NONE;
		}

		@Override
		synchronized void activate(Target target)
		{
			activations++;
		}
	}
}
