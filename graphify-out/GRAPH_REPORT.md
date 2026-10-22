# Graph Report - Proyek  (2026-10-05)

## Corpus Check
- 162 files · ~56,381 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 128 file(s) not represented in the graph (top: .xml 74, .glb 36, .zip 5)

## Summary
- 1809 nodes · 4039 edges · 114 communities (25 shown, 89 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 46 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Camera Capture and ML Scan
- Midtrans Payment Service
- Rig Pose Controller 3D
- Post DAO Room
- AR Scene Compose UI
- Registration and Auth UI
- AR Earth and Anchors
- Payment History DAO
- Pokedex Species DAO
- Firestore Service
- Backend API Service
- Backend Node Dependencies
- Pose Overlay Light Estimation
- Retrofit Clients Repositories
- User Inventory DAO
- AR Fragment Lifecycle
- ARSceneView Session
- Plane Visualizer Sceneform
- Plane Renderer
- WebAdmin App Logic
- PokeAPI Service
- Home Fragment UI
- CameraXR 3D Model Controls
- AR Session Management
- Dashboard Posts Fragments
- Settings Dialog
- Shop Dialog Daily Claim
- User DAO Entity
- User DAO API Queries
- Purchase History DAO
- Trainer Stats DAO
- ARCore Frame API
- AR Nodes Faces Streetscape
- Upload Post UI
- Admin Profile UI
- User Management UI
- Pokemon DAO
- Pokemon Database Room
- Pokemon Entity DAO
- Collection Fragment UI
- Pose Gesture Nodes
- Pokemon Repository Ops
- Collection ViewModel
- User Repository Ops
- Pose Frame Computation
- Feed ViewModel
- Camera Pose Math
- AR Camera Node
- Pokemon Management UI
- User Local Data Source
- API User DAO
- Post Repository Ops
- Home Banner Adapter
- Shop Adapter
- Anchor Node
- Cloud Anchor Node
- Pokemon Local Data Source
- Inventory Repository
- Inventory Adapter
- Dashboard Login ViewModels
- Pokemon Management ViewModel
- Augmented Image Tracking
- Pokemon Adapter Views
- Inventory Fragment UI
- XR Model Tracking Controls
- Pose Tracker MediaPipe
- Post Adapter
- User Adapter Views
- Session Manager Auth
- Home Feed Adapter
- Pokemon Selection Adapter
- App Database Room
- Pokemon Local Data Ops
- Home Info Adapter
- History Adapter
- AR Lifecycle Observer
- Trackable Node
- Main Activity
- Admin Activity
- Starter Selection Activity
- Feed Fragment UI
- History Activity
- Profile Fragment UI
- Home ViewModel
- User Management ViewModel
- Hit Result Node
- Instrumented Tests
- AR Tap to Place Gesture
- Project Architecture Overview
- Login Activity UI
- Pokedex ViewHolder
- Payment Success Activity
- Item User Activity
- YUV to Bitmap Util
- Unit Tests
- Augmented Face Node
- Plane Render Modes
- Pokedex Local Data Source
- TrainerStats Local Data Source
- Pokemon Mapper
- App Launcher Icons
- AR Pose Modes

## God Nodes (most connected - your core abstractions)
1. `Post` - 82 edges
2. `User` - 77 edges
3. `PokemonEntity` - 73 edges
4. `CameraXRFragment` - 61 edges
5. `BackendApiService` - 47 edges
6. `FirestoreService` - 44 edges
7. `ARSceneView` - 41 edges
8. `UserInventory` - 39 edges
9. `SessionManager` - 35 edges
10. `PostRepository` - 31 edges

## Surprising Connections (you probably didn't know these)
- `Panel Admin Web (Firestore CRUD)` --references--> `Admin login screen (loginScreen, loginEmail, loginBtn)`  [EXTRACTED]
  RANGKUMAN_BUKU_PROYEK_MAD.md → webadmin/public/index.html
- `Top-up Koin via Midtrans Snap + WebView` --conceptually_related_to--> `Admin CRUD sections (users, posts/shop, pokemon, payments tables + modal)`  [INFERRED]
  RANGKUMAN_BUKU_PROYEK_MAD.md → webadmin/public/index.html
- `ARSceneView` --calls--> `ARCore`  [INFERRED]
  app/temp_sources/io/github/sceneview/ar/ARSceneView.kt → app/temp_sources/io/github/sceneview/ar/ARCore.kt
- `canHostCloudAnchor()` --references--> `ARCameraNode`  [EXTRACTED]
  app/temp_sources/io/github/sceneview/ar/arcore/ArSession.kt → app/temp_sources/io/github/sceneview/ar/node/ARCameraNode.kt
- `createAnchorOrNull()` --calls--> `createAnchor()`  [INFERRED]
  app/temp_sources/io/github/sceneview/ar/arcore/Trackable.kt → app/temp_sources/io/github/sceneview/ar/arcore/Earth.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Four-part system integration (Android, backend, web admin)** — rangkuman_buku_proyek_mad_aplikasi_android, rangkuman_buku_proyek_mad_backend_api, rangkuman_buku_proyek_mad_panel_admin [EXTRACTED 0.75]

## Communities (114 total, 89 thin omitted)

### Community 0 - "Camera Capture and ML Scan"
Cohesion: 0.05
Nodes (3): CameraFragment, ARCore, getDescription()

### Community 1 - "Midtrans Payment Service"
Cohesion: 0.06
Nodes (17): MidtransClient, MidtransConfig, MidtransCoreService, Callbacks, CustomerDetails, ItemDetail, SnapTransactionRequest, SnapTransactionResponse (+9 more)

### Community 3 - "Post DAO Room"
Cohesion: 0.06
Nodes (8): DiffCallback, PostDao, PostLocalDataSource, Post, ApiPostDao, DiffCallback, DiffCallback, DiffCallback

### Community 4 - "AR Scene Compose UI"
Cohesion: 0.07
Nodes (5): ARScene(), ARScenePreview(), rememberARCameraNode(), rememberARCameraStream(), rememberAREnvironment()

### Community 5 - "Registration and Auth UI"
Cohesion: 0.08
Nodes (5): RegisterActivity, AuthResult, FirebaseAuthManager, FirebaseStorageManager, RegisterViewModel

### Community 6 - "AR Earth and Anchors"
Cohesion: 0.10
Nodes (8): createAnchor(), createAnchorOrNull(), filterTypes(), findByType(), firstByTypeOrNull(), isValid(), RooftopAnchorNode, TerrainAnchorNode

### Community 7 - "Payment History DAO"
Cohesion: 0.08
Nodes (6): PaymentHistoryDao, PaymentHistory, ApiPaymentHistoryDao, PaymentMethodActivity, PaymentWebViewActivity, WebViewClient

### Community 8 - "Pokedex Species DAO"
Cohesion: 0.08
Nodes (7): PokedexSpeciesDao, PokedexSpecies, ApiPokedexSpeciesDao, PokedexAdapter, PokedexItem, PokedexDialogFragment, PokedexGeneration

### Community 11 - "Backend Node Dependencies"
Cohesion: 0.06
Nodes (29): mysql, pool, dependencies, cors, dotenv, express, mysql2, description (+21 more)

### Community 12 - "Pose Overlay Light Estimation"
Cohesion: 0.10
Nodes (3): PoseOverlayView, Estimation, LightEstimator

### Community 13 - "Retrofit Clients Repositories"
Cohesion: 0.11
Nodes (3): BackendRetrofitClient, SyncWorker, NetworkUtils

### Community 14 - "User Inventory DAO"
Cohesion: 0.13
Nodes (4): UserInventoryDao, InventoryLocalDataSource, UserInventory, ApiUserInventoryDao

### Community 16 - "AR Fragment Lifecycle"
Cohesion: 0.15
Nodes (10): addLifecycleListener(), addTransformChangedListener(), ArFragment, BaseArFragment, configure(), getConfig(), getScene(), OnTapModelListener (+2 more)

### Community 21 - "WebAdmin App Logic"
Cohesion: 0.16
Nodes (18): app, auth, closeModal(), db, escapeHtml(), firebaseConfig, givePokemonForm(), givePokemonTo() (+10 more)

### Community 22 - "PokeAPI Service"
Cohesion: 0.16
Nodes (7): PokeApiService, RetrofitClient, PokemonRemoteDataSource, PokemonResponse, PokemonType, Sprites, TypeSlot

### Community 25 - "AR Session Management"
Cohesion: 0.14
Nodes (4): addAugmentedImage(), ARSession, canHostCloudAnchor(), configure()

### Community 26 - "Dashboard Posts Fragments"
Cohesion: 0.15
Nodes (3): DashboardFragment, ManagePostsFragment, ViewModelFactory

### Community 32 - "Purchase History DAO"
Cohesion: 0.19
Nodes (3): PurchaseHistoryDao, PurchaseHistory, ApiPurchaseHistoryDao

### Community 34 - "ARCore Frame API"
Cohesion: 0.19
Nodes (10): fps(), getUpdatedAugmentedFaces(), getUpdatedAugmentedImages(), getUpdatedPlanes(), getUpdatedStreetscapeGeometries(), getUpdatedTrackables(), hasUpdatedTrackable(), hitTest() (+2 more)

### Community 50 - "Camera Pose Math"
Cohesion: 0.18
Nodes (3): getProjectionTransform(), distanceTo(), distanceToPlane()

### Community 56 - "Home Banner Adapter"
Cohesion: 0.24
Nodes (3): BannerDisplayItem, BannerViewHolder, HomeBannerAdapter

### Community 62 - "Inventory Adapter"
Cohesion: 0.21
Nodes (3): InventoryAdapter, ViewHolder, InventoryItem

### Community 68 - "XR Model Tracking Controls"
Cohesion: 0.20
Nodes (4): XrMode, CAPTURE, PLACE, TRACKING

### Community 93 - "Project Architecture Overview"
Cohesion: 0.43
Nodes (7): Aplikasi Mobile Android Pokemon (MVVM + Repository), Backend REST API Express + MySQL/TiDB, Panel Admin Web (Firestore CRUD), Top-up Koin via Midtrans Snap + WebView, Admin dashboard stats (statUsers, statPosts, statPokemon, statBanned), Admin CRUD sections (users, posts/shop, pokemon, payments tables + modal), Admin login screen (loginScreen, loginEmail, loginBtn)

### Community 104 - "Plane Render Modes"
Cohesion: 0.67
Nodes (3): PlaneRendererMode, RENDER_ALL, RENDER_CENTER

## Knowledge Gaps
- **42 isolated node(s):** `PokedexLocalDataSource`, `TrainerStatsLocalDataSource`, `PokemonMapper`, `PLACE`, `CAPTURE` (+37 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 295 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **89 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CameraXRFragment` connect `CameraXR 3D Model Controls` to `Rig Pose Controller 3D`, `XR Model Tracking Controls`, `Fragment View Init`, `Pose Tracker MediaPipe`, `Session Manager Auth`, `Pokemon Entity DAO`, `Pokemon Selection Adapter`, `Pose Overlay Light Estimation`, `Pokemon Repository Ops`, `CameraXR Fragment UI`, `Pose Frame Computation`, `ARSceneView Session`, `AR Camera Stream Hit Test`, `Dashboard Posts Fragments`, `AR Tap to Place Gesture`?**
  _High betweenness centrality (0.115) - this node is a cross-community bridge._
- **Why does `PokemonEntity` connect `Pokemon Entity DAO` to `Firestore Service`, `Backend API Service`, `Retrofit Clients Repositories`, `CameraXR Fragment UI`, `CameraXR 3D Model Controls`, `RecyclerView Adapters`, `Pokemon DAO`, `Collection Fragment UI`, `Pokemon Repository Ops`, `Collection ViewModel`, `Pokemon Management UI`, `Pokemon Local Data Source`, `Pokemon Management ViewModel`, `Pokemon Adapter Views`, `Pokemon Selection Adapter`, `Pokemon Local Data Ops`, `Sync Operations`, `Starter Selection Activity`, `Home ViewModel`?**
  _High betweenness centrality (0.106) - this node is a cross-community bridge._
- **Why does `ARSceneView` connect `ARSceneView Session` to `Camera Capture and ML Scan`, `Rig Pose Controller 3D`, `AR Scene Compose UI`, `Pose Overlay Light Estimation`, `CameraXR Fragment UI`, `AR Lifecycle Observer`, `AR Camera Stream Hit Test`, `AR Camera Node`, `Plane Renderer`, `CameraXR 3D Model Controls`, `Hit Result Node`?**
  _High betweenness centrality (0.102) - this node is a cross-community bridge._
- **What connects `PokedexLocalDataSource`, `TrainerStatsLocalDataSource`, `PokemonMapper` to the rest of the system?**
  _42 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Camera Capture and ML Scan` be split into smaller, more focused modules?**
  _Cohesion score 0.052884615384615384 - nodes in this community are weakly interconnected._
- **Should `Midtrans Payment Service` be split into smaller, more focused modules?**
  _Cohesion score 0.05654761904761905 - nodes in this community are weakly interconnected._
- **Should `Rig Pose Controller 3D` be split into smaller, more focused modules?**
  _Cohesion score 0.05683563748079877 - nodes in this community are weakly interconnected._