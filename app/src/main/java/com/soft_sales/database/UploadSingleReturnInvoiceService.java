package com.soft_sales.database;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.soft_sales.R;
import com.soft_sales.broad_cast_receiver.BroadCastCancelSingleInvoiceNotification;
import com.soft_sales.model.CreateOnlineInvoice;
import com.soft_sales.model.EventsModel;
import com.soft_sales.model.InvoiceModel;
import com.soft_sales.model.ProductModel;
import com.soft_sales.model.SingleOnlineInvoice;
import com.soft_sales.model.SingleProductDataModel;
import com.soft_sales.model.UserModel;
import com.soft_sales.preferences.Preferences;
import com.soft_sales.remote.Api;
import com.soft_sales.share.App;
import com.soft_sales.share.Common;
import com.soft_sales.share.NetworkUtils;
import com.soft_sales.tags.Tags;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

public class UploadSingleReturnInvoiceService extends Service {
    private Preferences preferences;
    private UserModel userModel;
    private InvoiceModel invoiceModel;
    private AppDatabase appDatabase;
    private DAO dao;
    private NotificationManager manager;
    private CompositeDisposable disposable = new CompositeDisposable();
    private Context context;
    private int product_index = 0;
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

        imagePath ="";
        product_index =0;
        updateLocalInvoice();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        invoiceModel = (InvoiceModel) intent.getSerializableExtra("data");

        return START_STICKY;
    }

    private void updateLocalInvoice() {
        if (invoiceModel.getInvoice_online_id().equals("0")){
            invoiceModel.setUpdated(false);
        }else {
            invoiceModel.setUpdated(true);

        }
        dao.updateInvoice(invoiceModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        Log.e("local_updated","success");
                        if (NetworkUtils.getConnectivityStatus(context)){

                            uploadUnUploadedProducts();
                        }else {
                            stopSelf();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("error",e.getMessage());
                    }
                });
    }


    private void uploadUnUploadedProducts() {
        if (product_index<invoiceModel.getProducts().size()){
            ProductModel productModel = invoiceModel.getProducts().get(product_index);
            if (productModel.getId()!=null&&!productModel.getId().isEmpty()&&!productModel.getId().equals("0")){
                product_index++;
                uploadUnUploadedProducts();
            }else {
                insertOnlineProduct(productModel);
            }
        }else {
            if (invoiceModel.getInvoice_online_id().equals("0")){
                insertOnline();
            }else {
                createReturnInvoiceOnline();
            }

        }
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

    private void insertOnlineProduct(ProductModel productModel) {
        Log.e("online","inserting"+"___"+productModel.getPrice());
        File file = createImageFile();
        try {
            FileOutputStream fileOutputStream = null;

            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(productModel.getLocal_image());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }




        RequestBody title_ar = Common.getRequestBodyText(productModel.getTitle_ar());
        RequestBody title_en = Common.getRequestBodyText(productModel.getTitle_en());
        RequestBody price = Common.getRequestBodyText(productModel.getPrice());
        RequestBody category_id = Common.getRequestBodyText(productModel.getCategory_id());
        MultipartBody.Part photo = null;
        if (imagePath!=null&&!imagePath.isEmpty()){
            photo = Common.getMultiPartFromPath(imagePath, "photo");;
        }

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
                                product_index++;
                                uploadUnUploadedProducts();

                                Log.e("addProduct", response.body().getStatus() + "_" + response.body().getMessage().toString());

                            }
                        } else {
                            product_index++;
                            uploadUnUploadedProducts();

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
                        product_index++;
                        uploadUnUploadedProducts();
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
                        invoiceModel.getProducts().set(product_index,productModel);
                        dao.updateProductOnlineIdInCrossTable(productModel.getProduct_local_id(),productModel.getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new CompletableObserver() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        disposable.add(d);
                                    }

                                    @Override
                                    public void onComplete() {
                                        invoiceModel.getProducts().set(product_index,productModel);
                                        product_index++;
                                        uploadUnUploadedProducts();

                                        Log.e("local","updated");

                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        product_index++;
                                        uploadUnUploadedProducts();
                                        Log.e("updateLocalProduct", e.getMessage());
                                    }
                                });





                    }

                    @Override
                    public void onError(Throwable e) {
                        product_index++;
                        uploadUnUploadedProducts();
                        Log.e("updateLocalProduct", e.getMessage());
                    }
                });

    }

    private void insertOnline() {


        Log.e("invoice", "inserting_online");
        List<CreateOnlineInvoice.Details> products = new ArrayList<>();
        for (ProductModel item : invoiceModel.getProducts()) {
            double total = item.getInvoiceCrossModel().getProduct_amount()*item.getInvoiceCrossModel().getProduct_price();
            products.add(new CreateOnlineInvoice.Details(item.getId(),item.getInvoiceCrossModel().getProduct_price()+"",item.getInvoiceCrossModel().getProduct_amount()+"",total+""));
        }
        CreateOnlineInvoice createOnlineInvoice = new CreateOnlineInvoice(invoiceModel.getCustomer_name(),invoiceModel.getCode()+"",invoiceModel.getTotal_products()+"",invoiceModel.getTotal_after_discount()+"",invoiceModel.getTotal_after_tax()+"",invoiceModel.getTotal()+"",invoiceModel.getDiscount()+"",invoiceModel.getTax_value(),invoiceModel.getTax_method(),invoiceModel.getTax_per(),invoiceModel.getDate(),invoiceModel.isIs_back(),invoiceModel.getPay_type(),products);

        Api.getService(Tags.getBaseUrl(context))
                .createInvoice(userModel.getData().getAccess_token(),createOnlineInvoice)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Response<SingleOnlineInvoice>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Response<SingleOnlineInvoice> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                if (response.body().getStatus() == 200) {
                                    Log.e("invoice", "inserted_online");

                                    invoiceModel.setUpdated(false);
                                    invoiceModel.setInvoice_online_id(response.body().getData().getId());
                                    invoiceModel.setCode(Integer.parseInt(response.body().getData().getCode()));
                                    dao.updateInvoice(invoiceModel)
                                            .subscribeOn(Schedulers.computation())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new CompletableObserver() {
                                                @Override
                                                public void onSubscribe(@NonNull Disposable d) {
                                                    disposable.add(d);

                                                }

                                                @Override
                                                public void onComplete() {
                                                    Log.e("invoice", "updated_local");
                                                   updateInvoiceCrossTable(invoiceModel);

                                                }

                                                @Override
                                                public void onError(@NonNull Throwable e) {
                                                    stopSelf();
                                                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });


                                } else {
                                    Log.e("error", response.body().getStatus() + "__");
                                    Log.e("error", response.body().getMessage().toString() + "__");

                                    stopSelf();


                                }
                            }
                        } else {
                            try {
                                Log.e("err", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            stopSelf();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("err_item", e.getMessage());
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        stopSelf();

                    }
                });
    }

    private void createReturnInvoiceOnline() {

        Api.getService(Tags.getBaseUrl(context))
                .createReturnInvoice(userModel.getData().getAccess_token(),invoiceModel.getInvoice_online_id())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Response<SingleOnlineInvoice>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Response<SingleOnlineInvoice> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                if (response.body().getStatus() == 200) {
                                    Log.e("invoice", "inserted_online");

                                    invoiceModel.setUpdated(false);
                                    invoiceModel.setInvoice_online_id(response.body().getData().getId());
                                    invoiceModel.setCode(Integer.parseInt(response.body().getData().getCode()));
                                    dao.updateInvoice(invoiceModel)
                                            .subscribeOn(Schedulers.computation())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new CompletableObserver() {
                                                @Override
                                                public void onSubscribe(@NonNull Disposable d) {
                                                    disposable.add(d);

                                                }

                                                @Override
                                                public void onComplete() {
                                                    Log.e("invoice", "updated_local");
                                                    Toast.makeText(context, R.string.done, Toast.LENGTH_SHORT).show();
                                                    stopSelf();
                                                }

                                                @Override
                                                public void onError(@NonNull Throwable e) {
                                                    stopSelf();
                                                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });


                                } else {
                                    Log.e("error", response.body().getStatus() + "__");
                                    Log.e("error", response.body().getMessage().toString() + "__");

                                    stopSelf();


                                }
                            }
                        } else {
                            try {
                                Log.e("err", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            stopSelf();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("err_item", e.getMessage());
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        stopSelf();

                    }
                });
    }

    private void updateInvoiceCrossTable(InvoiceModel invoiceModel){
        dao.updateInvoiceOnlineIdInCrossTable(invoiceModel.getInvoice_local_id(),invoiceModel.getInvoice_online_id())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        EventBus.getDefault().post(new EventsModel(true));
                        stopSelf();

                        Log.e("local","updated");

                    }

                    @Override
                    public void onError(Throwable e) {
                        stopSelf();
                        Log.e("updateLocalProduct", e.getMessage());
                    }
                });
    }

    private void createNotification() {
        String title = context.getString(R.string.create_return_invoice);

        Intent cancelIntent = new Intent(this, BroadCastCancelSingleInvoiceNotification.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, App.CHANNEL_ID_SINGLE_RETURN_INViOCE);
        builder.setAutoCancel(true);
        builder.setOngoing(true);
        builder.setChannelId(App.CHANNEL_ID_SINGLE_RETURN_INViOCE);
        builder.setContentTitle(title);
        builder.setContentText(context.getString(R.string.uploading));
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.addAction(0, context.getString(R.string.dismiss), cancelPendingIntent);
        builder.setProgress(100, 100, true);
        builder.setContentText(context.getString(R.string.uploading));
        builder.setCategory(Notification.CATEGORY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(Tags.not_single_return_invoice_id, builder.build());

        } else {
            manager.notify(Tags.not_tag_single_return_invoice, Tags.not_single_return_invoice_id, builder.build());

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
            manager.cancel(Tags.not_tag_single_return_invoice, Tags.not_single_return_invoice_id);
        }

    }
}
