package com.example.blankapp.viewmodel

import com.example.blankapp.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for the multi-step registration flow.
 * Manages registration state across all steps.
 */
@HiltViewModel
class RegistrationViewModel @Inject constructor() : BaseViewModel() {

    // Current step (0-7)
    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    // Student details
    private val _firstName = MutableStateFlow("")
    val firstName: StateFlow<String> = _firstName.asStateFlow()

    private val _lastName = MutableStateFlow("")
    val lastName: StateFlow<String> = _lastName.asStateFlow()

    private val _dateOfBirth = MutableStateFlow("")
    val dateOfBirth: StateFlow<String> = _dateOfBirth.asStateFlow()

    private val _gender = MutableStateFlow("")
    val gender: StateFlow<String> = _gender.asStateFlow()

    private val _grade = MutableStateFlow("")
    val grade: StateFlow<String> = _grade.asStateFlow()

    private val _school = MutableStateFlow("")
    val school: StateFlow<String> = _school.asStateFlow()

    private val _classNumber = MutableStateFlow("")
    val classNumber: StateFlow<String> = _classNumber.asStateFlow()

    private val _teacherName = MutableStateFlow("")
    val teacherName: StateFlow<String> = _teacherName.asStateFlow()

    // Sports/Activities
    private val _selectedSports = MutableStateFlow<List<String>>(emptyList())
    val selectedSports: StateFlow<List<String>> = _selectedSports.asStateFlow()

    // Collection/Transport
    private val _collectionPerson1 = MutableStateFlow("")
    val collectionPerson1: StateFlow<String> = _collectionPerson1.asStateFlow()

    private val _contact1 = MutableStateFlow("")
    val contact1: StateFlow<String> = _contact1.asStateFlow()

    private val _vehicleReg1 = MutableStateFlow("")
    val vehicleReg1: StateFlow<String> = _vehicleReg1.asStateFlow()

    private val _collectionPerson2 = MutableStateFlow("")
    val collectionPerson2: StateFlow<String> = _collectionPerson2.asStateFlow()

    private val _contact2 = MutableStateFlow("")
    val contact2: StateFlow<String> = _contact2.asStateFlow()

    private val _vehicleReg2 = MutableStateFlow("")
    val vehicleReg2: StateFlow<String> = _vehicleReg2.asStateFlow()

    private val _transportRequired = MutableStateFlow(false)
    val transportRequired: StateFlow<Boolean> = _transportRequired.asStateFlow()

    // Medical info
    private val _doctorName = MutableStateFlow("")
    val doctorName: StateFlow<String> = _doctorName.asStateFlow()

    private val _doctorPhone = MutableStateFlow("")
    val doctorPhone: StateFlow<String> = _doctorPhone.asStateFlow()

    private val _allergies = MutableStateFlow("")
    val allergies: StateFlow<String> = _allergies.asStateFlow()

    private val _medicalConditions = MutableStateFlow<List<String>>(emptyList())
    val medicalConditions: StateFlow<List<String>> = _medicalConditions.asStateFlow()

    private val _medicalAid = MutableStateFlow("")
    val medicalAid: StateFlow<String> = _medicalAid.asStateFlow()

    private val _medicalAidNumber = MutableStateFlow("")
    val medicalAidNumber: StateFlow<String> = _medicalAidNumber.asStateFlow()

    // Parent details
    private val _motherName = MutableStateFlow("")
    val motherName: StateFlow<String> = _motherName.asStateFlow()

    private val _motherSurname = MutableStateFlow("")
    val motherSurname: StateFlow<String> = _motherSurname.asStateFlow()

    private val _motherIdNumber = MutableStateFlow("")
    val motherIdNumber: StateFlow<String> = _motherIdNumber.asStateFlow()

    private val _motherEmployer = MutableStateFlow("")
    val motherEmployer: StateFlow<String> = _motherEmployer.asStateFlow()

    private val _motherWorkPhone = MutableStateFlow("")
    val motherWorkPhone: StateFlow<String> = _motherWorkPhone.asStateFlow()

    private val _motherCell = MutableStateFlow("")
    val motherCell: StateFlow<String> = _motherCell.asStateFlow()

    private val _motherEmail = MutableStateFlow("")
    val motherEmail: StateFlow<String> = _motherEmail.asStateFlow()

    private val _fatherName = MutableStateFlow("")
    val fatherName: StateFlow<String> = _fatherName.asStateFlow()

    private val _fatherSurname = MutableStateFlow("")
    val fatherSurname: StateFlow<String> = _fatherSurname.asStateFlow()

    private val _fatherIdNumber = MutableStateFlow("")
    val fatherIdNumber: StateFlow<String> = _fatherIdNumber.asStateFlow()

    private val _fatherEmployer = MutableStateFlow("")
    val fatherEmployer: StateFlow<String> = _fatherEmployer.asStateFlow()

    private val _fatherWorkPhone = MutableStateFlow("")
    val fatherWorkPhone: StateFlow<String> = _fatherWorkPhone.asStateFlow()

    private val _fatherCell = MutableStateFlow("")
    val fatherCell: StateFlow<String> = _fatherCell.asStateFlow()

    private val _fatherEmail = MutableStateFlow("")
    val fatherEmail: StateFlow<String> = _fatherEmail.asStateFlow()

    // Consent
    private val _photoConsent = MutableStateFlow(false)
    val photoConsent: StateFlow<Boolean> = _photoConsent.asStateFlow()

    private val _signatureCaptured = MutableStateFlow(false)
    val signatureCaptured: StateFlow<Boolean> = _signatureCaptured.asStateFlow()

    // Payment
    private val _paymentMethod = MutableStateFlow("")
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _paymentCompleted = MutableStateFlow(false)
    val paymentCompleted: StateFlow<Boolean> = _paymentCompleted.asStateFlow()

    /**
     * Navigate to next step.
     */
    fun nextStep() {
        if (_currentStep.value < 7) {
            _currentStep.value++
        }
    }

    /**
     * Navigate to previous step.
     */
    fun previousStep() {
        if (_currentStep.value > 0) {
            _currentStep.value--
        }
    }

    /**
     * Go to specific step.
     */
    fun goToStep(step: Int) {
        if (step in 0..7) {
            _currentStep.value = step
        }
    }

    // Student details setters
    fun setFirstName(value: String) { _firstName.value = value }
    fun setLastName(value: String) { _lastName.value = value }
    fun setDateOfBirth(value: String) { _dateOfBirth.value = value }
    fun setGender(value: String) { _gender.value = value }
    fun setGrade(value: String) { _grade.value = value }
    fun setSchool(value: String) { _school.value = value }
    fun setClassNumber(value: String) { _classNumber.value = value }
    fun setTeacherName(value: String) { _teacherName.value = value }

    // Sports/Activities
    fun toggleSport(sport: String) {
        _selectedSports.value = if (sport in _selectedSports.value) {
            _selectedSports.value - sport
        } else {
            _selectedSports.value + sport
        }
    }

    // Collection/Transport setters
    fun setCollectionPerson1(value: String) { _collectionPerson1.value = value }
    fun setContact1(value: String) { _contact1.value = value }
    fun setVehicleReg1(value: String) { _vehicleReg1.value = value }
    fun setCollectionPerson2(value: String) { _collectionPerson2.value = value }
    fun setContact2(value: String) { _contact2.value = value }
    fun setVehicleReg2(value: String) { _vehicleReg2.value = value }
    fun setTransportRequired(value: Boolean) { _transportRequired.value = value }

    // Medical info setters
    fun setDoctorName(value: String) { _doctorName.value = value }
    fun setDoctorPhone(value: String) { _doctorPhone.value = value }
    fun setAllergies(value: String) { _allergies.value = value }
    fun setMedicalAid(value: String) { _medicalAid.value = value }
    fun setMedicalAidNumber(value: String) { _medicalAidNumber.value = value }

    fun toggleMedicalCondition(condition: String) {
        _medicalConditions.value = if (condition in _medicalConditions.value) {
            _medicalConditions.value - condition
        } else {
            _medicalConditions.value + condition
        }
    }

    // Parent details setters
    fun setMotherName(value: String) { _motherName.value = value }
    fun setMotherSurname(value: String) { _motherSurname.value = value }
    fun setMotherIdNumber(value: String) { _motherIdNumber.value = value }
    fun setMotherEmployer(value: String) { _motherEmployer.value = value }
    fun setMotherWorkPhone(value: String) { _motherWorkPhone.value = value }
    fun setMotherCell(value: String) { _motherCell.value = value }
    fun setMotherEmail(value: String) { _motherEmail.value = value }

    fun setFatherName(value: String) { _fatherName.value = value }
    fun setFatherSurname(value: String) { _fatherSurname.value = value }
    fun setFatherIdNumber(value: String) { _fatherIdNumber.value = value }
    fun setFatherEmployer(value: String) { _fatherEmployer.value = value }
    fun setFatherWorkPhone(value: String) { _fatherWorkPhone.value = value }
    fun setFatherCell(value: String) { _fatherCell.value = value }
    fun setFatherEmail(value: String) { _fatherEmail.value = value }

    // Consent
    fun setPhotoConsent(value: Boolean) { _photoConsent.value = value }
    fun setSignatureCaptured(value: Boolean) { _signatureCaptured.value = value }

    // Payment
    fun setPaymentMethod(value: String) { _paymentMethod.value = value }
    fun setPaymentCompleted(value: Boolean) { _paymentCompleted.value = value }

    /**
     * Validate current step.
     */
    fun validateCurrentStep(): Boolean {
        return when (_currentStep.value) {
            0 -> _firstName.value.isNotBlank() && _lastName.value.isNotBlank() && _grade.value.isNotBlank()
            1 -> true // Sports are optional
            2 -> _collectionPerson1.value.isNotBlank() && _contact1.value.isNotBlank()
            3 -> true // Medical info is optional
            4 -> _motherName.value.isNotBlank() && _motherCell.value.isNotBlank()
            5 -> _photoConsent.value && _signatureCaptured.value
            6 -> _paymentMethod.value.isNotBlank()
            7 -> true // Review step
            else -> false
        }
    }

    /**
     * Submit registration.
     */
    fun submitRegistration(onSuccess: () -> Unit) {
        launchWithLoading {
            // In a real app, this would call the API
            // For now, just simulate success
            kotlinx.coroutines.delay(1000)
            onSuccess()
        }
    }

    /**
     * Reset registration form.
     */
    fun reset() {
        _currentStep.value = 0
        _firstName.value = ""
        _lastName.value = ""
        _dateOfBirth.value = ""
        _gender.value = ""
        _grade.value = ""
        _school.value = ""
        _classNumber.value = ""
        _teacherName.value = ""
        _selectedSports.value = emptyList()
        _collectionPerson1.value = ""
        _contact1.value = ""
        _vehicleReg1.value = ""
        _collectionPerson2.value = ""
        _contact2.value = ""
        _vehicleReg2.value = ""
        _transportRequired.value = false
        _doctorName.value = ""
        _doctorPhone.value = ""
        _allergies.value = ""
        _medicalConditions.value = emptyList()
        _medicalAid.value = ""
        _medicalAidNumber.value = ""
        _motherName.value = ""
        _motherSurname.value = ""
        _motherIdNumber.value = ""
        _motherEmployer.value = ""
        _motherWorkPhone.value = ""
        _motherCell.value = ""
        _motherEmail.value = ""
        _fatherName.value = ""
        _fatherSurname.value = ""
        _fatherIdNumber.value = ""
        _fatherEmployer.value = ""
        _fatherWorkPhone.value = ""
        _fatherCell.value = ""
        _fatherEmail.value = ""
        _photoConsent.value = false
        _signatureCaptured.value = false
        _paymentMethod.value = ""
        _paymentCompleted.value = false
    }
}
