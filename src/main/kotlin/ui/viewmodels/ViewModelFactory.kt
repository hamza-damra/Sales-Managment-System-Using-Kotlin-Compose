package ui.viewmodels

import data.api.services.*
import data.repository.*
import data.di.AppDependencies

/**
 * Factory class for creating ViewModels with their dependencies
 * Now uses the main AppContainer for proper authentication
 */
object ViewModelFactory {

    // Use the main app container for all dependencies to ensure proper authentication
    private val container = AppDependencies.container
    
    // ViewModels - now using authenticated services from main container
    fun createSupplierViewModel(): SupplierViewModel {
        return SupplierViewModel(container.supplierRepository)
    }

    fun createProductViewModel(): ProductViewModel {
        return ProductViewModel(container.productRepository, container.categoryRepository)
    }

    fun createCategoryViewModel(): CategoryViewModel {
        return CategoryViewModel(container.categoryRepository)
    }

    fun createSalesViewModel(): SalesViewModel {
        return SalesViewModel(container.salesRepository, container.customerRepository, container.productRepository, container.promotionRepository)
    }

    fun createCustomerViewModel(): CustomerViewModel {
        return CustomerViewModel(container.customerRepository)
    }

    fun createReturnsViewModel(): ReturnsViewModel {
        return ReturnsViewModel(container.returnRepository, container.customerRepository, container.productRepository)
    }

    fun createPromotionViewModel(): PromotionViewModel {
        return PromotionViewModel(container.promotionRepository)
    }
}
