package com.soft_sales.database;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.soft_sales.R;
import com.soft_sales.broad_cast_receiver.BroadCastCancelProductNotification;
import com.soft_sales.model.AddProductModel;
import com.soft_sales.model.ProductDataModel;
import com.soft_sales.model.ProductModel;
import com.soft_sales.model.SingleProductDataModel;
import com.soft_sales.model.UserModel;
import com.soft_sales.preferences.Preferences;
import com.soft_sales.remote.Api;
import com.soft_sales.share.App;
import com.soft_sales.share.Common;
import com.soft_sales.share.NetworkUtils;
import com.soft_sales.tags.Tags;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class UploadProductService extends Service {
    private Preferences preferences;
    private UserModel userModel;
    private AddProductModel addProductModel;
    private AppDatabase appDatabase;
    private DAO dao;
    private NotificationManager manager;
    private CompositeDisposable disposable = new CompositeDisposable();
    private Context context;
    private String imagePath ="";

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        appDatabase = AppDatabase.getInstance(getApplication());
        dao = appDatabase.getDAO();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        context = this;
        createNotification();

        if (NetworkUtils.getConnectivityStatus(this)){
            Toast.makeText(context, context.getString(R.string.uploading), Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        addProductModel = (AddProductModel) intent.getSerializableExtra("data");
        insertProductLocal();
        return START_NOT_STICKY;
    }

    private void insertProductLocal() {
        Glide.with(context)
                .asBitmap()
                .load(new File(addProductModel.getPhoto_path()))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Log.e("productImage","success");
                        byte[] bytes = Common.getByteArray(resource);
                        File file = createImageFile();
                        try {
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            fileOutputStream.write(bytes);
                            fileOutputStream.flush();
                            fileOutputStream.close();

                            String price = "0";
                            if (addProductModel.getPrice()!=null&&!addProductModel.getPrice().isEmpty()){
                                price = addProductModel.getPrice();
                            }
                            ProductModel productModel = new ProductModel("0", addProductModel.getCategory_id(), addProductModel.getName_en() != null ? addProductModel.getName_en() : "", addProductModel.getName_ar(), "", price, "", bytes);
                            dao.insertProduct(productModel)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Long>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {
                                            disposable.add(d);
                                        }

                                        @Override
                                        public void onSuccess(Long aLong) {
                                            Log.e("local","inserted");
                                            productModel.setProduct_local_id(aLong);
                                            insertOnline(productModel);
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Log.e("insertProduct", e.getMessage());
                                        }
                                    });

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                });


    }


    private File createImageFile() {
        File imageFile = null;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HH:mm", Locale.ENGLISH).format(new Date());
        String imageName = "JPEG_" + timeStamp + "_";
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath(), "soft_sales_photos");
        if (!file.exists()) {
            file.mkdirs();
        }

        try {
            imageFile = File.createTempFile(imageName, ".jpg", file);
            imagePath = imageFile.getAbsolutePath();
            Log.e("path", imagePath + "");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("err", e.getMessage());
        }

        return imageFile;

    }

    private void insertOnline(ProductModel productModel) {
        Log.e("online","inserting"+"___"+productModel.getPrice());
        RequestBody title_ar = Common.getRequestBodyText(productModel.getTitle_ar());
        RequestBody title_en = Common.getRequestBodyText(productModel.getTitle_en());
        RequestBody price = Common.getRequestBodyText(productModel.getPrice());
        RequestBody category_id = Common.getRequestBodyText(productModel.getCategory_id());
        MultipartBody.Part photo = Common.getMultiPartFromPath(imagePath, "photo");
        Log.e("dddd",productModel.getLocal_image().toString());

        Api.getService(Tags.getBaseUrl(context))
                .addProduct(userModel.getData().getAccess_token(), title_ar, title_en, category_id, price, photo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Response<SingleProductDataModel>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Response<SingleProductDataModel> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null && response.body().getStatus() == 200) {
                                Log.e("online","inserted");

                                productModel.setId(response.body().getData().getId());
                                updateProductLocal(productModel);
                            } else {
                                stopSelf();

                                Log.e("addProduct", response.body().getStatus() + "_" + response.body().getMessage().toString());

                            }
                        } else {
                            stopSelf();
                            if (response.code() == 402) {
                                Toast.makeText(context, R.string.account_blocked, Toast.LENGTH_LONG).show();
                            } else if (response.code() == 410) {
                                Toast.makeText(context, R.string.package_finished, Toast.LENGTH_LONG).show();

                            } else if (response.code() == 411) {
                                Toast.makeText(context, R.string.domain_invalid, Toast.LENGTH_LONG).show();

                            }

                            try {
                                Log.e("err", response.code() + "_" + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        stopSelf();
                        Log.e("err_items", e.getMessage());

                    }
                });
    }

    private void updateProductLocal(ProductModel productModel) {
        Log.e("local","updating");

        dao.updateProductOnlineIdByLocalId(productModel.getId(),productModel.getProduct_local_id())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        Log.e("local","updated");

                        stopSelf();
                        Toast.makeText(context, R.string.done, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        stopSelf();
                        Log.e("updateLocalProduct", e.getMessage());
                    }
                });

    }

    private void createNotification() {
        String title = context.getString(R.string.add_product);
        String body = context.getString(R.string.uploading);

        Intent cancelIntent = new Intent(this, BroadCastCancelProductNotification.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, App.CHANNEL_ID_PRODUCT);
        builder.setAutoCancel(true);
        builder.setOngoing(true);
        builder.setChannelId(App.CHANNEL_ID_PRODUCT);
        builder.setContentTitle(title);
        builder.setContentText(body);
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setPriority(NotificationCompat.PRIORITY_LOW);
        builder.addAction(0, context.getString(R.string.dismiss), cancelPendingIntent);
        builder.setProgress(100, 100, true);
        builder.setContentText(body);
        builder.setCategory(Notification.CATEGORY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(Tags.not_product_id, builder.build());

        } else {
            manager.notify(Tags.not_tag_product, Tags.not_product_id, builder.build());

        }


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("service", "destroyed");
        disposable.clear();
        if (manager != null) {
            manager.cancel(Tags.not_tag_product, Tags.not_product_id);
        }

    }
}
