package com.example.blankapp.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.blankapp.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Parent Home/Dashboard screen.
 * Manages children, documents, balance, and activity data.
 */
@HiltViewModel
class ParentHomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    // Children data
    private val _children = MutableStateFlow<List<MockStudent>>(emptyList())
    val children: StateFlow<List<MockStudent>> = _children.asStateFlow()

    // Documents data
    private val _documents = MutableStateFlow<List<MockDocument>>(emptyList())
    val documents: StateFlow<List<MockDocument>> = _documents.asStateFlow()

    // Family balance
    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    // Current user
    private val _currentUser = MutableStateFlow<MockUser?>(null)
    val currentUser: StateFlow<MockUser?> = _currentUser.asStateFlow()

    /**
     * Load dashboard data for the current parent.
     */
    fun loadDashboard() {
        val user = authRepository.getCurrentUser()
        _currentUser.value = user
        val parentId = user?.id ?: return

        launchWithLoading {
            // Load from backend (Supabase) — no mock data
            _children.value = try {
                SupabaseRepository.getParentStudents(parentId)
            } catch (e: Exception) {
                emptyList()
            }
            _documents.value = try {
                // Documents are per-student in the backend
                _children.value.flatMap { student ->
                    SupabaseRepository.getStudentDocuments(student.id)
                }
            } catch (e: Exception) {
                emptyList()
            }
            _balance.value = try {
                SupabaseRepository.getParentInvoices(parentId)
                    .filter { it.status == InvoiceStatus.PENDING || it.status == InvoiceStatus.OVERDUE }
                    .sumOf { it.amount }
            } catch (e: Exception) {
                0.0
            }
        }
    }

    /**
     * Refresh dashboard data.
     */
    fun refresh() {
        loadDashboard()
    }

    /**
     * Get child count.
     */
    fun getChildCount(): Int = _children.value.size

    /**
     * Get document count.
     */
    fun getDocumentCount(): Int = _documents.value.size

    /**
     * Get formatted balance.
     */
    fun getFormattedBalance(): String = "R${_balance.value.toInt()}"
}
