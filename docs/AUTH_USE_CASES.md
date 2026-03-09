# Authentication Use Cases

These are the four canonical authentication use cases for M14N. All platform implementations (Android, iOS, JVM, wasmJs) must comply with this spec. Technical details (which SDK, where the identity is stored locally) vary per platform — the expected behaviour does not.

---

## UC1 — First Launch

App has no stored user identity.

**Flow:**
1. App detects no existing identity in local storage
2. App creates an anonymous user via the auth provider
3. Anonymous identity is stored locally
4. App syncs the new identity with the backend

**Result:** User can use the app without identifying themselves. Backend has a new anonymous profile.

---

## UC2 — Subsequent Launch

App finds a stored user identity.

**Flow:**
1. App finds an existing identity in local storage
2. App restores the session (no new user created)
3. App syncs with the backend

**Result:** User continues exactly as they were — anonymous or authenticated. Nothing new is created.

---

## UC3 — Sign In with Google (No Prior Account)

User has an anonymous identity and authenticates with Google for the first time.

**Flow:**
1. User triggers Google sign-in
2. Auth provider confirms no existing account for this Google identity
3. A new authenticated identity is created, linked to the Google account
4. The app transitions from anonymous to authenticated
5. Personal data (email, display name) is attached to the identity
6. App syncs the updated identity with the backend

**Result:** User is now authenticated. Backend profile has `is_anonymous = false` with email and display name populated. Next app launch follows UC2 as an authenticated user.

---

## UC4 — Sign In with Google (Prior Account Exists)

User has an anonymous identity on a new device or channel, and authenticates with Google. The auth provider recognises the Google account as belonging to a previously existing user.

**Flow:**
1. User triggers Google sign-in
2. Auth provider returns the identity of the previously existing user (not the current anonymous one)
3. App discards the current anonymous identity
4. App switches to the previous user's identity
5. App syncs with the backend

**Result:** User is authenticated with their existing account across all devices and channels. The abandoned anonymous identity is no longer used. Backend profile from the previous account is preserved.

---

## Out of Scope (for now)

- **Sign out**: not defined. Once authenticated, there is no specified path back to anonymous.
- **Account deletion**: not defined.
- **Linking multiple providers** (e.g., Google + Apple on the same account): not defined.
