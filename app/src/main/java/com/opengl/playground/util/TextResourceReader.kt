package com.opengl.playground.util

import android.content.Context

object TextResourceReader {
    fun readTextFileFromResource(context: Context, resourceId: Int): String {
        val body = StringBuilder()
        context.resources.openRawResource(resourceId).bufferedReader().use {
            it.forEachLine { line ->
                body.append(line)
                body.append('\n')
            }
        }

        return body.toString()
    }
}