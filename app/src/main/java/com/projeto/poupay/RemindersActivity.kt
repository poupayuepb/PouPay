package com.projeto.poupay

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.projeto.poupay.alerts.MessageAlert
import com.projeto.poupay.alerts.NotificationReminderWorker
import com.projeto.poupay.databinding.ActivityRemindersBinding
import com.projeto.poupay.reminders.RemindersAdapter
import com.projeto.poupay.sql.SqlQueries
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RemindersActivity : AppCompatActivity() {
    private var notificationEnabled: Boolean = false
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
                        NotificationReminderWorker.cancel(this, contentItem)
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
            if (notificationEnabled) {
                if (isChecked) {
                    NotificationReminderWorker.start(this, contentItem)
                } else {
                    NotificationReminderWorker.cancel(this, contentItem)
                }
            }
        }

        binding.RemindersAddButton.setOnClickListener {
            setResult(RESULT_CLICKED)
            finish()
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestNotificationPermission()
        else notificationEnabled = true
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                this.notificationEnabled = isGranted
                if (isGranted) MessageAlert.create(this, MessageAlert.Type.SUCCESS, "Notificações habilitadas.")
                else MessageAlert.create(this, MessageAlert.Type.ERROR, "Notificações não serão exibidas.")
            }.launch(Manifest.permission.POST_NOTIFICATIONS)
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

    companion object {
        const val RESULT_CLICKED = 1
    }
}