package io.portalhq.portalhackathon.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.portalhq.android.Portal
import io.portalhq.portalhackathon.core.commonconstants.BlockChainConstants.ARBITRUM
import io.portalhq.portalhackathon.core.commonconstants.BlockChainConstants.ARBITRUM_SEPOLIA
import io.portalhq.portalhackathon.core.commonconstants.BlockChainConstants.ARBITRUM_RPC_URL
import io.portalhq.portalhackathon.core.commonconstants.BlockChainConstants.ARBITRUM_SEPOLIA_RPC_URL
import io.portalhq.portalhackathon.core.commonconstants.PortalConstants
import io.portalhq.portalhackathon.data.OkhttpAuthorizationInterceptor
import io.portalhq.portalhackathon.data.MyPortalApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PortalModule {
    @Provides
    @Singleton
    fun providePortal(): Portal {
        return Portal(
            apiKey = PortalConstants.PORTAL_CLIENT_API_KEY,
            rpcConfig = mapOf(
                ARBITRUM to ARBITRUM_RPC_URL,
                ARBITRUM_SEPOLIA to ARBITRUM_SEPOLIA_RPC_URL,
            ),
            legacyEthChainId = 0, // this is for legacy code and no longer relevant in this new Portal SDK
            autoApprove = true
        )
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Provides
    @Singleton
    fun provideHttpClient(
        authorizationInterceptor: OkhttpAuthorizationInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authorizationInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideMyPortalApi(
        httpClient: OkHttpClient
    ): MyPortalApi {
        val retrofit =  Retrofit.Builder()
            .baseUrl(PortalConstants.PORTAL_ARB_SEPOLIA_API_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(MyPortalApi::class.java)
    }
}
