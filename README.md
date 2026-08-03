# 🐾 Fitagotchi

**A gamified fitness tracker with a Tamagotchi-style virtual pet.**
Work out to level up, evolve, and care for your companion — or watch it starve if you skip too many days.

Built for **Android** with **Kotlin** + **Jetpack Compose**, in a Neo-Brutalist pixel-art style.

---

## 📖 What is Fitagotchi?

Fitagotchi turns exercise into pet care. You hatch a companion, and every workout you complete feeds it XP, earns coins, and keeps it happy. Neglect it — skip your workouts — and its hunger drains until it starves. It's a fitness app that gives you something to keep alive.

The whole thing runs on bodyweight, no-equipment exercises, and adapts its difficulty to how hard *you* say each session felt.

---

## ✨ Features

### 🥚 Onboarding & your pet
- Step-by-step setup: gender, goal, body shape, focus areas, birth year, height, weight
- iPhone-style scroll wheels for the number inputs, with **CM/FT** and **KG/LB** unit toggles
- A live **BMI** calculation with a health-category readout
- Pick one of four starter eggs — **Dog, Cat, Capybara, Rabbit** — and name your hatchling

### 🏠 The living dashboard
- Your pet lives in a box and behaves on its own: it **walks, hops, waddles, flies, and curls up to sleep** depending on the species
- **Double-tap** the pet to make it get up and wander, or lie down where it stands — otherwise it decides for itself on a random timer
- **Hunger** and **XP** bars, a level indicator, and a tap-to-rename name pill
- A **backpack drawer** that slides out to feed your pet food you've bought

### 💪 Smart workout engine
- Generates a circuit of **5–13 exercises** tailored to your body shape, BMI, and chosen focus areas
- 21 bodyweight exercises across Chest, Arms, Legs, Core, Back, and Cardio
- Reps for strength moves, timed seconds for cardio/isometric holds
- **Adaptive difficulty**: after each session you rate it *Too Hard / Just Right / Too Easy*, and the next workout scales accordingly
- Live rest timer, per-exercise info sheets, and a real workout **duration timer** that measures your actual time spent

### 🎁 Rewards & progression
- Earn **coins** per session, boosted by your daily **streak**
- Gain **XP**, level up, and **evolve** your pet at level 5
- **Each pet keeps its own level** — a level-5 dog stays level 5 even if you switch to a fresh level-1 capybara

### 🛒 Shop & 🎰 Gacha
- **Shop:** buy food (Apple, Ramen, Protein Shake) and habitat wallpapers
- **5 habitats:** Pixel Beach, Neon City, Retro Gym, Mossy Forest, Deep Space — equipping one swaps both the background *and* your pet's outfit
- **Gacha machine:** spend coins for random pulls — Common (food), Uncommon (wallpaper), Rare (a second-chance starter pet), Epic (the exclusive **Dragon**). Duplicates auto-refund coins.

### 📅 Calendar & 📊 stats
- A real-time calendar highlighting today in gold, with checkmarks on workout days
- A **"Me" profile page** with lifetime stats and rolling 7-day **duration & calorie** charts
- Calories estimated with the science-based formula: `kcal/min = MET × 3.5 × weight ÷ 200`

### 👤 Accounts
- **Register / login** with username + password (password shown/hidden via an eye toggle)
- Your full game state — coins, pet, level, inventory, history — saves to your account and restores on login
- Change your username or password (current password required), or wipe your account (password-verified)
- Pick from **5 pixel-pet avatars**

### 💀 Death & respawn
- Miss too many days and your pet starves — hunger drains ~25/day
- **Guests** lose everything and start over
- **Logged-in players** keep their coins and inventory, and respawn a new companion (or switch to another pet they own from the Gacha)

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose (100% — no XML layouts) |
| State | ViewModel + Compose state |
| Live persistence | Jetpack DataStore (whole game state as JSON) |
| Accounts database | SQLite (`SQLiteOpenHelper`) |
| Serialization | kotlinx-serialization |
| Build | Gradle (Kotlin DSL) |
| Design | Neo-Brutalist pixel art; Quicksand Bold (UI) + Space Mono (data) |

---

## 📂 Project Structure

```
app/src/main/java/com/fitagotchi/app/
├── MainActivity.kt            # Entry point + screen router
├── model/
│   └── Models.kt              # All data models, exercise library,
│                              #   game tuning constants, calorie engine
├── engine/
│   └── WorkoutEngine.kt       # Workout generation + difficulty scaling
├── vm/
│   └── AppViewModel.kt        # All app logic + state (the brain)
├── data/
│   ├── StateRepository.kt     # DataStore — live session persistence
│   └── UserDatabase.kt        # SQLite — multi-account storage
└── ui/
    ├── theme/Theme.kt         # Design system + brutal() modifier
    ├── components/            # Reusable widgets (buttons, cards, bars)
    ├── onboarding/            # Setup flow + scroll-wheel pickers
    ├── hub/                   # Dashboard, calendar, shop, living pet
    ├── workout/               # Active exercise, rest, review, rewards
    ├── gacha/                 # Gacha machine
    ├── me/                    # Profile hub, login/register, stats
    ├── respawn/               # Pet-death replacement picker
    └── settings/              # Preferences + demo tools
```

---

## 🚀 Getting Started

### Requirements
- Android Studio (latest stable)
- Android SDK 24+ (minSdk)
- An emulator or physical device

### Run it
1. Clone the repo:
   ```bash
   git clone https://github.com/<your-username>/fitagotchi.git
   ```
2. Open the project in Android Studio and let Gradle sync.
3. Press **Run ▶**.

### Fonts (optional but recommended)
The design uses **Quicksand Bold** and **Space Mono**. Without them the app falls back to system fonts and still runs. To add them:
1. Download the TTFs (Google Fonts).
2. Place them in `app/src/main/res/font/` with **lowercase, underscore** names:
   `quicksand_bold.ttf`, `space_mono.ttf`, `space_mono_bold.ttf`
3. Update the two font lines in `ui/theme/Theme.kt` (a comment there shows exactly how).

### 🔑 Demo account
A demo account is seeded from source code on first launch:

> **Username:** `demo`  **Password:** `1234`

It comes pre-loaded with an evolved pet, coins, items, and workout history so the app is immediately explorable.

---

## 🎬 Presentation / Demo Tools

Under **Me → Settings → Demo Tools** there's a panel built for live demos:
- **Gift Coins** (+1,000 / +10,000)
- **Live Hunger Decay** — drain hunger in real time to demo starvation
- **XP per Exercise** — crank it up to show evolution fast
- **Skip +1 Day** — fast-forward the in-app date to demo streak bonuses and the calendar without waiting real days

> ⏰ **Emulator timezone note:** emulators default to UTC. If the in-app date looks a day behind, set the emulator's timezone (Settings → System → Date & time) to your local zone.

---

## 🧠 How It Works (a few highlights)

- **One source of truth.** The entire game is a single `AppState` object. The ViewModel is the only thing that mutates it, and every change write-throughs to both DataStore (live) and SQLite (your account).
- **Adaptive difficulty.** A single `intensityMultiplier` (clamped 0.4–3.0) scales every exercise's reps/seconds, nudged ±20% by your post-workout feedback.
- **Per-pet progress.** Each pet's level/XP/evolution is banked separately, so switching companions doesn't share progress.
- **Calorie math.** Based on the Compendium of Physical Activities MET values — see `CalorieEngine` in `Models.kt`.

---

## ⚠️ Scope & Limitations

This is a student capstone project, built to demonstrate a complete, working Android app. A few honest notes:
- **Accounts are on-device.** SQLite stores accounts locally — they survive restarts and logout, but not uninstalling the app, and don't sync across devices. Real cross-device accounts would need a backend (e.g. Firebase Auth + Firestore).
- **Passwords** are SHA-256 hashed (never stored in plain text), but unsalted — fine for a demo, though production apps should use a salted, slow hash like bcrypt.
- **Calories are estimates**, not measurements — workout *duration* is measured live, but kcal is derived from MET values.

---

## 📜 Credits

- Exercise data based on the **Compendium of Physical Activities** MET values.
- Fonts: **Quicksand** and **Space Mono** (Google Fonts, Open Font License).
- Built as a Capstone Project.

---

*Keep your pet fed. Keep yourself moving.* 🐕💪
