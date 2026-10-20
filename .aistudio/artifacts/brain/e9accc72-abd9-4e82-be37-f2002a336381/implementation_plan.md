# FrameShift Two-Way AI Conversion Studio — Fidelity Sliders, Owner-Only Admin & Clean Glass UI

An end-to-end upgrade to FrameShift Studio ensuring complete accuracy as a **Two-Way AI Conversion Studio** (bidirectional Photo ↔ Video, Actor ↔ Persona, and Voice ↔ Lip-Sync conversion), enforcing **Strict Owner-Only Admin Visibility** so admin settings and controls only appear for `wblanc1021@gmail.com`, introducing a responsive **Video Fidelity Slider UI Component** (Resolution, Frame Rate, and Compression/Bitrate) across all platform versions, refining workspace chrome to sleek glass borders while keeping the signature spinning rainbow border on the Login screen, and upgrading app-wide typography to **Plus Jakarta Sans**, **Inter**, and **JetBrains Mono**.

## User Review & Critical Decisions

> [!IMPORTANT]
> **Owner-Exclusive Admin Security & Visibility**: All Root Admin settings, telemetry ribbons, founder badges, and unlimited override tiers are strictly gated so they **only appear when authenticated as `wblanc1021@gmail.com`**. Standard users see a clean public Creator Studio with zero exposed admin UI or founder email references.

- **Confirmed Decision 1 — Strict Owner-Only Admin Gate (`wblanc1021@gmail.com` Exclusive)**:
  - Admin controls, the Top-Bar Admin Pill, the Admin Control Center tab, the `ADMIN_FOUNDER` unlimited tier, and the Root Admin telemetry ribbon are completely hidden for general users.
  - They unlock and render **only** when the signed-in Google account matches `wblanc1021@gmail.com` (or when verified directly with the owner's `wblanc1021@gmail.com` credentials).
- **Confirmed Decision 2 — Two-Way AI Conversion Studio**: Full bidirectional conversion architecture supporting **Forward Conversion** (*Source Video / Live Cam → Target Character Photo*), **Inverse Conversion** (*Reference Photo → Motion-Driven Video*), **Dual Role Swap** (*Actor A ↔ Actor B bidirectional exchange*), and **Reverse Extraction** (*Video Footage → Reusable Character Identity & Voice Profile*), plus Two-Way Audio ↔ Lip-Sync conversion.
- **Confirmed Decision 3 — Typography Pairing**: Bundle and apply **Plus Jakarta Sans** for display headers and section titles, **Inter** for clean studio body controls and labels, and **JetBrains Mono** for numeric readouts (resolution, FPS, compression %, bitrates, timecodes, and seeds).
- **Confirmed Decision 4 — Spinning Rainbow Border Scope**: Retain the animated spinning rainbow border exclusively on the **Login / Auth Portal** screen, while transitioning all internal studio cards, preview monitors, badges, and buttons to refined, static glassmorphic borders (`1.dp` translucent specular strokes).
- **Confirmed Decision 5 — Adaptive Video Fidelity Slider Component**: A unified, responsive slider control panel that adapts across **Mobile**, **Computer Pro**, **Web Cloud**, and **All-in-One** views for fine-tuning **Resolution** (`480p` to `4K`), **Frame Rate** (`24 fps` to `120 fps`), and **Compression Level** (`0% Lossless` to `90% Ultra-Light` with live Mbps bitrate and codec telemetry).

---

## 1. Overview & Core Concept

### What It Does
- **Two-Way AI Conversion Engine**:
  1. **Video-to-Persona (Forward Recast)**: Transfers a reference photo or vault character onto source footage or live camera streams while preserving actor motion, micro-expressions, and scene lighting.
  2. **Photo-to-Video (Inverse Motion Drive)**: Animates static character portraits or uploaded photos using driving video motion trajectories, audio visemes, or prompt-guided camera paths.
  3. **Bidirectional Actor Swap & Identity Extraction**: Swaps identities two ways between scene subjects (`Subject A ↔ Subject B`) or extracts a clean character reference sheet and voice print directly from existing video footage.
  4. **Two-Way Voice & Lip-Sync Bridge**: Converts source speech into a target voice clone while synchronizing facial lip geometry in both directions.
- **Owner-Exclusive Admin Architecture**:
  - Public login and workspace screens never expose `wblanc1021@gmail.com`, admin toggles, or root override tabs to regular users.
  - When `wblanc1021@gmail.com` authenticates, the studio automatically elevates the session and reveals the private Admin Control Center, unlimited credit overrides, experimental model routing, and cluster telemetry HUD.
- **Fine-Grained Video Fidelity Mastering**: Gives creators continuous and stepped slider precision over output **Resolution**, **Frame Rate (FPS)**, and **Compression / Bitrate** alongside real-time bandwidth, file-size, and visual quality feedback in the live split-wipe preview.

### Target Audience & Persona
- Filmmakers, VFX directors, virtual streamers, and content creators who need rapid, bidirectional media transformation between still character designs, live performances, and mastered video outputs across devices—with private founder administration reserved strictly for the app owner.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Public Creator Login with Signature Spinning Rainbow Border & Automatic Owner Detection**:
   - The Login Portal presents a sleek **FrameShift Two-Way AI Conversion Studio** card wrapped in the signature spinning rainbow border (`animatedRainbowBorder`).
   - Standard users sign in via Google without seeing any admin email or admin mode toggle.
   - When `wblanc1021@gmail.com` signs in (or authenticates via a discreet owner passkey prompt), the session automatically activates Root Admin privileges and surfaces owner-only controls inside the app.

2. **Owner-Only Admin Settings vs. Standard Creator Config**:
   - **For `wblanc1021@gmail.com` ONLY**:
     - The Top App Bar displays the **Admin (`wblanc1021`)** badge and live Admin Telemetry HUD ribbon.
     - Opening the Control Center reveals the **Admin Override Tab**, **Root Admin Unlimited ($0 Founder Override)** tier, cluster queue bypass, and experimental engine unlocks.
   - **For All Other Users**:
     - The Top App Bar shows only **Config** (Staged Plans, Personal API Keys, Connection Mesh) and **Sync**.
     - The Control Center shows only standard creator tabs (`Plans`, `API Vault`, `Mesh & Net`) and standard tiers (`Creator Sandbox`, `Studio Pro`, `Neural Director Max`).

3. **Two-Way AI Conversion Mode & Polarity Switch**:
   - In the **Studio**, **Live Transformation Preview**, and **Unified Workspace**, a prominent **Two-Way Conversion Bar** lets users select or flip the conversion direction at any time:
     - `Forward: Video + Photo ➔ Recast Video` (Drive target character identity onto source footage)
     - `Inverse: Photo + Motion ➔ Driven Video` (Animate still photo using source motion/audio driver)
     - `Bi-Directional: Actor A ⇄ Actor B Swap` (Mutual two-way identity & voice exchange)
     - `Extract: Video ➔ Character Sheet & Voice Clone` (Reverse-engineer reusable persona from video)
   - Tapping **"⇄ Flip Polarity"** instantaneously swaps the active Source Driver and Target Reference with live visual confirmation in the split-screen wipe monitor.

4. **Interactive Video Fidelity Slider Component (All Platform Versions)**:
   - Integrated directly into both the Studio Mastering Panel and the Unified Workspace (scaling cleanly across Compact Mobile, Medium Web, and Expanded Desktop layouts):
     - **Resolution Slider**: Stepped slider across `480p SD`, `720p HD`, `1080p FHD`, `1440p QHD`, and `4K UHD` with pixel dimensions and aspect ratio tags.
     - **Frame Rate (FPS) Slider**: Smooth/stepped slider across `24 fps (Cinema)`, `30 fps (Standard)`, `48 fps (HFR)`, `60 fps (Smooth)`, and `120 fps (Master)` updating live preview cadence.
     - **Compression & Bitrate Slider**: Fine-tuning slider from `0% (Lossless RAW / ProRes)` to `90% (High Compression / Low Latency)` with dynamic readout of estimated bitrate (`Mbps`), codec (`HEVC / AV1 / H.264`), and projected output size (`MB/min`).
     - **One-Tap Mastering Presets**: Quick pills (`Cinema Master`, `Broadcast 60`, `Balanced Studio`, `Web Stream`) that snap all three fidelity sliders into coordinated targets.

### Visual Identity & Theme
- **Aesthetic Direction**: High-Contrast Precision Dark Studio — calm obsidian surfaces with crisp glassmorphic borders, zero distracting rainbow spin inside the workspace, and tactile orange/cyan/amber signal accents.
- **Color Palette & Mood**:
  - Background Atmosphere: `ObsidianBlack` (`#0D0E11`), `CharcoalCard` (`#16181E`), `CharcoalElevated` (`#1F222B`).
  - Glass Borders: Translucent precision borders (`#2A2E39` default, `#3D4454` elevated hover/active, and subtle `GeminiCyan` / `NeonOrange` status hairlines).
  - Primary Accents: `NeonOrange` (`#FF5722`) for primary conversion & fidelity sliders, `GeminiCyan` (`#00E5FF`) for bidirectional link & telemetry, `GeminiAmber` (`#FFB300`) for mastering indicators, `GeminiGreen` (`#00E676`) for active sync.
- **Typography & Hierarchy**:
  - **Display & Section Headers**: `Plus Jakarta Sans` (SemiBold / Bold / ExtraBold) — smooth geometric curves with modern studio authority.
  - **Body & UI Controls**: `Inter` (Regular / Medium / SemiBold) — effortless scannability across compact mobile sheets and dense desktop inspector panels.
  - **Telemetry, Sliders & Timecodes**: `JetBrains Mono` (Medium / Bold) — crisp tabular alignment for `1080p • 60 FPS • 22% Comp • 48.5 Mbps`.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Strict Identity-Bound Admin Visibility (`wblanc1021@gmail.com`)**
  - *Chosen Approach*: Gate all admin UI affordances (`TopAppBar` admin pill, `ControlCenterTab.ADMIN`, `SubscriptionTierOption.ADMIN_FOUNDER`, admin telemetry banner, and admin status badges) behind `adminSession.isAdminAuthenticated`, which is strictly granted only when the authenticated user's email equals `wblanc1021@gmail.com` (and automatically cleared if any other email signs in).
  - *Why*: Ensures admin settings, founder overrides, and owner identifiers only ever show up for the owner (`wblanc1021@gmail.com`) and are completely invisible to other users.
- **Decision 2: True Two-Way Conversion Polarity State in Project Model**
  - *Chosen Approach*: Persist `conversionDirection` (`FORWARD_RECAST`, `INVERSE_PHOTO_DRIVE`, `BIDIRECTIONAL_SWAP`, `REVERSE_EXTRACT`) alongside `resolution`, `frameRate`, and `compressionLevel` directly on `RecastProject` and Room `ProjectSyncEntity`.
  - *Why*: Ensures switching between Mobile, Desktop, and Web views—or beaming projects across devices—preserves the exact two-way conversion mode and video fidelity slider values without state loss.
- **Decision 3: Scoped Rainbow Border vs. Clean Studio Glass**
  - *Chosen Approach*: Keep `animatedRainbowBorder` active on `AuthScreen` while refactoring `AiStudioRainbowCard`, `AiStudioBadge`, `AiStudioRainbowButton`, and screen containers in workspace screens to use a clean, non-spinning glass border with subtle specular highlights.
  - *Why*: Preserves the memorable holographic welcome on Login while eliminating visual fatigue during prolonged video editing and parameter tuning.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                     PUBLIC LOGIN PORTAL (RAINBOW BORDER)                     │
│     • Spinning Rainbow Border Retained Exclusively on Login Card             │
│     • Google CredentialManager Sign-In                                       │
│     • Identity Check: email == "wblanc1021@gmail.com"                        │
└──────────────────┬────────────────────────────────────────┬──────────────────┘
                   │ (Match: Owner)                         │ (Standard User)
                   ▼                                        ▼
┌──────────────────────────────────────┐ ┌─────────────────────────────────────┐
│   OWNER-ONLY ADMIN LAYER (VISIBLE)   │ │    STANDARD CREATOR LAYER (CLEAN)   │
│ • Admin Top Pill & Telemetry HUD     │ │ • Admin UI Completely Hidden        │
│ • Admin Control Tab & Overrides      │ │ • Standard Plans, API & Mesh Tabs   │
│ • Founder Unlimited Tier ($0)        │ │ • Creator / Pro / Director Tiers    │
└──────────────────┬───────────────────┘ └──────────────────┬──────────────────┘
                   └───────────────────┬────────────────────┘
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                TWO-WAY AI CONVERSION & FIDELITY STATE ENGINE                 │
│                                                                              │
│  ┌──────────────────────────────────┐  ┌──────────────────────────────────┐  │
│  │   BIDIRECTIONAL CONVERSION HUB   │  │   VIDEO FIDELITY SLIDER ENGINE   │  │
│  │  • Forward: Video + Photo ➔ Cast │  │  • Resolution: 480p..4K Stepped  │  │
│  │  • Inverse: Photo + Motion ➔ Vid │  │  • Frame Rate: 24..120 FPS       │  │
│  │  • Dual: Actor A ⇄ Actor B Swap  │  │  • Compression: 0%..90% + Mbps   │  │
│  │  • Extract: Video ➔ Persona/Vox  │  │  • Live Bitrate & Codec Estimator│  │
│  └─────────────────┬────────────────┘  └─────────────────┬────────────────┘  │
│                    └──────────────────┬──────────────────┘                   │
│                                       ▼                                      │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │            ADAPTIVE MULTI-VERSION WORKSPACE (CLEAN GLASS UI)           │  │
│  │  ┌──────────────┐  ┌───────────────┐  ┌──────────────┐  ┌───────────┐  │  │
│  │  │ Mobile View  │  │ Computer Pro  │  │  Web Cloud   │  │ All-in-1  │  │  │
│  │  │ Touch Stack  │  │ Dual Rack HUD │  │ Stream Bar   │  │ Tri-Dock  │  │  │
│  │  └──────────────┘  └───────────────┘  └──────────────┘  └───────────┘  │  │
│  └────────────────────────────────────┬───────────────────────────────────┘  │
└───────────────────────────────────────┼──────────────────────────────────────┘
                                        ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│              HYBRID PERSISTENCE & TYPOGRAPHY FOUNDATION                      │
│  • Room SQLite (`ProjectSyncEntity`) + Firestore (`/users/{uid}/projects`)   │
│  • Bundled Fonts: Plus Jakarta Sans (Headers) + Inter (Body) + JetBrains Mono│
└──────────────────────────────────────────────────────────────────────────────┘
```

### Data Model & State Extensions
- **Owner-Exclusive Admin Gate (`StudioConfigRepository`)**:
  - `isAdminAuthenticated` is strictly true **only** when the authenticated user's email matches `wblanc1021@gmail.com` (with strict non-blank email + passkey verification if elevating manually). Signing in with any other email immediately sets `isAdminAuthenticated = false` and hides all admin UI.
- **Project Fidelity & Two-Way Fields (`RecastProject` & `ProjectSyncEntity`)**:
  - `conversionDirection: String` (`"forward_recast"`, `"inverse_drive"`, `"dual_swap"`, `"reverse_extract"`)
  - `resolution: String` (`"480p"`, `"720p"`, `"1080p"`, `"1440p"`, `"4K"`)
  - `frameRate: Long` (`24L`, `30L`, `48L`, `60L`, `120L`)
  - `compressionLevel: Long` (`0L..90L`, default `22L`)
  - Derived telemetry helper computing real-time target bitrate (`Mbps`), codec (`ProRes 422 HQ`, `AV1 10-bit`, `HEVC Main10`, `H.264 Fast`), and estimated file size per minute based on resolution, FPS, and compression slider values.

### Interactive Component & State Mapping
1. **Owner-Only Admin Visibility Enforcement**:
   - `AuthScreen`: Removes public admin banners and email displays; presents a clean Two-Way AI Conversion Studio login with the spinning rainbow border, automatically unlocking admin status when `wblanc1021@gmail.com` authenticates.
   - `MainActivity` & `AdminAndConfigCenterModal`: Conditionally renders the Admin top-bar pill, Admin telemetry ribbon, `ControlCenterTab.ADMIN` tab, and `SubscriptionTierOption.ADMIN_FOUNDER` tier strictly when `adminSession.isAdminAuthenticated` is true.
2. **Two-Way Conversion Direction Selector & Polarity Flip**:
   - Selecting a mode or clicking **"⇄ Invert Polarity"** updates `project.conversionDirection` and swaps source/target labels and visual overlays inside `LiveTransformationPreviewCard`, `StudioScreen`, and `UnifiedWorkspaceScreen`.
3. **Video Fidelity Slider Component (`FidelityControlsPanel` & `UnifiedWorkspaceScreen`)**:
   - Dragging the **Resolution Slider** snaps between `480p`, `720p`, `1080p`, `1440p`, and `4K`, immediately updating the resolution badge and preview HUD.
   - Dragging the **Frame Rate Slider** adjusts `frameRate` (`24`–`120` FPS) and updates the live preview frame loop cadence and telemetry bar.
   - Dragging the **Compression Slider** (`0%`–`90%`) dynamically recalculates estimated Mbps bitrate and visual compression simulation in the preview monitor.
4. **Clean Glass UI & Smooth Typography**:
   - `AuthScreen` retains `Modifier.animatedRainbowBorder(...)` on the login card.
   - All workspace components use clean matte/glass borders with smooth `Plus Jakarta Sans`, `Inter`, and `JetBrains Mono` typography.
