package io.lab10.vallet

interface TokenStorageBase {
    fun create()
    fun store()
    fun fetch()
}