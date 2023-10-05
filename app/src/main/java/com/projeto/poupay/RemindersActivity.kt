package com.projeto.poupay

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.projeto.poupay.alerts.MessageAlert
import com.projeto.poupay.databinding.ActivityRemindersBinding
import com.projeto.poupay.reminders.RemindersAdapter
import com.projeto.poupay.sql.SqlQueries
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class RemindersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemindersBinding
    private lateinit var adapter: RemindersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = RemindersAdapter()
        binding.RemindersList.adapter = adapter
        binding.RemindersList.layoutManager = LinearLayoutManager(this)

        binding.RemindersFilterSelector.onSelectChangeListener = { id, _ ->
            if (id == 3) {

                val materialDatePicker = MaterialDatePicker.Builder
                    .dateRangePicker()
                    .setTitleText("SELECIONE UM INTERVALO")
                    .setPositiveButtonText("Filtrar")
                    .build()

                materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
                materialDatePicker.addOnCancelListener { binding.RemindersFilterSelector.setSelectedButton(0) }
                materialDatePicker.addOnNegativeButtonClickListener { binding.RemindersFilterSelector.setSelectedButton(0) }
                materialDatePicker.addOnPositiveButtonClickListener { updateAll(Pair(it.first, it.second)) }
            } else updateAll()
        }
        binding.RemindersFilterSelector.setSelectedButton(0)

        binding.RemindersClose.setOnClickListener { onBackPressed() }

        adapter.onItemCheckListener = { contentItem, checkbox ->
            setLoadingMode(true)
            MessageAlert.create(
                this,
                "Tem certeza que deseja marcar a transação \"${contentItem.description}\" " +
                        "como concluída? Isso fará com que esta seja inserida no painel de tr" +
                        "asações com a data atual (${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())})",
                "Concluir Transação",
                {
                    SqlQueries.finishReminder(this, contentItem.id, {
                        checkbox.isEnabled = false
                        setLoadingMode(false)
                    }, {
                        showErroMessage(R.string.sqlerror)
                        checkbox.isChecked = false
                    })
                }, {
                    checkbox.isChecked = false
                    setLoadingMode(false)
                }
            )
        }

        adapter.onItemNotificationChangeListener = { contentItem, isChecked ->
            // TODO: Implementar quando o botao de notificação é clicado
        }
    }

    private fun updateAll(filteredDate: Pair<Long, Long> = Pair(0L, 0L)) {
        setLoadingMode(true)
        binding.RemindersEmpty.isVisible = false
        adapter.clear()
        SqlQueries.getHeader(this, { _, outcome, income ->
            binding.RemindersHeaderOut.text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(outcome)
            binding.RemindersHeaderIn.text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(income)

            val date = Calendar.getInstance().time
            val dateString = SimpleDateFormat("dd", Locale.getDefault()).format(date) + " de " + SimpleDateFormat("MMMM", Locale.getDefault()).format(date)
            binding.RemindersDate.text = dateString

            SqlQueries.getFutureEntry(this, binding.RemindersFilterSelector.selectedId, {
                for (item in it) adapter.add(item)
                setLoadingMode(false)
            }, {
                showErroMessage(R.string.sqlerror)
            }, filteredDate)

        }, { showErroMessage(R.string.sqlerror) })
    }

    private fun showErroMessage(stringId: Int) {
        MessageAlert.create(this, MessageAlert.Type.ERROR, getString(stringId))
        setLoadingMode(false)
    }

    private fun setLoadingMode(active: Boolean) {
        binding.RemindersLoadingProgress.isVisible = active
        binding.RemindersClose.isEnabled = !active
        binding.RemindersFilterSelector.isEnabled = !active

    }
}