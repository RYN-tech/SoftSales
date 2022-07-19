package com.soft_sales.uis.activity_add_product;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.multidex.BuildConfig;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.soft_sales.R;
import com.soft_sales.adapter.SpinnerCategoriesAdapter;
import com.soft_sales.databinding.ActivityAddProductBinding;
import com.soft_sales.databinding.ActivityLoginBinding;
import com.soft_sales.databinding.DialogChooseImageBinding;
import com.soft_sales.model.AddProductModel;
import com.soft_sales.model.CategoryModel;
import com.soft_sales.model.LoginModel;
import com.soft_sales.mvvm.ActivityAddProductMvvm;
import com.soft_sales.mvvm.ActivityHomeMvvm;
import com.soft_sales.mvvm.ActivityLoginMvvm;
import com.soft_sales.share.Common;
import com.soft_sales.uis.activity_base.BaseActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AddProductActivity extends BaseActivity {
    private ActivityAddProductBinding binding;
    private ActivityAddProductMvvm mvvm;
    private ActivityResultLauncher<String[]> permissions;
    private ActivityResultLauncher<Intent> launcher;
    private int req;
    private Uri cameraUri = null;
    private String cameraImagePath;
    private SpinnerCategoriesAdapter adapter;
    private AddProductModel model;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_product);
        initView();
    }

    private void initView() {
        model = new AddProductModel();
        mvvm = ViewModelProviders.of(this).get(ActivityAddProductMvvm.class);

        setUpToolbar(binding.toolbar, getString(R.string.add_product), R.color.white, R.color.black);
        mvvm.getOnCategoryDataSuccess().observe(this, categories -> {
            if (adapter != null) {
                if (categories.size() > 0) {
                    model.setCategory_id(categories.get(0).getId());
                }

                adapter.updateList(categories);
            }
        });
        binding.setModel(model);

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                CategoryModel categoryModel = (CategoryModel) adapterView.getSelectedItem();
                model.setCategory_id(categoryModel.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        adapter = new SpinnerCategoriesAdapter(this, getLang());
        binding.spinner.setAdapter(adapter);
        binding.selectPhoto.setOnClickListener(view -> openSheet());

        permissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
            Log.e("permissions", isGranted.toString());
            if (!isGranted.containsValue(false)) {

                if (req == 1) {
                    openGallery();
                } else {
                    openCamera();
                }
            } else {
                Toast.makeText(this, "Permission to access photo denied", Toast.LENGTH_SHORT).show();

            }
        });
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (req == 1 && result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                if (result.getData().getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        binding.cardImage.setVisibility(View.VISIBLE);
                        binding.image.setImageURI(uri);
                        model.setPhoto_path(Common.getImagePath(this, uri));
                    }
                }


            } else if (req == 2 && result.getResultCode() == Activity.RESULT_OK) {
                binding.cardImage.setVisibility(View.VISIBLE);
                binding.image.setImageURI(cameraUri);
                model.setPhoto_path(cameraImagePath);


            }

        });

        binding.delete.setOnClickListener(view -> {
            cameraUri = null;
            binding.cardImage.setVisibility(View.GONE);
            binding.image.setImageURI(null);
            model.setPhoto_path("");
        });


        binding.btnSave.setOnClickListener(view -> {
            mvvm.addProduct(model);
            new Handler().postDelayed(this::finish, 1000);
        });
    }


    private void openSheet() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_bg);
        DialogChooseImageBinding imageBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_choose_image, null, false);
        dialog.setView(imageBinding.getRoot());
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        imageBinding.tvCamera.setOnClickListener(v -> {
            checkCameraPermission();
            dialog.dismiss();
        });
        imageBinding.tvGallery.setOnClickListener(v -> {
            checkGalleryPermission();
            dialog.dismiss();
        });
        imageBinding.tvCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.show();
    }

    private void checkCameraPermission() {
        req = 2;
        String[] permissions = new String[]{BaseActivity.WRITE_PERM, BaseActivity.CAM_PERM};
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, permissions[1]) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            this.permissions.launch(permissions);

        }
    }

    private void checkGalleryPermission() {
        req = 1;
        String[] permissions = new String[]{BaseActivity.READ_PERM};
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
            this.permissions.launch(permissions);
        } else {
            openGallery();
        }
    }

    private void openGallery() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        launcher.launch(Intent.createChooser(intent, "Choose image"));


    }

    private void openCamera() {
        req = 2;
        File fileImage = createImageFile();
        if (fileImage != null) {
            cameraUri = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()), BuildConfig.APPLICATION_ID + ".provider", fileImage);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
            launcher.launch(intent);
        } else {
            Toast.makeText(this, "You don't allow to access photos", Toast.LENGTH_SHORT).show();
        }

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
            cameraImagePath = imageFile.getAbsolutePath();
            Log.e("path", cameraImagePath + "");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("err", e.getMessage());
        }

        return imageFile;

    }

}