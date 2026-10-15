package com.osrsjokes;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DrawAnimationTest
{
	@Test
	public void clampsBeforeStartAndAfterAStalledFrame()
	{
		assertEquals(0, DrawAnimation.progress(-1), 0);
		assertEquals(0, DrawAnimation.progress(0), 0);
		assertEquals(0.5, DrawAnimation.progress(DrawAnimation.DURATION_NANOS / 2), 0);
		assertEquals(1, DrawAnimation.progress(DrawAnimation.DURATION_NANOS), 0);
		assertEquals(1, DrawAnimation.progress(5_000_000_000L), 0);
	}

	@Test
	public void easingMovesForwardWithoutOvershooting()
	{
		double previous = 0;
		for (int i = 0; i <= 100; i++)
		{
			double current = DrawAnimation.ease(i / 100.0);
			assertTrue(current >= previous && current <= 1);
			previous = current;
		}
		assertEquals(1, previous, 0);
	}
}
