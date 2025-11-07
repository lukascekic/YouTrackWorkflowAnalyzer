package com.youtrack.analyzer.util

import mu.KotlinLogging

inline fun <reified T> T.logger() = KotlinLogging.logger {}
