# UI Components

Reusable Compose components for chart display.

## Chart Components

### ChartScreen
**Location:** `ui/chart/ChartScreen.kt`

Main screen. Handles loading/success/error states.

**Props:**
- `viewModel: ChartViewModel` (injected via Koin)

**States:**
- `Loading`: Shows CircularProgressIndicator
- `Success`: Shows ChartTopBar + ChartTrackList
- `Error`: Shows error message + Retry button

---

### ChartTopBar
**Location:** `ui/chart/components/ChartTopBar.kt`

Top bar with week picker and chart selector.

**Props:**
- `weekDate: String` - Current week
- `availableCharts: List<Chart>` - Chart options
- `selectedChart: Chart` - Active chart
- `onChartSelected: (Long) -> Unit` - Chart switch callback

**Children:**
- `WeekPicker` (currently non-functional)
- `ChartSelectorRow`

---

### ChartSelectorRow
**Location:** `ui/chart/components/ChartSelectorRow.kt`

Horizontal scrolling chart selector with FilterChips.

**Props:**
- `availableCharts: List<Chart>`
- `selectedChart: Chart`
- `onChartSelected: (Long) -> Unit`

**Behavior:** Highlights selected chart, scrollable

---

### ChartTrackList
**Location:** `ui/chart/components/ChartTrackList.kt`

Scrollable list of chart tracks (LazyColumn).

**Props:**
- `chartTracks: List<ChartTrack>`

**Children:** `ChartTrackItem` for each track

---

### ChartTrackItem
**Location:** `ui/chart/components/ChartTrackItem.kt`

Single track row with position, thumbnail, and metadata.

**Layout:**
```
[#1] [↑3] [🎵] Track Title
            Artist Name
```

**Props:**
- `chartTrack: ChartTrack`

**Children:**
- Position number (bold, 40dp width)
- `RankChangeIndicator`
- `TrackThumbnail` (56dp square)
- Track title + artist (column)

---

### RankChangeIndicator
**Location:** `ui/chart/components/RankChangeIndicator.kt`

Shows rank movement with arrows and colors.

**Props:**
- `chartTrack: ChartTrack`

**Display:**
- Debut: "NEW" badge (primary color)
- Up: Green ↑ + number
- Down: Red ↓ + number
- No change: "—"

---

### TrackThumbnail
**Location:** `ui/chart/components/TrackThumbnail.kt`

Album art placeholder (backend has no images).

**Props:**
- `track: Track`

**Display:** Music note icon on surfaceVariant background

**TODO:** Integrate Spotify/YouTube Music API for real images

---

### WeekPicker
**Location:** `ui/chart/components/WeekPicker.kt`

Week display with calendar icon (currently non-functional).

**Props:**
- `weekDate: String`
- `onClick: () -> Unit` (not implemented)

**Display:** "Week of {date}" in surface variant

**TODO:** Add DatePicker dialog for historical weeks

---

## Styling

**Theme:** Material Design 3
**Icons:** Material Icons Extended
**Typography:** Material default
**Colors:** Dynamic (system theme aware)
