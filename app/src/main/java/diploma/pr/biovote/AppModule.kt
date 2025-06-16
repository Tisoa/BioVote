// app/src/main/java/diploma/pr/biovote/AppModule.kt
package diploma.pr.biovote

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import diploma.pr.biovote.data.local.TokenManager
import diploma.pr.biovote.data.remote.model.ApiClient
import diploma.pr.biovote.data.remote.model.ApiService
import diploma.pr.biovote.data.repository.AuthRepository
import diploma.pr.biovote.data.repository.PollRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideApiService(): ApiService =
        ApiClient.service

    @Provides @Singleton
    fun provideTokenManager(@ApplicationContext ctx: Context): TokenManager =
        TokenManager(ctx)

    @Provides @Singleton
    fun provideAuthRepo(api: ApiService): AuthRepository =
        AuthRepository(api)

    @Provides @Singleton
    fun providePollRepo(api: ApiService): PollRepository =
        PollRepository(api)
}