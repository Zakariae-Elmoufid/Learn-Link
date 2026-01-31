# Unit Testing Lifecycle in Spring Boot

## What are Unit Tests?

Unit tests verify that individual components (units) of code work correctly **in isolation**. They test a single method or class without relying on external dependencies like databases, web services, or file systems.

---

## Why Write Unit Tests?

✅ **Find bugs early** - Catch issues before they reach production  
✅ **Prevent regressions** - Ensure new code doesn't break existing features  
✅ **Document behavior** - Tests show how code should work  
✅ **Enable refactoring** - Change code confidently knowing tests will catch breaks  
✅ **Faster feedback** - Run in milliseconds vs. integration tests  

---

## Test Lifecycle in JUnit 5

### Lifecycle Annotations

```java
@BeforeAll      // Runs ONCE before all tests in the class (static method)
@BeforeEach     // Runs BEFORE EACH test method
@Test           // Marks a method as a test
@AfterEach      // Runs AFTER EACH test method
@AfterAll       // Runs ONCE after all tests complete (static method)
```

### Execution Flow

```
@BeforeAll
    ↓
@BeforeEach → @Test (test1) → @AfterEach
    ↓
@BeforeEach → @Test (test2) → @AfterEach
    ↓
@BeforeEach → @Test (test3) → @AfterEach
    ↓
@AfterAll
```

### Example

```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {

    @BeforeAll
    static void setupOnce() {
        // Runs once before all tests
        // Use for expensive setup (e.g., start test containers)
    }

    @BeforeEach
    void setUp() {
        // Runs before each test
        // Initialize fresh test data
        testData = new TestData();
    }

    @Test
    void testSomething() {
        // The actual test
    }

    @AfterEach
    void tearDown() {
        // Runs after each test
        // Clean up resources
        testData = null;
    }

    @AfterAll
    static void cleanupOnce() {
        // Runs once after all tests
        // Use for expensive cleanup
    }
}
```

---

## AAA Pattern (Arrange-Act-Assert)

Every test should follow this structure:

```java
@Test
void testCreateTask_Success() {
    // ARRANGE (Given) - Set up test conditions
    TaskRequest request = new TaskRequest(...);
    when(repository.save(any())).thenReturn(task);
    
    // ACT (When) - Execute the method being tested
    TaskResponse result = service.createTask(userId, request);
    
    // ASSERT (Then) - Verify the results
    assertNotNull(result);
    assertEquals("Expected", result.getTitle());
    verify(repository, times(1)).save(any());
}
```

---

## Mockito: Mocking Dependencies

### Why Mock?

Unit tests should test **one thing in isolation**. Mocking replaces real dependencies with fake objects you control.

### Key Annotations

```java
@ExtendWith(MockitoExtension.class)  // Enable Mockito
class ServiceTest {

    @Mock
    private Repository repository;  // Create mock object
    
    @Mock
    private Mapper mapper;
    
    @InjectMocks
    private ServiceImpl service;  // Inject mocks into this
}
```

### Stubbing Behavior

```java
// When this method is called, return this value
when(repository.findById(1L)).thenReturn(Optional.of(task));

// For any argument
when(repository.save(any(Task.class))).thenReturn(task);

// Throw an exception
when(repository.findById(999L))
    .thenThrow(new ResourceNotFoundException());

// Do nothing (for void methods)
doNothing().when(repository).delete(task);
```

### Verifying Method Calls

```java
// Verify method was called once
verify(repository, times(1)).save(any());

// Verify never called
verify(repository, never()).delete(any());

// Verify called at least once
verify(repository, atLeastOnce()).findById(1L);

// Verify exact arguments
verify(repository).save(eq(task));
```

### Capturing Arguments

```java
ArgumentCaptor<TaskCreatedEvent> eventCaptor = 
    ArgumentCaptor.forClass(TaskCreatedEvent.class);
    
verify(eventPublisher).publishEvent(eventCaptor.capture());

TaskCreatedEvent event = eventCaptor.getValue();
assertEquals(1L, event.getUserId());
```

---

## Common Assertions

```java
// Equality
assertEquals(expected, actual);
assertNotEquals(notExpected, actual);

// Null checks
assertNull(value);
assertNotNull(value);

// Boolean
assertTrue(condition);
assertFalse(condition);

// Exceptions
assertThrows(Exception.class, () -> service.doSomething());

// Collections
assertIterableEquals(expectedList, actualList);
assertTrue(list.contains(item));
```

---

## Best Practices

### ✅ DO

1. **Test one thing per test**
   ```java
   @Test
   void testCreateTask_Success() { ... }
   
   @Test
   void testCreateTask_WithNullTags() { ... }
   ```

2. **Use descriptive test names**
   ```java
   @Test
   @DisplayName("Should throw ResourceNotFoundException when task not found")
   void testGetTaskById_NotFound() { ... }
   ```

3. **Keep tests independent**  
   - Each test should run in isolation
   - Don't depend on test execution order

4. **Test edge cases**
   - Null values
   - Empty lists
   - Boundary conditions
   - Exception scenarios

5. **Keep tests fast**
   - Use mocks, not real databases
   - Target: <5 seconds for all unit tests

### ❌ DON'T

1. **Don't test external dependencies**  
   ❌ Real database connections  
   ✅ Mock the repository

2. **Don't over-mock**  
   ❌ Mocking simple DTOs or POJOs  
   ✅ Only mock complex dependencies

3. **Don't ignore failing tests**  
   Fix or remove them immediately

4. **Don't test framework code**  
   ❌ Testing Spring's dependency injection  
   ✅ Test YOUR business logic

---

## Running Tests

### Maven Commands

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TaskServiceImplTest

# Run specific test method
mvn test -Dtest=TaskServiceImplTest#testCreateTask_Success

# Run tests with coverage (if Jacoco configured)
mvn clean test jacoco:report

# Skip tests during build
mvn clean install -DskipTests
```

### IDE Support

**IntelliJ IDEA:**
- Right-click test class → Run 'TaskServiceImplTest'
- Click green arrow next to test method
- Ctrl+Shift+F10 to run current test

**VS Code:**
- Install "Test Runner for Java" extension
- Click "Run Test" above test method
- View results in Test Explorer

---

## Test Coverage

**Coverage metrics:**
- **Line Coverage**: % of code lines executed
- **Branch Coverage**: % of if/else branches taken
- **Method Coverage**: % of methods called

**Target: 80%+ coverage for critical business logic**

**View coverage in Maven (with Jacoco):**
```bash
mvn clean test jacoco:report
# Open: target/site/jacoco/index.html
```

---

## Example: Complete Test Class

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private TaskMapper taskMapper;
    
    @InjectMocks
    private TaskServiceImpl taskService;
    
    private Task task;
    private TaskResponse response;

    @BeforeEach
    void setUp() {
        task = Task.builder()
                .id(1L)
                .title("Test Task")
                .build();
                
        response = TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .build();
    }

    @Test
    @DisplayName("Should create task successfully")
    void testCreateTask_Success() {
        // Arrange
        when(taskRepository.save(any())).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(response);
        
        // Act
        TaskResponse result = taskService.createTask(1L, request);
        
        // Assert
        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should throw exception when task not found")
    void testGetTaskById_NotFound() {
        // Arrange
        when(taskRepository.findById(999L))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> taskService.getTaskById(999L));
    }
}
```

---

## Summary

**Unit Test Lifecycle:**
1. `@BeforeEach` - Setup test data
2. `@Test` - Execute and verify
3. `@AfterEach` - Cleanup (if needed)

**Key Concepts:**
- **AAA Pattern**: Arrange → Act → Assert
- **Mocking**: Isolate code under test
- **Verification**: Ensure methods were called
- **Fast Execution**: Milliseconds, not seconds

**Remember:**
- Write tests for **YOUR** code, not frameworks
- Keep tests **simple** and **focused**
- Test **behavior**, not implementation
- Aim for **80%+ coverage** on business logic
