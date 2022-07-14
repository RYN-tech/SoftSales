package com.soft_sales.general_ui;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;


import com.soft_sales.R;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class GeneralMethod {

    @BindingAdapter("error")
    public static void errorValidation(View view, String error) {
        if (view instanceof EditText) {
            EditText ed = (EditText) view;
            ed.setError(error);
        } else if (view instanceof TextView) {
            TextView tv = (TextView) view;
            tv.setError(error);


        }
    }


    @BindingAdapter("local_image")
    public static void localImage(View view,byte[] local_image) {
        if (view instanceof CircleImageView) {
            CircleImageView imageView = (CircleImageView) view;
            if (local_image != null && local_image.length > 0) {
                Glide.with(imageView)
                        .asBitmap()
                        .load(local_image)
                        .into(imageView);
            }
        } else if (view instanceof RoundedImageView) {
            RoundedImageView imageView = (RoundedImageView) view;

            if (local_image != null && local_image.length > 0) {
                Glide.with(imageView)
                        .asBitmap()
                        .load(local_image)
                        .into(imageView);
            }
        } else if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;

            if (local_image != null && local_image.length > 0) {
                Glide.with(imageView)
                        .asBitmap()
                        .load(local_image)
                        .into(imageView);
            }
        }

    }

    @BindingAdapter("date")
    public static void date(TextView view,String dateStamp){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        String date = simpleDateFormat.format(Long.parseLong(dateStamp));
        view.setText(date);

    }

    @BindingAdapter("invoice_date")
    public static void invoice_date(TextView view,String dateStamp){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH);
        String date = simpleDateFormat.format(Long.parseLong(dateStamp));
        view.setText(date);

    }
}










