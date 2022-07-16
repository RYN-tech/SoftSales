package com.soft_sales.mvvm;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.soft_sales.R;
import com.soft_sales.model.AlarmModel;
import com.soft_sales.model.LoginModel;
import com.soft_sales.model.SettingDataModel;
import com.soft_sales.model.SettingModel;
import com.soft_sales.model.UserModel;
import com.soft_sales.remote.Api;
import com.soft_sales.share.Common;
import com.soft_sales.tags.Tags;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.ByteArrayOutputStream;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class ActivityLoginMvvm extends AndroidViewModel {
    private CompositeDisposable disposable = new CompositeDisposable();
    private MutableLiveData<UserModel> onDataSuccess;

    public ActivityLoginMvvm(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<UserModel> getOnDataSuccess() {
        if (onDataSuccess == null) {
            onDataSuccess = new MutableLiveData<>();
        }
        return onDataSuccess;
    }

    public void Login(Context context, LoginModel model, String lang) {
        ProgressDialog dialog = Common.createProgressDialog(context, context.getString(R.string.logging));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.getBaseUrl(context))
                .login(lang, model.getUserName(), model.getPassword())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Response<UserModel>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Response<UserModel> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().getStatus() == 200) {
                                getUserSettings(dialog, context, response.body());
                            } else if (response.body().getStatus() == 400) {
                                dialog.dismiss();
                                Toast.makeText(context, R.string.inv_cred, Toast.LENGTH_LONG).show();
                            } else if (response.body().getStatus() == 410) {
                                dialog.dismiss();

                                Toast.makeText(context, R.string.account_used, Toast.LENGTH_LONG).show();

                            }
                        } else if (response.code() == 402) {
                            dialog.dismiss();

                            Toast.makeText(context, R.string.account_blocked, Toast.LENGTH_LONG).show();
                        } else if (response.code() == 410) {
                            dialog.dismiss();

                            Toast.makeText(context, R.string.package_finished, Toast.LENGTH_LONG).show();

                        } else if (response.code() == 411) {
                            dialog.dismiss();

                            Toast.makeText(context, R.string.domain_invalid, Toast.LENGTH_LONG).show();

                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
    }

    private void getUserSettings(ProgressDialog dialog, Context context, UserModel model) {
        Api.getService(Tags.getBaseUrl(context))
                .getSetting(model.getData().getAccess_token())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Response<SettingDataModel>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Response<SettingDataModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().getStatus() == 200) {
                                model.getData().setSetting(response.body().getData());
                                convertSettingLogo(context,model);
                            }
                        } else if (response.code() == 402) {
                            dialog.dismiss();

                            Toast.makeText(context, R.string.account_blocked, Toast.LENGTH_LONG).show();
                        } else if (response.code() == 410) {                                dialog.dismiss();
                            dialog.dismiss();


                            Toast.makeText(context, R.string.package_finished, Toast.LENGTH_LONG).show();

                        } else if (response.code() == 411) {
                            dialog.dismiss();

                            Toast.makeText(context, R.string.domain_invalid, Toast.LENGTH_LONG).show();

                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (e.getMessage() != null && e.getMessage().contains("Unable to resolve host")) {
                            Toast.makeText(context, R.string.ch_net_con, Toast.LENGTH_SHORT).show();
                        } else if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                            Toast.makeText(context, R.string.ch_net_con, Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();

                        }

                        dialog.dismiss();
                    }
                });
    }

    private void convertSettingLogo(Context context,UserModel model) {
        Glide.with(context)
                .asBitmap()
                .load(Uri.parse(Tags.getBaseUrl(context)+model.getData().getSetting().getLogo()))
                .into(new SimpleTarget<Bitmap>() {

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        AlarmModel alarmModel= new AlarmModel();
                        alarmModel.schedule(context.getApplicationContext());

                        getOnDataSuccess().setValue(model);

                    }

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        resource.compress(Bitmap.CompressFormat.PNG,100,outputStream);
                        UserModel.Data data = model.getData();
                        SettingModel settingModel = data.getSetting();
                        settingModel.setImageBitmap(outputStream.toByteArray());
                        data.setSetting(settingModel);
                        getOnDataSuccess().setValue(model);

                        AlarmModel alarmModel= new AlarmModel();
                        alarmModel.schedule(context.getApplicationContext());



                    }
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
