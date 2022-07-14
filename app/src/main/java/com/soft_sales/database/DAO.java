package com.soft_sales.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.soft_sales.model.CategoryModel;
import com.soft_sales.model.CategoryWithProducts;
import com.soft_sales.model.InvoiceCrossModel;
import com.soft_sales.model.InvoiceModel;
import com.soft_sales.model.InvoiceWithProductsModel;
import com.soft_sales.model.ProductModel;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface DAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertCategories(List<CategoryModel> categories);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertCategory(CategoryModel categoryModel);

    @Query("SELECT * FROM categorymodel")
    Single<List<CategoryModel>> getCategories();

    @Query("SELECT * FROM categorymodel WHERE id =:id")
    Single<CategoryModel> getCategoryByOnlineId(String id);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    Completable updateCategory(CategoryModel categoryModel);

    @Query("SELECT * FROM productmodel")
    Single<List<ProductModel>> getProducts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertProducts(List<ProductModel> products);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    Completable updateProduct(ProductModel productModel);

    @Query("SELECT * FROM productmodel WHERE id =:product_online_id")
    Single<ProductModel> getLocalProductByOnlineId(String product_online_id);

    @Query("SELECT * FROM productmodel WHERE id =:product_online_id")
    Single<List<ProductModel>> getLocalProductsByOnlineId(String product_online_id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertInvoiceCrossTable(List<InvoiceCrossModel> list);

    @Query("SELECT * FROM invoicemodel WHERE is_back =:isBack")
    Single<List<InvoiceModel>> getSalesInvoices(boolean isBack);

    @Query("SELECT * FROM invoicemodel WHERE invoice_online_id =:online_id ORDER BY code DESC")
    Single<List<InvoiceModel>> getUnSyncLocalSalesInvoices(String online_id);

    @Query("SELECT * FROM invoicemodel WHERE invoice_online_id <>:online_id ORDER BY code DESC")
    Single<List<InvoiceModel>> getSyncLocalSalesInvoices(String online_id);


    @Query("SELECT * FROM invoicemodel ORDER BY code DESC")
    Single<List<InvoiceModel>> getAllInvoices();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insertInvoice(InvoiceModel invoiceModel);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insertProduct(ProductModel productModel);

    @Query("UPDATE productmodel SET id =:product_online_id")
    Completable updateProductOnlineId(String product_online_id);

    @Query("UPDATE productmodel SET id =:product_online_id WHERE product_local_id =:product_local_id")
    Completable updateProductOnlineIdByLocalId(String product_online_id,long product_local_id);


    @Query("SELECT * FROM categorymodel")
    Single<List<CategoryWithProducts>> getCategoriesWithProducts();

    @Query("SELECT * FROM productmodel WHERE product_local_id =:product_local_id")
    Single<ProductModel> getProductByLocalId(long product_local_id);

    @Transaction
    @Query("SELECT * FROM invoicemodel ORDER BY invoice_local_id DESC LIMIT 1")
    Single<InvoiceModel> getLastInvoice();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    Completable updateInvoice(InvoiceModel model);

    @Query("UPDATE invoicecrossmodel SET product_online_id =:product_online_id WHERE product_local_id =:product_local_id")
    Completable updateProductOnlineIdInCrossTable(long product_local_id, String product_online_id);

    @Query("UPDATE invoicecrossmodel SET invoice_online_id =:invoice_online_id WHERE product_local_id =:invoice_local_id")
    Completable updateInvoiceOnlineIdInCrossTable(long invoice_local_id, String invoice_online_id);

    @Transaction
    @Query("SELECT * FROM invoicemodel WHERE invoice_local_id =:invoice_local_id")
    Single<InvoiceWithProductsModel> getInvoiceWithProducts(long invoice_local_id);

    @Transaction
    @Query("SELECT * FROM invoicecrossmodel WHERE invoice_local_id =:invoice_local_id AND product_local_id =:product_local_id")
    Single<InvoiceCrossModel> getProductWithAmount(long invoice_local_id,long product_local_id);

    @Transaction
    @Query("SELECT * FROM invoicemodel WHERE code =:code AND is_back =:isBack")
    Single<InvoiceWithProductsModel> getInvoiceWithProductsByCode(String code,boolean isBack);


    @Transaction
    @Query("SELECT * FROM invoicemodel WHERE invoice_online_id =:invoice_online_id OR isUpdated =:isUpdated")
    Single<List<InvoiceWithProductsModel>> getLocalInvoiceWithProducts(String invoice_online_id,boolean isUpdated);


    @Query("DELETE FROM categorymodel")
    Completable dropCategoryTable();

    @Query("DELETE FROM productmodel")
    Completable dropProductTable();

    @Query("DELETE FROM invoicemodel")
    Completable dropInvoiceTable();

    @Query("DELETE FROM invoicecrossmodel")
    Completable dropInvoiceCrossTable();


}
