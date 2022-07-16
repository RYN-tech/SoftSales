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
import com.soft_sales.broad_cast_receiver.BroadCastCancelProductsNotification;
import com.soft_sales.model.EventsModel;
import com.soft_sales.model.InvoiceModel;
import com.soft_sales.model.ProductDataModel;
import com.soft_sales.model.ProductModel;
import com.soft_sales.model.UserModel;
import com.soft_sales.preferences.Preferences;
import com.soft_sales.remote.Api;
import com.soft_sales.share.App;
import com.soft_sales.share.Common;
import com.soft_sales.share.NetworkUtils;
import com.soft_sales.tags.Tags;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class LoadProductsService extends Service {
    private List<ProductModel> list;
    private AppDatabase appDatabase;
    private DAO dao;
    private NotificationManager manager;
    private Preferences preferences;
    private UserModel userModel;
    private CompositeDisposable disposable = new CompositeDisposable();
    private int page = 1;
    private List<ProductModel> data = new ArrayList<>();
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("service_LoadItem","created");
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        list = new ArrayList<>();
        appDatabase = AppDatabase.getInstance(getApplication());
        dao = appDatabase.getDAO();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        context = this;
        createNotification();
        if (NetworkUtils.getConnectivityStatus(this)){
            getOnlineProduct(page);

        }else {
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return START_STICKY;
    }


    private void getOnlineProduct(int page) {
        Log.e("pageItems",page+"");

        Api.getService(Tags.getBaseUrl(context))
                .getProducts(userModel.getData().getAccess_token(), page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Response<ProductDataModel>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Response<ProductDataModel> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null && response.body().getStatus() == 200) {
                                if (response.body().getData().getData().size() > 0) {
                                    list.clear();
                                    list.addAll(response.body().getData().getData());

                                    downLoadImage(0);
                                } else {



                                    getLocalSalesInvoices();

                                }
                            } else {
                                getLocalSalesInvoices();


                                Log.e("err_item", response.body().getStatus() + "_" + response.body().getMessage().toString());

                            }
                        } else {

                            if (response.code() == 402) {
                                Toast.makeText(context, R.string.account_blocked, Toast.LENGTH_LONG).show();
                            } else if (response.code() == 410) {
                                Toast.makeText(context, R.string.package_finished, Toast.LENGTH_LONG).show();

                            } else if (response.code() == 411) {
                                Toast.makeText(context, R.string.domain_invalid, Toast.LENGTH_LONG).show();

                            }

                            getLocalSalesInvoices();
                            try {
                                Log.e("err", response.code() + "_" + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        getLocalSalesInvoices();
                        Log.e("err_items", e.getMessage());

                    }
                });
    }


    private void downLoadImage(int index) {
        Log.e("index",index+"__"+list.size());
        if (index < this.list.size()) {
            ProductModel model = this.list.get(index);

            if (model.getPhoto() != null && !model.getPhoto().isEmpty()) {

                Glide.with(this)
                        .asBitmap()
                        .load(Uri.parse(Tags.getBaseUrl(context)+model.getPhoto()))
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                                Log.e("load","image");
                                int nh = (int) ( resource.getHeight() * (512.0 / resource.getWidth()) );
                                Bitmap bitmap = Bitmap.createScaledBitmap(resource, 512, nh, true);

                                model.setLocal_image(Common.getByteArray(bitmap));

                                data.add(model);
                                int newIndex = index + 1;
                                downLoadImage(newIndex);
                            }
                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                int newIndex = index + 1;
                                data.add(model);
                                downLoadImage(newIndex);
                            }
                        });
            }

        } else {
            insertToLocalDatabase(data, page);
        }


    }

    private void insertToLocalDatabase(List<ProductModel> data, int page) {
        Log.e("inserting_local", data.size() + "_");
        dao.insertProducts(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        LoadProductsService.this.data.clear();
                        int newPage = page + 1;
                        LoadProductsService.this.page = newPage;
                        getOnlineProduct(newPage);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("error", e.getMessage());
                    }
                });

    }

    private void getLocalSalesInvoices() {
        dao.getSalesInvoices(false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<InvoiceModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(List<InvoiceModel> invoices) {
                        if (invoices.size()==0){
                            if (NetworkUtils.getConnectivityStatus(getApplication().getApplicationContext())) {
                                Intent intent1 = new Intent(LoadProductsService.this, LoadSalesInvoiceService.class);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    startForegroundService(intent1);
                                } else {
                                    startService(intent1);

                                }
                            }
                        }else {
                            EventBus.getDefault().post(new EventsModel(false));

                        }
                        stopSelf();
                    }

                    @Override
                    public void onError(Throwable e) {
                        EventBus.getDefault().post(new EventsModel(false));

                        stopSelf();
                        Log.e("service_invoice",e.getMessage());
                    }
                });


    }





    private void createNotification() {
        Intent cancelIntent = new Intent(this, BroadCastCancelProductsNotification.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, App.CHANNEL_ID_PRODUCTS);
        builder.setAutoCancel(true);
        builder.setOngoing(true);
        builder.setChannelId(App.CHANNEL_ID_PRODUCTS);
        builder.setContentTitle(context.getString(R.string.products));
        builder.setContentText(context.getString(R.string.downloading));
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setPriority(NotificationCompat.PRIORITY_LOW);
        builder.addAction(0, context.getString(R.string.dismiss), cancelPendingIntent);
        builder.setProgress(100, 100, true);
        builder.setContentText(context.getString(R.string.downloading));
        builder.setCategory(Notification.CATEGORY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(Tags.not_products_id, builder.build());

        } else {
            manager.notify(Tags.not_tag_products, Tags.not_products_id, builder.build());

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
        Log.e("productService", "destroyed");
        disposable.clear();
        if (manager != null) {
            manager.cancel(Tags.not_tag_products, Tags.not_products_id);
        }

    }
}
