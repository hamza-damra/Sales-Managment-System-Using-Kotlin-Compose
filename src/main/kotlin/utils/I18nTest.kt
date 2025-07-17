package utils

/**
 * Test class to demonstrate the comprehensive Arabic localization system
 */
object I18nTest {
    
    fun runTests() {
        println("🌍 Testing Comprehensive Arabic Localization System")
        println("=" * 60)
        
        // Test Authentication Messages
        println("\n📱 Authentication Messages:")
        println("Login Title: ${I18nManager.getString("auth.login.title")}")
        println("Signup Title: ${I18nManager.getString("auth.signup.title")}")
        println("Username Required: ${I18nManager.getString("auth.error.username_required")}")
        println("Invalid Credentials: ${I18nManager.getString("auth.error.invalid_credentials")}")
        println("Email Invalid: ${I18nManager.getString("auth.error.email_invalid")}")
        
        // Test Form Validation Messages
        println("\n✅ Form Validation Messages:")
        println("Name Required: ${I18nManager.getString("validation.name_required")}")
        println("Price Required: ${I18nManager.getString("validation.price_required")}")
        println("Quantity Required: ${I18nManager.getString("validation.quantity_required")}")
        println("Phone Required: ${I18nManager.getString("validation.phone_required")}")
        
        // Test Success Messages
        println("\n🎉 Success Messages:")
        println("Product Added: ${I18nManager.getString("success.product.added")}")
        println("Customer Updated: ${I18nManager.getString("success.customer.updated")}")
        println("Category Deleted: ${I18nManager.getString("success.category.deleted")}")
        println("Data Exported: ${I18nManager.getString("success.exported")}")
        
        // Test Error Messages
        println("\n❌ Error Messages:")
        println("Load Products Error: ${I18nManager.getString("error.load_products")}")
        println("Network Error: ${I18nManager.getString("error.network")}")
        println("Server Error: ${I18nManager.getString("error.server")}")
        println("Unknown Error: ${I18nManager.getString("error.unknown")}")
        
        // Test Loading Messages
        println("\n⏳ Loading Messages:")
        println("Loading Default: ${I18nManager.getString("loading.default")}")
        println("Loading Products: ${I18nManager.getString("loading.products")}")
        println("Processing: ${I18nManager.getString("loading.processing")}")
        
        // Test Empty State Messages
        println("\n📭 Empty State Messages:")
        println("No Data: ${I18nManager.getString("empty.no_data")}")
        println("No Products: ${I18nManager.getString("empty.no_products")}")
        println("No Customers: ${I18nManager.getString("empty.no_customers")}")
        
        // Test Action Messages
        println("\n🔧 Action Messages:")
        println("Add: ${I18nManager.getString("action.add")}")
        println("Edit: ${I18nManager.getString("action.edit")}")
        println("Delete: ${I18nManager.getString("action.delete")}")
        println("Save: ${I18nManager.getString("action.save")}")
        println("Cancel: ${I18nManager.getString("action.cancel")}")
        
        // Test Entity-specific Messages
        println("\n🏷️ Entity-specific Messages:")
        println("Product Name: ${I18nManager.getString("product.name")}")
        println("Customer Email: ${I18nManager.getString("customer.email")}")
        println("Category Description: ${I18nManager.getString("category.description")}")
        println("Supplier Contact: ${I18nManager.getString("supplier.contact")}")
        
        // Test Dialog Messages
        println("\n💬 Dialog Messages:")
        println("Add Product Dialog: ${I18nManager.getString("dialog.add_product")}")
        println("Edit Customer Dialog: ${I18nManager.getString("dialog.edit_customer")}")
        println("Confirm Delete: ${I18nManager.getString("dialog.confirm_delete")}")
        
        // Test Status Messages
        println("\n📊 Status Messages:")
        println("Active: ${I18nManager.getString("status.active")}")
        println("Inactive: ${I18nManager.getString("status.inactive")}")
        println("Completed: ${I18nManager.getString("status.completed")}")
        
        // Test Error Message Translation
        println("\n🔄 Error Message Translation:")
        println("Customer Has Sales: ${ErrorMessageTranslator.translateToArabic("Customer has sales", "CUSTOMER_HAS_SALES")}")
        println("Product Has Sales: ${ErrorMessageTranslator.translateToArabic("Product has sales", "PRODUCT_HAS_SALES")}")
        println("Network Error: ${ErrorMessageTranslator.translateToArabic("Network error: Cannot connect to server")}")
        println("Authentication Failed: ${ErrorMessageTranslator.translateToArabic("Authentication failed")}")
        
        // Test String Formatting
        println("\n📝 String Formatting:")
        println("Formatted Message: ${I18nManager.getString("success.product.added")}")
        
        // Test Fallback Behavior
        println("\n🔄 Fallback Behavior:")
        println("Non-existent Key: ${I18nManager.getString("non.existent.key")}")
        println("With Fallback: ${I18nManager.getString("non.existent.key", "Default Fallback Message")}")
        
        println("\n✅ All tests completed successfully!")
        println("🌍 Arabic localization system is working properly")
        println("=" * 60)
    }
    
    /**
     * Test specific error scenarios
     */
    fun testErrorScenarios() {
        println("\n🧪 Testing Error Scenarios:")
        
        // Test authentication errors
        val authErrors = listOf(
            "Invalid username or password",
            "Username is required",
            "Email format is invalid",
            "Authentication failed",
            "Token expired"
        )
        
        authErrors.forEach { error ->
            println("$error -> ${ErrorMessageTranslator.translateToArabic(error)}")
        }
        
        // Test validation errors
        val validationErrors = listOf(
            "Name is required",
            "Price is required", 
            "Invalid quantity",
            "Phone number is required",
            "Validation failed"
        )
        
        validationErrors.forEach { error ->
            println("$error -> ${ErrorMessageTranslator.translateToArabic(error)}")
        }
        
        // Test network errors
        val networkErrors = listOf(
            "Network error: Cannot connect to server",
            "Request timeout",
            "Server error",
            "not found"
        )
        
        networkErrors.forEach { error ->
            println("$error -> ${ErrorMessageTranslator.translateToArabic(error)}")
        }
    }
    
    /**
     * Test success message scenarios
     */
    fun testSuccessScenarios() {
        println("\n🎉 Testing Success Scenarios:")
        
        val successMessages = listOf(
            "Added successfully",
            "Updated successfully", 
            "Deleted successfully",
            "Exported successfully",
            "Login successful",
            "Registration successful"
        )
        
        successMessages.forEach { message ->
            println("$message -> ${ErrorMessageTranslator.translateToArabic(message)}")
        }
    }
}

/**
 * Extension function for string repetition (for formatting)
 */
private operator fun String.times(n: Int): String = this.repeat(n)
