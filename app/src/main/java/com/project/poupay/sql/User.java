package com.project.poupay.sql;

import android.content.Context;

import androidx.annotation.Nullable;

import com.project.poupay.plans.Plan;
import com.project.poupay.view.ContentItem;
import com.project.poupay.view.ContentReportItem;

import org.apache.commons.lang3.StringUtils;

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

    public static void addPlan(String goalName, double value, String description, String date, Context context, OnSqlAddOperationSucessListener sucessListener, OnSqlOperationFailedListener failListener) {
        String query = "INSERT INTO planejamentos(objetivos,usuario_id,descricao,valor, data)\n" +
                "VALUES('" + goalName + "', '" + username + "','" + description + "', " + value + ", '" + date + "');";
        new Sql(context, query).setOnQueryCompleteListener((result, queryException) -> {
            if (queryException == null) sucessListener.onSucess();
            else failListener.onFailed(queryException);
        }).start();
    }

    public static void addPlanValue(int planId, double value, String description, Context context, OnSqlAddOperationSucessListener sucessListener, OnSqlOperationFailedListener failListener) {
        String query = "INSERT INTO historico_planejamentos(planejamento_id, data, instituicao_financeira, valor) \n" +
                "VALUES (" + planId + ", now(), '" + description + "', " + value + ");";
        new Sql(context, query).setOnQueryCompleteListener((result, queryException) -> {
            if (queryException == null) sucessListener.onSucess();
            else failListener.onFailed(queryException);
        }).start();
    }

    public static void getPlans(Context context, OnSqlGetPlanOperationSucessListener sucessListener, OnSqlOperationFailedListener failedListener) {
        String query = "SELECT planejamentos.id as id, planejamentos.data as date, objetivos, descricao, planejamentos.valor as total, historico_planejamentos.id as h_id, historico_planejamentos.data as h_date, historico_planejamentos.instituicao_financeira as h_description, historico_planejamentos.valor as h_value\n" +
                "FROM historico_planejamentos\n" +
                "RIGHT JOIN planejamentos ON historico_planejamentos.planejamento_id = planejamentos.id\n" +
                "WHERE usuario_id = '" + username + "'";
        new Sql(context, query).setOnQueryCompleteListener((result, queryException) -> {
            if (queryException != null) failedListener.onFailed(queryException);
            else {
                try {
                    List<Plan> plans = new ArrayList<>();
                    while (result.next()) {
                        int id = result.getInt("id");
                        String data = new SimpleDateFormat("dd", Locale.getDefault()).format(result.getDate("date")) +
                                " de " + new SimpleDateFormat("MMMM", Locale.getDefault()).format(result.getDate("date")) +
                                " de " + new SimpleDateFormat("yyyy", Locale.getDefault()).format(result.getDate("date"));
                        String descricao = result.getString("descricao");
                        String objetivos = result.getString("objetivos");
                        double total = result.getDouble("total");
                        int h_id = result.getInt("h_id");
                        String h_date = result.getString("h_date");
                        String h_description = result.getString("h_description");
                        double h_value = result.getDouble("h_value");

                        ContentItem contentItem = null;
                        if (h_id != 0) {
                            contentItem = new ContentItem(context, h_id, String.valueOf(h_id), h_description, h_date, -1, h_value, 0);
                        }
                        boolean planAlreadyExist = false;
                        for (Plan innerPlan : plans) {
                            if (innerPlan.getId() == id) {
                                planAlreadyExist = true;
                                if (contentItem != null) innerPlan.add(contentItem);
                            }
                        }
                        if (!planAlreadyExist) {
                            Plan newPlan = new Plan(id, objetivos, descricao, data, total);
                            if (contentItem != null) newPlan.add(contentItem);
                            plans.add(newPlan);
                        }
                    }
                    sucessListener.onSucess(plans);
                } catch (SQLException ex) {
                    failedListener.onFailed(ex);
                }
            }
        }).start();
    }

    public static void getEntry(Context context, int filter, OnSqlGetOperationSucessListener
            sucessListener, OnSqlOperationFailedListener failedListener) {
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
            if (queryException != null) failedListener.onFailed(queryException);
            else {
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
                                contents.add(new ContentItem(context, id, data, descricao, categoria, categoria_id, valor, parcelas));
                                break;
                            case FILTER_MONTH:
                                data = new SimpleDateFormat("MMMM", Locale.getDefault()).format(Date.valueOf(result.getString("month"))) + " de " + new SimpleDateFormat("yyyy", Locale.getDefault()).format(Date.valueOf(result.getString("month")));
                                contents.add(new ContentItem(context, -1, data, categoria, "", categoria_id, valor, 0));
                                break;
                            case FILTER_YEAR:
                                data = result.getString("year");
                                contents.add(new ContentItem(context, -1, data, categoria, "", categoria_id, valor, 0));
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

    public static void getEntry(Context context, long dateInit,
                                long dateEnd, OnSqlGetOperationSucessListener sucessListener, OnSqlOperationFailedListener
                                        failedListener) {
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
            if (queryException != null) failedListener.onFailed(queryException);
            else {
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
                        contents.add(new ContentItem(context, id, data, descricao, categoria, categoria_id, valor, parcelas));
                    }
                    sucessListener.onSucess(contents);
                } catch (SQLException ex) {
                    failedListener.onFailed(ex);
                }
            }
        }).start();
    }

    public static void getHeader(Context context, OnSqlGetHeaderOperationSucessListener
            sucessListener, OnSqlOperationFailedListener failedListener) {
        String query = "SELECT sum(valor) as total, sum(valor) filter (where valor >= 0) as receita, sum(valor) filter (where valor < 0) as despesa FROM transacoes WHERE usuario_nome = '" + username + "';";
        new Sql(context, query).setOnQueryCompleteListener((result, queryException) -> {
            if (queryException != null) failedListener.onFailed(queryException);
            else {
                try {
                    double total = 0, despesa = 0, receita = 0;
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

    public static void getReportItems(Context context, int type, int category, int year,
                                      int month, OnSqlGetReportOperationSucessListener
                                              sucessListener, OnSqlOperationFailedListener failedListener) {
        String query = category == 0 ? "SELECT sum(valor) as price, to_char(date_trunc('month', data), 'YYYY-MM-01') as month, LOWER(descricao) as desc\n" +
                "FROM (SELECT valor, data, descricao FROM transacoes WHERE valor " + (type == 1 ? ">=" : "<") + "0 AND usuario_nome = '" + username + "') as t\n" +
                "WHERE to_char(date_trunc('month', data), 'YYYY-MM-01') LIKE '" + year + "-" + (month < 10 ? "0" : "") + month + "%'\n" +
                "GROUP BY month, descricao;\n" : "SELECT sum(valor) as price, to_char(date_trunc('month', data), 'YYYY-MM-01') as month, nome as desc\n" +
                "FROM (SELECT valor, data, nome FROM transacoes INNER JOIN categorias ON categoria_id = categorias.id WHERE valor" + (type == 1 ? ">=" : "<") + "0 AND usuario_nome = '" + username + "') as t\n" +
                "WHERE to_char(date_trunc('month', data), 'YYYY-MM-01') LIKE '" + year + "-" + (month < 10 ? "0" : "") + month + "%'\n" +
                "GROUP BY month, nome;";

        new Sql(context, query).setOnQueryCompleteListener((result, queryException) -> {
            if (queryException != null) failedListener.onFailed(queryException);
            else {
                try {
                    List<ContentReportItem> contents = new ArrayList<>();
                    while (result.next()) {
                        double valor = result.getDouble("price");
                        String descricao = StringUtils.capitalize(result.getString("desc"));
                        contents.add(new ContentReportItem(descricao, valor));
                    }
                    sucessListener.onSucess(contents);
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

    public interface OnSqlGetPlanOperationSucessListener {
        void onSucess(List<Plan> contents);
    }

    public interface OnSqlGetReportOperationSucessListener {
        void onSucess(List<ContentReportItem> contents);
    }

    public interface OnSqlGetHeaderOperationSucessListener {
        void onSucess(double total, double despesa, double receita);
    }

    public interface OnSqlOperationFailedListener {
        void onFailed(SQLException exception);
    }
}
