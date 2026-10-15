package com.osrsjokes;

import com.google.inject.Provides;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "OSRS Jokes",
	description = "Draw hand-written joke cards from a deck",
	tags = {"jokes", "cards", "deck", "humour"}
)
public class OsrsJokesPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private OsrsJokesConfig config;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private MouseManager mouseManager;
	@Inject
	private JokeDeckOverlay overlay;
	@Inject
	private JokeMouseListener mouseListener;
	@Inject
	private ConfigManager configManager;
	@Inject
	private ClientThread clientThread;
	@Inject
	private ClientToolbar clientToolbar;

	private ExecutorService ioExecutor;
	private Future<?> loadTask;
	private long generation;
	private long loadRequest;
	private boolean running;
	private List<DeckDefinition> decks = List.of();
	private String activeDeckId = "";
	private JokeSettingsPanel settingsPanel;
	private NavigationButton navigation;
	private String loadStatus = "Loading decks...";
	private boolean loading;
	private boolean loadError;

	@Override
	protected synchronized void startUp()
	{
		running = true;
		long session = ++generation;
		ioExecutor = Executors.newSingleThreadExecutor(runnable ->
		{
			Thread thread = new Thread(runnable, "osrs-jokes-decks");
			thread.setDaemon(true);
			return thread;
		});
		overlay.reset();
		updateVisibility();
		overlayManager.add(overlay);
		mouseManager.registerMouseListener(mouseListener);
		SwingUtilities.invokeLater(() -> createSettings(session));
		reloadDecks();
	}

	@Override
	protected synchronized void shutDown()
	{
		running = false;
		generation++;
		if (loadTask != null)
		{
			loadTask.cancel(true);
			loadTask = null;
		}
		if (ioExecutor != null)
		{
			ioExecutor.shutdownNow();
			ioExecutor = null;
		}
		if (navigation != null)
		{
			clientToolbar.removeNavigation(navigation);
			navigation = null;
		}
		settingsPanel = null;
		decks = List.of();
		activeDeckId = "";
		overlay.setVisible(false);
		mouseManager.unregisterMouseListener(mouseListener);
		mouseListener.reset();
		overlayManager.remove(overlay);
		overlay.useDeck(List.of());
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (OsrsJokesConfig.GROUP.equals(event.getGroup()))
		{
			clientThread.invoke(this::updateVisibility);
			SwingUtilities.invokeLater(() -> applySelection(false));
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		updateVisibility();
	}

	private synchronized void updateVisibility()
	{
		overlay.setVisible(running && config.showDeck() && client.getGameState() == GameState.LOGGED_IN);
	}

	private synchronized void createSettings(long session)
	{
		if (!running || session != generation)
		{
			return;
		}
		settingsPanel = new JokeSettingsPanel(
			id -> configManager.setConfiguration(OsrsJokesConfig.GROUP, "selectedDeck", id),
			visible -> configManager.setConfiguration(OsrsJokesConfig.GROUP, "showDeck", visible),
			this::reloadDecks);
		navigation = NavigationButton.builder().tooltip("OSRS Jokes settings")
			.icon(JokeSettingsPanel.icon()).panel(settingsPanel).priority(6).build();
		clientToolbar.addNavigation(navigation);
		refreshPanel();
	}

	private synchronized void reloadDecks()
	{
		if (!running)
		{
			return;
		}
		long session = generation;
		long request = ++loadRequest;
		if (loadTask != null)
		{
			loadTask.cancel(true);
		}
		loading = true;
		loadError = false;
		loadStatus = "Loading decks...";
		SwingUtilities.invokeLater(this::refreshPanel);
		loadTask = ioExecutor.submit(() ->
		{
			try
			{
				List<DeckDefinition> loaded = DeckStorage.load(DeckStorage.FILE);
				SwingUtilities.invokeLater(() -> finishLoad(session, request, loaded, null));
			}
			catch (IOException | IllegalArgumentException exception)
			{
				log.debug("Unable to load joke decks", exception);
				SwingUtilities.invokeLater(() -> finishLoad(session, request, null, exception.getMessage()));
			}
		});
	}

	private synchronized void finishLoad(long session, long request, List<DeckDefinition> loaded, String error)
	{
		// A stopped/restarted plugin or a superseded request must not receive stale results.
		if (!running || session != generation || request != loadRequest)
		{
			return;
		}
		loading = false;
		loadTask = null;
		loadError = loaded == null;
		if (loadError)
		{
			loadStatus = "Could not load decks: " + error + (decks.isEmpty()
				? ". Fix decks.json and reload." : ". Keeping the previously loaded decks.");
			refreshPanel();
			return;
		}
		decks = loaded;
		loadStatus = decks.size() + " decks loaded.";
		applySelection(true);
	}

	private synchronized void applySelection(boolean reload)
	{
		if (!running)
		{
			return;
		}
		DeckDefinition selected = decks.stream().filter(deck -> deck.getId().equals(config.selectedDeck()))
			.findFirst().orElse(decks.isEmpty() ? null : decks.get(0));
		String id = selected == null ? "" : selected.getId();
		if (reload || !id.equals(activeDeckId))
		{
			activeDeckId = id;
			overlay.useDeck(selected == null ? List.of() : selected.getJokes());
		}
		if (selected != null && !id.equals(config.selectedDeck()))
		{
			configManager.setConfiguration(OsrsJokesConfig.GROUP, "selectedDeck", id);
		}
		refreshPanel();
	}

	private synchronized void refreshPanel()
	{
		if (running && settingsPanel != null)
		{
			settingsPanel.update(decks, activeDeckId, config.showDeck());
			settingsPanel.status(loadStatus, loading, loadError);
		}
	}

	@Provides
	OsrsJokesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(OsrsJokesConfig.class);
	}
}
