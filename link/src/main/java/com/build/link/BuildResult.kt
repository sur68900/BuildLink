package com.build.link

sealed class BuildResult {
    data class Error(val e: Throwable) : BuildResult()
    data class Success(val link: String) : BuildResult()
}