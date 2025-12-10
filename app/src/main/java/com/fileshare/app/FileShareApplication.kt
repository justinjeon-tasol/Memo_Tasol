package com.fileshare.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.fileshare.app.di.AppContainer

class FileShareApplication : Application(), ImageLoaderFactory {
    
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        // AppContainer 생성 시 Context 전달
        container = AppContainer(applicationContext)
    }
    
    // Coil이 이미지 로딩 시 인증 헤더를 포함하도록 커스텀 OkHttpClient 사용
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient(container.okHttpClient)
            .crossfade(true)
            .build()
    }
}
