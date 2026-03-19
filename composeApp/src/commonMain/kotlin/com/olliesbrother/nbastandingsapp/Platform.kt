package com.olliesbrother.nbastandingsapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform