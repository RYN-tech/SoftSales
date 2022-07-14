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
import android.os.Environment;
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
import com.soft_sales.broad_cast_receiver.BroadCastCancelCategoryNotification;
import com.soft_sales.model.CategoryDataModel;
import com.soft_sales.model.CategoryModel;
import com.soft_sales.model.CreateOnlineInvoice;
import com.soft_sales.model.EventsModel;
import com.soft_sales.model.InvoiceCrossModel;
import com.soft_sales.model.InvoiceModel;
import com.soft_sales.model.InvoiceWithProductsModel;
import com.soft_sales.model.ProductDataModel;
import com.soft_sales.model.ProductModel;
import com.soft_sales.model.SettingDataModel;
import com.soft_sales.model.SettingModel;
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

import java.io.ByteArrayOutputStream;
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

public class AppSyncService extends Service {
    private AppDatabase appDatabase;
    private DAO dao;
    private NotificationManager manager;
    private CompositeDisposable disposable = new CompositeDisposable();
    private Preferences preferences;
    private UserModel userModel;
    private Context context;
    private String imagePath = "";
    private List<ProductModel> products = new ArrayList<>();
    private List<ProductModel> items = new ArrayList<>();
    private List<ProductModel> onlineProducts = new ArrayList<>();
    private List<CategoryModel> onlineCategories = new ArrayList<>();

    private List<InvoiceModel> invoices = new ArrayList<>();
    private List<InvoiceWithProductsModel> invoiceWithProductsModelList = new ArrayList<>();
    private InvoiceWithProductsModel invoiceWithProductsModel;

    @Override
    public void onCreate() {
        super.onCreate();
        appDatabase = AppDatabase.getInstance(getApplication());
        dao = appDatabase.getDAO();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        Log.e("app","sync updated");

        if (NetworkUtils.getConnectivityStatus(context)) {
            createNotification(getString(R.string.sync_data));

            getLocalProducts();
        } else {
            stopSelf();
        }
        return START_STICKY;
    }

    private void getLocalProducts() {
        dao.getLocalProductsByOnlineId("0")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<ProductModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(List<ProductModel> list) {
                        products = new ArrayList<>();
                        products.addAll(list);
                        insertOnlineProduct(0);

                    }

                    @Override
                    public void onError(Throwable e) {
                        getUnUploadedInvoices();
                    }
                });
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
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("err", e.getMessage());
        }

        return imageFile;

    }


    private void insertOnlineProduct(int index) {
        imagePath = "";
        if (index < products.size()) {
            ProductModel productModel = products.get(index);
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
            if (imagePath != null && !imagePath.isEmpty()) {
                photo = Common.getMultiPartFromPath(imagePath, "photo");

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
                                    productModel.setId(response.body().getData().getId());
                                    updateProductLocal(productModel, index);
                                } else {
                                    int product_index = index + 1;

                                    insertOnlineProduct(product_index);


                                }
                            } else {
                                int product_index = index + 1;
                                insertOnlineProduct(product_index);

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
                            int product_index = index + 1;
                            insertOnlineProduct(product_index);
                            Log.e("err_items", e.getMessage());

                        }
                    });

        } else {
            getUnUploadedInvoices();
        }


    }


    private void updateProductLocal(ProductModel productModel, int index) {

        Log.e("online_id",productModel.getId()+"__");

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
                        dao.updateProductOnlineIdInCrossTable(productModel.getProduct_local_id(), productModel.getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new CompletableObserver() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        disposable.add(d);
                                    }

                                    @Override
                                    public void onComplete() {
                                        int product_index = index + 1;

                                        insertOnlineProduct(product_index);


                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        int product_index = index + 1;

                                        insertOnlineProduct(product_index);
                                        Log.e("updateLocalProduct", e.getMessage());
                                    }
                                });


                    }

                    @Override
                    public void onError(Throwable e) {
                        int product_index = index + 1;

                        insertOnlineProduct(product_index);
                        Log.e("updateLocalProduct", e.getMessage());
                    }
                });

    }

    private void getUnUploadedInvoices() {
        getInvoiceWithProducts();
    }

    public void getInvoiceWithProducts() {

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
                        invoiceWithProductsModelList = list;
                        Log.e("unUploaded_invoices",list.size()+"");
                        invoices.clear();
                        prepareData(0);

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("error", e.getMessage());
                    }
                });
    }

    private void prepareData(int i) {
        if (i < invoiceWithProductsModelList.size()) {
            invoiceWithProductsModel = invoiceWithProductsModelList.get(i);
            items = new ArrayList<>();
            getProductAmount(0, i);
        } else {
            for (InvoiceModel model:invoices){
                Log.e("invoice_id",model.getInvoice_local_id()+"");
                for (ProductModel productModel:model.getProducts()){
                    Log.e("name",productModel.getTitle_ar()+"__"+productModel.getInvoiceCrossModel().getProduct_price()+"__"+productModel.getInvoiceCrossModel().getProduct_amount());
                }
            }
            uploadInvoices(0);
        }
    }

    private void getProductAmount(int product_index, int invoice_index) {
        Log.e("ssszzzzz",invoiceWithProductsModel.getProducts().size()+"__");
        if (product_index < invoiceWithProductsModel.getProducts().size()) {
            ProductModel productModel = invoiceWithProductsModel.getProducts().get(product_index);
            Log.e("pro_title",productModel.getTitle_ar());

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
                            Log.e("mmmm",productModel.getTitle_ar());
                            items.add(productModel);
                            int index = product_index + 1;
                            getProductAmount(index, invoice_index);


                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        } else {
            InvoiceModel invoiceModel = invoiceWithProductsModel.getInvoiceModel();
            invoiceModel.setProducts(items);
            invoices.add(invoiceModel);
            items = new ArrayList<>();
            int newInvoiceIndex = invoice_index + 1;
            prepareData(newInvoiceIndex);

        }
    }


    private void uploadInvoices(int index) {
        if (index < invoices.size()) {
            InvoiceModel invoiceModel = invoices.get(index);

            if (invoiceModel.getInvoice_online_id().equals("0")) {
                insertInvoiceOnline(invoiceModel, index);
            } else if (invoiceModel.isUpdated()) {
                createReturnInvoiceOnline(invoiceModel, index);
            } else {
                int newIndex = index + 1;
                uploadInvoices(newIndex);
            }
        } else {
            getOnlineProduct(1);
        }
    }


    private void insertInvoiceOnline(InvoiceModel invoiceModel, int invoice_index) {



        List<CreateOnlineInvoice.Details> products = new ArrayList<>();
        for (ProductModel item : invoiceModel.getProducts()) {
            Log.e("name",item.getTitle_ar()+"__"+item.getId());
            double total = item.getInvoiceCrossModel().getProduct_amount() * item.getInvoiceCrossModel().getProduct_price();
            products.add(new CreateOnlineInvoice.Details(item.getId(), item.getInvoiceCrossModel().getProduct_price() + "", item.getInvoiceCrossModel().getProduct_amount() + "", total + ""));
        }
        CreateOnlineInvoice createOnlineInvoice = new CreateOnlineInvoice(invoiceModel.getCustomer_name(), invoiceModel.getCode() + "", invoiceModel.getTotal_products() + "", invoiceModel.getTotal_after_discount() + "", invoiceModel.getTotal_after_tax() + "", invoiceModel.getTotal() + "", invoiceModel.getDiscount() + "", invoiceModel.getTax_value(), invoiceModel.getTax_method(), invoiceModel.getTax_per(), invoiceModel.getDate(), invoiceModel.isIs_back(), invoiceModel.getPay_type(), products);

        Api.getService(Tags.getBaseUrl(context))
                .createInvoice(userModel.getData().getAccess_token(), createOnlineInvoice)
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
                                                    updateInvoiceCrossTable(invoiceModel, invoice_index);

                                                }

                                                @Override
                                                public void onError(@NonNull Throwable e) {
                                                    int newInvoiceIndex = invoice_index + 1;
                                                    uploadInvoices(newInvoiceIndex);
                                                }
                                            });


                                } else {
                                    Log.e("error", response.body().getStatus() + "__");
                                    Log.e("error", response.body().getMessage().toString() + "__");

                                    int newInvoiceIndex = invoice_index + 1;
                                    uploadInvoices(newInvoiceIndex);


                                }
                            }
                        } else {
                            try {
                                Log.e("err", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            int newInvoiceIndex = invoice_index + 1;
                            uploadInvoices(newInvoiceIndex);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("err_item", e.getMessage());
                        int newInvoiceIndex = invoice_index + 1;
                        uploadInvoices(newInvoiceIndex);

                    }
                });
    }

    private void updateInvoiceCrossTable(InvoiceModel invoiceModel, int invoice_index) {
        dao.updateInvoiceOnlineIdInCrossTable(invoiceModel.getInvoice_local_id(), invoiceModel.getInvoice_online_id())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        int newInvoiceIndex = invoice_index + 1;
                        uploadInvoices(newInvoiceIndex);

                        Log.e("local", "updated");

                    }

                    @Override
                    public void onError(Throwable e) {
                        int newInvoiceIndex = invoice_index + 1;
                        uploadInvoices(newInvoiceIndex);

                        Log.e("updateLocalProduct", e.getMessage());
                    }
                });
    }

    private void createReturnInvoiceOnline(InvoiceModel invoiceModel, int invoice_index) {

        Api.getService(Tags.getBaseUrl(context))
                .createReturnInvoice(userModel.getData().getAccess_token(), invoiceModel.getInvoice_online_id())
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
                                                    int newInvoiceIndex = invoice_index + 1;
                                                    uploadInvoices(newInvoiceIndex);
                                                }

                                                @Override
                                                public void onError(@NonNull Throwable e) {
                                                    int newInvoiceIndex = invoice_index + 1;
                                                    uploadInvoices(newInvoiceIndex);
                                                }
                                            });


                                } else {
                                    Log.e("error", response.body().getStatus() + "__");
                                    Log.e("error", response.body().getMessage().toString() + "__");

                                    int newInvoiceIndex = invoice_index + 1;
                                    uploadInvoices(newInvoiceIndex);

                                }
                            }
                        } else {
                            int newInvoiceIndex = invoice_index + 1;
                            uploadInvoices(newInvoiceIndex);
                            try {
                                Log.e("err", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("err_item", e.getMessage());
                        int newInvoiceIndex = invoice_index + 1;
                        uploadInvoices(newInvoiceIndex);

                    }
                });
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    private void getOnlineProduct(int page) {
        Log.e("pageItems", page + "");

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
                                    onlineProducts = response.body().getData().getData();

                                    prepareOnlineProductData(0, page);
                                } else {
                                    getOnlineCategory(1);

                                }
                            } else {
                                getOnlineCategory(1);

                                Log.e("err_item", response.body().getStatus() + "_" + response.body().getMessage().toString());

                            }
                        } else {
                            getOnlineCategory(1);
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
                        getOnlineCategory(1);
                        Log.e("err_items", e.getMessage());

                    }
                });
    }

    private void prepareOnlineProductData(int product_index, int page) {
        if (product_index < onlineProducts.size()) {
            ProductModel productModel = onlineProducts.get(product_index);
            Log.e("price",productModel.getPrice());
            dao.getLocalProductByOnlineId(productModel.getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<ProductModel>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            disposable.add(d);
                        }

                        @Override
                        public void onSuccess(ProductModel model) {
                            if (productModel.getPhoto() != null && !productModel.getPhoto().isEmpty()) {

                                Glide.with(context)
                                        .asBitmap()
                                        .load(Uri.parse(Tags.getBaseUrl(context) + productModel.getPhoto()))
                                        .into(new SimpleTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                                                Log.e("load", "image");
                                                int nh = (int) (resource.getHeight() * (512.0 / resource.getWidth()));
                                                Bitmap bitmap = Bitmap.createScaledBitmap(resource, 512, nh, true);
                                                model.setLocal_image(Common.getByteArray(bitmap));
                                                model.setTitle_ar(productModel.getTitle_ar());
                                                model.setTitle_en(productModel.getTitle_en());
                                                model.setAdded_by_id(productModel.getAdded_by_id());
                                                model.setPrice(productModel.getPrice());
                                                model.setCategory_id(productModel.getCategory_id());
                                                dao.updateProduct(model)
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(new CompletableObserver() {
                                                            @Override
                                                            public void onSubscribe(Disposable d) {
                                                                disposable.add(d);
                                                            }

                                                            @Override
                                                            public void onComplete() {
                                                                int newIndex = product_index + 1;
                                                                prepareOnlineProductData(newIndex, page);
                                                            }

                                                            @Override
                                                            public void onError(Throwable e) {
                                                                int newIndex = product_index + 1;
                                                                prepareOnlineProductData(newIndex, page);
                                                            }
                                                        });

                                            }

                                            @Override
                                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                                model.setTitle_ar(productModel.getTitle_ar());
                                                model.setTitle_en(productModel.getTitle_en());
                                                model.setAdded_by_id(productModel.getAdded_by_id());
                                                model.setPrice(productModel.getPrice());
                                                model.setCategory_id(productModel.getCategory_id());
                                                dao.updateProduct(model)
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(new CompletableObserver() {
                                                            @Override
                                                            public void onSubscribe(Disposable d) {
                                                                disposable.add(d);
                                                            }

                                                            @Override
                                                            public void onComplete() {
                                                                int newIndex = product_index + 1;
                                                                prepareOnlineProductData(newIndex, page);
                                                            }

                                                            @Override
                                                            public void onError(Throwable e) {
                                                                int newIndex = product_index + 1;
                                                                prepareOnlineProductData(newIndex, page);
                                                            }
                                                        });
                                            }
                                        });
                            } else {
                                model.setTitle_ar(productModel.getTitle_ar());
                                model.setTitle_en(productModel.getTitle_en());
                                model.setAdded_by_id(productModel.getAdded_by_id());
                                model.setPrice(productModel.getPrice());
                                model.setCategory_id(productModel.getCategory_id());
                                dao.updateProduct(model)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new CompletableObserver() {
                                            @Override
                                            public void onSubscribe(Disposable d) {
                                                disposable.add(d);
                                            }

                                            @Override
                                            public void onComplete() {
                                                int newIndex = product_index + 1;
                                                prepareOnlineProductData(newIndex, page);
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                int newIndex = product_index + 1;
                                                prepareOnlineProductData(newIndex, page);
                                            }
                                        });


                            }




                        }

                        @Override
                        public void onError(Throwable e) {

                            downLoadImage(productModel, product_index, page);
                            Log.e("productOnlineID", e.getMessage());
                        }
                    });

        } else {
            int newPage = page + 1;
            getOnlineProduct(newPage);
        }
    }


    private void downLoadImage(ProductModel model, int product_index, int page) {
        if (model.getPhoto() != null && !model.getPhoto().isEmpty()) {

            Glide.with(this)
                    .asBitmap()
                    .load(Uri.parse(Tags.getBaseUrl(context) + model.getPhoto()))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                            Log.e("load", "image");
                            int nh = (int) (resource.getHeight() * (512.0 / resource.getWidth()));
                            Bitmap bitmap = Bitmap.createScaledBitmap(resource, 512, nh, true);
                            model.setLocal_image(Common.getByteArray(bitmap));
                            dao.insertProduct(model)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Long>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {
                                            disposable.add(d);
                                        }

                                        @Override
                                        public void onSuccess(Long aLong) {
                                            Log.e("local", "inserted");
                                            int newIndex = product_index + 1;
                                            prepareOnlineProductData(newIndex, page);
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            int newIndex = product_index + 1;
                                            prepareOnlineProductData(newIndex, page);
                                            Log.e("insertProduct", e.getMessage());
                                        }
                                    });

                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            int newIndex = product_index + 1;
                            prepareOnlineProductData(newIndex, page);
                        }
                    });
        } else {
            int newIndex = product_index + 1;
            prepareOnlineProductData(newIndex, page);
        }


    }

    ///////////////////////////////////////////////////////////////////////////////

    private void getOnlineCategory(int page) {
        Log.e("categoryPage", page + "");
        Api.getService(Tags.getBaseUrl(this))
                .getDepartments(userModel.getData().getAccess_token(), page)
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
                                    if (response.body().getData().getData().size() > 0) {
                                        onlineCategories = response.body().getData().getData();
                                        prepareOnlineCategory(0, page);

                                    } else {
                                        getUserData();
                                    }
                                } else {
                                    getUserData();
                                }
                            } else {
                                getUserData();
                            }
                        } else {
                            if (response.code() == 402) {
                                Toast.makeText(context, R.string.account_blocked, Toast.LENGTH_LONG).show();
                            } else if (response.code() == 410) {
                                Toast.makeText(context, R.string.package_finished, Toast.LENGTH_LONG).show();

                            } else if (response.code() == 411) {
                                Toast.makeText(context, R.string.domain_invalid, Toast.LENGTH_LONG).show();

                            }
                            getUserData();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("error", e.getMessage());
                        getUserData();

                    }
                });

    }


    private void prepareOnlineCategory(int index, int page) {
        Log.e("cat_size",onlineCategories.size()+"___"+index);
        if (index < onlineCategories.size()) {

            CategoryModel categoryModel = onlineCategories.get(index);
            dao.getCategoryByOnlineId(categoryModel.getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<CategoryModel>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            disposable.add(d);
                        }

                        @Override
                        public void onSuccess(CategoryModel model) {
                            Log.e("ssdsf","sdfsd");
                            model.setTitle_ar(categoryModel.getTitle_ar());
                            model.setTitle_en(categoryModel.getTitle_ar());
                            dao.updateCategory(model)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new CompletableObserver() {
                                        @Override
                                        public void onSubscribe(Disposable d) {
                                            disposable.add(d);
                                        }

                                        @Override
                                        public void onComplete() {
                                            int newIndex = index + 1;
                                            prepareOnlineCategory(newIndex, page);
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            int newIndex = index + 1;
                                            prepareOnlineCategory(newIndex, page);
                                        }
                                    });

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("eeeee",e.getMessage());
                            dao.insertCategory(categoryModel)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new CompletableObserver() {
                                        @Override
                                        public void onSubscribe(Disposable d) {
                                            disposable.add(d);
                                        }

                                        @Override
                                        public void onComplete() {
                                            int newIndex = index + 1;
                                            prepareOnlineCategory(newIndex, page);
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            int newIndex = index + 1;
                                            prepareOnlineCategory(newIndex, page);
                                        }
                                    });
                        }
                    });
        } else {
            int newPage = page + 1;
            getOnlineCategory(newPage);
        }
    }

    /////////////////////////////////////////////////
    private void getUserData() {
        Log.e("data","user_data");
        Api.getService(Tags.getBaseUrl(context))
                .getProfile(userModel.getData().getAccess_token())
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
                                Log.e("sdas","sada");
                                convertSettingLogo(context, response.body());
                            }
                        } else if (response.code() == 402) {
                            stopSelf();
                            Toast.makeText(context, R.string.account_blocked, Toast.LENGTH_LONG).show();
                        } else if (response.code() == 410) {
                            stopSelf();


                            Toast.makeText(context, R.string.package_finished, Toast.LENGTH_LONG).show();

                        } else if (response.code() == 411) {
                            stopSelf();

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

                        stopSelf();
                    }
                });
    }

    private void convertSettingLogo(Context context, UserModel model) {
        Glide.with(context)
                .asBitmap()
                .load(Uri.parse(Tags.getBaseUrl(context) + model.getData().getSetting().getLogo()))
                .into(new SimpleTarget<Bitmap>() {

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        preferences.createUpdateUserData(context, model);
                        EventBus.getDefault().post(model);
                        stopSelf();


                    }

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        resource.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        UserModel.Data data = model.getData();
                        SettingModel settingModel = data.getSetting();
                        settingModel.setImageBitmap(outputStream.toByteArray());
                        data.setSetting(settingModel);
                        preferences.createUpdateUserData(context, model);
                        EventBus.getDefault().post(model);

                        stopSelf();


                    }
                });
    }

    private void createNotification(String content) {
        Intent cancelIntent = new Intent(this, BroadCastCancelCategoryNotification.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, App.CHANNEL_ID_SOFT_APP);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setChannelId(App.CHANNEL_ID_SOFT_APP);
        builder.setContentTitle(getString(R.string.sync_data));
        builder.setContentText(content);
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.addAction(0, context.getString(R.string.dismiss), cancelPendingIntent);
        builder.setProgress(100, 100, true);
        builder.setContentText(context.getString(R.string.downloading));
        builder.setCategory(Notification.CATEGORY_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(Tags.not_soft_app_id, builder.build());

        } else {
            manager.notify(Tags.not_tag_soft_app, Tags.not_soft_app_id, builder.build());

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
            manager.cancel(Tags.not_tag_soft_app, Tags.not_soft_app_id);
        }

    }


}
