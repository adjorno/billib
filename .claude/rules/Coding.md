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
- **DOES** use one of:
  - Prefix: `Default-`, `Base-`
  - Suffix: `-Impl`
  - Or descriptive name pointing to implementation details

**Examples:**
- ✅ `BillibApiImpl`
- ✅ `DefaultChartRepository`
- ✅ `BaseUserService`
- ✅ `KtorBillibApi` (descriptive - shows it uses Ktor)
- ✅ `MockChartRepository` (descriptive - shows it's a mock)
- ✅ `InMemoryUserService` (descriptive - shows storage type)

### Class Size Limit
- **Maximum 100 meaningful lines of code per class**
- Excludes comments, blank lines, and imports
- If a class exceeds this limit, refactor into smaller, focused classes
- Promotes single responsibility principle and maintainability

## Rationale

These rules ensure:
1. **Clean interfaces**: No Hungarian notation or noise in interface names
2. **Clear implementations**: Easy to identify concrete classes and their purpose
3. **Maintainable code**: Small, focused classes that are easy to understand and test
4. **Testability**: Interface-based design enables easy mocking and testing
