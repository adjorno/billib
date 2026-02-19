---
name: compose-screen
description: Scaffold a new Compose Multiplatform screen with ViewModel, UI state, and navigation. Use when adding new screens to the frontend.
allowed-tools: Read, Write, Grep, Glob, AskUserQuestion
argument-hint: <screen-name> <description>
---

# compose-screen: Scaffold Compose Multiplatform Screen

This skill scaffolds a complete screen following the project's Compose Multiplatform architecture.

## Usage

```bash
/compose-screen <screen-name> <description>
```

**Examples:**
- `/compose-screen Artist "Display artist details and discography"`
- `/compose-screen Settings "User preferences and app settings"`

## Questions to Ask User

Before scaffolding, clarify:

1. **Data source**: Where does the data come from? (repository, API, local storage)
2. **UI states needed**: Besides Loading/Success/Error, any custom states?
3. **User interactions**: What actions can users take? (buttons, filters, navigation)
4. **Navigation**: Is this a top-level destination or nested screen?
5. **Dependencies**: What repositories/services does the ViewModel need?

## Architecture Pattern

Based on existing code (`frontend/composeApp/src/commonMain/kotlin/com/ifochka/m14n/ui/`):

```
frontend/composeApp/src/commonMain/kotlin/com/ifochka/m14n/ui/<feature>/
├── <Feature>Screen.kt      # Composable UI
├── <Feature>UiState.kt     # Sealed interface for state
├── <Feature>ViewModel.kt   # Business logic & state management
└── components/             # Feature-specific composables
    └── <Component>.kt
```

## Template: UI State

**Location**: `frontend/composeApp/src/commonMain/kotlin/com/ifochka/m14n/ui/<feature>/<Feature>UiState.kt`

```kotlin
package com.ifochka.m14n.ui.<feature>

import com.ifochka.m14n.data.error.<Feature>Error

sealed interface <Feature>UiState {
    data object Loading : <Feature>UiState

    data class Success(
        val data: <DataType>,
        // Add more fields as needed
    ) : <Feature>UiState

    data class Error(
        val error: <Feature>Error,
    ) : <Feature>UiState
}
```

**Key principles:**
- ✅ **Immutable**: All properties are `val`
- ✅ **Sealed interface**: Exhaustive when expressions
- ✅ **Data classes**: Success and Error states hold data
- ✅ **Data object**: Loading state has no data

## Template: ViewModel

**Location**: `frontend/composeApp/src/commonMain/kotlin/com/ifochka/m14n/ui/<feature>/<Feature>ViewModel.kt`

```kotlin
package com.ifochka.m14n.ui.<feature>

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ifochka.m14n.data.error.ErrorMapper
import com.ifochka.m14n.data.repository.<Feature>Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class <Feature>ViewModel(
    private val repository: <Feature>Repository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<<Feature>UiState>(<Feature>UiState.Loading)
    val uiState: StateFlow<<Feature>UiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = <Feature>UiState.Loading

            repository.getData()
                .onSuccess { data ->
                    _uiState.value = <Feature>UiState.Success(data = data)
                }
                .onFailure { throwable ->
                    _uiState.value = <Feature>UiState.Error(ErrorMapper.mapError(throwable))
                }
        }
    }

    fun retry() {
        loadData()
    }

    // Add more user interaction methods
}
```

**Key principles:**
- ✅ **Constructor injection**: Dependencies as parameters (Koin will inject)
- ✅ **Immutable state exposure**: Public `StateFlow`, private `MutableStateFlow`
- ✅ **viewModelScope**: Lifecycle-aware coroutines
- ✅ **init block**: Load initial data
- ✅ **Public methods**: User interactions (retry, select, filter, etc.)

## Template: Screen

**Location**: `frontend/composeApp/src/commonMain/kotlin/com/ifochka/m14n/ui/<feature>/<Feature>Screen.kt`

```kotlin
package com.ifochka.m14n.ui.<feature>

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun <Feature>Screen(viewModel: <Feature>ViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        when (val state = uiState) {
            is <Feature>UiState.Loading -> {
                LoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }

            is <Feature>UiState.Success -> {
                SuccessState(
                    data = state.data,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onAction = { /* handle user action */ },
                )
            }

            is <Feature>UiState.Error -> {
                ErrorState(
                    error = state.error,
                    onRetry = { viewModel.retry() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SuccessState(
    data: <DataType>,
    modifier: Modifier = Modifier,
    onAction: () -> Unit,
) {
    Column(modifier = modifier) {
        // Your UI here
    }
}

@Composable
private fun ErrorState(
    error: <Feature>Error,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = error.toDisplayMessage(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp),
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
```

**Key principles:**
- ✅ **koinViewModel()**: Dependency injection for ViewModel
- ✅ **collectAsState()**: Observe StateFlow as Compose state
- ✅ **when expression**: Exhaustive state handling
- ✅ **Extract composables**: Loading/Success/Error as separate functions
- ✅ **Scaffold**: Material3 layout structure
- ✅ **Named arguments**: Required for 3+ parameters
- ✅ **Trailing commas**: On multi-line arguments

## Koin Module Registration

Don't forget to register the ViewModel in Koin module:

**Location**: `frontend/composeApp/src/commonMain/kotlin/com/ifochka/m14n/di/AppModule.kt`

```kotlin
import org.koin.core.module.dsl.viewModel

val appModule = module {
    // ... existing ViewModels ...

    viewModel { <Feature>ViewModel(repository = get()) }
}
```

## Navigation Integration (if needed)

If this screen requires navigation, update the navigation graph:

**Location**: `frontend/composeApp/src/commonMain/kotlin/com/ifochka/m14n/navigation/NavGraph.kt`

```kotlin
// Add route
object <Feature>Route : NavigationRoute("<feature>")

// Add to NavHost
composable<<Feature>Route> {
    <Feature>Screen()
}
```

## Output

After scaffolding:
1. List all created files
2. Show what needs to be added to Koin module
3. Show how to navigate to this screen (if applicable)
4. Remind to run `/ktlint-fix` to format the code
5. Remind to test the screen (loading, success, error states)
