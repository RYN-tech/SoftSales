package com.soft_sales.database;

import android.app.Application;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.soft_sales.model.CategoryModel;
import com.soft_sales.model.InvoiceCrossModel;
import com.soft_sales.model.InvoiceModel;
import com.soft_sales.model.ProductModel;

@Database(entities = {CategoryModel.class, ProductModel.class, InvoiceModel.class, InvoiceCrossModel.class}, exportSchema = false, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public static AppDatabase instance = null;


    public abstract DAO getDAO();

    public static synchronized AppDatabase getInstance(Application application) {
        if (instance == null) {
            instance = Room.databaseBuilder(application, AppDatabase.class, "soft_sales_database.db")
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return instance;
    }
}
