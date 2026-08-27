package com.example.blankapp.viewmodel

import com.example.blankapp.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for Parent Finance/Invoices screen.
 * Manages invoices, payments, and balance data.
 */
@HiltViewModel
class ParentFinanceViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    // Invoices data
    private val _invoices = MutableStateFlow<List<MockInvoice>>(emptyList())
    val invoices: StateFlow<List<MockInvoice>> = _invoices.asStateFlow()

    // Family balance
    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    // Overdue invoices
    private val _overdueInvoices = MutableStateFlow<List<MockInvoice>>(emptyList())
    val overdueInvoices: StateFlow<List<MockInvoice>> = _overdueInvoices.asStateFlow()

    /**
     * Load finance data for the current parent.
     */
    fun loadFinance() {
        val parentId = authRepository.getCurrentUser()?.id ?: return

        launchWithLoading {
            // Get students for this parent
            val students = getStudentsByParent(parentId)
            // Get invoices for all students
            val allInvoices = mutableListOf<MockInvoice>()
            for (student in students) {
                allInvoices.addAll(getInvoicesByStudent(student.id))
            }
            _invoices.value = allInvoices
            _balance.value = getFamilyBalance(parentId)
            _overdueInvoices.value = allInvoices.filter { it.status == InvoiceStatus.OVERDUE }
        }
    }

    /**
     * Refresh finance data.
     */
    fun refresh() {
        loadFinance()
    }

    /**
     * Get total outstanding amount.
     */
    fun getTotalOutstanding(): Double {
        return _invoices.value
            .filter { it.status == InvoiceStatus.PENDING || it.status == InvoiceStatus.OVERDUE }
            .sumOf { it.amount }
    }

    /**
     * Get formatted balance.
     */
    fun getFormattedBalance(): String = "R${_balance.value.toInt()}"

    /**
     * Get overdue count.
     */
    fun getOverdueCount(): Int = _overdueInvoices.value.size
}
