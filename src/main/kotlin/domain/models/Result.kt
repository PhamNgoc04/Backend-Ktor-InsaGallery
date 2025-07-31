package com.codewithngoc.instagallery.domain.models

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    data class Loading(val isLoading: Boolean) : Result<Nothing>()
}
