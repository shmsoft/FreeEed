# Build & Release — quick reference

How to build FreeEed from `mark`, test it, and publish the daily. Written so you
don't have to re-derive it each time.

## Branch model
- **`main`** — frozen public release. Don't touch.
- **`mark`** — your staging. **Build and verify here.**
- **`dev`** — verified / published; the **daily that testers get builds from `dev`**.
- **Rule:** only `mark` flows into `dev` (never commit directly to `dev`), so
  `mark → dev` stays a clean `--ff-only` and the branches never diverge.

## Prereqs (once)
- `SHMSOFT_HOME` set (e.g. `~/projects/SHMsoft`) and `SCAIA_HOME` set. The release
  script exits immediately if `SHMSOFT_HOME` is unset.
- Both repos cloned at `$SHMSOFT_HOME/FreeEed` and `$SHMSOFT_HOME/FreeEedUI`.
- The release builds each repo from **whatever branch it's checked out on** — so
  both must be on `mark` for a mark build.
- Build from a **clean, committed** tree. A trailing `+` on the build SHA means
  uncommitted changes — don't promote such a build.

---

## A. Test build from `mark` (never touches S3)
1. Put both repos on `mark`:
   ```bash
   git -C $SHMSOFT_HOME/FreeEed   checkout mark
   git -C $SHMSOFT_HOME/FreeEedUI checkout mark
   ```
2. Build (Linux-only, no S3):
   ```bash
   cd $SHMSOFT_HOME/release && ./release.sh          # convenience wrapper (default: NO_UPLOAD + LINUX_ONLY)
   # or, equivalently, directly:
   NO_UPLOAD=1 LINUX_ONLY=1 $SHMSOFT_HOME/FreeEed/release_freeeed_complete.sh
   ```
   Output installer: `$SHMSOFT_HOME/release/<VERSION>/FreeEed-<VERSION>-Linux.run`
3. **Services are NOT needed to build** — the release runs `mvn ... -DskipTests`, so
   it doesn't depend on Solr/Tika. (Services are only for *testing*, below.)

## B. Install, then test
4. Install:
   ```bash
   $SHMSOFT_HOME/release/<VERSION>/FreeEed-<VERSION>-Linux.run   # installs to ~/.local/share/FreeEed
   ```
   The installer clears the old exploded review webapp so FreeEedUI updates deploy
   (otherwise Tomcat serves stale code).
5. **Start services** (now you need them):
   Control Panel → **Start All Services** (or `cd ~/.local/share/FreeEed && ./start_dev_services.sh`).
   Processing needs Solr + Tika; case creation needs the review app (Tomcat :8090).
6. Test: process a case, open it in review, exercise the stories (sort/paging,
   tag-all, export).
7. Stop when done: Control Panel → **Stop All Services** (or `./stop_dev_services.sh`).

## C. Verify which build you're running
The About dialog / Control Panel footer / pack `VERSION` show:
```
FreeEed <version> (build <YYYY-MM-DD HH:MM UTC>, g<sha>)
```
Confirm `g<sha>` matches your `mark` HEAD. A trailing `+` = built from a dirty tree.
Testers confirm their build the same way — have them paste that line in bug reports.

## D. Promote `mark → dev` (after it verifies)
```bash
for r in FreeEed FreeEedUI; do
  git -C $SHMSOFT_HOME/$r checkout dev
  git -C $SHMSOFT_HOME/$r merge --ff-only mark
  git -C $SHMSOFT_HOME/$r push origin dev
done
```
Only `mark → dev`, `--ff-only`. If it won't fast-forward, something landed on `dev`
directly — reconcile before publishing.

## E. Publish the daily (from `dev` → S3)
1. Both repos on `dev` (from step D).
2. Publish:
   ```bash
   PUBLISH=1 ./release.sh        # refuses unless the FreeEed repo is on 'dev'
   # or directly (uploads to S3):
   $SHMSOFT_HOME/FreeEed/release_freeeed_complete.sh
   ```
3. Testers download from the freeeed.org / S3 `-latest-` links and confirm their
   build via the About stamp (C).

## F. Cut an official release (from `dev` → `main`)
Done ~weekly, when a verified daily is worth marketing. **This is the ONLY time
`main` moves** — the marketed "released" build people download, not a daily/preview.
1. **Verify a daily (E) on the real path** — install the published `-latest-`, smoke-test.
2. **Drop the preview suffix:** set `V` in `Version.java` to the GA number
   (e.g. `10.8.6`, no `-PREVIEW`). Commit on `mark`; promote `mark → dev` (ff-only).
3. **Publish the GA from `dev`:** `PUBLISH=1 ./release.sh` — now builds
   `FreeEed-10.8.6-*` and overwrites the `-latest-` links with the GA. Confirm on S3.
4. **Mark the release in `main`:** `git checkout main && git merge --ff-only dev &&
   git push origin main` (both repos). `main` now == the published GA.
5. **Open the next cycle:** bump `V` to the next `-PREVIEW` (e.g. `10.8.7-PREVIEW`)
   on `mark`; promote `mark → dev`. Dailies resume, clearly ahead of the shipped GA.

The three channels: **internal** (A, from `mark`, no upload) → **daily** (E, from
`dev`, uploaded — testers) → **release** (F, to `main` — marketing/downloaders).

---

## Gotchas (learned the hard way)
- **Services: test only, not build.** The release skips tests; don't start services
  just to build.
- **No automated test gate.** `-DskipTests` means the JUnit suite doesn't run during
  release — manual testing is the current gate. (A separate `mvn test` with services
  up is the way to run the suite.)
- **Stale review webapp:** fixed — the installer now removes the old exploded
  `webapps/freeeedui` so updates deploy. If you ever see old FreeEedUI behavior after
  an update, that's the symptom; Stop All, delete that dir, Start All.
- **Stale Tomcat:** `stop_all.sh` force-kills it now. If `:8090` won't come up,
  Stop All → Start All.
- **Dirty build:** a `+` in the build SHA = uncommitted changes; commit first, don't
  promote a `+` build.
- **Solr match-all is `*:*`** (a bare `*` returns HTTP 500) — relevant if you touch
  review search code.
