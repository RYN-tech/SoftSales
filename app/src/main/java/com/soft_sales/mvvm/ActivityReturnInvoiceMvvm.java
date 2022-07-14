package com.soft_sales.mvvm;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.soft_sales.database.AppDatabase;
import com.soft_sales.database.DAO;
import com.soft_sales.database.UploadSingleInvoiceService;
import com.soft_sales.database.UploadSingleReturnInvoiceService;
import com.soft_sales.model.InvoiceCrossModel;
import com.soft_sales.model.InvoiceModel;
import com.soft_sales.model.InvoiceWithProductsModel;
import com.soft_sales.model.ProductModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ActivityReturnInvoiceMvvm extends AndroidViewModel {
    private MutableLiveData<InvoiceModel> onSingleInvoiceSuccess;
    private AppDatabase database;
    private DAO dao;
    private CompositeDisposable disposable = new CompositeDisposable();
    private InvoiceModel invoiceModel;
    private InvoiceWithProductsModel invoiceWithProductsModel;
    private List<ProductModel> products = new ArrayList<>();

    public ActivityReturnInvoiceMvvm(@NonNull Application application) {
        super(application);
        database = AppDatabase.getInstance(application);
        dao = database.getDAO();
    }



    public MutableLiveData<InvoiceModel> getOnSingleInvoiceSuccess() {
        if (onSingleInvoiceSuccess == null) {
            onSingleInvoiceSuccess = new MutableLiveData<>();
        }
        return onSingleInvoiceSuccess;
    }


    public void getInvoice(String code) {

        dao.getInvoiceWithProductsByCode(code,false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<InvoiceWithProductsModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(InvoiceWithProductsModel model) {
                        invoiceWithProductsModel = model;
                        invoiceModel = invoiceWithProductsModel.getInvoiceModel();
                        products.clear();
                        getProductAmount(0);

                    }

                    @Override
                    public void onError(Throwable e) {
                        getOnSingleInvoiceSuccess().setValue(null);
                        Log.e("error", e.getMessage());
                    }
                });
    }

    private void getProductAmount(int product_index) {
        if (product_index < invoiceWithProductsModel.getProducts().size()) {
            ProductModel productModel = invoiceWithProductsModel.getProducts().get(product_index);
            dao.getProductWithAmount(invoiceWithProductsModel.getInvoiceModel().getInvoice_local_id(), productModel.getProduct_local_id())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<InvoiceCrossModel>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            disposable.add(d);
                        }

                        @Override
                        public void onSuccess(InvoiceCrossModel invoiceCrossModel) {
                            productModel.setInvoiceCrossModel(invoiceCrossModel);
                            products.add(productModel);
                            int index = product_index + 1;
                            getProductAmount(index);


                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        } else {

            invoiceModel.setProducts(products);
            getOnSingleInvoiceSuccess().setValue(invoiceModel);
        }
    }

    public void createReturnInvoice(Context context,InvoiceModel invoiceModel){
        Intent intent = new Intent(context, UploadSingleReturnInvoiceService.class);
        intent.putExtra("data",invoiceModel);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);

        }

    }

}
