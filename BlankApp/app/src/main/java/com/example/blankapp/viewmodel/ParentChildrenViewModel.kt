package com.example.blankapp.viewmodel

import com.example.blankapp.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for Parent Children list screen.
 * Manages the list of children and their details.
 */
@HiltViewModel
class ParentChildrenViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    // Children data
    private val _children = MutableStateFlow<List<MockStudent>>(emptyList())
    val children: StateFlow<List<MockStudent>> = _children.asStateFlow()

    // Selected child for detail view
    private val _selectedChild = MutableStateFlow<MockStudent?>(null)
    val selectedChild: StateFlow<MockStudent?> = _selectedChild.asStateFlow()

    /**
     * Load children for the current parent.
     */
    fun loadChildren() {
        val parentId = authRepository.getCurrentUser()?.id ?: return

        launchWithLoading {
            _children.value = getStudentsByParent(parentId)
        }
    }

    /**
     * Select a child for detail view.
     */
    fun selectChild(student: MockStudent) {
        _selectedChild.value = student
    }

    /**
     * Clear selected child.
     */
    fun clearSelectedChild() {
        _selectedChild.value = null
    }

    /**
     * Get child by ID.
     */
    fun getChildById(id: String): MockStudent? {
        return _children.value.find { it.id == id }
    }
}
