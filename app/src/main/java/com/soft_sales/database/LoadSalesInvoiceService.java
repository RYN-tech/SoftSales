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
import com.soft_sales.broad_cast_receiver.BroadCastCancelProductsNotification;
import com.soft_sales.broad_cast_receiver.BroadCastCancelSalesInvoicesNotification;
import com.soft_sales.model.CategoryDataModel;
import com.soft_sales.model.EventsModel;
import com.soft_sales.model.InvoiceCrossModel;
import com.soft_sales.model.InvoiceDataModel;
import com.soft_sales.model.InvoiceModel;
import com.soft_sales.model.OnlineInvoiceModel;
import com.soft_sales.model.ProductModel;
import com.soft_sales.model.UserModel;
import com.soft_sales.preferences.Preferences;
import com.soft_sales.remote.Api;
import com.soft_sales.share.App;
import com.soft_sales.share.Common;
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

public class LoadSalesInvoiceService extends Service {
    private List<OnlineInvoiceModel> list;
    private AppDatabase appDatabase;
    private DAO dao;
    private NotificationManager manager;
    private Preferences preferences;
    private UserModel userModel;
    private CompositeDisposable disposable = new CompositeDisposable();
    private int page = 1;
    private int mainIndex = 0;
    private int childIndex = 0;
    List<InvoiceCrossModel> crossData = new ArrayList<>();
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("service_LoadInvoice","created");
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        list = new ArrayList<>();
        appDatabase = AppDatabase.getInstance(getApplication());
        dao = appDatabase.getDAO();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        createNotification();
        getOnlineInvoices(page);

        return START_STICKY;
    }

    private void getOnlineInvoices(int page) {
        Log.e("page",page+"");
        childIndex = 0;
        Log.e("invoice","gettingOnline"+page);
        Api.getService(Tags.getBaseUrl(this))
                .getInvoices(userModel.getData().getAccess_token(),page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Response<InvoiceDataModel>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Response<InvoiceDataModel> response) {
                        Log.e("code",response.body().getStatus()+"");
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                if (response.body().getStatus() == 200) {
                                    if (response.body().getData().getData().size()>0){
                                        Log.e("onlineSalesSize",response.body().getData().getData().size()+"");
                                        list.clear();
                                        list.addAll(response.body().getData().getData());
                                        Log.e("list",list.size()+"");
                                        mainIndex = 0;
                                        insertLocalInvoice(mainIndex);
                                    }else {
                                        Log.e("done","done");
                                        EventBus.getDefault().post(new EventsModel(false));
                                        stopSelf();
                                    }
                                } else {
                                    EventBus.getDefault().post(new EventsModel(false));
                                    stopSelf();
                                }
                            } else {
                                EventBus.getDefault().post(new EventsModel(false));

                                stopSelf();
                            }
                        } else {
                            try {
                                Log.e("error",response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (response.code() == 402) {
                                Toast.makeText(context, R.string.account_blocked, Toast.LENGTH_LONG).show();
                            } else if (response.code() == 410) {
                                Toast.makeText(context, R.string.package_finished, Toast.LENGTH_LONG).show();

                            } else if (response.code() == 411) {
                                Toast.makeText(context, R.string.domain_invalid, Toast.LENGTH_LONG).show();

                            }
                            EventBus.getDefault().post(new EventsModel(false));

                            stopSelf();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("error", e.getMessage());
                        EventBus.getDefault().post(new EventsModel(false));

                        stopSelf();

                    }
                });
    }

    private void insertLocalInvoice(int mainIndex) {
        Log.e("localSales","insertingLocal");

        childIndex = 0;
        Log.e("listSize",mainIndex+"__"+list.size()+"");
        if (mainIndex<list.size()){
            Log.e("1","1");
            OnlineInvoiceModel onlineInvoiceModel = list.get(mainIndex);
            InvoiceModel invoiceModel = new InvoiceModel(onlineInvoiceModel.getId(),Integer.parseInt(onlineInvoiceModel.getCode()),onlineInvoiceModel.getCustomer_name(),Double.parseDouble(onlineInvoiceModel.getTotal()),Double.parseDouble(onlineInvoiceModel.getDiscount()),Double.parseDouble(onlineInvoiceModel.getProduct_total()),Double.parseDouble(onlineInvoiceModel.getTotal_after_discount()),Double.parseDouble(onlineInvoiceModel.getTotal_after_tax()),onlineInvoiceModel.getPay_type(),onlineInvoiceModel.getTax(),onlineInvoiceModel.getTax_method(),onlineInvoiceModel.getTax_per(),onlineInvoiceModel.getOrder_date_time(),onlineInvoiceModel.isIs_back(),onlineInvoiceModel.getAdded_by_id());

            dao.insertInvoice(invoiceModel)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Long>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            disposable.add(d);
                        }

                        @Override
                        public void onSuccess(Long aLong) {
                            invoiceModel.setInvoice_local_id(aLong);
                            insertLocalCrossTable(invoiceModel);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("insertInvoice",e.getMessage());
                        }
                    });
        }else {
            this.page = this.page+1;
            getOnlineInvoices(this.page);
        }


    }

    private void insertLocalCrossTable(InvoiceModel invoiceModel) {
        Log.e("inserting","crossTable");
        crossData.clear();

        OnlineInvoiceModel onlineInvoiceModel = list.get(mainIndex);
        for (OnlineInvoiceModel.Detail pro:onlineInvoiceModel.getDetails()){

            InvoiceCrossModel model = new InvoiceCrossModel(invoiceModel.getInvoice_local_id(),onlineInvoiceModel.getId(),0,pro.getProduct().getId(),Double.parseDouble(pro.price),Double.parseDouble(pro.qty));
            crossData.add(model);
        }

        updateCrossTable(childIndex);

    }

    private void updateCrossTable(int index) {
        Log.e("update","crossTable"+crossData.size());
        if (index<crossData.size()){
            InvoiceCrossModel model = crossData.get(index);

            dao.getLocalProductByOnlineId(model.getProduct_online_id())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<ProductModel>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            disposable.add(d);
                        }

                        @Override
                        public void onSuccess(ProductModel productModel) {
                            model.setProduct_local_id(productModel.getProduct_local_id());
                            crossData.set(index,model);
                            LoadSalesInvoiceService.this.childIndex++;
                            updateCrossTable(LoadSalesInvoiceService.this.childIndex++);

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("productOnlineID",e.getMessage());
                        }
                    });

        }else {
            dao.insertInvoiceCrossTable(crossData)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            disposable.add(d);
                        }

                        @Override
                        public void onComplete() {
                            LoadSalesInvoiceService.this.mainIndex= LoadSalesInvoiceService.this.mainIndex+1;
                            insertLocalInvoice(LoadSalesInvoiceService.this.mainIndex);
                        }

                        @Override
                        public void onError(Throwable e) {
                            LoadSalesInvoiceService.this.mainIndex= LoadSalesInvoiceService.this.mainIndex+1;
                            insertLocalInvoice(LoadSalesInvoiceService.this.mainIndex);
                        }
                    });

        }
    }


    private void createNotification() {
        Intent cancelIntent = new Intent(this, BroadCastCancelSalesInvoicesNotification.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, App.CHANNEL_ID_INVOICES);
        builder.setAutoCancel(true);
        builder.setOngoing(true);
        builder.setChannelId(App.CHANNEL_ID_INVOICES);
        builder.setContentTitle(getString(R.string.invoices));
        builder.setContentText(context.getString(R.string.downloading));
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setPriority(NotificationCompat.PRIORITY_LOW);
        builder.addAction(0, context.getString(R.string.dismiss), cancelPendingIntent);
        builder.setProgress(100, 100, true);
        builder.setContentText(context.getString(R.string.downloading));
        builder.setCategory(Notification.CATEGORY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(Tags.not_invoices_id, builder.build());

        } else {
            manager.notify(Tags.not_tag_invoices, Tags.not_invoices_id, builder.build());

        }


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("productService", "destroyed");
        disposable.clear();
        if (manager != null) {
            manager.cancel(Tags.not_tag_invoices, Tags.not_invoices_id);
        }

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
