package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.FormProduct
import com.example.data.FormRepository
import com.example.data.GeminiClient
import com.example.data.GroupMeta
import com.example.data.WithdrawalForm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class Screen {
    Dashboard, List, Details, Products, Preview
}

class MainViewModel(private val repository: FormRepository) : ViewModel() {

    // Main screen navigation state
    private val _currentScreen = MutableStateFlow(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Forms list with combined search queries
    val forms: StateFlow<List<WithdrawalForm>> = repository.allForms
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredForms: StateFlow<List<WithdrawalForm>> = combine(forms, searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            list.filter {
                it.chemistDetails.contains(query, ignoreCase = true) ||
                it.salesCenter.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active form state (currently editing)
    private val _activeForm = MutableStateFlow<WithdrawalForm?>(null)
    val activeForm: StateFlow<WithdrawalForm?> = _activeForm.asStateFlow()

    // Selected Month filter for dashboard reports ('all' or 'YYYY-MM')
    private val _dashboardMonthFilter = MutableStateFlow("all")
    val dashboardMonthFilter: StateFlow<String> = _dashboardMonthFilter.asStateFlow()

    // Loading states for Gemini services
    private val _isAiScanning = MutableStateFlow(false)
    val isAiScanning: StateFlow<Boolean> = _isAiScanning.asStateFlow()

    private val _isAiFormatting = MutableStateFlow(false)
    val isAiFormatting: StateFlow<Boolean> = _isAiFormatting.asStateFlow()

    // Navigation and workflow controllers
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setDashboardMonthFilter(monthKey: String) {
        _dashboardMonthFilter.value = monthKey
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Form logic
    fun createNewForm() {
        viewModelScope.launch(Dispatchers.IO) {
            val defaultMeta = listOf("UHP", "UMP", "UD", "NUV", "S&N", "Others").associateWith {
                GroupMeta("", "", "")
            }
            val newForm = WithdrawalForm(
                id = UUID.randomUUID().toString(),
                createdAt = System.currentTimeMillis(),
                groupMetaMap = defaultMeta
            )
            repository.insertForm(newForm)
            _activeForm.value = newForm
            viewModelScope.launch(Dispatchers.Main) {
                navigateTo(Screen.Details)
            }
        }
    }

    fun editForm(form: WithdrawalForm) {
        _activeForm.value = form
        navigateTo(Screen.Details)
    }

    fun deleteForm(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFormById(id)
            if (_activeForm.value?.id == id) {
                _activeForm.value = null
            }
        }
    }

    fun updateActiveForm(updater: (WithdrawalForm) -> WithdrawalForm) {
        val current = _activeForm.value ?: return
        val updated = updater(current)
        _activeForm.value = updated
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateForm(updated)
        }
    }

    // Product actions
    fun addProduct(product: FormProduct) {
        updateActiveForm { current ->
            // Check if product with identical name exists to sum quantities
            val existingIndex = current.products.indexOfFirst {
                it.name.trim().equals(product.name.trim(), ignoreCase = true)
            }
            val updatedProducts = current.products.toMutableList()
            if (existingIndex >= 0) {
                val existing = updatedProducts[existingIndex]
                val merged = existing.copy(qty = existing.qty + product.qty)
                updatedProducts[existingIndex] = merged
            } else {
                updatedProducts.add(product)
            }
            current.copy(products = updatedProducts)
        }
    }

    fun updateProduct(index: Int, product: FormProduct) {
        updateActiveForm { current ->
            val updatedProducts = current.products.toMutableList()
            if (index in updatedProducts.indices) {
                updatedProducts[index] = product
            }
            current.copy(products = updatedProducts)
        }
    }

    fun deleteProduct(index: Int) {
        updateActiveForm { current ->
            val updatedProducts = current.products.toMutableList()
            if (index in updatedProducts.indices) {
                updatedProducts.removeAt(index)
            }
            current.copy(products = updatedProducts)
        }
    }

    // Gemini actions
    fun formatChemistDetails() {
        val form = _activeForm.value ?: return
        if (form.chemistDetails.isBlank()) return

        _isAiFormatting.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val formatted = GeminiClient.formatChemistDetails(form.chemistDetails)
                viewModelScope.launch(Dispatchers.Main) {
                    updateActiveForm { it.copy(chemistDetails = formatted) }
                }
            } finally {
                _isAiFormatting.value = false
            }
        }
    }

    fun scanInvoiceImage(base64Image: String, mimeType: String, onSuccess: (Int) -> Unit, onError: () -> Unit) {
        val form = _activeForm.value ?: return
        _isAiScanning.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val extracted = GeminiClient.scanInvoiceForProducts(base64Image, mimeType)
                if (extracted.isNotEmpty()) {
                    viewModelScope.launch(Dispatchers.Main) {
                        updateActiveForm { current ->
                            val updated = current.products.toMutableList()
                            extracted.forEach { product ->
                                val existingIndex = updated.indexOfFirst {
                                    it.name.trim().equals(product.name.trim(), ignoreCase = true)
                                }
                                if (existingIndex >= 0) {
                                    val existing = updated[existingIndex]
                                    updated[existingIndex] = existing.copy(qty = existing.qty + product.qty)
                                } else {
                                    updated.add(product)
                                }
                            }
                            current.copy(products = updated)
                        }
                        onSuccess(extracted.size)
                    }
                } else {
                    viewModelScope.launch(Dispatchers.Main) { onError() }
                }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) { onError() }
            } finally {
                _isAiScanning.value = false
            }
        }
    }
}

class MainViewModelFactory(private val repository: FormRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
