package com.soft_sales.mvvm;

import android.app.ActivityManager;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.soft_sales.R;
import com.soft_sales.database.AppDatabase;
import com.soft_sales.database.AppSyncService;
import com.soft_sales.database.DAO;
import com.soft_sales.database.LoadCategoryService;
import com.soft_sales.database.LoadProductsService;
import com.soft_sales.database.LoadSalesInvoiceService;
import com.soft_sales.model.CategoryModel;
import com.soft_sales.model.InvoiceModel;
import com.soft_sales.model.InvoiceWithProductsModel;
import com.soft_sales.model.ProductModel;
import com.soft_sales.model.ResponseModel;
import com.soft_sales.model.UserModel;
import com.soft_sales.remote.Api;
import com.soft_sales.share.Common;
import com.soft_sales.share.NetworkUtils;
import com.soft_sales.tags.Tags;

import java.io.IOException;
import java.util.List;

import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class ActivityHomeMvvm extends AndroidViewModel {
    private AppDatabase database;
    private DAO dao;
    private CompositeDisposable disposable = new CompositeDisposable();
    private MutableLiveData<Boolean> isSynchronization;
    private MutableLiveData<Boolean> onLogoutSuccess;

    public ActivityHomeMvvm(@NonNull Application application) {
        super(application);
        database = AppDatabase.getInstance(application);
        dao = database.getDAO();
    }

    public MutableLiveData<Boolean> getIsSynchronization() {
        if (isSynchronization == null) {
            isSynchronization = new MutableLiveData<>();
        }
        return isSynchronization;
    }

    public MutableLiveData<Boolean> getOnLogoutSuccess() {
        if (onLogoutSuccess == null) {
            onLogoutSuccess = new MutableLiveData<>();
        }
        return onLogoutSuccess;
    }

    public void getCategories(Context context) {
        dao.getCategories().subscribeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<CategoryModel>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull List<CategoryModel> list) {
                        if (list.size() == 0) {
                            if (NetworkUtils.getConnectivityStatus(getApplication().getApplicationContext())) {
                                getIsSynchronization().setValue(true);
                                Intent intent = new Intent(getApplication().getApplicationContext(), LoadCategoryService.class);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    getApplication().getApplicationContext().startForegroundService(intent);
                                } else {
                                    getApplication().getApplicationContext().startService(intent);

                                }
                                Log.e("categoryService", "started");

                            }

                        } else {
                            getProducts(context);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("catHomeMvvm", e.getMessage());
                        getProducts(context);
                        Toast.makeText(getApplication().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getProducts(Context context) {
        dao.getProducts().subscribeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
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
                                getIsSynchronization().setValue(true);
                                Intent intent = new Intent(getApplication().getApplicationContext(), LoadProductsService.class);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    getApplication().getApplicationContext().startForegroundService(intent);
                                } else {
                                    getApplication().getApplicationContext().startService(intent);

                                }
                                Log.e("itemsService", "started");

                            }


                        } else {
                            getLocalSalesInvoices(context);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("itmachomemvv", e.getMessage());
                        getLocalSalesInvoices(context);
                        Toast.makeText(getApplication().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void getLocalSalesInvoices(Context context) {
        dao.getSalesInvoices(false)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<InvoiceModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(List<InvoiceModel> invoices) {
                        Log.e("sales", invoices.size() + "");
                        if (invoices.size() == 0) {

                            if (NetworkUtils.getConnectivityStatus(getApplication().getApplicationContext())) {
                                Intent intent1 = new Intent(context, LoadSalesInvoiceService.class);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    context.startForegroundService(intent1);
                                } else {
                                    context.startService(intent1);

                                }
                            }

                        } else {
                            getIsSynchronization().setValue(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        getIsSynchronization().setValue(false);
                        Log.e("service_invoice", e.getMessage());
                    }
                });


    }

    public void syncData(Context context) {

        if (!isMyServiceRunning(context,AppSyncService.class)){
            Intent intent = new Intent(context, AppSyncService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);

            }
        }else {
            Toast.makeText(context, "Sync data have started", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean isMyServiceRunning(Context context,Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void prepareDataToLogout(Context context, UserModel userModel) {
        dao.getLocalProductsByOnlineId("0")
                .subscribeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<ProductModel>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull List<ProductModel> list) {
                        if (list.size() == 0) {
                            getUnUploadedInvoice(context, userModel);
                        } else {
                            String text = list.size() + " " + context.getString(R.string.items) + " " + context.getString(R.string.un_uploaded);
                            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        getUnUploadedInvoice(context, userModel);
                        Log.e("itmachomemvv", e.getMessage());

                    }
                });
    }

    private void getUnUploadedInvoice(Context context, UserModel userModel) {
        Log.e("qq","qqq");
        dao.getLocalInvoiceWithProducts("0", true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<InvoiceWithProductsModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(List<InvoiceWithProductsModel> list) {
                        if (list.size() > 0) {
                            String text = list.size() + " " + context.getString(R.string.invoices) + " " + context.getString(R.string.un_uploaded);
                            Toast.makeText(context, text, Toast.LENGTH_LONG).show();

                        } else {
                            logout(userModel, context);
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        logout(userModel, context);

                        Log.e("error", e.getMessage());
                    }
                });
    }

    private void logout(UserModel userModel, Context context) {
        Log.e("vv","vvv");
        ProgressDialog dialog = Common.createProgressDialog(context, context.getResources().getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.getBaseUrl(context)).logout(userModel.getData().getAccess_token())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Response<ResponseModel>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Response<ResponseModel> statusResponseResponse) {

                        Log.e("dsad",statusResponseResponse.body().getStatus()+"");

                        if (statusResponseResponse.isSuccessful()) {

                            if (statusResponseResponse.body().getStatus() == 200) {
                             dropCategoryTable(dialog);
                            }else if (statusResponseResponse.body().getStatus()==406){
                                dropCategoryTable(dialog);
                                dialog.dismiss();
                            }



                        } else {
                            try {
                                Log.e("error",statusResponseResponse.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable throwable) {
                        Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
    }

    private void dropCategoryTable( ProgressDialog dialog) {
        Log.e("yy","uuu");

        dao.dropCategoryTable()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        dropProductTable(dialog);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("error", e.getMessage());
                        dropProductTable(dialog);

                    }
                });
    }

    private void dropProductTable( ProgressDialog dialog) {
        Log.e("tt","yy");
        dao.dropProductTable()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        dropInvoiceTable(dialog);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("error", e.getMessage());
                        dropInvoiceTable(dialog);

                    }
                });
    }

    private void dropInvoiceTable( ProgressDialog dialog) {
        Log.e("rr","rr");
        dao.dropInvoiceTable()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        dropInvoiceCrossTable(dialog);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("error", e.getMessage());
                        dropInvoiceCrossTable(dialog);

                    }
                });
    }

    private void dropInvoiceCrossTable( ProgressDialog dialog) {
        Log.e("ddd","dd");
        dao.dropInvoiceCrossTable()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        getOnLogoutSuccess().setValue(true);
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        getOnLogoutSuccess().setValue(true);
                        Log.e("1","1");
                        Log.e("error", e.getMessage());
                        dialog.dismiss();

                    }
                });
    }

}
