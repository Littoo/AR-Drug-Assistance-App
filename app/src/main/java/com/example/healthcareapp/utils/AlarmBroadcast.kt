package com.example.healthcareapp.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.healthcareapp.Constants
import com.example.healthcareapp.R
import com.example.healthcareapp.activities.ARActivity
import com.example.healthcareapp.activities.NotificationMessageActivity
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class AlarmBroadcast : BroadcastReceiver() {



    override fun onReceive(context: Context, intent: Intent) {

        val bundle: Bundle? = intent.extras
        var player = MediaPlayer.create(context,R.raw.child_voice_take_your_medicine)

        val res: Resources = Resources.getSystem()
//        val label = String.format(res.getString( R.string.notificaiton_label),bundle?.getString("label"))
        val label = "Goal : ${bundle?.getString("label")}"

        val medicine = bundle?.getString("medicine")
//        val date  = String.format(res.getString( R.string.notificaiton_time),
//            bundle?.getString("date") + " " + bundle?.getString("time"))
        val date = "Time : ${bundle?.getString("time")}"
        val id = Integer.parseInt(intent.getData()?.getSchemeSpecificPart() ?: "timer: 1")

        Constants.user_name = "Earl Tagalog"
//        TODO("Create a NotificationMessage Activity and layout")
        //Clicking on Notification
        val newintent: Intent = Intent(context,ARActivity::class.java )
        newintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        newintent.putExtra("message", label)
        newintent.putExtra("predictedDrug", medicine?.lowercase() )
        newintent.putExtra("notes_enabled", true)
        newintent.putExtra("presc_label", bundle?.getString("label"))
        newintent.putExtra("presc_time", bundle?.getString("time"))
        newintent.putExtra("presc_notes_instructions",
            bundle?.getString("instructions_notes"))
        newintent.putExtra("presc_photo", bundle?.getString("photo"))

        //Notification Builder
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, id, newintent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notificationManager: NotificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE) as NotificationManager

        var mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            context, "notify_$id")

        var contentView = RemoteViews(context.packageName, R.layout.notification_layout)
        contentView.setImageViewResource(R.id.pillicon, R.drawable.drug_pill)


//        val pendingswitchIntent: PendingIntent = PendingIntent.getBroadcast(context, 0, newintent, 0 )
        val pendingswitchIntent: PendingIntent = PendingIntent.getActivity(context, id, newintent, PendingIntent.FLAG_UPDATE_CURRENT )
        contentView.setOnClickPendingIntent(R.id.drugInfoButton, pendingswitchIntent).apply {
            Constants.startTime = SystemClock.elapsedRealtime()
        }

        contentView.setTextViewText(R.id.notifLabel, label)
        contentView.setTextViewText(R.id.notifTime, date)
        contentView.setTextViewText(R.id.notifDrugName, medicine)
        mBuilder.setSmallIcon(R.drawable.ic_alarm_white_24dp)
        mBuilder.setAutoCancel(true)
        mBuilder.setOngoing(true)
        mBuilder.setPriority(Notification.PRIORITY_HIGH)
        mBuilder.setOnlyAlertOnce(true)
        mBuilder.build().flags = Notification.FLAG_NO_CLEAR or Notification.PRIORITY_HIGH
        mBuilder.setContent(contentView)
//        mBuilder.setSound( Settings.System.DEFAULT_RINGTONE_URI, )
        mBuilder.setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel_id = "channel_id"
            val defaultSoundUri: Uri =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            var channel: NotificationChannel = NotificationChannel(
                channel_id, "channel name", NotificationManager.IMPORTANCE_HIGH)
            channel.enableVibration(true);
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            channel.setSound(defaultSoundUri, audioAttributes)
            notificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channel_id);
        }

        val notification = mBuilder.build()
        notificationManager.notify(id, notification)
        Constants.startTime = SystemClock.elapsedRealtime()


        //Alarm Sound
        player.isLooping = true
        player.start()

        Executors.newSingleThreadScheduledExecutor().schedule({
//            TODO("Do something")
            player.stop()
            player.release()
            player = null
        }, 10, TimeUnit.SECONDS)

    }

}