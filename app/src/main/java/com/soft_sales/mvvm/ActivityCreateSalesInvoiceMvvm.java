package com.soft_sales.mvvm;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.soft_sales.database.AppDatabase;
import com.soft_sales.database.DAO;
import com.soft_sales.database.UploadInvoiceService;
import com.soft_sales.model.CategoryModel;
import com.soft_sales.model.CategoryWithProducts;
import com.soft_sales.model.InvoiceModel;
import com.soft_sales.model.ProductModel;
import com.soft_sales.model.UserModel;
import com.soft_sales.share.Common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ActivityCreateSalesInvoiceMvvm extends AndroidViewModel {
    private AppDatabase database;
    private DAO dao;
    private CompositeDisposable disposable = new CompositeDisposable();
    private MutableLiveData<List<CategoryWithProducts>> onCategorySuccess;
    private MutableLiveData<Boolean> canCreateInvoice;
    private MutableLiveData<List<ProductModel>> onCartSuccess;
    private MutableLiveData<InvoiceModel> invoiceModelSuccess;
    private MutableLiveData<String> customerName;
    private MutableLiveData<Integer> totalQty;
    private MutableLiveData<Double> totalPrice;
    private MutableLiveData<Double> VAT;
    private MutableLiveData<Double> totalProducts;
    private MutableLiveData<Double> totalAfterDiscount;
    private MutableLiveData<Double> discount;
    private MutableLiveData<Double> vatValue;
    private MutableLiveData<Double> totalAfterDiscountVat; // final total
    private MutableLiveData<String> selectedPaymentMethods;





    public ActivityCreateSalesInvoiceMvvm(@NonNull Application application) {
        super(application);
        database = AppDatabase.getInstance(application);
        dao = database.getDAO();
    }
    public MutableLiveData<List<CategoryWithProducts>> getOnCategorySuccess() {
        if (onCategorySuccess == null) {
            onCategorySuccess = new MutableLiveData<>();
        }
        return onCategorySuccess;
    }

    public MutableLiveData<String> getSelectedPaymentMethods() {
        if (selectedPaymentMethods == null) {
            selectedPaymentMethods = new MutableLiveData<>();

        }
        return selectedPaymentMethods;
    }
    public MutableLiveData<String> getCustomerName() {
        if (customerName == null) {
            customerName = new MutableLiveData<>();
        }
        return customerName;
    }

    public MutableLiveData<Boolean> getCanCreateInvoice() {
        if (canCreateInvoice == null) {
            canCreateInvoice = new MutableLiveData<>();
        }
        return canCreateInvoice;
    }

    public MutableLiveData<List<ProductModel>> getOnCartSuccess() {
        if (onCartSuccess == null) {
            onCartSuccess = new MutableLiveData<>();
            List<ProductModel> list = new ArrayList<>();
            onCartSuccess.setValue(list);
        }
        return onCartSuccess;
    }


    public MutableLiveData<InvoiceModel> getInvoiceModelSuccess() {
        if (invoiceModelSuccess == null) {
            invoiceModelSuccess = new MutableLiveData<>();
            invoiceModelSuccess.setValue(new InvoiceModel());

        }
        return invoiceModelSuccess;
    }

    public MutableLiveData<Integer> getTotalQty() {
        if (totalQty == null) {
            totalQty = new MutableLiveData<>();

        }
        return totalQty;
    }

    public MutableLiveData<Double> getTotalPrice() {
        if (totalPrice == null) {
            totalPrice = new MutableLiveData<>();

        }
        return totalPrice;
    }
    public MutableLiveData<Double> getVAT() {
        if (VAT == null) {
            VAT = new MutableLiveData<>();

        }
        return VAT;
    }

    public MutableLiveData<Double> getVATValue() {
        if (vatValue == null) {
            vatValue = new MutableLiveData<>();

        }
        return vatValue;
    }

    public MutableLiveData<Double> getDiscount() {
        if (discount == null) {
            discount = new MutableLiveData<>();
            discount.setValue(0.0);
        }
        return discount;
    }

    public MutableLiveData<Double> getTotalAfterDiscount() {
        if (totalAfterDiscount == null) {
            totalAfterDiscount = new MutableLiveData<>();

        }
        return totalAfterDiscount;
    }

    public MutableLiveData<Double> getTotalProducts() {
        if (totalProducts == null) {
            totalProducts = new MutableLiveData<>();

        }
        return totalProducts;
    }

    public MutableLiveData<Double> getTotalAfterDiscountVat() {
        if (totalAfterDiscountVat == null) {
            totalAfterDiscountVat = new MutableLiveData<>();

        }
        return totalAfterDiscountVat;
    }



    public void getCategories(Context context) {
        dao.getCategoriesWithProducts().subscribeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<CategoryWithProducts>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull List<CategoryWithProducts> list) {
                        Log.e("size",list.size()+"");
                        if (list.size()>0){
                            CategoryWithProducts categoryWithProducts = list.get(0);
                            CategoryModel categoryModel = categoryWithProducts.getCategoryModel();
                            categoryModel.setSelected(true);
                            categoryWithProducts.setCategoryModel(categoryModel);
                            list.set(0,categoryWithProducts);
                        }
                       getOnCategorySuccess().setValue(list);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        getOnCategorySuccess().setValue(new ArrayList<>());
                        Log.e("catInvoiceMvvm",e.getMessage());
                        Toast.makeText(getApplication().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void addCustomerToCart(String customerName) {
        if (getInvoiceModelSuccess().getValue() != null) {
            if (customerName!=null&&!customerName.isEmpty()) {
                 getInvoiceModelSuccess().getValue().setCustomer_name(customerName);

                if (getOnCartSuccess().getValue() != null && getOnCartSuccess().getValue().size() > 0) {
                    getCanCreateInvoice().setValue(true);
                } else {
                    getCanCreateInvoice().setValue(false);

                }
            } else {
                getInvoiceModelSuccess().getValue().setCustomer_name("");
                getCanCreateInvoice().setValue(false);

            }

        } else {
            getCanCreateInvoice().setValue(false);

        }
    }


    public void addItemToCart(ProductModel itemModel, UserModel userModel) {
        if (getOnCartSuccess().getValue() != null) {
            if (itemModel.getAmount()>0){
                int pos = getItemPos(itemModel.getProduct_local_id());
                if (getInvoiceModelSuccess().getValue() != null && getInvoiceModelSuccess().getValue().getCustomer_name()!=null&&!getInvoiceModelSuccess().getValue().getCustomer_name().isEmpty()) {
                    getCanCreateInvoice().setValue(true);

                }
                if (pos != -1) {

                    getOnCartSuccess().getValue().set(pos, itemModel);
                } else {

                    getOnCartSuccess().getValue().add(itemModel);
                }
                getOnCartSuccess().setValue(getOnCartSuccess().getValue());
                calculateTotalAmount(userModel);
            }else {
                if (itemModel.getAmount() == 0) {
                    deleteItem(itemModel,userModel);
                }
            }



        } else {

            if (getInvoiceModelSuccess().getValue() != null && getInvoiceModelSuccess().getValue().getCustomer_name()!=null&&!getInvoiceModelSuccess().getValue().getCustomer_name().isEmpty()) {
                getCanCreateInvoice().setValue(true);

            }
        }
    }

    public void deleteItem(ProductModel itemModel,UserModel userModel) {
        int pos = getItemPos(itemModel.getProduct_local_id());
        if (pos != -1 && getOnCartSuccess().getValue() != null) {

            getOnCartSuccess().getValue().remove(pos);
            getOnCartSuccess().setValue(getOnCartSuccess().getValue());
            calculateTotalAmount(userModel);
            if (getOnCartSuccess().getValue().size() > 0) {
                if (getInvoiceModelSuccess().getValue() != null && getInvoiceModelSuccess().getValue().getCustomer_name()!=null&&!getInvoiceModelSuccess().getValue().getCustomer_name().isEmpty()) {
                    getCanCreateInvoice().setValue(true);

                } else {
                    getCanCreateInvoice().setValue(false);

                }

            } else {
                getCanCreateInvoice().setValue(false);

            }
        }

    }

    public int getItemPos(long item_id) {
        if (getOnCartSuccess().getValue() != null) {
            for (int index = 0; index < getOnCartSuccess().getValue().size(); index++) {
                if (item_id == getOnCartSuccess().getValue().get(index).getProduct_local_id()) {
                    return index;
                }
            }
        }
        return -1;
    }

    public void calculateTotalAmount(UserModel userModel) {
        if (getOnCartSuccess().getValue() != null) {
            int amount = 0;
            double totalItemPrice = 0;
            double vat = 0;
            double discount = 0;
            double totalAfterDiscount = 0;
            double totalAfterDiscountVAT = 0;
            double totalProducts = 0;
            double tax_value = 0.0;
            double item_vat_value = 1;
            if (userModel.getData().getSetting().getTax_method().equals("inclusive")){
                item_vat_value = 1+userModel.getData().getSetting().getTax_val()/100;
            }
            double vat_value = 0.0;
            if (getVAT().getValue()!=null){
                vat =  getVAT().getValue();
            }

            if (getDiscount().getValue()!=null){
                discount = getDiscount().getValue();
            }
            for (ProductModel model : getOnCartSuccess().getValue()) {
                amount += model.getAmount();
                double item_price = (Double.parseDouble(model.getPrice())/item_vat_value);
                tax_value += Double.parseDouble(model.getPrice())*(userModel.getData().getSetting().getTax_val()/100);
                totalItemPrice += item_price*model.getAmount();
                //vat_value += item_price*model.getAmount()*(vat/100);
            }


            totalProducts = totalItemPrice;
            totalAfterDiscount= totalItemPrice-discount;

            vat_value = totalAfterDiscount*(vat/100);


            totalAfterDiscountVAT = totalAfterDiscount +vat_value;

            Log.e("totalbeforvat",totalProducts+"totalafterdisc"+totalAfterDiscount+"VatValue"+vat_value+"totalafterdiscvat"+totalAfterDiscountVAT);

            getVATValue().setValue(vat_value);
            getTotalPrice().setValue(Common.formatPriceFromDoubleToDouble(totalItemPrice));
            getTotalQty().setValue(amount);
            getTotalProducts().setValue(Common.formatPriceFromDoubleToDouble(totalProducts));
            getTotalAfterDiscount().setValue(Common.formatPriceFromDoubleToDouble(totalAfterDiscount));
            getTotalAfterDiscountVat().setValue(Common.formatPriceFromDoubleToDouble(totalAfterDiscountVAT));

        }
    }

    public void createInvoice(Context context,UserModel userModel){

        InvoiceModel invoiceModel = getInvoiceModelSuccess().getValue();
        invoiceModel.setInvoice_online_id("0");
        invoiceModel.setCustomer_name(getCustomerName().getValue());
        invoiceModel.setProducts(getOnCartSuccess().getValue());
        invoiceModel.setPay_type(getSelectedPaymentMethods().getValue());
        invoiceModel.setTax_method(userModel.getData().getSetting().getTax_method());
        invoiceModel.setTotal_after_tax(getTotalAfterDiscountVat().getValue());
        invoiceModel.setTotal_products(getTotalProducts().getValue());
        invoiceModel.setDiscount(getDiscount().getValue());
        invoiceModel.setTotal_after_discount(getTotalAfterDiscount().getValue());
        invoiceModel.setTax_value(String.valueOf(Common.formatPriceFromDoubleToDouble(getVATValue().getValue())));
        invoiceModel.setTotal(getTotalAfterDiscountVat().getValue());
        invoiceModel.setTax_per(String.valueOf(getVAT().getValue()));
        invoiceModel.setDate(String.valueOf(getDate()));
        invoiceModel.setIs_back(false);

        Intent intent = new Intent(context, UploadInvoiceService.class);
        intent.putExtra("data",invoiceModel);
        context.startService(intent);



    }

    private long getDate(){
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis();
    }


}
