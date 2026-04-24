# Play Store Submission Checklist

Everything you need to submit TileBlast to Google Play, organized in upload order.

## 0. Before you start
- [x] Signed AAB built — `app/build/outputs/bundle/release/app-release.aab` (9.26 MB)
- [x] Keystore created — `tileblast-release.jks` (gitignored)
- [ ] **Back up the keystore file + password somewhere safe** ← do this NOW
- [ ] $25 paid + Play Console account approved (1–2 day wait after signup)

## 1. Create the app
Play Console → Create app
- Name: TileBlast
- Game / Free / English (US)

## 2. Upload AAB to Internal Testing first
Release → Testing → Internal testing → Create release → upload `app-release.aab`
- Enable Play App Signing when prompted (recommended)
- Add yourself as a tester (your Gmail) so you can install via Play Store on test device

## 3. Required Policy / App Content sections
Each of these must be filled in. Files in this folder match each one:

| Section | Source | Notes |
|---|---|---|
| Privacy policy | `privacy_policy.md` | Host as a public URL — see below |
| Ads | listing.md | Answer YES, contains ads |
| Content rating | `listing.md` (IARC section) | All NO answers → E rating |
| Data safety | `data_safety.md` | Step-by-step answer key |
| Target audience | `listing.md` | 13+ |
| News app | — | NO |
| Government app | — | NO |
| COVID-19 contact tracing | — | NO |
| Health content | — | NO |
| Financial features | — | NO |
| Loot boxes | — | NO |

## 4. Main store listing
Play Console → Grow → Store presence → Main store listing.
Pull copy from `listing.md`.

Required graphics:
- [ ] App icon — **512 × 512 PNG, no alpha**
- [ ] Feature graphic — **1024 × 500 JPG/PNG**
- [ ] At least 2 phone screenshots — saved in `screenshots/`
- [ ] (Optional) 7" + 10" tablet screenshots
- [ ] (Optional) Promo video (YouTube URL)

## 5. Hosting the privacy policy

Play Console requires the privacy policy at a **public URL** (raw .md or pdf is fine, but rendered HTML is best). Easiest free options:

### Option A — GitHub Pages (free, takes 5 minutes)
1. In your TileBlast GitHub repo: Settings → Pages → Source = `main` branch, `/docs` folder
2. Create `docs/privacy.md` with the content from `privacy_policy.md`
3. Wait ~1 min for GitHub to build
4. URL: `https://dj2nazty.github.io/TileBlast/privacy.html` (or `.md`)

### Option B — Free privacy generator with hosting
- https://app-privacy-policy-generator.firebaseapp.com — fill form, copy generated text, host on their site or yours
- https://www.privacypolicies.com — free tier hosts the page for you

### Option C — Upload to Google Sites (free)
Create a one-page Google Site, paste the policy, publish. URL: `https://sites.google.com/view/tileblast-privacy`

Pick whichever takes less than 10 minutes.

## 6. Promote to Production
After internal testing works on at least one real device:
Release → Production → Create release → reuse same AAB → Rollout
First-release review takes 3–7 days.

## Files in this folder
- `listing.md` — store metadata, descriptions, content rating answers
- `privacy_policy.md` — privacy policy text (host this)
- `data_safety.md` — answer key for the Data safety form
- `screenshots/` — raw screenshots from the emulator (no phone frame)
- `README.md` — this file
