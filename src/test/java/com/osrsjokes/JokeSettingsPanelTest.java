package com.osrsjokes;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JokeSettingsPanelTest
{
	@Test
	public void dropdownUsesStableIdsAndRefreshDoesNotWriteSelection() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			List<String> selected = new ArrayList<>();
			JokeSettingsPanel panel = new JokeSettingsPanel(selected::add, ignored -> { }, () -> { });
			List<DeckDefinition> decks = List.of(new DeckDefinition("first", "First", List.of()),
				new DeckDefinition("second", "Second", List.of()));
			panel.update(decks, "second", true);
			JComboBox<?> combo = null;
			for (Component component : panel.getComponents())
			{
				if (component instanceof JComboBox)
				{
					combo = (JComboBox<?>) component;
				}
			}
			assertNotNull(combo);
			assertEquals("second", ((DeckDefinition) combo.getSelectedItem()).getId());
			assertTrue(selected.isEmpty());
			combo.setSelectedIndex(0);
			assertEquals(List.of("first"), selected);
			panel.update(List.of(), "", false);
			assertFalse(combo.isEnabled());
			assertEquals(List.of("first"), selected);
		});
	}
}
