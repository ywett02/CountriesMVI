package com.strv.ktools

import android.util.Log

private var logTag = "Log"
private var logEnabled = true
private var showCodeLocation = true
private var showCodeLocationThread = false
private var showCodeLocationLine = false

fun setLogEnabled(enabled: Boolean) {
    logEnabled = enabled
}

fun setLogTag(tag: String) {
    logTag = tag
}

fun setShowCodeLocation(enabled: Boolean) {
    showCodeLocation = enabled
}

fun setShowCodeLocationThread(enabled: Boolean) {
    showCodeLocationThread = enabled
}

fun setShowCodeLocationLine(enabled: Boolean) {
    showCodeLocationLine = enabled
}

fun log(message: String, vararg args: Any?) {
    if (logEnabled) Log.d(logTag, getCodeLocation().toString() + message.format(args))
}

fun logD(message: String, vararg args: Any?) {
    if (logEnabled) Log.d(logTag, getCodeLocation().toString() + message.format(args))
}

fun logE(message: String, vararg args: Any?) {
    if (logEnabled) Log.e(logTag, getCodeLocation().toString() + message.format(args))
}

fun logI(message: String, vararg args: Any?) {
    if (logEnabled) Log.i(logTag, getCodeLocation().toString() + message.format(args))
}

fun logW(message: String, vararg args: Any?) {
    if (logEnabled) Log.w(logTag, getCodeLocation().toString() + message.format(args))
}

fun Any?.logMe() {
    if (logEnabled) Log.d(logTag, getCodeLocation().toString() + this.toString())
}

fun Any?.logMeD() {
    if (logEnabled) Log.d(logTag, getCodeLocation().toString() + this.toString())
}

fun Any?.logMeI() {
    if (logEnabled) Log.i(logTag, getCodeLocation().toString() + this.toString())
}

private fun getCodeLocation(depth: Int = 3): CodeLocation {
    val stackTrace = Throwable().stackTrace
    val filteredStackTrace = arrayOfNulls<StackTraceElement>(stackTrace.size - depth)
    System.arraycopy(stackTrace, depth, filteredStackTrace, 0, filteredStackTrace.size)
    return CodeLocation(filteredStackTrace)
}

private class CodeLocation(stackTrace: Array<StackTraceElement?>) {
    private val thread: String
    private val fileName: String
    private val className: String
    private val method: String
    private val lineNumber: Int

    init {
        val root = stackTrace[0]
        thread = Thread.currentThread().name
        fileName = root!!.fileName
        val className = root.className
        this.className = className.substring(className.lastIndexOf('.') + 1)
        method = root.methodName
        lineNumber = root.lineNumber
    }

    override fun toString(): String {
        val builder = StringBuilder()
        if (showCodeLocation) {
            builder.append('[')
            if (showCodeLocationThread) {
                builder.append(thread)
                builder.append('.')
            }
            builder.append(className)
            builder.append('.')
            builder.append(method)
            if (showCodeLocationLine) {
                builder.append('(')
                builder.append(fileName)
                builder.append(':')
                builder.append(lineNumber)
                builder.append(')')
            }
            builder.append("] ")
        }
        return builder.toString()
    }
}


