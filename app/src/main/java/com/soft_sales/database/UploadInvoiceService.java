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
import com.soft_sales.broad_cast_receiver.BroadCastCancelInvoiceNotification;
import com.soft_sales.model.CreateOnlineInvoice;
import com.soft_sales.model.InvoiceCrossModel;
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

public class UploadInvoiceService extends Service {
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

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        invoiceModel = (InvoiceModel) intent.getSerializableExtra("data");

        if (NetworkUtils.getConnectivityStatus(this)){
            createNotification();
            Toast.makeText(context, context.getString(R.string.uploading), Toast.LENGTH_LONG).show();
        }

        dao.getLastInvoice()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<InvoiceModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(InvoiceModel invoiceModel) {
                        Log.e("last_id", invoiceModel.getCode()+"");
                        if (invoiceModel.getCode()!=0) {
                            insertLocal(invoiceModel.getCode());

                        } else {
                            insertLocal(0);

                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        insertLocal(0);

                    }
                });


        return START_STICKY;
    }


    private void insertLocal(int lastInvoiceCode) {
        int lstCode = lastInvoiceCode+1;
        dao.insertInvoice(invoiceModel)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Long aLong) {
                        Log.e("inv_sales", "inserted_local____" + aLong.intValue()+"__"+lastInvoiceCode);
                        invoiceModel.setInvoice_local_id(aLong.intValue());
                        invoiceModel.setCode(lstCode);
                        dao.updateInvoice(invoiceModel)
                                .subscribeOn(Schedulers.computation())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new CompletableObserver() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        disposable.add(d);
                                    }

                                    @Override
                                    public void onComplete() {
                                        insertLocalCrossTable();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.e("error",e.getMessage());

                                    }
                                });


                    }


                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("error", e.getMessage());
                        if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {

                        } else {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                        stopSelf();
                    }
                });
    }

    private void insertLocalCrossTable() {
        List<InvoiceCrossModel> crossModelList = new ArrayList<>();
        long local_id = invoiceModel.getInvoice_local_id();
        for (ProductModel model : invoiceModel.getProducts()) {
            InvoiceCrossModel itemsCrossModel = new InvoiceCrossModel(local_id,invoiceModel.getInvoice_online_id(),model.getProduct_local_id(),model.getId(),Double.parseDouble(model.getPrice()),model.getAmount());
            crossModelList.add(itemsCrossModel);
        }

        dao.insertInvoiceCrossTable(crossModelList)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                       /* Toast.makeText(context, getApplicationContext().getString(R.string.done), Toast.LENGTH_SHORT).show();
                        stopSelf();*/
                        EventBus.getDefault().post(invoiceModel);
                        if (NetworkUtils.getConnectivityStatus(context)) {
                            product_index = 0;
                            imagePath ="";
                            uploadUnUploadedProducts();
                        } else {
                            Toast.makeText(context, context.getString(R.string.done), Toast.LENGTH_SHORT).show();
                            stopSelf();
                        }

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();

                        stopSelf();
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
            insertOnline();
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
        dao.getProductByLocalId(productModel.getProduct_local_id())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ProductModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(ProductModel model) {
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
                        RequestBody price = Common.getRequestBodyText(model.getPrice());
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

                    @Override
                    public void onError(Throwable e) {

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
            double total = item.getAmount()*Double.parseDouble(item.getPrice());
            products.add(new CreateOnlineInvoice.Details(item.getId(),item.getPrice(),item.getAmount()+"",total+""));
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
        String title = context.getString(R.string.create_sale_invoice);

        Intent cancelIntent = new Intent(this, BroadCastCancelInvoiceNotification.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, App.CHANNEL_ID_INVOICE);
        builder.setAutoCancel(true);
        builder.setOngoing(true);
        builder.setChannelId(App.CHANNEL_ID_INVOICE);
        builder.setContentTitle(title);
        builder.setContentText(context.getString(R.string.uploading));
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setPriority(NotificationCompat.PRIORITY_LOW);
        builder.addAction(0, context.getString(R.string.dismiss), cancelPendingIntent);
        builder.setProgress(100, 100, true);
        builder.setContentText(context.getString(R.string.uploading));
        builder.setCategory(Notification.CATEGORY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(Tags.not_invoice_id, builder.build());

        } else {
            manager.notify(Tags.not_tag_invoice, Tags.not_invoice_id, builder.build());

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
            manager.cancel(Tags.not_tag_invoice, Tags.not_invoice_id);
        }

    }
}
