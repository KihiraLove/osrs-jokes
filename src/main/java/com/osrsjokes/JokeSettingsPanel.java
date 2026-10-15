package com.osrsjokes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import net.runelite.client.ui.PluginPanel;

/** Created and updated exclusively on the Swing event thread. */
final class JokeSettingsPanel extends PluginPanel
{
	private final JCheckBox showDeck = new JCheckBox("Show deck");
	private final JComboBox<DeckDefinition> decks = new JComboBox<>();
	private final JButton reload = new JButton("Reload decks");
	private final JTextArea status = text("");
	private boolean updating;

	JokeSettingsPanel(Consumer<String> select, Consumer<Boolean> visibility, Runnable reloadDecks)
	{
		add(new JLabel("OSRS Jokes settings"));
		add(showDeck);
		add(new JLabel("Current deck"));
		decks.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 12, 28));
		add(decks);
		add(reload);
		add(text("Edit .runelite/osrs-jokes/decks.json, then reload to apply your changes."));
		add(status);
		decks.addActionListener(event ->
		{
			DeckDefinition selected = (DeckDefinition) decks.getSelectedItem();
			if (!updating && selected != null)
			{
				select.accept(selected.getId());
			}
		});
		showDeck.addActionListener(event ->
		{
			if (!updating)
			{
				visibility.accept(showDeck.isSelected());
			}
		});
		reload.addActionListener(event -> reloadDecks.run());
	}

	void update(List<DeckDefinition> definitions, String selectedId, boolean visible)
	{
		updating = true;
		decks.removeAllItems();
		for (DeckDefinition definition : definitions)
		{
			decks.addItem(definition);
			if (definition.getId().equals(selectedId))
			{
				decks.setSelectedItem(definition);
			}
		}
		decks.setEnabled(!definitions.isEmpty());
		showDeck.setSelected(visible);
		updating = false;
	}

	void status(String message, boolean loading, boolean error)
	{
		status.setText(message);
		status.setForeground(error ? new Color(0xFF9B85) : new Color(0xCFCBC2));
		reload.setEnabled(!loading);
		revalidate();
		repaint();
	}

	private static JTextArea text(String value)
	{
		JTextArea text = new JTextArea(value);
		text.setEditable(false);
		text.setOpaque(false);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		return text;
	}

	static BufferedImage icon()
	{
		BufferedImage icon = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = icon.createGraphics();
		g.setColor(new Color(0x77746C));
		g.fillRoundRect(7, 4, 15, 19, 4, 4);
		g.setColor(new Color(0x45433E));
		g.fillRoundRect(2, 1, 15, 19, 4, 4);
		g.setColor(new Color(0xCFB778));
		g.drawRoundRect(2, 1, 15, 19, 4, 4);
		g.drawPolygon(new int[]{9, 13, 9, 5}, new int[]{5, 10, 15, 10}, 4);
		g.dispose();
		return icon;
	}
}
