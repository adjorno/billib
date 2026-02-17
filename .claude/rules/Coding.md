# Coding Standards

## Dependency Injection & Interfaces

### Dependency Passing
- **Always pass dependencies as interfaces or data classes**
- Enables testability, mocking, and loose coupling
- Avoid passing concrete implementations directly

### Interface Naming
- **Does NOT** start with prefix `I-`
- **Does NOT** use suffix `-Itf` or `-Interface`
- Use clear, descriptive names without special markers

**Examples:**
- ✅ `BillibApi`
- ✅ `ChartRepository`
- ✅ `UserService`
- ❌ `IBillibApi`
- ❌ `ChartRepositoryInterface`
- ❌ `UserServiceItf`

### Implementation Naming
- **MUST** use descriptive names that indicate implementation details
- **DO NOT** use generic prefixes/suffixes like `Default-`, `Base-`, or `-Impl`
- Name should tell you HOW it works or WHAT it uses

**Recommended prefixes/patterns:**
- `Network-`, `Offline-`, `Composite-` (data source)
- `Ktor-`, `Retrofit-`, `OkHttp-` (HTTP client)
- `InMemory-`, `Database-`, `File-` (storage type)
- `Mock-`, `Fake-`, `Test-` (testing)
- `Cached-`, `Lazy-`, `Async-` (behavior)

**Examples:**
- ✅ `KtorBillibApi` (uses Ktor)
- ✅ `NetworkChartRepository` (network-based)
- ✅ `OfflineChartRepository` (offline/cached)
- ✅ `CompositeUserService` (combines multiple sources)
- ✅ `InMemoryUserService` (in-memory storage)
- ✅ `MockChartRepository` (mock for testing)
- ❌ `BillibApiImpl` (doesn't tell you anything)
- ❌ `DefaultChartRepository` (generic, not descriptive)
- ❌ `BaseUserService` (generic, not descriptive)

### Class Size Limit
- **Maximum 100 meaningful lines of code per class**
- Excludes comments, blank lines, and imports
- If a class exceeds this limit, refactor into smaller, focused classes
- Promotes single responsibility principle and maintainability

## Dependency Inversion Principle

### Feature Modules Define Their Own Dependencies
- **Feature modules MUST define their required dependencies via interfaces**
- **DO NOT** use partially compatible types from lower modules
- **DO NOT** depend directly on concrete implementations from other modules

### Consumer Responsibility
- It is the **feature consumer's responsibility** to implement the dependency
- Consumer must correctly map existing data to the interface defined by the feature
- This ensures loose coupling and prevents features from depending on implementation details

### Benefits
- Features remain independent and reusable
- Lower modules don't dictate feature requirements
- Easy to swap implementations without changing features
- Clear separation of concerns

**Example:**

```kotlin
// ❌ BAD: Feature uses type from lower module
// feature/UserProfile.kt
class UserProfileFeature(private val userService: com.company.data.UserService) {
    // Now coupled to data module's UserService implementation
}

// ✅ GOOD: Feature defines its own interface
// feature/UserProfile.kt
interface UserProfileDataSource {
    suspend fun getUser(id: String): UserProfile
}

class UserProfileFeature(private val dataSource: UserProfileDataSource) {
    // Feature only knows about its own interface
}

// Consumer implements the mapping
// app/di/AppModule.kt
class UserServiceAdapter(
    private val userService: com.company.data.UserService
) : UserProfileDataSource {
    override suspend fun getUser(id: String): UserProfile {
        // Consumer's responsibility to map data.User -> feature.UserProfile
        return userService.fetchUser(id).toUserProfile()
    }
}
```

## Immutability and State Management

### Prefer Immutability
- **Favor immutable data structures** (`val` over `var`, data classes)
- Use `copy()` for modifications instead of mutating existing objects
- Immutable code is easier to reason about, test, and debug

### Avoid Stateful Solutions
- **Stateful solutions are complex to maintain**
- State increases complexity: race conditions, synchronization, unexpected side effects
- Prefer functional approaches with immutable data transformations
- When state is necessary, isolate it and manage it explicitly (ViewModel, StateFlow)

### Guidelines
- Use `val` by default, only `var` when mutation is truly needed
- Make data classes immutable (all properties `val`)
- Use sealed classes for state representation
- Prefer pure functions (same input → same output, no side effects)
- Use flows/streams for reactive state changes

**Examples:**

```kotlin
// ❌ BAD: Mutable state scattered throughout
class ChartManager {
    var currentChart: Chart? = null
    var isLoading = false
    var error: String? = null

    fun loadChart(id: Long) {
        isLoading = true
        // Complex state management, race conditions possible
    }
}

// ✅ GOOD: Immutable state with sealed classes
sealed interface ChartUiState {
    data object Loading : ChartUiState
    data class Success(val chart: Chart) : ChartUiState
    data class Error(val message: String) : ChartUiState
}

class ChartViewModel {
    private val _state = MutableStateFlow<ChartUiState>(Loading)
    val state: StateFlow<ChartUiState> = _state.asStateFlow()

    // State is isolated, immutable, and explicit
}

// ❌ BAD: Mutable data class
data class User(
    var name: String,
    var email: String
)

// ✅ GOOD: Immutable data class
data class User(
    val name: String,
    val email: String
)

// Use copy() for "modifications"
val updatedUser = user.copy(name = "New Name")
```

## Function Call Conventions

### Named Arguments for 3+ Parameters
- **MUST use named arguments** when calling functions with 3 or more parameters
- Makes code self-documenting and prevents argument order mistakes
- Improves readability and maintainability
- **Note:** No ktlint rule enforces this - relies on code review

**Examples:**

```kotlin
// ❌ BAD: Positional arguments for 3+ params
createUser("John", "john@example.com", true, "admin")

// ✅ GOOD: Named arguments
createUser(
    name = "John",
    email = "john@example.com",
    isActive = true,
    role = "admin",
)

// ✅ OK: 2 parameters - named arguments optional but recommended for clarity
val point = Point(x = 10, y = 20)

// ✅ OK: Single parameter - no need for named argument
repository.getUser(userId)
```

### Trailing Commas
- **Enforced by ktlint** via `trailing-comma-on-call-site` and `trailing-comma-on-declaration-site`
- When arguments/parameters are on separate lines, last one MUST end with a comma
- Benefits: cleaner diffs, easier reordering, fewer merge conflicts

**Examples:**

```kotlin
// ✅ GOOD: Trailing comma on last argument
createUser(
    name = "John",
    email = "john@example.com",
    isActive = true,
    role = "admin", // <- trailing comma required
)

// ✅ GOOD: Trailing comma on last parameter
fun createUser(
    name: String,
    email: String,
    isActive: Boolean,
    role: String, // <- trailing comma required
) {
    // ...
}

// ✅ OK: Single line - no trailing comma needed
createUser(name = "John", email = "john@example.com", isActive = true, role = "admin")
```

## Rationale

These rules ensure:
1. **Clean interfaces**: No Hungarian notation or noise in interface names
2. **Self-documenting implementations**: Class name immediately tells you HOW it works
3. **Easier debugging**: Know implementation details without opening the file
4. **Better code review**: Reviewers understand architecture at a glance
5. **Maintainable code**: Small, focused classes that are easy to understand and test
6. **Testability**: Interface-based design enables easy mocking and testing

## Examples in Context

```kotlin
// Interface - clean, no prefix/suffix
interface BillibApi {
    suspend fun getAllCharts(): Result<List<Chart>>
}

// Implementations - descriptive names
class KtorBillibApi(private val httpClient: HttpClient) : BillibApi { ... }
class MockBillibApi(private val testData: List<Chart>) : BillibApi { ... }

// Repository pattern
interface ChartRepository {
    suspend fun getCharts(): List<Chart>
}

class NetworkChartRepository(private val api: BillibApi) : ChartRepository { ... }
class CachedChartRepository(private val cache: Cache, private val network: ChartRepository) : ChartRepository { ... }
class OfflineChartRepository(private val database: Database) : ChartRepository { ... }
```
