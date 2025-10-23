package hi.petter.presentation.ui.chat.viewmodel

import androidx.lifecycle.*
import hi.petter.domain.model.Contact
import hi.petter.domain.model.User
import hi.petter.domain.usecase.GetContactsUseCase
import hi.petter.domain.usecase.SearchContactsUseCase
import hi.petter.domain.usecase.AddContactUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactViewModel(
    private val getContactsUseCase: GetContactsUseCase,
    private val searchContactsUseCase: SearchContactsUseCase,
    private val addContactUseCase: AddContactUseCase
) : ViewModel() {

    private val _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> = _contacts

    private val _searchResults = MutableLiveData<List<Contact>>()
    val searchResults: LiveData<List<Contact>> = _searchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val result = getContactsUseCase()
                if (result.isSuccess) {
                    _contacts.postValue(result.getOrNull() ?: emptyList())
                } else {
                    _errorMessage.postValue(result.exceptionOrNull()?.message ?: "加载联系人失败")
                }
                _errorMessage.postValue(null)
            } catch (e: Exception) {
                _errorMessage.postValue(e.message ?: "加载联系人失败")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun searchContacts(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = searchContactsUseCase(query)
                if (result.isSuccess) {
                    _searchResults.postValue(result.getOrNull() ?: emptyList())
                } else {
                    _errorMessage.postValue(result.exceptionOrNull()?.message ?: "搜索失败")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("搜索失败: ${e.message}")
            }
        }
    }

    fun addContact(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                addContactUseCase(userId)
                loadContacts() // 重新加载联系人列表
                _errorMessage.postValue(null)
            } catch (e: Exception) {
                _errorMessage.postValue(e.message ?: "添加联系人失败")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun refreshContacts() {
        loadContacts()
    }
}

class ContactViewModelFactory(
    private val getContactsUseCase: GetContactsUseCase,
    private val searchContactsUseCase: SearchContactsUseCase,
    private val addContactUseCase: AddContactUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactViewModel(
                getContactsUseCase,
                searchContactsUseCase,
                addContactUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}