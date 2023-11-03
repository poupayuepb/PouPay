package com.projeto.poupay.sql

import android.content.Context
import com.projeto.poupay.plans.views.ContentPlanItem
import com.projeto.poupay.plans.views.Plan
import com.projeto.poupay.reminders.ReminderEntry
import com.projeto.poupay.view.ContentItem
import com.projeto.poupay.view.ContentReportItem
import org.apache.commons.lang3.StringUtils
import java.sql.Date
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

class SqlQueries {

    companion object {
        var username: String = ""

        private const val FILTER_DAY = 0
        private const val FILTER_MONTH = 1
        private const val FILTER_YEAR = 2

        fun addEntry(value: Double, description: String, category: Int, portion: Int = 0, context: Context, date: String = "now()", sucessListener: () -> Unit, failListener: (exceprion: SQLException) -> Unit) {
            val condition = if (date == "now()") true else SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)?.before(java.util.Date()) ?: false
            val stringDate = if (date == "now()") date else "'$date'"
            var query = ""



            if(portion > 0) {
                for (i in 1..portion) {
                    query += "INSERT INTO transacoes(usuario_nome, valor, data, descricao, categoria_id, parcelas, condicao) " +
                            "VALUES('$username',${value / portion},$stringDate + interval '$i month', '$description', $category, $i, false);\n"
                }
            } else{
                query = "INSERT INTO transacoes(usuario_nome, valor, data, descricao, categoria_id, parcelas, condicao) " +
                        "VALUES('$username',$value,$stringDate, '$description', $category, $portion, $condition);"
            }

            println(query)

            Sql(query, context) { _, queryException ->
                if (queryException == null) sucessListener.invoke()
                else failListener.invoke(queryException)
            }.start()
        }

        fun addPlan(goalName: String, value: Double, description: String, date: String, context: Context, sucessListener: () -> Unit, failListener: (exceprion: SQLException) -> Unit) {
            val query = "INSERT INTO planejamentos(objetivos,usuario_id,descricao,valor, data) " +
                    "VALUES('$goalName', '$username','$description', $value, '$date');"
            Sql(query, context) { _, queryException ->
                if (queryException == null) sucessListener.invoke()
                else failListener.invoke(queryException)
            }.start()
        }

        fun addPlanValue(planId: Int, value: Double, description: String, context: Context, sucessListener: () -> Unit, failListener: (exception: SQLException) -> Unit) {
            val query = "INSERT INTO historico_planejamentos(planejamento_id, data, instituicao_financeira, valor) \n" +
                    "VALUES ($planId, now(), '$description', $value);"
            Sql(query, context) { _, queryException ->
                if (queryException == null) sucessListener.invoke()
                else failListener.invoke(queryException)
            }.start()
        }

        fun getPlans(context: Context, sucessListener: (contents: List<Plan>) -> Unit, failListener: (exception: SQLException) -> Unit) {
            val query = "SELECT planejamentos.id as id, planejamentos.data as date, objetivos, descricao, planejamentos.valor as total, historico_planejamentos.id as h_id, historico_planejamentos.data as h_date, historico_planejamentos.instituicao_financeira as h_description, historico_planejamentos.valor as h_value\n" +
                    "FROM historico_planejamentos RIGHT JOIN planejamentos ON historico_planejamentos.planejamento_id = planejamentos.id\n" +
                    "WHERE usuario_id = '$username';"

            Sql(query, context) { result, queryException ->
                if (queryException != null) failListener.invoke(queryException)
                else if (result == null) failListener.invoke(SQLException())
                else {
                    try {
                        val plans = mutableListOf<Plan>()
                        while (result.next()) {
                            val id = result.getInt("id")
                            val data = SimpleDateFormat("dd", Locale.getDefault()).format(result.getDate("date")) +
                                    " de " + SimpleDateFormat("MMMM", Locale.getDefault()).format(result.getDate("date")) +
                                    " de " + SimpleDateFormat("yyyy", Locale.getDefault()).format(result.getDate("date"))
                            val description = result.getString("descricao")
                            val objetives = result.getString("objetivos")
                            val total = result.getDouble("total")
                            val hId = result.getInt("h_id")
                            val hDate = result.getString("h_date")
                            val hDescription = result.getString("h_description")
                            val hValue = result.getDouble("h_value")

                            var contentItem: ContentPlanItem? = null
                            if (hId != 0) contentItem = ContentPlanItem(context, hId, hDescription, hDate, hValue)
                            var planAlreadyExist = false
                            for (innerPlan: Plan in plans) {
                                if (innerPlan.id == id) {
                                    planAlreadyExist = true
                                    if (contentItem != null) innerPlan.add(contentItem)
                                }
                            }
                            if (!planAlreadyExist) {
                                val newPlan = Plan(id, objetives, description, data, total)
                                if (contentItem != null) newPlan.add(contentItem)
                                plans.add(newPlan)
                            }
                        }
                        sucessListener.invoke(plans)
                    } catch (exception: SQLException) {
                        failListener.invoke(exception)
                    }
                }
            }.start()
        }

        fun getFutureEntry(context: Context, filter: Int = 0, sucessListener: (MutableList<ReminderEntry>) -> Unit, failListener: (exception: SQLException) -> Unit, filteredDate: Pair<Long, Long> = Pair(0L, 0L)) {

            val dateFormat = when (filter) {
                1 -> SimpleDateFormat("MMMM 'de' yyyy", Locale.getDefault())
                2 -> SimpleDateFormat("yyyy", Locale.getDefault())
                else -> SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault())
            }
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")

            val initDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(filteredDate.first)
            val finalDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(filteredDate.second)

            val query = "SELECT id, valor, data, descricao, categoria_id, parcelas, condicao " +
                    "FROM transacoes " +
                    "WHERE usuario_nome = '$username' " +
                    "and condicao = false " +
                    "${if (filter == 3) "and data > '$initDate' and data < '$finalDate'" else ""} " +
                    "ORDER BY data"

            Sql(query, context) { result, queryException ->
                if (queryException != null) failListener.invoke(queryException)
                else if (result == null) failListener.invoke(SQLException())
                else {
                    try {
                        val remindersList: MutableList<ReminderEntry> = ArrayList()
                        while (result.next()) {
                            remindersList.add(
                                ReminderEntry(
                                    id = result.getInt("id"),
                                    value = result.getDouble("valor"),
                                    date = result.getDate("data"),
                                    description = result.getString("descricao"),
                                    category = result.getInt("categoria_id"),
                                    parcels = result.getInt("parcelas"),
                                    condition = result.getBoolean("condicao"),
                                    stringDate = dateFormat.format(result.getDate("data"))
                                )
                            )
                        }
                        sucessListener.invoke(remindersList)
                    } catch (exception: SQLException) {
                        failListener.invoke(exception)
                    }
                }
            }.start()
        }

        fun getEntry(context: Context, filter: Int, sucessListener: (contents: MutableList<ContentItem>) -> Unit, failListener: (SQLException) -> Unit) {
            var query = ""
            when (filter) {
                FILTER_DAY -> query =
                    "SELECT transacoes.id,valor,data,descricao,categoria_id,nome as categoria,parcelas FROM transacoes \n" +
                            "INNER JOIN categorias c ON categoria_id = c.id\n" +
                            "WHERE data >= current_date - 7\n" +
                            "AND data < now()\n" +
                            "AND condicao = true\n" +
                            "AND usuario_nome = '$username'\n" + "ORDER BY data DESC"

                FILTER_MONTH -> query =
                    "SELECT sum(valor) as valor,to_char(date_trunc('month', data), 'YYYY-MM-01') as month,categoria_id,nome as categoria FROM transacoes \n" +
                            "INNER JOIN categorias c ON categoria_id = c.id\n" +
                            "WHERE data >= current_date - interval '1 year'\n" +
                            "AND data < now()\n" +
                            "AND condicao = true\n" +
                            "AND usuario_nome = '$username'\n" +
                            "GROUP BY to_char(date_trunc('month', data), 'YYMM'), categoria_id, nome, month\n" +
                            "ORDER BY to_char(date_trunc('month', data), 'YYMM') DESC"

                FILTER_YEAR -> query =
                    "SELECT sum(valor) as valor,to_char(date_trunc('year', data), 'YYYY') as year,categoria_id,nome as categoria FROM transacoes \n" +
                            "INNER JOIN categorias c ON categoria_id = c.id\n" +
                            "AND usuario_nome = '$username'\n" +
                            "AND data < now()\n" +
                            "AND condicao = true\n" +
                            "GROUP BY to_char(date_trunc('year', data), 'YYYY'), categoria_id, nome, year\n" +
                            "ORDER BY to_char(date_trunc('year', data), 'YYYY') DESC"
            }

            Sql(query, context) { result, queryException ->
                if (queryException != null) failListener.invoke(queryException)
                else if (result == null) failListener.invoke(SQLException())
                else {
                    try {
                        val contents: MutableList<ContentItem> = ArrayList()
                        while (result.next()) {
                            val value = result.getDouble("valor")
                            val category = result.getString("categoria")
                            var date: String
                            val categoryId = result.getInt("categoria_id")
                            when (filter) {
                                FILTER_DAY -> {
                                    val id = result.getInt("id")
                                    date = SimpleDateFormat("dd", Locale.getDefault()).format(result.getDate("data")) + " de " + SimpleDateFormat("MMMM", Locale.getDefault()).format(result.getDate("data"))
                                    val description = result.getString("descricao")
                                    val parcels = result.getInt("parcelas")
                                    contents.add(ContentItem(context, id, date, description, category, categoryId, value, parcels))
                                }

                                FILTER_MONTH -> {
                                    date = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date.valueOf(result.getString("month"))) + " de " + SimpleDateFormat("yyyy", Locale.getDefault()).format(Date.valueOf(result.getString("month")))
                                    contents.add(ContentItem(context, -1, date, category, "", categoryId, value, 0))
                                }

                                FILTER_YEAR -> {
                                    date = result.getString("year")
                                    contents.add(ContentItem(context, -1, date, category, "", categoryId, value, 0))
                                }
                            }
                        }
                        sucessListener.invoke(contents)
                    } catch (exception: SQLException) {
                        failListener.invoke(exception)
                    }
                }
            }.start()
        }

        fun getEntry(context: Context, dateInit: Long, dateEnd: Long, sucessListener: (contents: MutableList<ContentItem>) -> Unit, failListener: (SQLException) -> Unit) {
            val calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("UTC")
            calendar.timeInMillis = dateInit
            val dateInitFormated = format.format(calendar.time)
            calendar.timeInMillis = dateEnd
            val dateEndFormated = format.format(calendar.time)
            val query = "SELECT transacoes.id,valor,data,descricao,categoria_id,nome as categoria,parcelas FROM transacoes \n" +
                    "INNER JOIN categorias c ON categoria_id = c.id\n" +
                    "WHERE data >= '" + dateInitFormated + " 00:00:00'::timestamp \n" +
                    "AND data < '" + dateEndFormated + " 23:59:59'::timestamp\n" +
                    "AND condicao = true\n" +
                    "AND usuario_nome = '" + username + "'\n" +
                    "ORDER BY data DESC"
            Sql(query, context) { result, queryException ->
                if (queryException != null) failListener.invoke(queryException)
                else if (result == null) failListener.invoke(SQLException())
                else {
                    try {
                        val contents: MutableList<ContentItem> = ArrayList()
                        while (result.next()) {
                            val valor = result.getDouble("valor")
                            val categoria = result.getString("categoria")
                            val categoriaId = result.getInt("categoria_id")
                            val id = result.getInt("id")
                            val data = (SimpleDateFormat("dd", Locale.getDefault()).format(result.getDate("data"))
                                    + " de " + SimpleDateFormat("MMMM", Locale.getDefault()).format(result.getDate("data"))
                                    + " de " + SimpleDateFormat("yyyy", Locale.getDefault()).format(result.getDate("data")))
                            val descricao = result.getString("descricao")
                            val parcelas = result.getInt("parcelas")
                            contents.add(ContentItem(context, id, data, descricao, categoria, categoriaId, valor, parcelas))
                        }
                        sucessListener.invoke(contents)
                    } catch (ex: SQLException) {
                        failListener.invoke(ex)
                    }
                }
            }.start()
        }

        fun getHeader(context: Context, sucessListener: (total: Double, outcome: Double, income: Double) -> Unit, failListener: (exception: SQLException) -> Unit) {
            val query = "SELECT sum(valor) as total, sum(valor) filter (where valor >= 0) as receita, sum(valor) filter (where valor < 0) as despesa " +
                    "FROM transacoes " +
                    "WHERE usuario_nome = '$username' " +
                    "AND condicao = true\n" +
                    "and data < now();"
            Sql(query, context) { result, queryException ->
                if (queryException != null) failListener.invoke(queryException)
                else if (result == null) failListener.invoke(SQLException())
                else {
                    try {
                        var total = 0.0
                        var outcome = 0.0
                        var income = 0.0
                        while (result.next()) {
                            total = result.getDouble("total")
                            outcome = result.getDouble("despesa")
                            income = result.getDouble("receita")
                        }
                        sucessListener.invoke(total, abs(outcome), income)
                    } catch (exception: SQLException) {
                        failListener.invoke(exception)
                    }
                }
            }.start()

        }

        fun getReportItems(context: Context, type: Int, category: Int, year: Int, month: Int, sucessListener: (items: MutableList<ContentReportItem>) -> Unit, failListener: (exception: SQLException) -> Unit) {

            val dateLike = if (month >= 0)
                "$year-${if (month < 10) "0" else ""}$month%"
            else
                "$year-%"

            val query = if (category == 0) {
                "SELECT sum(valor) as price, to_char(date_trunc('month', data), 'YYYY-MM-01') as month, LOWER(descricao) as desc, descricao\n" +
                        "FROM (SELECT valor, data, descricao FROM transacoes WHERE valor ${if (type == 1) ">=" else "<"} 0 AND usuario_nome = '$username') as t\n" +
                        "WHERE to_char(date_trunc('month', data), 'YYYY-MM-01') LIKE '$dateLike' GROUP BY month, descricao\n" +
                        "ORDER BY descricao ASC;"
            } else {
                "SELECT valor as price, to_char(date_trunc('month', data), 'YYYY-MM-01') as month, nome as desc, descricao, categoria_id\n" +
                        "FROM (SELECT descricao, valor, data, nome, categoria_id FROM transacoes INNER JOIN categorias ON categoria_id = categorias.id WHERE valor ${if (type == 1) ">=" else "<"} 0 AND usuario_nome = '$username') as t\n" +
                        "WHERE to_char(date_trunc('month', data), 'YYYY-MM-01') LIKE '$dateLike'\n" +
                        "ORDER BY categoria_id ASC;\n"
            }
            Sql(query, context) { result, queryException ->
                if (queryException != null) failListener.invoke(queryException)
                else if (result == null) failListener.invoke(SQLException())
                else {
                    try {
                        val contents: MutableList<ContentReportItem> = ArrayList()
                        while (result.next()) {
                            val valor = result.getDouble("price")
                            val descricao = StringUtils.capitalize(result.getString("desc"))
                            val subtitle = result.getString("descricao")
                            contents.add(ContentReportItem(descricao, valor, subtitle ?: ""))
                        }
                        sucessListener.invoke(contents)
                    } catch (ex: SQLException) {
                        failListener.invoke(ex)
                    }
                }
            }.start()
        }

        fun deleteEntry(id: Int, context: Context, sucessListener: () -> Unit, failListener: (exception: SQLException) -> Unit) {
            val query = "DELETE FROM transacoes WHERE id = $id;"
            Sql(query, context) { _, queryException ->
                if (queryException != null) failListener.invoke(queryException)
                else sucessListener.invoke()
            }.start()
        }

        fun deletePlanContent(id: Int, context: Context, sucessListener: () -> Unit, failListener: (exception: SQLException) -> Unit) {
            val query = "DELETE FROM historico_planejamentos WHERE id = $id;"
            Sql(query, context) { _, queryException ->
                if (queryException != null) failListener.invoke(queryException)
                else sucessListener.invoke()
            }.start()
        }

        fun deletePlan(planId: Int, context: Context, sucessListener: () -> Unit, failListener: (exception: SQLException) -> Unit) {
            val query = "DELETE FROM planejamentos WHERE id = $planId;"
            Sql(query, context) { _, queryException ->
                if (queryException != null) failListener.invoke(queryException)
                else sucessListener.invoke()
            }.start()
        }

        fun finishReminder(context: Context, id: Int, sucessListener: () -> Unit, failListener: (exception: SQLException) -> Unit) {
            val query = "UPDATE transacoes SET data = now(), condicao = true WHERE ID = $id;"
            Sql(query, context) { _, queryException ->
                if (queryException != null) failListener.invoke(queryException)
                else sucessListener.invoke()
            }.start()
        }
    }
}