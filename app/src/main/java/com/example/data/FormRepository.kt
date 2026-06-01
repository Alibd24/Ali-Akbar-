package com.example.data

import kotlinx.coroutines.flow.Flow

class FormRepository(private val formDao: FormDao) {
    val allForms: Flow<List<WithdrawalForm>> = formDao.getAllForms()

    suspend fun getFormById(id: String): WithdrawalForm? {
        return formDao.getFormById(id)
    }

    suspend fun insertForm(form: WithdrawalForm) {
        formDao.insertForm(form)
    }

    suspend fun updateForm(form: WithdrawalForm) {
        formDao.updateForm(form)
    }

    suspend fun deleteForm(form: WithdrawalForm) {
        formDao.deleteForm(form)
    }

    suspend fun deleteFormById(id: String) {
        formDao.deleteFormById(id)
    }
}
