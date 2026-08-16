# Noor — Islamic Companion App (Foundation build)

A green-themed Android app. This first build includes:

- **Prayer Times** (fully working): uses your location, calculates the five daily
  prayers offline, lets you switch between **Hanafi / Shafi / Jaffri**, and schedules
  an **azan notification** at each prayer time.
- **Listen / Read / Ayah**: navigable placeholders — added in the next builds.

## Get an installable APK without installing anything (GitHub Actions)

1. Create a free account at github.com and click **New repository** (name it e.g. `noor`).
2. Upload every file/folder from this project into that repo
   (drag them into the "uploading an existing file" page, or use GitHub Desktop).
3. Go to the repo's **Actions** tab → if prompted, enable workflows.
4. The **Build APK** workflow runs automatically on upload. Click the latest run,
   wait ~3–5 min, then download the **Noor-debug-apk** artifact at the bottom.
5. Copy the `.apk` to your phone and open it. Allow "install from unknown sources"
   when Android asks. Done.

To rebuild anytime: Actions tab → **Build APK** → **Run workflow**.

## On your phone, first launch

- Open the app → **Set my location** → allow location + notifications.
- Pick your school of thought. Prayer times appear and azan alarms are scheduled.
- On some phones, allow **Alarms & reminders** for Noor in system settings so the
  azan fires exactly on time, and disable battery optimisation for the app.

## Notes / accuracy

- The Arabic Qur'an text is the same for everyone — Tajweed / IndoPak / Uthmani are
  *script styles*, handled later as a display setting (not tied to school of thought).
- School of thought affects **prayer times** here: Hanafi = later Asr; Shafi = standard
  Asr (both via the Karachi method used in Pakistan); Jaffri = Shia Ithna-Ashari angles.
- Custom azan audio: drop an `azan.mp3` into `app/src/main/res/raw/` and set it as the
  notification-channel sound in `AdhanNotifier.kt`.

## Build in Android Studio instead (optional)

Open the project folder, let Gradle sync, then **Build → Build APK(s)**.
If it complains about a missing Gradle wrapper, run `gradle wrapper` in the project
folder once (or let Android Studio regenerate it).
