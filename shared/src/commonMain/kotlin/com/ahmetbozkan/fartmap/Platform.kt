package com.ahmetbozkan.fartmap

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform