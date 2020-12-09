package `in`.programy.spotube.exoplayer

import `in`.programy.spotube.R
import `in`.programy.spotube.model.Item
import `in`.programy.spotube.ui.MainActivity
import `in`.programy.spotube.util.Constants.NOTIFICATION_CHANNEL_ID
import `in`.programy.spotube.util.Constants.NOTIFICATION_ID
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.session.MediaController
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class MusicNotificationManager(
    private val context: Context,
    notificationListener: PlayerNotificationManager.NotificationListener,
    songs: MutableList<Item>
) {
    private val notificationManager: PlayerNotificationManager

    init {
        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
            context,
            NOTIFICATION_CHANNEL_ID,
            R.string.notification_channel_name,
            R.string.notification_channel_description,
            NOTIFICATION_ID,
            DescriptionAdapter(songs),
            notificationListener
        ).apply {
            setSmallIcon(R.drawable.ic_baseline_music_note_24)
        }
    }
    fun showNotification(player: Player){
        notificationManager.setPlayer(player)
    }

    private inner class DescriptionAdapter(
            val songs: MutableList<Item>
    ):PlayerNotificationManager.MediaDescriptionAdapter{
        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            val i = Intent(context, MainActivity::class.java)
            return PendingIntent.getActivity(context,0,i,PendingIntent.FLAG_UPDATE_CURRENT)
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            return songs[player.currentWindowIndex].snippet.channelTitle
            //return "text"
        }

        override fun getCurrentContentTitle(player: Player): CharSequence {
            return songs[player.currentWindowIndex].snippet.title
            //return "text"
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            Glide.with(context).asBitmap()
                .load(songs[player.currentWindowIndex].snippet.thumbnails.medium.url)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        callback.onBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) = Unit
                })
            return null
        }

    }
}