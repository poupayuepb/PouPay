package com.project.poupay.sql;

import android.app.Activity;
import android.content.Context;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Sql extends Thread {
    private final String mQuery;
    private final Activity mContext;
    private OnQueryCompleteListener onQueryCompleteListener = (result, queryException) -> {};
    private static Connection mConnection;

    public Sql(Context context, String query) {
        this.mQuery = query;
        this.mContext = (Activity) context;
    }

    @Override
    public void run() {
        super.run();
        try {
            Class.forName("org.postgresql.Driver");
        }
        catch (java.lang.ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        ResultSet result = null;
        SQLException queryException = null;
        try {
            if (mConnection != null) while (true) if (mConnection.isClosed()) break;
            mConnection = DriverManager.getConnection("jdbc:postgresql://silly.db.elephantsql.com:5432/tbvikyyo", "tbvikyyo", "HNe_vfCMwGEjTSczo-5xH-cmMoOaUJJN");
            if(mQuery.startsWith("SELECT")){
                result = mConnection.createStatement().executeQuery(mQuery);
            } else{
                mConnection.createStatement().executeUpdate(mQuery);
            }

        } catch (SQLException e) {
            queryException = e;
        } finally {
            try {
                if (mConnection != null) mConnection.close();
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
        }
        ResultSet finalResult = result;
        SQLException finalQueryException = queryException;
        mContext.runOnUiThread(() -> onQueryCompleteListener.onQueryComplete(finalResult, finalQueryException));
    }

    public Sql setOnQueryCompleteListener(OnQueryCompleteListener newListener){
        onQueryCompleteListener = newListener;
        return this;
    }

    public interface OnQueryCompleteListener {
        void onQueryComplete(ResultSet result, SQLException queryException);
    }
}
