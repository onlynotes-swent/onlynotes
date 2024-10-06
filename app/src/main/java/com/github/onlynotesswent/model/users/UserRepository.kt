package com.github.onlynotesswent.model.users

interface UserRepository {

    fun init(onSuccess: () -> Unit)

    fun getUserById(id: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)

    fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    fun deleteUserById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    fun addUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    fun getUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit)

    fun getNewUid(): String
}