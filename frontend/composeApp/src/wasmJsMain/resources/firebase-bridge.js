// Firebase Auth Bridge
function jsFirebaseInit(apiKey, projectId, appId, authDomain) {
    firebase.initializeApp({ apiKey: apiKey, projectId: projectId, appId: appId, authDomain: authDomain });
}
function jsUseFirebaseEmulator() {
    firebase.auth().useEmulator("http://localhost:9099");
}
function jsSignInAnonymously() {
    console.log('[Auth] Signing in anonymously...');
    return firebase.auth().signInAnonymously();
}
function jsGetIdToken() {
    const user = firebase.auth().currentUser;
    return user ? user.getIdToken(false) : Promise.resolve(null);
}
function jsGetIsAnonymous() {
    const user = firebase.auth().currentUser;
    return user ? user.isAnonymous : true;
}
function jsIsSignedIn() {
    return firebase.auth().currentUser !== null;
}
function jsLinkWithEmail(email, password) {
    const user = firebase.auth().currentUser;
    const cred = firebase.auth.EmailAuthProvider.credential(email, password);
    return user ? user.linkWithCredential(cred)
                : firebase.auth().createUserWithEmailAndPassword(email, password);
}
function jsSignInWithEmail(email, password) {
    return firebase.auth().signInWithEmailAndPassword(email, password);
}
async function jsSignInWithGoogle() {
    const user = firebase.auth().currentUser;
    console.log('[Auth] Sign in with Google — currentUser:', user ? `uid=${user.uid} isAnonymous=${user.isAnonymous}` : 'none');
    const provider = new firebase.auth.GoogleAuthProvider();
    try {
        return user ? await user.linkWithPopup(provider) : await firebase.auth().signInWithPopup(provider);
    } catch (error) {
        if (error.code === 'auth/credential-already-in-use') {
            if (error.credential) {
                console.log('[Auth] UC4: credential-already-in-use — signInWithCredential to switch to existing account');
                return await firebase.auth().signInWithCredential(error.credential);
            } else {
                console.warn('[Auth] UC4: credential-already-in-use but error.credential is null — cannot recover (emulator limitation?)');
            }
        }
        throw error;
    }
}
function jsSignInWithApple() {
    const user = firebase.auth().currentUser;
    const provider = new firebase.auth.OAuthProvider('apple.com');
    return user ? user.linkWithRedirect(provider) : firebase.auth().signInWithRedirect(provider);
}
async function jsSignInWithApplePopup(serviceId, apiBaseUrl) {
    // 1. Generate raw nonce (32 random bytes → base64url)
    const rawBytes = crypto.getRandomValues(new Uint8Array(32));
    const rawNonce = btoa(String.fromCharCode(...rawBytes))
        .replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');

    // 2. SHA-256 hash (Apple puts hashedNonce in id_token nonce claim)
    const hashBuffer = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(rawNonce));
    const hashedNonce = Array.from(new Uint8Array(hashBuffer))
        .map(b => b.toString(16).padStart(2, '0')).join('');

    // 3. Apple SDK popup
    AppleID.auth.init({
        clientId: serviceId,
        scope: 'name email',
        redirectURI: 'https://m14n.com',
        nonce: hashedNonce,
        usePopup: true,
    });
    const response = await AppleID.auth.signIn();
    const idToken = response.authorization.id_token;

    // 4. Exchange for Firebase custom token
    const res = await fetch(new URL('/auth/apple/custom-token', apiBaseUrl), {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ appleIdToken: idToken, nonce: rawNonce }),
    });
    if (!res.ok) throw new Error('[Auth] Apple custom token exchange failed: ' + res.status);
    const { customToken } = await res.json();

    // 5. Sign in — onAuthStateChanged fires and syncs state automatically
    await firebase.auth().signInWithCustomToken(customToken);
}
async function jsGetRedirectResult() {
    try {
        const result = await firebase.auth().getRedirectResult();
        if (result && result.user) {
            console.log('[Auth] Redirect result: uid=' + result.user.uid + ' provider=' + result.additionalUserInfo?.providerId);
        } else {
            console.log('[Auth] Redirect result: no pending redirect');
        }
    } catch (error) {
        console.error('[Auth] Redirect result error:', error.code, error.message);
        if (error.code === 'auth/credential-already-in-use') {
            const credential = error.credential
                || (() => { try { return firebase.auth.OAuthProvider.credentialFromError(error); } catch(e) { return null; } })()
                || (() => { try { return firebase.auth.GoogleAuthProvider.credentialFromError(error); } catch(e) { return null; } })();
            if (credential) {
                console.log('[Auth] Credential already in use — signing in with existing credential');
                await firebase.auth().signInWithCredential(credential);
            } else {
                console.warn('[Auth] credential-already-in-use but credential is null — cannot recover');
            }
        }
    }
}
function jsOnAuthStateChanged(callback) {
    firebase.auth().onAuthStateChanged(function(user) {
        if (!user) {
            // Null fires transiently on cold start before signInAnonymously completes.
            // When logout is implemented, it must update _authState manually in Kotlin
            // rather than relying on this null emission.
            console.log('[Auth] onAuthStateChanged: signed out — skipping');
            return;
        }
        console.log('[Auth] onAuthStateChanged: uid=' + user.uid
            + ' isAnonymous=' + user.isAnonymous
            + ' provider=' + (user.providerData[0]?.providerId ?? 'none'));
        callback(!user.isAnonymous);
    });
}
function jsGetCurrentUserEmail() {
    return firebase.auth().currentUser?.email ?? null;
}
function jsConsoleLog(msg) {
    console.log(msg);
}
function jsConsoleError(msg) {
    console.error(msg);
}

console.log('[Firebase Bridge] Initialized');
