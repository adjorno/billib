// localStorage Bridge for Artwork Persistence
function jsLocalStorageGet(key) {
    return localStorage.getItem(key);
}

function jsLocalStorageSet(key, value) {
    localStorage.setItem(key, value);
}

console.log('[localStorage Bridge] Initialized');
