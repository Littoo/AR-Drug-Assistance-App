package com.example.healthcareapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.activities.ReminderActivity
import com.example.healthcareapp.activities.ReminderInfoActivity
import com.example.healthcareapp.reminder_database.ReminderClass
import com.example.healthcareapp.utils.TimeUtil

class ReminderAdapter(private val reminderList: List<ReminderClass>, val context: Context) :
    RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val reminderItemView = LayoutInflater.from(parent.context).inflate(
            R.layout.reminder_item,parent,false
        )

        return ReminderViewHolder(reminderItemView)

    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {

        val currentReminder = reminderList[position]
        holder.cardReminderLabel.text = currentReminder.reminderLabel
        holder.cardReminderMedicine.text = currentReminder.reminderMedicine
        holder.cardReminderTime.text = currentReminder.reminderTime

        holder.cardReminderDate.text = currentReminder.reminderDisplayDate

        holder.deletebutton.setOnClickListener {
            val intent = Intent(it.context, ReminderActivity::class.java)
            intent.putExtra("reminderId", currentReminder.id)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        holder.card.setOnClickListener {
            val intent = Intent(it.context, ReminderInfoActivity::class.java)
            intent.putExtra("reminderId", currentReminder.id)
            intent.putExtra("medicine", currentReminder.reminderMedicine)
            intent.putExtra("label", currentReminder.reminderLabel)
            intent.putExtra("instructions_notes", currentReminder.reminderNotes)
            intent.putExtra("time", currentReminder.reminderTime)
            intent.putExtra("date", currentReminder.reminderDisplayDate)
            intent.putExtra("photo", currentReminder.reminderPhoto)
//            Toast.makeText(context, " id: ${currentReminder.id}", Toast.LENGTH_SHORT).show()
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(intent)

        }

    }

    override fun getItemCount(): Int {
        return reminderList.size
    }

    class ReminderViewHolder(reminderItemView: View):
            RecyclerView.ViewHolder(reminderItemView) {

        val deletebutton: ImageButton = reminderItemView.findViewById<ImageButton>(R.id.deleteReminderButton)

        val card: CardView = reminderItemView.findViewById(R.id.card_layout)

        val cardReminderLabel: TextView = reminderItemView.findViewById(R.id.cardReminderLabel)
        val cardReminderMedicine: TextView = reminderItemView.findViewById(R.id.cardReminderMedicine)
        val cardReminderTime: TextView = reminderItemView.findViewById(R.id.cardReminderTime)
        val cardReminderDate: TextView = reminderItemView.findViewById(R.id.cardReminderDate)

    }
}
