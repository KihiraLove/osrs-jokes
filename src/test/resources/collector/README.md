# Collector export fixtures

These UTF-8 files are copied unchanged from `docs/examples/` in
[osrs-jokes-collector](https://github.com/KihiraLove/osrs-jokes-collector).
The collector's `pnpm fixtures:write` command seeds an isolated PostgreSQL
schema, logs in, and saves the raw bytes from `POST /admin/export`. The renamed
fixture first submits the real deck-rename action. No JSON conversion is used.

Verified exporter implementation: collector revision
`f72a5d50a54384cd8a569d0f23cfaf25afd3b890` (the exporter is unchanged by the
compatibility work). Verified importer implementation: plugin revision
`8bcce9948d80a03f26c2a6773ea43dc141d2bd35` (also unchanged).

| File | SHA-256 |
| --- | --- |
| `one-deck.json` | `1b8a7cd4a1f19b22edc07527964655610580dbd493c1135d98bf029621103994` |
| `decks.json` | `f2075b39a75824b919152d1eb122228780cada635c64751e1f0c5922717e3655` |
| `renamed-decks.json` | `7299775506d677be4b22047be7093606ae399ec338d8fd54bdc8ae5f38b1178d` |

To refresh, use `pnpm fixtures:write` in the collector, review the output, then
copy these three files here byte-for-byte. Run `pnpm fixtures:check` there and
the focused tests here:

```sh
./gradlew test --tests com.osrsjokes.CollectorImportTest --tests com.osrsjokes.DeckCodecTest --tests com.osrsjokes.DeckStorageTest --tests com.osrsjokes.JokeDeckTest --tests com.osrsjokes.JokeSettingsPanelTest
```

On 2026-11-26, all 17 tests in those five classes passed with Gradle 8.10 and
Eclipse Temurin JDK 21, targeting Java 11 (class major version 55). Collector
fixture regeneration/checking and all nine export HTTP tests passed too.

Keep this manifest and the collector's `docs/plugin-compatibility.md` in sync.
These are automated codec/storage/UI-model checks, not in-game verification.
