// JavaScript helper for JSONP functionality in Kotlin/Wasm
// Provides browser API access that's not directly available in Kotlin/Wasm

export function encodeURI(str) {
    return encodeURIComponent(str);
}

export function setWindowCallback(name, callback) {
    window[name] = function(response) {
        // Convert response object to JSON string for Kotlin
        const jsonString = JSON.stringify(response);
        callback(jsonString);
    };
}

export function deleteWindowCallback(name) {
    delete window[name];
}
