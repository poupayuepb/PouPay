package com.projeto.poupay.sql

import android.app.Activity
import android.content.Context
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

class Sql(
    private val query: String,
    private val context: Context,
    private var onQueryCompleteListener: (ResultSet?, SQLException?) -> Unit = { _: ResultSet?, _: SQLException? -> }
) : Thread() {

    private var mConnection: Connection? = null

    override fun run() {
        super.run()
        try {
            Class.forName("org.postgresql.Driver")
        } catch (exception: ClassNotFoundException) {
            exception.printStackTrace()
            println(exception.message)
        }
        var result: ResultSet? = null
        var queryException: SQLException? = null
        try {
            if (mConnection != null) {
                while (true) {
                    if (mConnection!!.isClosed) break
                }
            }
            mConnection = DriverManager.getConnection("jdbc:postgresql://silly.db.elephantsql.com:5432/tbvikyyo", "tbvikyyo", "HNe_vfCMwGEjTSczo-5xH-cmMoOaUJJN")
            if (query.startsWith("SELECT")) {
                result = mConnection?.createStatement()?.executeQuery(query)
            } else {
                mConnection?.createStatement()?.executeUpdate(query)
            }
        } catch (exception: SQLException) {
            queryException = exception
            println(exception.message)
        } finally {
            mConnection?.close()
        }
        val activityContext = context as Activity
        activityContext.runOnUiThread {
            onQueryCompleteListener.invoke(result, queryException)
        }

    }
}