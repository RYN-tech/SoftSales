package com.soft_sales.uis.activity_create_sales_invoice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.soft_sales.R;
import com.soft_sales.adapter.CategoriesAdapter;
import com.soft_sales.adapter.InvoiceSalesSelectedItemAdapter;
import com.soft_sales.adapter.ProductsAdapter;
import com.soft_sales.databinding.ActivityCreateSalesInvoiceBinding;
import com.soft_sales.databinding.DialogDiscountErrorBinding;
import com.soft_sales.databinding.DialogDiscountLayoutBinding;
import com.soft_sales.model.CategoryWithProducts;
import com.soft_sales.model.InvoiceModel;
import com.soft_sales.model.ProductModel;
import com.soft_sales.mvvm.ActivityCreateSalesInvoiceMvvm;
import com.soft_sales.share.Common;
import com.soft_sales.uis.activity_base.BaseActivity;
import com.soft_sales.uis.activity_print_invoice.PrintInvoiceActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class CreateSalesInvoiceActivity extends BaseActivity {
    private ActivityCreateSalesInvoiceMvvm mvvm;
    private ActivityCreateSalesInvoiceBinding binding;
    private CategoriesAdapter categoriesAdapter;
    private ProductsAdapter adapter;
    private InvoiceSalesSelectedItemAdapter itemAdapter;

    private int selectedItemPos = -1;
    private ProductModel selectedItemModel;
    private double amount = 0.0;
    private double discount;
    private BottomSheetBehavior behavior,behaviorItems;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_sales_invoice);
        initView();

    }

    private void initView() {
        setUpToolbar(binding.toolbar, getString(R.string.create_sale_invoice), R.color.white, R.color.black);
        binding.setUserModel(getUserModel());
        mvvm = ViewModelProviders.of(this).get(ActivityCreateSalesInvoiceMvvm.class);
        mvvm.getSelectedPaymentMethods().setValue("1");
        mvvm.getVAT().setValue(getUserModel().getData().getSetting().getTax_val());
        mvvm.getCanCreateInvoice().observe(this, aBoolean -> {
            binding.setCanCreateInvoice(aBoolean);
        });

        mvvm.getOnCategorySuccess().observe(this, list -> {
            if (list.size() > 0) {
                if (adapter != null) {
                    adapter.updateList(list.get(0).getProducts());
                }
            }
            if (categoriesAdapter != null) {
                categoriesAdapter.updateList(list);
            }
        });

        mvvm.getOnCartSuccess().observe(this, listOfSelectedItems -> {
            if (listOfSelectedItems != null) {
                if (listOfSelectedItems.size() > 0) {
                    binding.llItems.setVisibility(View.VISIBLE);


                } else {
                    binding.llItems.setVisibility(View.GONE);

                }
                if (itemAdapter != null) {
                    itemAdapter.updateList(listOfSelectedItems);
                }
            }
        });


        behavior = BottomSheetBehavior.from(binding.bottomSheet.llSheet);
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        behaviorItems = BottomSheetBehavior.from(binding.itemsBottomSheet.llSheet);
        behaviorItems.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        binding.bottomSheet.closeSheet.setOnClickListener(view -> {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        });

        itemAdapter = new InvoiceSalesSelectedItemAdapter(this, getLang());
        binding.itemsBottomSheet.recView.setLayoutManager(new GridLayoutManager(this, 3));
        binding.itemsBottomSheet.recView.setAdapter(itemAdapter);


        binding.recViewCategory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recViewCategory.setHasFixedSize(true);
        categoriesAdapter = new CategoriesAdapter(this, getLang());
        binding.recViewCategory.setAdapter(categoriesAdapter);

        binding.recView.setLayoutManager(new GridLayoutManager(this, 3));
        binding.recView.setHasFixedSize(true);
        adapter = new ProductsAdapter(this, getLang());
        binding.recView.setAdapter(adapter);


        binding.llCheckbox.setOnClickListener(view -> createDiscountDialog());
        binding.imageCheck.setOnClickListener(view -> {
            if (discount > 0) {
                discount = 0;
                mvvm.getDiscount().setValue(discount);
                mvvm.calculateTotalAmount(getUserModel());
            }

            binding.setIsChecked(discount > 0);
        });

        binding.cardViewDelete.setOnClickListener(view -> {
            binding.setCounter("0");
            amount = 0;
            selectedItemModel.setAmount(0);
            mvvm.addItemToCart(selectedItemModel, getUserModel());
            binding.setModel(selectedItemModel);
        });

        binding.imageIncrease.setOnClickListener(view -> {
            if (Double.parseDouble(selectedItemModel.getPrice()) > 0) {
                amount++;
                binding.setCounter(String.valueOf(amount));
                if (selectedItemModel != null) {
                    selectedItemModel.setAmount(amount);
                    //mvvm.addItemToCart(selectedItemModel, getUserModel());
                    binding.setModel(selectedItemModel);
                }
            } else {
                Toast.makeText(this, R.string.item_price_greater_zero, Toast.LENGTH_SHORT).show();
            }


        });
        binding.imageDecrease.setOnClickListener(view -> {
            if (Double.parseDouble(selectedItemModel.getPrice()) > 0) {
                if (amount > 0) {
                    amount--;
                    binding.setCounter(String.valueOf(amount));
                    if (selectedItemModel != null) {
                        selectedItemModel.setAmount(amount);
                        //mvvm.addItemToCart(selectedItemModel, getUserModel());
                        binding.setModel(selectedItemModel);

                    }
                }
            } else {
                Toast.makeText(this, R.string.item_price_greater_zero, Toast.LENGTH_SHORT).show();

            }


        });
        binding.imageEdit.setOnClickListener(view -> {
            if (binding.edtPrice.isEnabled()) {
                if (!binding.edtPrice.getText().toString().isEmpty()) {
                    double newItemPrice = Double.parseDouble(binding.edtPrice.getText().toString());
                    if (newItemPrice > 0) {
                        if (selectedItemModel != null) {
                            selectedItemModel.setAmount(amount);
                            selectedItemModel.setPrice(String.valueOf(newItemPrice));
                            // mvvm.addItemToCart(selectedItemModel, getUserModel());
                            binding.edtPrice.setEnabled(false);
                            Common.CloseKeyBoard(this, binding.edtPrice);
                            binding.imageEdit.setImageResource(R.drawable.ic_edit2);
                            binding.edtPrice.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
                        }
                    } else {
                        Toast.makeText(this, R.string.item_price_greater_zero, Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(this, R.string.enter_item_price, Toast.LENGTH_SHORT).show();
                }

            } else {
                binding.imageEdit.setImageResource(R.drawable.ic_done);
                binding.edtPrice.setEnabled(true);
                binding.edtPrice.requestFocus();
                Common.OpenKeyBoard(this, binding.edtPrice);
                binding.edtPrice.setBackgroundColor(0);


            }
        });
        binding.btnCreateInvoice.setOnClickListener(view -> {
            if (mvvm.getDiscount().getValue() != null && mvvm.getTotalPrice().getValue() != null) {
                if (mvvm.getDiscount().getValue() > mvvm.getTotalPrice().getValue()) {
                    createErrorDialog();
                } else {
                    binding.bottomSheet.setDiscount(Common.formatPriceFromDoubleToString(mvvm.getDiscount().getValue() != null ? mvvm.getDiscount().getValue() : 0));
                    binding.bottomSheet.setVat(Common.formatPriceFromDoubleToString(mvvm.getVAT().getValue() != null ? mvvm.getVAT().getValue() : 0));
                    binding.bottomSheet.setVatValue(Common.formatPriceFromDoubleToString(mvvm.getVATValue().getValue() != null ? mvvm.getVATValue().getValue() : 0));

                    binding.bottomSheet.setTotQty(mvvm.getTotalQty().getValue() + "");
                    binding.bottomSheet.setTotVaT(Common.formatPriceFromDoubleToString(mvvm.getTotalAfterDiscountVat().getValue() != null ? mvvm.getTotalAfterDiscountVat().getValue() : 0));
                    binding.bottomSheet.setTotBeforeVaT(Common.formatPriceFromDoubleToString(mvvm.getTotalProducts().getValue() != null ? mvvm.getTotalProducts().getValue() : 0));

                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

        });
        binding.edtCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() == 0) {
                    amount = 0.0;
                } else {
                    amount = Double.parseDouble(editable.toString());

                }


                if (Double.parseDouble(selectedItemModel.getPrice()) > 0) {
                    if (amount > 0) {
                        binding.setCounter(String.valueOf(amount));
                        if (selectedItemModel != null) {
                            selectedItemModel.setAmount(amount);
                            mvvm.addItemToCart(selectedItemModel, getUserModel());
                            binding.setModel(selectedItemModel);

                        }
                    }
                } else {
                    Toast.makeText(CreateSalesInvoiceActivity.this, R.string.item_price_greater_zero, Toast.LENGTH_SHORT).show();

                }
                binding.setCounter(String.valueOf(amount));
            }
        });
        binding.customer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() == 0) {
                    mvvm.getCustomerName().setValue("");
                    mvvm.addCustomerToCart("");
                } else {
                    mvvm.getCustomerName().setValue(editable.toString());
                    mvvm.addCustomerToCart(editable.toString());


                }


            }
        });
        binding.bottomSheet.cash.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b){
                mvvm.getSelectedPaymentMethods().setValue("1");
            }
        });
        binding.bottomSheet.mada.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b){
                mvvm.getSelectedPaymentMethods().setValue("2");
            }
        });

        binding.bottomSheet.btnPay.setOnClickListener(view -> {
            mvvm.createInvoice(this,getUserModel());
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        });
        binding.llItems.setOnClickListener(view -> {
            behaviorItems.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        binding.itemsBottomSheet.closeSheet.setOnClickListener(view -> {
            behaviorItems.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        binding.customer.setText(R.string.default_customer);
        mvvm.getCategories(this);

        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }

    }

    private void createDiscountDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_alert_bg);
        DialogDiscountLayoutBinding discountLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_discount_layout, null, false);
        if (discount > 0) {
            discountLayoutBinding.setDiscount(String.valueOf(discount));

        }
        discountLayoutBinding.imageClose.setOnClickListener(view -> {
            dialog.dismiss();
        });
        discountLayoutBinding.edtDiscount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                discountLayoutBinding.setEnable(editable.toString().length() > 0);
            }
        });

        discountLayoutBinding.btnConfirm.setOnClickListener(view -> {
            Common.CloseKeyBoard(this, discountLayoutBinding.edtDiscount);
            discount = Double.parseDouble(discountLayoutBinding.edtDiscount.getText().toString());

            if (discount > 0) {
                binding.setIsChecked(true);

            } else {
                binding.setIsChecked(false);
            }
            dialog.dismiss();
            mvvm.getDiscount().setValue(discount);
            mvvm.calculateTotalAmount(getUserModel());

        });
        dialog.setView(discountLayoutBinding.getRoot());
        dialog.show();
    }


    private void createErrorDialog() {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_bg);
        DialogDiscountErrorBinding errorBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_discount_error, null, false);
        errorBinding.setDiscount(String.valueOf(mvvm.getDiscount().getValue()));
        errorBinding.setTotal(String.valueOf(mvvm.getTotalPrice().getValue()));
        dialog.setView(errorBinding.getRoot());
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        errorBinding.imageClose.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }


    public void setSelectedCategory(CategoryWithProducts model) {
        adapter.updateList(model.getProducts());
    }

    public void setItemProduct(int adapterPosition, ProductModel model) {
        selectedItemPos = adapterPosition;
        selectedItemModel = model;
        binding.imageEdit.setImageResource(R.drawable.ic_edit2);
        binding.edtPrice.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
        binding.edtPrice.setEnabled(false);
        Common.CloseKeyBoard(this, binding.edtPrice);
        amount = model.getAmount();
        binding.setCounter(String.valueOf(amount));
        binding.llItemDetails.setVisibility(View.VISIBLE);
        binding.setModel(model);
    }

    public void deleteItem(ProductModel productModel) {
        binding.cardViewDelete.setVisibility(View.GONE);
        binding.setCounter("0");
        amount = 0;
        mvvm.deleteItem(productModel,getUserModel());
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void listenToInvoice(InvoiceModel invoiceModel){
        Intent intent = new Intent(this, PrintInvoiceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("data",invoiceModel);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }
}