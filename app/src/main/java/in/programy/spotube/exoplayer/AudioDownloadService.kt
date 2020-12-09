package `in`.programy.spotube.exoplayer

import `in`.programy.spotube.R
import `in`.programy.spotube.repository.SpoTubeRepository
import `in`.programy.spotube.ui.MainActivity
import `in`.programy.spotube.util.Constants.DOWNLOAD_NOTIFICATION_CHANNEL_ID
import `in`.programy.spotube.util.Constants.DOWNLOAD_NOTIFICATION_ID
import `in`.programy.spotube.util.Static.tempDownloadList
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.ui.DownloadNotificationHelper
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import javax.inject.Inject

@AndroidEntryPoint
class AudioDownloadService: DownloadService(
        DOWNLOAD_NOTIFICATION_ID,500, DOWNLOAD_NOTIFICATION_CHANNEL_ID,
        R.string.download_notification_channel_name, R.string.download_notification_channel_des
) {

    @Inject
    lateinit var downloadCache: SimpleCache

    @Inject
    lateinit var databaseProvider: ExoDatabaseProvider

    @Inject
    lateinit var dataSourceFactory: DefaultHttpDataSourceFactory

    @Inject
    lateinit var spoTubeRepository: SpoTubeRepository

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main+serviceJob)

    override fun getDownloadManager(): DownloadManager {
        val downloadExecutor = Executor { obj: Runnable -> obj.run() }

        val downloadManager = DownloadManager(
                this,databaseProvider,downloadCache,dataSourceFactory,downloadExecutor
        )

        downloadManager.addListener(object : DownloadManager.Listener{
            override fun onDownloadChanged(downloadManager: DownloadManager, download: Download, finalException: Exception?) {
                if(download.state == Download.STATE_COMPLETED){
                    for(i in tempDownloadList){
                        if(download.request.id == i.id.videoId){
                            serviceScope.launch {
                                spoTubeRepository.insert(i)
                            }
                            return
                        }
                    }
                }
            }
        })

        return downloadManager
    }

    override fun getForegroundNotification(downloads: MutableList<Download>): Notification =
            DownloadNotificationHelper(this, DOWNLOAD_NOTIFICATION_CHANNEL_ID).buildProgressNotification(
                    this,R.drawable.ic_baseline_cloud_download_24,
                    PendingIntent.getActivity(this,0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
                    ,"Downloading...",downloads
            )


    override fun getScheduler(): Scheduler? = null
}