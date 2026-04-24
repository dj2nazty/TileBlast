# Data Safety Form Answers

This is the cheat-sheet for filling out **Play Console → App content → Data safety**.

## Data collection and security

### Does your app collect or share any of the required user data types?
**YES** (because of AdMob — Google collects advertising data)

### Is all of the user data collected by your app encrypted in transit?
**YES** (AdMob uses HTTPS)

### Do you provide a way for users to request that their data be deleted?
**YES** — uninstalling the app removes all local data; Advertising ID can be reset in Android Settings → Google → Ads.

## Data types collected

Walk through each category. Mark only these:

### Location
- **Approximate location**: COLLECTED
  - Collected by: third party (Google AdMob)
  - Purpose: Advertising or marketing
  - Optional: No
  - Shared: No (collected by Google, not shared by us)

### Personal info
None.

### Financial info
None.

### Health and fitness
None.

### Messages
None.

### Photos and videos
None.

### Audio files
None.

### Files and docs
None.

### Calendar
None.

### Contacts
None.

### App activity
- **App interactions**: COLLECTED (ad clicks, ad views — by AdMob)
  - Purpose: Advertising or marketing
  - Optional: No

### Web browsing
None.

### App info and performance
- **Crash logs**: NOT COLLECTED (we don't use Firebase Crashlytics or similar)
- **Diagnostics**: NOT COLLECTED

### Device or other IDs
- **Device or other IDs**: COLLECTED (Advertising ID)
  - Purpose: Advertising or marketing
  - Optional: No
  - Shared: No

## Summary
Three checkboxes:
1. ☑ Approximate location → Advertising/marketing
2. ☑ App interactions → Advertising/marketing
3. ☑ Device or other IDs → Advertising/marketing

Everything else: leave unchecked.
