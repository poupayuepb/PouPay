package com.projeto.poupay.sql

import android.content.Context
import android.content.Context.MODE_PRIVATE

class Preferences {
    enum class Entry {
        REMIND_LOGIN_ENABLED,
        REMIND_LOGIN_USERNAME,
        REMIND_LOGIN_PASSWORD
    }

    companion object {
        private var prefsId = "com.project.poupay.${SqlQueries.username}."

        fun set(entry: Entry, value: Boolean, context: Context) {
            context.getSharedPreferences(prefsId, MODE_PRIVATE)
                .edit()
                .putBoolean(entry.toString(), value)
                .apply()
        }

        fun set(entry: Entry, value: String, context: Context) {
            context.getSharedPreferences(prefsId, MODE_PRIVATE)
                .edit()
                .putString(entry.toString(), value)
                .apply()
        }

        fun set(entry: Entry, value: Int, context: Context) {
            context.getSharedPreferences(prefsId, MODE_PRIVATE)
                .edit()
                .putInt(entry.toString(), value)
                .apply()
        }

        fun getBoolean(entry: Entry, defValue: Boolean, context: Context) = context.getSharedPreferences(prefsId, MODE_PRIVATE).getBoolean(entry.toString(), defValue)

        fun getString(entry: Entry, defValue: String, context: Context) = context.getSharedPreferences(prefsId, MODE_PRIVATE).getString(entry.toString(), defValue)

    }


}