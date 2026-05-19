package com.msrp.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.io.File

@Configuration
class WebConfig(
    @Value("\${msrp.image-storage-dir:./images}") private val imageStorageDir: String,
    @Value("\${api.base-path}") private val apiBasePath: String,
) : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val location = File(imageStorageDir).apply { mkdirs() }.canonicalPath
        registry.addResourceHandler("$apiBasePath/images/**")
            .addResourceLocations("file:$location/")
    }

    @Bean("curationExecutor")
    fun curationExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 1
        executor.maxPoolSize = 1
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(7200)
        executor.initialize()
        return executor
    }
}
