# Testing Guide

## Running Tests

```bash
# All tests
./gradlew test

# Frontend tests only
./gradlew :frontend:composeApp:test

# Backend tests only
./gradlew :backend:test

# Specific platform
./gradlew :frontend:composeApp:jvmTest         # Desktop
./gradlew :frontend:composeApp:testDebugUnitTest  # Android
```

## Test Strategy by Layer

### ViewModels (Unit Tests)
**What to test:**
- State transitions (Loading → Success → Error)
- User actions trigger correct repository calls
- Error mapping works correctly
- Initial state is Loading

**Example:**
```kotlin
@Test
fun `loadCharts success updates state`() = runTest {
    val mockRepo = mockk<ChartRepository> {
        coEvery { getAllCharts() } returns Result.success(testCharts)
        coEvery { getChartByDate(any(), any()) } returns Result.success(testChartList)
    }
    val viewModel = ChartViewModel(mockRepo)

    viewModel.uiState.test {
        assertEquals(ChartUiState.Loading, awaitItem())
        assertTrue(awaitItem() is ChartUiState.Success)
    }
}
```

### Repositories (Integration Tests)
**What to test:**
- API calls are made correctly
- Responses are parsed to models
- Errors are propagated as Result.failure

**Mock:** Use MockK or fake API implementation

### UI Components (Compose Tests)
**What to test:**
- Components render with given props
- User interactions trigger callbacks
- Error states display correctly

**Example:**
```kotlin
@Test
fun `ChartTrackItem displays rank and title`() = runComposeUiTest {
    setContent {
        ChartTrackItem(testChartTrack)
    }

    onNodeWithText("1").assertExists()
    onNodeWithText("Test Song").assertExists()
}
```

## Platform-Specific Testing

### Android
- Use Robolectric for local unit tests
- Instrumented tests require emulator/device

### Desktop (JVM)
- Standard JUnit tests
- No special setup required

### Web (WASM)
- Limited test support currently
- Test shared code via JVM tests

## What to Test

### Critical Paths
✅ Chart loading on app launch
✅ Chart switching updates data
✅ Network errors show user-friendly messages
✅ Empty chart list handled gracefully

### Edge Cases
✅ Offline mode (no network)
✅ Empty API responses
✅ Invalid chart IDs
✅ Malformed JSON responses

### Don't Test
❌ Compose framework behavior
❌ Ktor HTTP client internals
❌ Koin DI container

## Coverage Goals

- **ViewModels:** 80%+ (business logic)
- **Repositories:** 70%+ (API integration)
- **UI Components:** 50%+ (critical paths only)

## CI/CD

Tests run automatically on:
- Every push to feature branches
- Pull requests to main/master
- Pre-merge checks required

See `.github/workflows/ci.yml` for configuration.
