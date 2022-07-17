package com.soft_sales.share;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.multidex.MultiDexApplication;

import com.soft_sales.language.Language;
import com.soft_sales.printer_utils.SunmiPrintHelper;


public class App extends MultiDexApplication {
    public static final String CHANNEL_ID_CATEGORY ="soft_category_01_id";
    public static final String CHANNEL_NAME_CATEGORY ="soft_category_channel";

    public static final String CHANNEL_ID_PRODUCTS ="soft_products_02_id";
    public static final String CHANNEL_NAME_PRODUCTS ="soft_products_channel";

    public static final String CHANNEL_ID_INVOICES ="soft_invoices_03_id";
    public static final String CHANNEL_NAME_INVOICES ="soft_invoices_channel";

    public static final String CHANNEL_ID_PRODUCT ="soft_product_04_id";
    public static final String CHANNEL_NAME_PRODUCT ="soft_product_channel";

    public static final String CHANNEL_ID_INVOICE ="soft_invoice_05_id";
    public static final String CHANNEL_NAME_INVOICE ="soft_invoice_channel";

    public static final String CHANNEL_ID_SINGLE_INViOCE ="soft_single_invoice_06_id";
    public static final String CHANNEL_NAME_SINGLE_INVOICE ="soft_single_invoice_channel";

    public static final String CHANNEL_ID_SINGLE_RETURN_INViOCE ="soft_single_return_invoice_07_id";
    public static final String CHANNEL_NAME_SINGLE_RETURN_INVOICE ="soft_single_return_invoice_channel";

    public static final String CHANNEL_ID_SOFT_APP ="soft_sync_08_id";
    public static final String CHANNEL_NAME_SOFT_APP ="soft_sync_channel";


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(Language.updateResources(newBase,"ar"));
    }


    @Override
    public void onCreate() {
        super.onCreate();
        SunmiPrintHelper.getInstance().initSunmiPrinterService(this);
        createChannelCategory();
        createChannelProducts();
        createChannelInvoices();
        createChannelProduct();
        createChannelInvoice();
        createChannelSingleInvoice();
        createChannelSingleReturnInvoice();
        createChannelSyncSoftApp();
    }

    private void createChannelCategory() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_CATEGORY, CHANNEL_NAME_CATEGORY,NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("soft_app_category_service_channel");
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            manager.createNotificationChannel(channel);
        }
    }

    private void createChannelProducts() {
        NotificationManager  manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_PRODUCTS, CHANNEL_NAME_PRODUCTS,NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("soft_app_products_service_channel");
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            manager.createNotificationChannel(channel);
        }
    }

    private void createChannelInvoices() {
        NotificationManager  manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_INVOICES, CHANNEL_NAME_INVOICES,NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("soft_app_invoices_service_channel");
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            manager.createNotificationChannel(channel);
        }
    }

    private void createChannelProduct() {
        NotificationManager  manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_PRODUCT, CHANNEL_NAME_PRODUCT,NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("soft_app_product_service_channel");
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            manager.createNotificationChannel(channel);
        }
    }

    private void createChannelInvoice() {
        NotificationManager  manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_INVOICE, CHANNEL_NAME_INVOICE,NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("soft_app_invoice_service_channel");
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            manager.createNotificationChannel(channel);
        }
    }
    private void createChannelSingleInvoice() {
        NotificationManager  manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_SINGLE_INViOCE, CHANNEL_NAME_SINGLE_INVOICE,NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("soft_app_only_invoice_service_channel");
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            manager.createNotificationChannel(channel);
        }
    }

    private void createChannelSingleReturnInvoice() {
        NotificationManager  manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_SINGLE_RETURN_INViOCE, CHANNEL_NAME_SINGLE_RETURN_INVOICE,NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("soft_app_only_return_invoice_service_channel");
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            manager.createNotificationChannel(channel);
        }
    }

    private void createChannelSyncSoftApp() {
        NotificationManager  manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_SOFT_APP, CHANNEL_NAME_SOFT_APP,NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("soft_app_service_channel");
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            manager.createNotificationChannel(channel);
        }
    }


}

