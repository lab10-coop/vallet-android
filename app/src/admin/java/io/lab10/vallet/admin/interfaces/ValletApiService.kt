package io.lab10.vallet.admin.interfaces

import io.lab10.vallet.models.PriceList
import io.lab10.vallet.models.PriceListCreate
import io.lab10.vallet.models.Product
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ValletApiService {

    @GET("/price_lists/{token_name}")
    fun fetchPriceList(@Path("token_name") token_name: String): Observable<PriceList>

    @POST("/price_lists")
    fun createPriceList(@Body priceList: PriceListCreate): Observable<PriceList>

    companion object {
        fun create(apiBaseUrl: String): ValletApiService {

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(
                            RxJava2CallAdapterFactory.create())
                    .addConverterFactory(
                            GsonConverterFactory.create())
                    .baseUrl(apiBaseUrl)
                    .build()

            return retrofit.create(ValletApiService::class.java)
        }
    }

}