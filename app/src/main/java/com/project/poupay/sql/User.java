package com.project.poupay.sql;

import android.content.Context;

import androidx.annotation.Nullable;

import com.project.poupay.view.ContentItem;

import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class User {
    public static final int FILTER_DAY = 0;
    public static final int FILTER_MONTH = 1;
    public static final int FILTER_YEAR = 2;

    public static String username = "";

    public static void addEntry(double value, String description, int category, @Nullable Integer portion, Context context, OnSqlAddOperationSucessListener sucessListener, OnSqlOperationFailedListener failListener) {
        String pacelas = portion == null ? "NULL" : portion.toString();
        String query = "INSERT INTO transacoes(usuario_nome, valor, data, descricao, categoria_id, parcelas) " +
                "VALUES('" + username + "'," + value + ",now(), '" + description + "', " + category + ", " + portion + ");";
        new Sql(context, query).setOnQueryCompleteListener((result, queryException) -> {
            if (queryException == null) sucessListener.onSucess();
            else failListener.onFailed(queryException);
        }).start();
    }

    public static void getEntry(Context context, int filter, OnSqlGetOperationSucessListener sucessListener, OnSqlOperationFailedListener failedListener) {
        String query = "";
        switch (filter) {
            case FILTER_DAY:
                query = "SELECT transacoes.id,valor,data,descricao,categoria_id,nome as categoria,parcelas FROM transacoes \n" +
                        "INNER JOIN categorias c ON categoria_id = c.id\n" +
                        "WHERE data >= current_date - 7\n" +
                        "AND data < now()\n" +
                        "AND usuario_nome = '" + username + "'\n" +
                        "ORDER BY data DESC";
                break;

            case FILTER_MONTH:
                query = "SELECT sum(valor) as valor,to_char(date_trunc('month', data), 'YYYY-MM-01') as month,categoria_id,nome as categoria FROM transacoes \n" +
                        "INNER JOIN categorias c ON categoria_id = c.id\n" +
                        "WHERE data >= current_date - interval '1 year'\n" +
                        "AND data < now()\n" +
                        "AND usuario_nome = '" + username + "'\n" +
                        "GROUP BY to_char(date_trunc('month', data), 'YYMM'), categoria_id, nome, month\n" +
                        "ORDER BY to_char(date_trunc('month', data), 'YYMM') DESC";
                break;
            case FILTER_YEAR:
                query = "SELECT sum(valor) as valor,to_char(date_trunc('year', data), 'YYYY') as year,categoria_id,nome as categoria FROM transacoes \n" +
                        "INNER JOIN categorias c ON categoria_id = c.id\n" +
                        "AND usuario_nome = '" + username + "'\n" +
                        "GROUP BY to_char(date_trunc('year', data), 'YYYY'), categoria_id, nome, year\n" +
                        "ORDER BY to_char(date_trunc('year', data), 'YYYY') DESC";
                break;
        }
        new Sql(context, query).setOnQueryCompleteListener((result, queryException) -> {
            if (queryException != null) {
                failedListener.onFailed(queryException);
            } else {
                try {
                    List<ContentItem> contents = new ArrayList<>();
                    while (result.next()) {
                        double valor = result.getDouble("valor");
                        String categoria = result.getString("categoria");
                        int categoria_id = result.getInt("categoria_id");
                        String data;
                        switch (filter) {
                            case FILTER_DAY:
                                int id = result.getInt("id");
                                data = new SimpleDateFormat("dd", Locale.getDefault()).format(result.getDate("data")) + " de " + new SimpleDateFormat("MMMM", Locale.getDefault()).format(result.getDate("data"));
                                String descricao = result.getString("descricao");
                                int parcelas = result.getInt("parcelas");
                                contents.add(new ContentItem(context, id, data, descricao, categoria, categoria_id, valor));
                                break;

                            case FILTER_MONTH:
                                data = new SimpleDateFormat("MMMM", Locale.getDefault()).format(Date.valueOf(result.getString("month"))) + " de " + new SimpleDateFormat("yyyy", Locale.getDefault()).format(Date.valueOf(result.getString("month")));
                                contents.add(new ContentItem(context, -1, data, categoria, "", categoria_id, valor));
                                break;
                            case FILTER_YEAR:
                                data = result.getString("year");
                                contents.add(new ContentItem(context, -1, data, categoria, "", categoria_id, valor));
                                break;
                        }
                    }
                    sucessListener.onSucess(contents);
                } catch (SQLException ex) {
                    failedListener.onFailed(ex);
                }
            }
        }).start();
    }

    public static void getEntry(Context context, long dateInit, long dateEnd, OnSqlGetOperationSucessListener sucessListener, OnSqlOperationFailedListener failedListener) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        calendar.setTimeInMillis(dateInit);
        String dateInitFormated = format.format(calendar.getTime());

        calendar.setTimeInMillis(dateEnd);
        String dateEndFormated = format.format(calendar.getTime());


        String query = "SELECT transacoes.id,valor,data,descricao,categoria_id,nome as categoria,parcelas FROM transacoes \n" +
                "INNER JOIN categorias c ON categoria_id = c.id\n" +
                "WHERE data >= '" + dateInitFormated + " 00:00:00'::timestamp \n" +
                "AND data < '" + dateEndFormated + " 23:59:59'::timestamp\n" +
                "AND usuario_nome = '" + username + "'\n" +
                "ORDER BY data DESC";

        new Sql(context, query).setOnQueryCompleteListener((result, queryException) -> {
            if (queryException != null) {
                failedListener.onFailed(queryException);
            } else {
                try {
                    List<ContentItem> contents = new ArrayList<>();
                    while (result.next()) {
                        double valor = result.getDouble("valor");
                        String categoria = result.getString("categoria");
                        int categoria_id = result.getInt("categoria_id");
                        int id = result.getInt("id");
                        String data = new SimpleDateFormat("dd", Locale.getDefault()).format(result.getDate("data"))
                                + " de " + new SimpleDateFormat("MMMM", Locale.getDefault()).format(result.getDate("data"))
                                + " de " + new SimpleDateFormat("yyyy", Locale.getDefault()).format(result.getDate("data"));
                        String descricao = result.getString("descricao");
                        int parcelas = result.getInt("parcelas");
                        contents.add(new ContentItem(context, id, data, descricao, categoria, categoria_id, valor));
                    }
                    sucessListener.onSucess(contents);
                } catch (SQLException ex) {
                    failedListener.onFailed(ex);
                }
            }
        }).start();
    }

    public static void getHeader(Context context, OnSqlGetHeaderOperationSucessListener sucessListener, OnSqlOperationFailedListener failedListener) {
        String query = "SELECT sum(valor) as total, sum(valor) filter (where valor >= 0) as receita, sum(valor) filter (where valor < 0) as despesa FROM transacoes WHERE usuario_nome = '" + username + "';";
        new Sql(context, query).setOnQueryCompleteListener((result, queryException) -> {
            if (queryException != null) {
                failedListener.onFailed(queryException);
            } else {
                try {
                    double total = 0;
                    double despesa = 0;
                    double receita = 0;
                    while (result.next()) {
                        total = result.getDouble("total");
                        despesa = result.getDouble("despesa");
                        receita = result.getDouble("receita");
                    }
                    sucessListener.onSucess(total, Math.abs(despesa), receita);
                } catch (SQLException ex) {
                    failedListener.onFailed(ex);
                }
            }
        }).start();
    }


    public interface OnSqlAddOperationSucessListener {
        void onSucess();
    }

    public interface OnSqlGetOperationSucessListener {
        void onSucess(List<ContentItem> contents);
    }

    public interface OnSqlGetHeaderOperationSucessListener {
        void onSucess(double total, double despesa, double receita);
    }

    public interface OnSqlOperationFailedListener {
        void onFailed(SQLException exception);
    }
}
