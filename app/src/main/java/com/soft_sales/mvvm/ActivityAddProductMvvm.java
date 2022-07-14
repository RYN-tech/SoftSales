package com.soft_sales.mvvm;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.soft_sales.database.AppDatabase;
import com.soft_sales.database.DAO;
import com.soft_sales.database.LoadCategoryService;
import com.soft_sales.database.LoadProductsService;
import com.soft_sales.database.UploadProductService;
import com.soft_sales.model.AddProductModel;
import com.soft_sales.model.CategoryModel;
import com.soft_sales.share.NetworkUtils;

import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ActivityAddProductMvvm extends AndroidViewModel {
    private AppDatabase database;
    private DAO dao;
    private CompositeDisposable disposable = new CompositeDisposable();

    private MutableLiveData<List<CategoryModel>> onCategoryDataSuccess;


    public MutableLiveData<List<CategoryModel>> getOnCategoryDataSuccess(){
        if (onCategoryDataSuccess==null){
            onCategoryDataSuccess = new MutableLiveData<>();
        }

        return onCategoryDataSuccess;
    }
    public ActivityAddProductMvvm(@NonNull Application application) {
        super(application);
        database = AppDatabase.getInstance(application);
        dao = database.getDAO();
        getCategories();
    }


    public void getCategories() {
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
                        getOnCategoryDataSuccess().setValue(list);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("catHomeMvvm",e.getMessage());
                        Toast.makeText(getApplication().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void addProduct(AddProductModel addProductModel){
        Intent intent = new Intent(getApplication().getApplicationContext(), UploadProductService.class);
        intent.putExtra("data",addProductModel);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplication().getApplicationContext().startForegroundService(intent);
        }else {
            getApplication().getApplicationContext().startService(intent);

        }
    }
}
