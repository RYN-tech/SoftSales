package com.soft_sales.uis.activity_print_invoice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.soft_sales.R;
import com.soft_sales.adapter.SalesProductPrintAdapter;
import com.soft_sales.databinding.ActivityPrintInvoiceBinding;
import com.soft_sales.model.InvoiceModel;
import com.soft_sales.printer_utils.SunmiPrintHelper;
import com.soft_sales.printer_utils.ZatcaQRCodeGeneration;
import com.soft_sales.uis.activity_base.BaseActivity;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class PrintInvoiceActivity extends BaseActivity {
    private ActivityPrintInvoiceBinding binding;
    private InvoiceModel model;
    private SalesProductPrintAdapter adapter;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_print_invoice);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        model = (InvoiceModel) intent.getSerializableExtra("data");
    }

    private void initView() {

        Glide.with(this)
                .asBitmap()
                .load(getUserModel().getData().getSetting().getImageBitmap())
                .into(new SimpleTarget<Bitmap>() {

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        Log.e("sada","sda");
                        binding.logo.setVisibility(View.GONE);

                    }

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        binding.logo.setVisibility(View.VISIBLE);
                        binding.logo.setImageBitmap(resource);


                    }
                });

        setUpToolbar(binding.toolbar, "", R.color.white, R.color.black);
        binding.setModel(model);
        binding.setUserModel(getUserModel());
        adapter = new SalesProductPrintAdapter(this, getLang());
        adapter.updateList(model.getProducts());
        binding.recView.setLayoutManager(new LinearLayoutManager(this));
        binding.recView.setAdapter(adapter);

        ZatcaQRCodeGeneration.Builder builder = new ZatcaQRCodeGeneration.Builder();
        builder.sellerName(getUserModel().getData().getSetting().getName_ar()) // Shawrma House
                .taxNumber(getUserModel().getData().getSetting().getCommercial_number()) // 1234567890
                .invoiceDate(getInvoiceDate()) //..> 22/11/2021 03:00 am
                .totalAmount(model.getTotal() + "") // 100
                .taxAmount(model.getTax_per() + "");


        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            bitmap = barcodeEncoder.encodeBitmap(builder.getBase64(), BarcodeFormat.QR_CODE, 300, 300);
            binding.image.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }


        binding.btnPrint.setOnClickListener(view -> {
            printApiBitmap();
        });


    }


    private void printApiBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(binding.scrollView.getChildAt(0).getWidth(), binding.scrollView.getChildAt(0).getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        binding.scrollView.draw(canvas);

        if (bitmap != null) {

            Toast.makeText(this, "Printing", Toast.LENGTH_SHORT).show();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            double h = bitmap.getWidth() / 384.0;
            double dstH = bitmap.getHeight() / h;


            Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 384, (int) dstH, true);


            int width = newBitmap.getWidth();
            int height = newBitmap.getHeight();
            int newWidth = (width / 8 + 1) * 8;
            float scaleWidth = ((float) newWidth) / width;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, 1);
            Bitmap b = Bitmap.createBitmap(newBitmap, 0, 0, width, height, matrix, true);
            SunmiPrintHelper.getInstance().printBitmap(b, 1);


        }
    }

    private String getInvoiceDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH);
        String date = simpleDateFormat.format(Long.parseLong(model.getDate()));
        return date;
    }
}