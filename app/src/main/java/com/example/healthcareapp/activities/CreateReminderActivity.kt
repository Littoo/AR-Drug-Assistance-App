package com.example.healthcareapp.activities

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doBeforeTextChanged
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.healthcareapp.Constants
import com.example.healthcareapp.R
import com.example.healthcareapp.databinding.CreateReminderBinding
import com.example.healthcareapp.reminder_database.DatabaseClass
import com.example.healthcareapp.reminder_database.ReminderClass
import com.example.healthcareapp.utils.AlarmBroadcast
import com.example.healthcareapp.utils.RandomUtil
import com.example.healthcareapp.utils.TimeUtil
import com.bumptech.glide.request.target.Target
import com.example.healthcareapp.utils.WindowUIController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import org.checkerframework.checker.signedness.qual.Constant
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.ParseException
import java.util.*


class CreateReminderActivity: AppCompatActivity(){

    private var mImagePath = ""

    private lateinit var binding: CreateReminderBinding

    private var reminderTime: String? = "12:00"
    private var reminderDate: String? = ""
    private var reminderDisplayDate: String? = ""
    private var label: String? = ""
    private var medicine: String? = ""
    private var instruction_notes: String? = ""
    private var imageUri: String? = null

    private lateinit var final_calendar: Calendar

    private var databaseClass: DatabaseClass? = null

    private val selectImageFromGalleryResult1=  registerForActivityResult(
        ActivityResultContracts.GetContent(),
        ActivityResultCallback {
            Constants.startTime = SystemClock.elapsedRealtime()
            Glide.with(this)
                .load(it)
                .fitCenter()
                .override(1080,1920)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("TAG", "Error loading image", e)
                        binding.insertPhoto.text = "Upload Photo"
                        return false // important to return false so the error placeholder can be placed
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        val bitmap: Bitmap = (resource?.toBitmap() ?: null) as Bitmap
//                        Constants.startTime = SystemClock.elapsedRealtime()
                        mImagePath = saveImageToInternalStorage(bitmap)
                        imageUri = mImagePath
                        Log.d("Path", "Gile path is $mImagePath")
                        binding.insertPhoto.text = "Change Photo"
                        binding.uploadedPhoto.visibility = View.VISIBLE
                        Log.d("Time elapsed: ", "Time elapsed from uploading the photo" +
                                " : ${SystemClock.elapsedRealtime() - Constants.startTime} millis")
                        return false
                    }
                })
                .into(binding.uploadedPhoto)

        }
    )

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir("HealthCareApp", Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.png")
        try{
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG,100,stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }
        return file.absolutePath
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CreateReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("Time elapsed: ", "Time elapsed in opening the set new reminder" +
                " : ${SystemClock.elapsedRealtime() - Constants.startTime} millis")

        supportActionBar?.title = "Set New Reminder"

        val adapter = ArrayAdapter(this, R.layout.prescribing_list_item, R.id.infoItem, Constants.DRUGS_LIST)
        val dropdown = binding.medicineLabel.editText as? AutoCompleteTextView
        dropdown?.setAdapter(adapter)

        //can be put to onResume
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"))
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        val month1 = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR)
        val hour1 = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        binding.timeDescription.text = TimeUtil().FormatTime(hour1, minute)
        reminderTime = TimeUtil().FormatTime(hour1, minute)
        reminderDisplayDate = "${(month)} ${day}, ${year}"
        binding.dateDescription.text =  reminderDisplayDate
        reminderDate = "" + day + "-" + (month1 + 1) + "-" + year

        databaseClass = DatabaseClass.getDatabase(applicationContext)

        Log.d("Calendar", "Calendar: ${calendar.toString()}")

        final_calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"))
        final_calendar.apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND,0)
        }

        Log.d("Calendar", "Calendar: ${final_calendar.toString()}")

        binding.setTimebutton.setOnClickListener {
            Constants.startTime = SystemClock.elapsedRealtime()
            selectTime()
        }
        binding.setDatebutton.setOnClickListener {
            Constants.startTime = SystemClock.elapsedRealtime()
            selectDate()
        }

        binding.saveReminderButton.setOnClickListener {
            Constants.startTime = SystemClock.elapsedRealtime()
            onSave()
        }

        binding.insertPhoto.setOnClickListener {
            Constants.startTime = SystemClock.elapsedRealtime()
            selectImageFromGalleryResult1.launch("image/*")
        }

        dropdown?.doBeforeTextChanged { text, start, count, after ->
            Constants.startTime = SystemClock.elapsedRealtime()
        }
        dropdown?.doAfterTextChanged {
            Log.d("Time elapsed: ", "Time elapsed in selecting medicine" +
                    " : ${SystemClock.elapsedRealtime() - Constants.startTime} millis")
        }
        reportFullyDrawn()

    }

    private fun selectTime () {
        val calendar = Calendar.getInstance()
        val hour1 = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog: TimePickerDialog =
            TimePickerDialog(this, object : TimePickerDialog.OnTimeSetListener {
                override fun onTimeSet(tpicker: TimePicker?, hour: Int, min: Int) {
//                reminderTime = "${hour}:${min}"

                    final_calendar.set(Calendar.HOUR_OF_DAY, hour)
                    final_calendar.set(Calendar.MINUTE, min)
                    reminderTime = TimeUtil().FormatTime(hour, min)
                    binding.timeDescription.text = reminderTime

                }
            }, hour1, minute, false)
        timePickerDialog.show()
        Log.d("Time elapsed: ", "Time elapsed in opening the set  time" +
                " : ${SystemClock.elapsedRealtime() - Constants.startTime} millis")
    }

    private fun selectDate () {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog: DatePickerDialog = DatePickerDialog(this, object: DatePickerDialog.OnDateSetListener {

            override fun onDateSet(dPicker: DatePicker?, year1: Int, month1: Int, day1: Int) {
                Constants.startTime = SystemClock.elapsedRealtime()
                reminderDate = "" + day + "-" + (month1 + 1) + "-" + year
                final_calendar.set(Calendar.YEAR, year1)
                final_calendar.set(Calendar.MONTH, month1)
                final_calendar.set(Calendar.DAY_OF_MONTH, day1)
                val monthDisplayNames = Constants.LONG_MONTHS
                reminderDisplayDate =  "${(monthDisplayNames[month1])} ${day1}, ${year1}"
                binding.dateDescription.text =  reminderDisplayDate
            }

        }, year, month, day)
        datePickerDialog.show()

        Log.d("Time elapsed: ", "Time elapsed in opening the set new date" +
                " : ${SystemClock.elapsedRealtime() - Constants.startTime} millis")
    }

    private fun onSave () {

        label = binding.labelText.text.toString().trim()
        medicine = binding.medicineLabelText.text.toString().trim()
        instruction_notes = binding.notesText.text.toString().trim()

        if (instruction_notes == null) {
            instruction_notes = "None"
        }

        if (medicine.isNullOrEmpty()) {
            Toast.makeText(this, "Please input the medicine's brand name", Toast.LENGTH_SHORT).show()
        } else if (label.isNullOrEmpty()) {
            Toast.makeText(this, "Please input text on label", Toast.LENGTH_SHORT).show()
        } else { //more checks needed for dates and time should be greater than the current time
            if (reminderTime.isNullOrEmpty()) {
                Toast.makeText(this, "Please set a valid time", Toast.LENGTH_SHORT).show()
            } else if (reminderDate.isNullOrEmpty()) {
                Toast.makeText(this, "Please set a valid date", Toast.LENGTH_SHORT).show()
            } else {
                val reminder: ReminderClass = ReminderClass()
                reminderTime = reminderTime.toString().trim()
                reminderDate = reminderDate.toString().trim()
                reminder.reminderDate = reminderDate
                reminder.reminderDisplayDate = reminderDisplayDate
                reminder.reminderTime = reminderTime
                reminder.reminderLabel = label
                reminder.reminderMedicine = medicine
                reminder.reminderNotes = instruction_notes
                reminder.reminderPhoto = imageUri
                val successId: Long? = databaseClass?.ReminderDao()?.insertAll(reminder)
                Toast.makeText(this, " Success: $successId", Toast.LENGTH_SHORT).show()

                // just temporary, Goes back to previous page
                if (successId != null) {
                    Toast.makeText(this, " Saving success!", Toast.LENGTH_SHORT).show()
                    setAlarm(successId)
                    Log.d("Time elapsed: ", "Time elapsed in saving reminder" +
                            " : ${SystemClock.elapsedRealtime() - Constants.startTime} millis")
                    this.onBackPressed()
                } else {
                    Toast.makeText(this, " Saving failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setAlarm(
        successId: Long
    ) {
        val alarm: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent: Intent = Intent(applicationContext, AlarmBroadcast::class.java)
        val REQUEST_CODE = 2000 + successId.toInt()

        intent.setData(Uri.parse("timer:" + successId.toInt()))
        intent.putExtra("label", label)
        intent.putExtra("medicine", medicine)
        intent.putExtra("time", reminderTime)
        intent.putExtra("date", reminderDate)
        intent.putExtra("instructions_notes", instruction_notes)
        intent.putExtra("photo", imageUri)

        val pendingIntent =
            PendingIntent.getBroadcast(applicationContext,
                REQUEST_CODE, intent, PendingIntent.FLAG_ONE_SHOT)
        val dateandtime: String = "$reminderDate $reminderTime"
        val formatter: DateFormat = SimpleDateFormat("d-M-yyyy hh:mm")

        Log.d("Calendar", "On Save Calendar: ${final_calendar.toString()}")

        Log.d("DateFormat", "DateFormat: ${formatter.parse(dateandtime)};" +
                " Time: ${formatter.parse(dateandtime).time} or ${final_calendar.timeInMillis}," +
                " Request Code: ${REQUEST_CODE}")
        //More code to be added
        try {
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                /*date1.time + offset_12hr*/final_calendar.timeInMillis, pendingIntent)

            Log.d("Alarm", "Alarm Saved successfully!")
            //For repeating alarms
//            alarm.setRepeating(
//                AlarmManager.RTC_WAKEUP, date1.time,
//                AlarmManager.INTERVAL_DAY /* Try interval 1 minute for testing purposes*/,
//                pendingIntent
//            )
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        finish()
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        val  reminderActivityIntent = Intent(this, ReminderActivity::class.java)
        reminderActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(reminderActivityIntent)
        finish()
    }

    private fun getRandomNumber() = RandomUtil.getRandomInt()

}