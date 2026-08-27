package com.example.blankapp.viewmodel

import com.example.blankapp.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for Admin Home/Dashboard screen.
 * Manages admin stats, applications, and overview data.
 */
@HiltViewModel
class AdminHomeViewModel @Inject constructor() : BaseViewModel() {

    // Total students
    private val _totalStudents = MutableStateFlow(0)
    val totalStudents: StateFlow<Int> = _totalStudents.asStateFlow()

    // Pending applications
    private val _pendingApplications = MutableStateFlow(0)
    val pendingApplications: StateFlow<Int> = _pendingApplications.asStateFlow()

    // Outstanding balance
    private val _outstandingBalance = MutableStateFlow(0.0)
    val outstandingBalance: StateFlow<Double> = _outstandingBalance.asStateFlow()

    // Recent applications
    private val _recentApplications = MutableStateFlow<List<MockApplication>>(emptyList())
    val recentApplications: StateFlow<List<MockApplication>> = _recentApplications.asStateFlow()

    // All students
    private val _students = MutableStateFlow<List<MockStudent>>(emptyList())
    val students: StateFlow<List<MockStudent>> = _students.asStateFlow()

    // All applications
    private val _applications = MutableStateFlow<List<MockApplication>>(emptyList())
    val applications: StateFlow<List<MockApplication>> = _applications.asStateFlow()

    // Parent count
    private val _parentCount = MutableStateFlow(0)
    val parentCount: StateFlow<Int> = _parentCount.asStateFlow()

    /**
     * Load admin dashboard data from backend (Supabase) — no mock data.
     */
    fun loadDashboard() {
        launchWithLoading {
            try {
                val studentsResult = SupabaseRepository.getAllStudents()
                val applicationsResult = SupabaseRepository.getAllApplications()
                val invoicesResult = SupabaseRepository.getAllInvoices()
                val parentsResult = SupabaseRepository.getAllParents()
                _students.value = studentsResult
                _totalStudents.value = studentsResult.size
                _applications.value = applicationsResult
                _pendingApplications.value = applicationsResult.count {
                    it.status == ApplicationStatus.SUBMITTED || it.status == ApplicationStatus.UNDER_REVIEW
                }
                _outstandingBalance.value = invoicesResult
                    .filter { it.status == InvoiceStatus.PENDING || it.status == InvoiceStatus.OVERDUE }
                    .sumOf { it.amount }
                _recentApplications.value = applicationsResult.filter {
                    it.status == ApplicationStatus.UNDER_REVIEW
                }.take(5)
                _parentCount.value = parentsResult.size
                // Surface any backend failure that returned empty instead of throwing
                reportBackendErrorIfAny()
            } catch (e: Exception) {
                // Leave state as empty on failure
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
     * Get formatted outstanding balance.
     */
    fun getFormattedBalance(): String = "R${_outstandingBalance.value.toInt()}"

    /**
     * Get active students count.
     */
    fun getActiveStudentsCount(): Int {
        return _students.value.count { it.status == StudentStatus.ACTIVE }
    }

    /**
     * Get pending students count.
     */
    fun getPendingStudentsCount(): Int {
        return _students.value.count { it.status == StudentStatus.PENDING }
    }
}
