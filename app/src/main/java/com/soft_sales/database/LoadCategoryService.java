package com.soft_sales.database;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.soft_sales.R;
import com.soft_sales.broad_cast_receiver.BroadCastCancelCategoryNotification;
import com.soft_sales.model.CategoryDataModel;
import com.soft_sales.model.CategoryModel;
import com.soft_sales.model.EventsModel;
import com.soft_sales.model.InvoiceModel;
import com.soft_sales.model.ProductModel;
import com.soft_sales.model.UserModel;
import com.soft_sales.preferences.Preferences;
import com.soft_sales.remote.Api;
import com.soft_sales.share.App;
import com.soft_sales.share.NetworkUtils;
import com.soft_sales.tags.Tags;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class LoadCategoryService extends Service {
    private AppDatabase appDatabase;
    private DAO dao;
    private NotificationManager manager;
    private CompositeDisposable disposable = new CompositeDisposable();
    private Preferences preferences;
    private UserModel userModel;
    private Context context;
    private  int page = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        page = 1;
        appDatabase = AppDatabase.getInstance(getApplication());
        dao = appDatabase.getDAO();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        createNotification();
        getOnlineCategory(page);

        return START_STICKY;
    }

    private void getOnlineCategory(int page) {
        Log.e("categoryPage",page+"");
        Api.getService(Tags.getBaseUrl(this))
                .getDepartments(userModel.getData().getAccess_token(),page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Response<CategoryDataModel>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Response<CategoryDataModel> response) {

                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                if (response.body().getStatus() == 200) {
                                    if (response.body().getData().getData().size()>0){

                                        insertLocalCategory(response.body().getData().getData());

                                    }else {
                                        loadProducts();
                                    }
                                } else {
                                    loadProducts();
                                }
                            } else {
                                loadProducts();
                            }
                        } else {
                            if (response.code() == 402) {
                                Toast.makeText(context, R.string.account_blocked, Toast.LENGTH_LONG).show();
                            } else if (response.code() == 410) {
                                Toast.makeText(context, R.string.package_finished, Toast.LENGTH_LONG).show();

                            } else if (response.code() == 411) {
                                Toast.makeText(context, R.string.domain_invalid, Toast.LENGTH_LONG).show();

                            }
                            loadProducts();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("error", e.getMessage());
                        loadProducts();

                    }
                });

    }

    private void insertLocalCategory(List<CategoryModel> data) {
        Log.e("size",data.size()+"_");
        dao.insertCategories(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        LoadCategoryService.this.page++;
                        getOnlineCategory(LoadCategoryService.this.page++);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("loadCategoryError",e.getMessage());

                    }

                });
    }

    private void loadProducts() {
        dao.getProducts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<ProductModel>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull List<ProductModel> list) {
                        if (list.size() == 0) {
                            if (NetworkUtils.getConnectivityStatus(getApplication().getApplicationContext())) {
                                Intent intent1 = new Intent(LoadCategoryService.this, LoadProductsService.class);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    startForegroundService(intent1);
                                } else {
                                    startService(intent1);

                                }
                            }
                            stopSelf();


                        } else {

                            getLocalSalesInvoices();

                        }

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("error", e.getMessage());
                       getLocalSalesInvoices();


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
                                Intent intent1 = new Intent(LoadCategoryService.this, LoadSalesInvoiceService.class);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    startForegroundService(intent1);
                                } else {
                                    startService(intent1);

                                }
                            }
                        }
                        EventBus.getDefault().post(new EventsModel(false));

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
        Intent cancelIntent = new Intent(this, BroadCastCancelCategoryNotification.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, App.CHANNEL_ID_CATEGORY);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setChannelId(App.CHANNEL_ID_CATEGORY);
        builder.setContentTitle(context.getString(R.string.categories));
        builder.setContentText(context.getString(R.string.downloading));
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setPriority(NotificationCompat.PRIORITY_LOW);
        builder.addAction(0, context.getString(R.string.dismiss), cancelPendingIntent);
        builder.setProgress(100, 100, true);
        builder.setContentText(context.getString(R.string.downloading));
        builder.setCategory(Notification.CATEGORY_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(Tags.not_id, builder.build());

        } else {
            manager.notify(Tags.not_tag, Tags.not_id, builder.build());

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
        disposable.clear();
        if (manager != null) {
            manager.cancel(Tags.not_tag, Tags.not_id);
        }

    }
}
