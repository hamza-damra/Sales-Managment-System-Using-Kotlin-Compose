# Testing Recommendations for Sales-Promotion Integration

## Overview

This document provides comprehensive testing recommendations for the newly integrated sales-promotion functionality in the Kotlin Compose frontend. The testing strategy covers unit tests, integration tests, and user acceptance testing.

## Test Setup

### Prerequisites
1. **Java Development Kit**: Ensure JDK 11 or higher is installed and JAVA_HOME is set
2. **Gradle**: The project uses Gradle for build management
3. **Test Dependencies**: Added to build.gradle.kts:
   - JUnit 5 for unit testing
   - Kotlinx Coroutines Test for async testing
   - Ktor Client Mock for API testing

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "data.api.PromotionDataModelsTest"

# Run integration tests
./gradlew test --tests "integration.*"

# Run with detailed output
./gradlew test --info
```

## Test Categories

### 1. Unit Tests

#### Data Model Tests (`src/test/kotlin/data/api/PromotionDataModelsTest.kt`)
- ✅ **SaleDTO with promotion fields**: Validates enhanced SaleDTO structure
- ✅ **AppliedPromotionDTO creation**: Tests promotion application data
- ✅ **SaleDTO without promotions**: Ensures backward compatibility
- ✅ **PromotionDTO validation**: Tests promotion data structure

**Key Test Scenarios:**
```kotlin
@Test
fun `test SaleDTO with promotion fields`()
@Test
fun `test AppliedPromotionDTO creation`()
@Test
fun `test SaleDTO without promotions`()
@Test
fun `test PromotionDTO with all fields`()
```

#### Repository Tests (Recommended)
Create tests for `SalesRepository` promotion methods:
```kotlin
// src/test/kotlin/data/repository/SalesRepositoryTest.kt
@Test
fun `test createSale with coupon code`()
@Test
fun `test applyPromotionToSale success`()
@Test
fun `test removePromotionFromSale success`()
@Test
fun `test getEligiblePromotionsForSale`()
```

#### ViewModel Tests (Recommended)
Create tests for `SalesViewModel` promotion logic:
```kotlin
// src/test/kotlin/ui/viewmodels/SalesViewModelTest.kt
@Test
fun `test validateAndApplyPromotion success`()
@Test
fun `test validateAndApplyPromotion invalid coupon`()
@Test
fun `test createSale with promotion`()
@Test
fun `test promotion discount calculation`()
```

### 2. Integration Tests

#### Sales-Promotion Integration (`src/test/kotlin/integration/SalesPromotionIntegrationTest.kt`)
- ✅ **Sale creation with coupon**: Tests end-to-end promotion application
- ✅ **Promotion application to existing sale**: Tests promotion management
- ✅ **Eligible promotions retrieval**: Tests promotion discovery

**Key Test Scenarios:**
```kotlin
@Test
fun `test create sale with coupon code applies promotion`()
@Test
fun `test apply promotion to existing sale`()
@Test
fun `test get eligible promotions for sale`()
```

#### API Service Tests (Recommended)
Create tests for `SalesApiService` promotion endpoints:
```kotlin
// src/test/kotlin/data/api/services/SalesApiServiceTest.kt
@Test
fun `test createSale with couponCode parameter`()
@Test
fun `test applyPromotionToSale endpoint`()
@Test
fun `test removePromotionFromSale endpoint`()
@Test
fun `test getEligiblePromotionsForSale endpoint`()
```

### 3. UI Component Tests (Recommended)

#### Promotion UI Tests
Create tests for promotion-related UI components:
```kotlin
// src/test/kotlin/ui/screens/PromotionUITest.kt
@Test
fun `test PromotionCodeSection displays correctly`()
@Test
fun `test promotion validation feedback`()
@Test
fun `test applied promotion display`()
@Test
fun `test promotion error handling`()
```

## Manual Testing Scenarios

### 1. **Promotion Application During Sale Creation**

**Test Steps:**
1. Navigate to Sales → New Sale
2. Add products to cart
3. Select a customer
4. Enter a valid promotion code (e.g., "SUMMER20")
5. Click "Apply Promotion"
6. Verify discount is applied and totals are updated
7. Complete the sale
8. Verify promotion is saved with the sale

**Expected Results:**
- Promotion code validates successfully
- Discount amount is calculated correctly
- Final total reflects the discount
- Sale is created with promotion details

### 2. **Invalid Promotion Code Handling**

**Test Steps:**
1. Navigate to Sales → New Sale
2. Add products to cart
3. Enter an invalid promotion code
4. Click "Apply Promotion"
5. Verify error message is displayed

**Expected Results:**
- Clear error message in Arabic
- No discount applied
- User can try different code

### 3. **Minimum Order Amount Validation**

**Test Steps:**
1. Create a promotion with minimum order amount
2. Add products below the minimum amount
3. Try to apply the promotion
4. Verify validation message

**Expected Results:**
- Promotion is rejected
- Clear message about minimum order requirement
- User can add more products to meet minimum

### 4. **Promotion Removal**

**Test Steps:**
1. Apply a promotion to a sale
2. Click the remove/clear promotion button
3. Verify promotion is removed and totals updated

**Expected Results:**
- Promotion is cleared
- Totals revert to original amounts
- UI updates correctly

### 5. **Multiple Promotions (Future Feature)**

**Test Steps:**
1. Apply first promotion
2. Try to apply second promotion
3. Verify behavior based on stacking rules

**Expected Results:**
- Stacking rules are enforced
- Clear feedback on promotion conflicts
- Proper total calculations

## Performance Testing

### 1. **Promotion Validation Performance**
- Test promotion validation with large promotion lists
- Measure response times for promotion application
- Verify UI responsiveness during validation

### 2. **Sale Creation Performance**
- Test sale creation with promotions
- Measure impact of promotion calculations
- Verify no performance degradation

### 3. **Memory Usage**
- Monitor memory usage with promotion data
- Test with large numbers of applied promotions
- Verify proper cleanup of promotion state

## Error Handling Testing

### 1. **Network Errors**
- Test promotion application with network failures
- Verify graceful error handling
- Test retry mechanisms

### 2. **Backend Errors**
- Test with invalid promotion data from backend
- Verify error message translation
- Test fallback behaviors

### 3. **Validation Errors**
- Test all validation scenarios
- Verify user-friendly error messages
- Test error recovery flows

## Accessibility Testing

### 1. **Keyboard Navigation**
- Test promotion code input with keyboard only
- Verify tab order and focus management
- Test screen reader compatibility

### 2. **Visual Accessibility**
- Test promotion UI with high contrast themes
- Verify color-blind friendly design
- Test with different font sizes

## Regression Testing

### 1. **Existing Functionality**
- Verify existing sale creation still works
- Test sales without promotions
- Verify backward compatibility

### 2. **Data Migration**
- Test with existing sales data
- Verify legacy promotion fields work
- Test data consistency

## Test Data Setup

### Sample Promotions for Testing
```kotlin
// Percentage Promotion
val percentagePromotion = PromotionDTO(
    name = "Summer Sale 2024",
    type = "PERCENTAGE",
    discountValue = 20.0,
    couponCode = "SUMMER20",
    isActive = true
)

// Fixed Amount Promotion
val fixedAmountPromotion = PromotionDTO(
    name = "Welcome Discount",
    type = "FIXED_AMOUNT",
    discountValue = 15.0,
    couponCode = "WELCOME15",
    isActive = true
)

// Minimum Order Promotion
val minimumOrderPromotion = PromotionDTO(
    name = "Big Order Discount",
    type = "PERCENTAGE",
    discountValue = 10.0,
    minimumOrderAmount = 100.0,
    couponCode = "BIGORDER10",
    isActive = true
)
```

## Continuous Integration

### Automated Test Execution
- Run tests on every commit
- Generate test coverage reports
- Fail builds on test failures
- Monitor test performance

### Test Reporting
- Generate detailed test reports
- Track test coverage metrics
- Monitor test execution times
- Alert on test failures

## Conclusion

The testing strategy provides comprehensive coverage of the sales-promotion integration:

✅ **Unit Tests**: Validate individual components and business logic
✅ **Integration Tests**: Verify end-to-end workflows
✅ **Manual Testing**: Ensure user experience quality
✅ **Performance Testing**: Validate system performance
✅ **Error Handling**: Ensure robust error management
✅ **Regression Testing**: Maintain existing functionality

This testing approach ensures the promotion integration is reliable, performant, and user-friendly while maintaining the existing system's quality and stability.
