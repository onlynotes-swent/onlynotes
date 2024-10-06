package com.github.onlynotesswent.model.users

import androidx.lifecycle.ViewModel

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    fun getNewUid(): String {
        return repository.getNewUid()
    }

    fun addUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        repository.addUser(
            user,
            onSuccess,
            onFailure
        )
    }

    fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        repository.updateUser(
            user,
            onSuccess,
            onFailure
        )
    }

    fun getUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
        repository.getUsers(
            onSuccess,
            onFailure
        )
    }

    fun getUserById(id: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
        repository.getUserById(
            id,
            onSuccess,
            onFailure
        )
    }

    fun deleteUserById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        repository.deleteUserById(
            id,
            onSuccess,
            onFailure
        )
    }

    fun init(onSuccess: () -> Unit) {
        repository.init(onSuccess)
    }

}