(function () {
    'use strict';

    // ── Styles ──────────────────────────────────────────────────────────────────
    const styleEl = document.createElement('style');
    styleEl.textContent = `
        #splash-screen {
            position: fixed;
            inset: 0;
            overflow: hidden;
        }
        .glitch-text {
            text-shadow: 2px 0 #7c4dff, -2px 0 #ff4db8;
        }
        .bg-gradient-mesh {
            background-color: #140f23;
            background-image:
                radial-gradient(at 0% 0%, rgba(124, 77, 255, 0.15) 0px, transparent 50%),
                radial-gradient(at 100% 100%, rgba(124, 77, 255, 0.1) 0px, transparent 50%);
        }
        @keyframes typewriter {
            from { width: 0; }
            to   { width: 100%; }
        }
        .typewriter-text {
            display: inline-block;
            overflow: hidden;
            white-space: nowrap;
            border-right: 2px solid #7c4dff;
            animation: typewriter 3s steps(20, end) infinite;
        }
    `;
    document.head.appendChild(styleEl);

    // ── DOM ─────────────────────────────────────────────────────────────────────
    const splash = document.createElement('div');
    splash.id = 'splash-screen';
    splash.innerHTML = `
        <div class="relative flex h-full w-full flex-col items-center justify-center bg-gradient-mesh">

            <!-- Status bar mimic -->
            <div class="absolute top-0 w-full flex justify-between items-center p-6 opacity-60">
                <div class="text-sm font-medium tracking-tight">9:41</div>
                <div class="flex gap-2">
                    <span class="material-symbols-outlined text-[18px]">signal_cellular_4_bar</span>
                    <span class="material-symbols-outlined text-[18px]">wifi</span>
                    <span class="material-symbols-outlined text-[18px]">battery_full</span>
                </div>
            </div>

            <!-- Central logo -->
            <div class="flex flex-col items-center">
                <div class="mb-8 flex flex-col items-center">
                    <span class="font-mono text-primary text-sm tracking-widest typewriter-text">Music Malfunction</span>
                </div>
                <div class="mb-12 relative">
                    <div class="absolute inset-0 bg-primary/20 blur-3xl rounded-full scale-150"></div>
                    <h1 class="relative text-primary text-[84px] font-bold leading-none tracking-tighter glitch-text">
                        M14N
                    </h1>
                </div>
            </div>

            <!-- Bottom branding -->
            <div class="absolute bottom-12 flex flex-col items-center gap-2">
                <p class="text-primary/60 text-[10px] uppercase tracking-[0.3em] mb-1">Created by</p>
                <p class="text-slate-100 text-lg font-medium tracking-wide typewriter-text">
                    Mykhailo Dorokhin
                </p>
                <div class="flex items-center gap-3 mt-4">
                    <span class="h-[1px] w-8 bg-primary/30"></span>
                    <span class="text-primary/50 text-[10px] font-mono tracking-tighter">V1.0.0</span>
                    <span class="h-[1px] w-8 bg-primary/30"></span>
                </div>
            </div>

            <!-- Decorative blobs -->
            <div class="absolute top-1/4 -left-12 w-64 h-64 bg-primary/5 rounded-full blur-[100px]"></div>
            <div class="absolute bottom-1/4 -right-12 w-64 h-64 bg-primary/5 rounded-full blur-[100px]"></div>

        </div>
    `;
    document.body.insertBefore(splash, document.body.firstChild);
})();
