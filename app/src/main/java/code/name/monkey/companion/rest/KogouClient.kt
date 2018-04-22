package code.name.monkey.companion.rest


import android.content.Context
import code.name.monkey.companion.rest.service.KuGouApiService
import okhttp3.Cache
import okhttp3.Call
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

/**
 * Created by hemanths on 23/08/17.
 */

class KogouClient(client: Call.Factory) {

    val apiService: KuGouApiService

    constructor(context: Context) : this(createDefaultOkHttpClientBuilder(context).build())

    init {
        val restAdapter = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .callFactory(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        apiService = restAdapter.create(KuGouApiService::class.java)
    }

    companion object {
        val BASE_URL = "http://lyrics.kugou.com/"

        fun createDefaultCache(context: Context): Cache? {
            val cacheDir = File(context.cacheDir.absolutePath, "/okhttp-lastfm/")
            return if (cacheDir.mkdirs() || cacheDir.isDirectory) {
                Cache(cacheDir, (1024 * 1024 * 10).toLong())
            } else null
        }

        fun createCacheControlInterceptor(): Interceptor {
            return Interceptor { chain ->
                val modifiedRequest = chain.request().newBuilder()
                        .addHeader("Cache-Control", String.format("max-age=%d, max-stale=%d", 31536000, 31536000))
                        .build()
                chain.proceed(modifiedRequest)
            }
        }

        fun createDefaultOkHttpClientBuilder(context: Context): OkHttpClient.Builder {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY

            return OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .cache(createDefaultCache(context))
                    .addInterceptor(createCacheControlInterceptor())
        }
    }
}
