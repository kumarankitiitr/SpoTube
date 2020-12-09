package `in`.programy.spotube.dhilt

import `in`.programy.spotube.repository.SpoTubeRepository
import `in`.programy.spotube.room.ItemDatabase
import `in`.programy.spotube.ui.SpoTubeViewModel
import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {


    @Singleton
    @Provides
    fun provideSpoTubeViewModel(
            repository: SpoTubeRepository
    ) = SpoTubeViewModel(repository)

    @Singleton
    @Provides
    fun provideProgressiveMediaSourc(cacheDataSourceFactory: DataSource.Factory): ProgressiveMediaSource.Factory{
        return ProgressiveMediaSource.Factory(cacheDataSourceFactory)
    }


    @Singleton
    @Provides
    fun provideAudioAttributes() = AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

    @Singleton
    @Provides
    fun provideExoPlayer(
            @ApplicationContext context: Context,
            audioAttributes: AudioAttributes,
            cacheDataSourceFactory: DataSource.Factory
    ) = SimpleExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .build().apply {
                setAudioAttributes(audioAttributes, true)
                setHandleAudioBecomingNoisy(true)
            }

    @Singleton
    @Provides
    fun provideDataSourceFactory(
            @ApplicationContext context: Context
    ) = DefaultDataSourceFactory(context, Util.getUserAgent(context, "Spotube App"))


    @Singleton
    @Provides
    fun provideItemDatabase(
            @ApplicationContext context: Context
    ) = ItemDatabase(context)

    @Singleton
    @Provides
    fun provideCacheDataResourceFactory(downloadCache: SimpleCache, dataSourceFactory: DefaultHttpDataSourceFactory): DataSource.Factory =
            CacheDataSource.Factory()
                    .setCache(downloadCache)
                    .setUpstreamDataSourceFactory(dataSourceFactory)
                    .setCacheWriteDataSinkFactory(null) // Disable writing.

    @Singleton
    @Provides
    fun provideDatabaseProvider(
            @ApplicationContext context: Context
    ) = ExoDatabaseProvider(context)

    @Singleton
    @Provides
    fun provideDownloadCache(
            @ApplicationContext context: Context,
            databaseProvider: ExoDatabaseProvider
    ) = SimpleCache(
            File(context.getExternalFilesDir(null),"Spotube"),
            NoOpCacheEvictor(),
            databaseProvider)

    @Singleton
    @Provides
    fun providedataSourceFactory() =  DefaultHttpDataSourceFactory()


}