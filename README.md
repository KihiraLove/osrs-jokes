# OSRS Jokes

A clientside deck of hand-written jokes for RuneLite. Cards use an OSRS-grey
bevelled frame and RuneLite's bundled RuneScape font. No network requests, chat
messages, or game actions are involved.

## Using the deck

- Enable **Show deck** in the OSRS Jokes settings (off by default).
- Open the card icon in RuneLite's sidebar for the **Current deck** dropdown.
  It lists all decks from your file and saves the selection by deck ID. RuneLite's
  standard config dropdowns only support fixed enums, so the dynamic dropdown is
  in this plugin's settings panel. Show deck is available in both places.
- While logged in, left-click the deck. The next card slides out, grows and flips
  face-up over 440 ms. Extra clicks during the animation do not skip cards.
- Jokes follow their order in the selected deck; the last wraps to the first.
- Click the small **X** on a card to put it away. The deck stays visible.
- Hold **Alt** and drag to move the overlay using RuneLite's usual controls.
- Long jokes wrap across pages. Click the drawn card to cycle through its pages;
  clicking the deck always draws the next joke.
- Turning Show deck off hides both deck and card. Turning it on restores the
  current card. Hiding mid-animation finishes the animation immediately.
- Switching decks, successfully reloading, or restarting the plugin puts away
  the drawn card and starts the chosen deck from its first joke.

## Adding your jokes

Edit `.runelite/osrs-jokes/decks.json`, then click **Reload decks** in the sidebar.
The file is read at startup too; no rebuild is needed. The plugin creates the
folder and a starter file if it is missing. The General and OSRS starter decks
are empty unless you had already populated the old `JokeBook.JOKES` list, which
seeds General on first creation only.

The format is a small JSON dialect: double-quoted strings, objects and arrays,
plus `//` line comments, `/* block comments */` and optional trailing commas.
Our own `DeckCodec.serialize` / `deserialize` methods handle it without a JSON
library or Java object serialization. The serializer emits plain JSON; loading
never rewrites an existing file, so comments and formatting stay intact.

```jsonc
{
  "decks": [
    {
      "id": "general",
      "name": "General",
      "jokes": [
        "Your first setup\n\nYour first punchline",
        "Your second setup\n\nYour second punchline",
      ],
    },
    {
      "id": "osrs",
      "name": "OSRS",
      "jokes": [],
    },
  ],
}
```

Use `\n` for a line break and `\n\n` for a blank line. Escape quotation marks as
`\"` and backslashes as `\\`. Standard JSON string escapes, including `\uXXXX`,
are supported. Blank joke strings are ignored when drawing cards.

Add as many deck objects as you like. Each needs a unique, non-blank `id`, a
non-blank display `name`, and a `jokes` array of strings. Keep IDs stable when
renaming/reordering decks. If the selected deck is removed, the first remaining
deck is selected. An empty deck (or empty collection) draws a “No jokes yet” card.

Malformed files report the line and column in the settings panel. A failed reload
keeps the previous decks and current card; fix the file and reload again. Files
are loaded on a background worker, never on the client/render thread. Shutdown
cancels the worker and discards any stale result.

## Development

Java sources target Java 11. `./gradlew test` runs offline codec, storage, deck,
animation, layout and mouse interaction tests. `./gradlew run` is the development
entry point. No new dependencies are required.

In-game verification is manual: check Show deck on/off, repeated draws and
wraparound with your jokes, rapid clicks during animation, switching decks,
selection persistence after restart, and reload after valid/invalid edits.
Check hiding or disabling during a draw and ensure card/deck clicks do not
click through to the game in both fixed and resizable layouts.

Rendering approach reference: [OSRS TCG](https://github.com/Azderi/osrs-tcg),
which uses RuneLite overlays, Java2D card painting and mouse listeners. The
card artwork and implementation here are drawn locally without TCG assets.
