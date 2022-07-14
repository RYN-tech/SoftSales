package com.soft_sales.services;


import com.soft_sales.model.CategoryDataModel;
import com.soft_sales.model.CreateOnlineInvoice;
import com.soft_sales.model.InvoiceDataModel;
import com.soft_sales.model.ProductDataModel;
import com.soft_sales.model.ResponseModel;
import com.soft_sales.model.SettingDataModel;
import com.soft_sales.model.SingleOnlineInvoice;
import com.soft_sales.model.SingleProductDataModel;
import com.soft_sales.model.UserModel;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface Service {
    @FormUrlEncoded
    @POST("api/auth/login")
    Single<Response<UserModel>> login(@Field("lang") String lang,
                                      @Field("user_name") String user_name,
                                      @Field("password") String password);

    @GET("api/home/setting")
    Single<Response<SettingDataModel>> getSetting(@Header("Authorization") String authorization);

    @GET("api/home/categories")
    Single<Response<CategoryDataModel>> getDepartments(@Header("Authorization") String authorization,
                                                       @Query("page") int page
    );

    @GET("api/home/products")
    Single<Response<ProductDataModel>> getProducts(@Header("Authorization") String authorization,
                                                   @Query("page") int page
    );

    @GET("api/order/orders")
    Single<Response<InvoiceDataModel>> getInvoices(@Header("Authorization") String authorization,
                                                   @Query("page") int page
    );

    @Multipart
    @POST("api/home/addProduct")
    Single<Response<SingleProductDataModel>> addProduct(@Header("Authorization") String authorization,
                                                        @Part("title_ar") RequestBody title_ar,
                                                        @Part("title_en") RequestBody title_en,
                                                        @Part("category_id") RequestBody category_id,
                                                        @Part("price") RequestBody price,
                                                        @Part MultipartBody.Part photo);

    @POST("api/order/storeOrder")
    Single<Response<SingleOnlineInvoice>> createInvoice(@Header("Authorization") String authorization,
                                                        @Body CreateOnlineInvoice model
    );

    @FormUrlEncoded
    @POST("api/order/backOrder")
    Single<Response<SingleOnlineInvoice>> createReturnInvoice(@Header("Authorization") String authorization,
                                                              @Field("id") String invoice_id
    );

    @POST("api/auth/getProfile")
    Single<Response<UserModel>> getProfile(@Header("Authorization") String authorization);

    @POST("api/auth/logout")
    Single<Response<ResponseModel>> logout(@Header("Authorization") String authorization


    );
}