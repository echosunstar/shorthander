# Shorthander - there is only one

A privacy-first, widget-only 100% offline cryptographic scratchpad for Android. 

It does not seek to replace your favourite reminder or calendar app. It is meant for rapid telegraphic-style personal notes.

Built using Capacitor and Ionic, featuring native Android desktop widgets for immediate secure note-taking.

# The Philosophy: The "Anti-Productivity" Tool

Shorthander does not seek to replace **Notion, Obsidian, Google Calendar**, or your favorite heavy-duty second-brain app. 

Those platforms are built for structured knowledge management, complex database linking, and long-form planning. They excel at organization, but they introduce a bottleneck: **mental friction**. 

When you need to jot down a fleeting thought *right now*, opening a cloud-synced app, navigating a folder tree, creating a new page, and adding a title kills the moment. 

Shorthander is the digital equivalent of a crumpled piece of scrap paper. It is built for **Cognitive Shorthand** (see below) : minimalistic mental triggers that mean nothing to the rest of the world, but mean everything to your own brain in that exact moment.

**"Cognitive Shorthand"**

A stranger looking at your Android widget may see  see  gibberish but your brain doesn't need full sentences; it just needs a spark.

* **The Notion/Obsidian Way:** > *"Remember to check the tire pressure before the weekend road trip and make sure it's inflated to exactly 36 PSI."* (Requires formatting, categorization, and a checkbox).
* **The Shorthander Way:** > **`36 rt`**

To an outsider **`36 rt`** looks like random typing. To your brain, it instantly unlocks the entire memory cascade: *36 PSI, Road Trip*. 

Other everyday examples of Shorthander notes:
* **`b9 rtn`** -> Return the library book/parcel by the 9th.
* **`x del`** -> Delete my X account.

# Security Through Subtext

Because Shorthander logs are completely offline and encrypted, your data is physically safe. But by using cognitive shorthand, your notes gain a second layer of defense: **semantic privacy**. 

you can type fully-formed sentences of course, but it is designed first and foremost to hold abbreviations that mean only something to you.

Even if your phone is unlocked and someone shoulder-surfs your active home-screen widget, they cannot steal information they do not have the mental key to decode. 

# Keyboard Ergonomics: Zero-Friction Typing

Because Shorthander is built for hyper-rapid entry, every millisecond matters. 

On mobile devices, the single biggest physical bottleneck is **keyboard switching** : constantly tapping the `?123` or symbol keys to bounce between alphabetical letters and secondary layout grids just to type a single punctuation mark.

Shorthander eliminates this friction by allowing you to write multi-part notes without ever leaving your primary alphabetical keyboard layer.

## example: The Quick-Separator Macro (`..` ➔ ` # `)
When chaining multiple distinct shorthand notes together, you need a visual boundary to separate your thoughts. 

Instead of forcing you to open the symbol keyboard to find a hash (`#`) or pipe (`|`), Shorthander utilizes an easy-access double tap.

* **The Input:** Type two standard periods (`..`) right on your default letter keyboard.
* **The Expansion:** Shorthander instantly replaces them with a clean, padded structural separator (` # `).

### Example in Action:
* **What you type:** `by mlk..bk` *(11 seamless taps, 0 layout switches)*
* **What expands on screen:** `by mlk # bk`

By keeping your thumbs locked onto the default keyboard layer, you can stack contextual triggers sequentially without friction.

## Features

* **Abbreviations:** keyword macros  automatically expand into longer structural templates or text blocks when typed, eliminating repetitive keystrokes entirely.
* **Instant Home-Screen Widgets:** native Android desktop widget designed for rapid-fire data entry and instant visibility without needing to open a full application interface.
* **Zero-Permission Isolation:** Built without the `android.permission.INTERNET` flag. It is structurally impossible for your data, telemetry, or analytics to leave your physical hardware. No ads obviously.
* **Encrypted Storage:** All scratchpad data is fully encrypted at rest on your device, ensuring total security if your phone is lost or stolen.
* **Lightweight Footprint:** no frameworks, no background battery drain, dark-mode optimization .

# Privacy & Security Blueprint

Shorthander is engineered for data isolation. Your notes belong to you, and only you.

* **No Network Permissions:** The `android.permission.INTERNET` flag is absent from this application's manifest. It is physically impossible for the app to leak your data, serve ads, or phone home.
* **Cryptographic Storage:** All scratchpad data is fully encrypted at rest on your device hardware.
* **Verified Pipeline Provenance:** The APK  production binary is compiled and signed inside GitHub Actions

---

## Installation

### Option 1: Direct Download (Verified APK)
You can download the standalone installer directly from the official releases page:

**[Download Latest Shorthander APK](https://github.com/echosunstar/shorthander/releases/latest)**

*Note: Because this app is distributed independently of the main Google Play store ecosystem, your device will show a standard "Unknown Sources" or "File might be harmful" warning. You can safely bypass this, as the open-source footprint verifies zero tracking overhead.*

### Option 2: App Stores (Coming Soon)
* **F-Droid Track:** Submission in progress for inclusion in the official privacy-first open-source index.
* **Google Play Track:** Closed testing tracks currently preparing for mainstream distribution.

---

## Local Development & Build

If you want to audit the source code or compile the binary locally on your own machine:

```bash
# Install web assets and dependencies
npm install
npm run build

npx cap sync android

cd android
./gradlew installDebug