package com.osrsjokes;

/** Time-based animation, independent of frame rate and without scheduled tasks. */
final class DrawAnimation
{
	static final long DURATION_NANOS = 440_000_000L;

	private DrawAnimation()
	{
	}

	static double progress(long elapsedNanos)
	{
		return Math.max(0, Math.min(1, (double) elapsedNanos / DURATION_NANOS));
	}

	static double ease(double progress)
	{
		return 1 - Math.pow(1 - progress, 3);
	}
}
