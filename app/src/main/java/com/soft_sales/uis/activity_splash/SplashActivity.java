package com.soft_sales.uis.activity_splash;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.soft_sales.R;
import com.soft_sales.uis.activity_base.BaseActivity;
import com.soft_sales.uis.activity_base_url.BaseUrlActivity;
import com.soft_sales.uis.activity_home.HomeActivity;
import com.soft_sales.databinding.ActivitySplashBinding;
import com.soft_sales.uis.activity_login.LoginActivity;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SplashActivity extends BaseActivity {
    private ActivitySplashBinding binding;
    private CompositeDisposable disposable = new CompositeDisposable();
    private ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        initView();

    }

    private void initView() {

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode()==RESULT_OK){
                    navigateToLoginActivity();
                }
            }
        });
        Observable.timer(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull Long aLong) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        if (getBaseUrl()==null){
                            navigateToBaseUrlActivity();
                        }else {
                            if (getUserModel()==null){
                                navigateToLoginActivity();
                            }else {
                                navigateToHomeActivity();
                            }
                        }
                    }
                });


    }

    private void navigateToBaseUrlActivity() {
        Intent intent = new Intent(this, BaseUrlActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        launcher.launch(intent);
    }


    private void navigateToHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    private void navigateToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}

