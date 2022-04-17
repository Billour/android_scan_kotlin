package com.repongroup.inventory.providers

import android.annotation.SuppressLint
import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import com.repongroup.inventory.Custodian_main.Companion.TAG

class CustodianContentProvider: ContentProvider() {

    private lateinit var mCustodian: SQLiteDatabase

    companion object {
        private val AUTHORITY: String = "com.repongroup.inventory.providers.CustodianContentProvider"
        private val DB_NANE: String = "Custodians.db"
        private val DB_TABLE: String = "Custodian_NJ"
        private val DB_TABLE2: String = "Department"
        private val DB_TABLE3: String = "Custodian_RP"
        private val DB_TABLE_CUSTODAIN_NJ: Int = 1
        private val DB_TABLE_DEPARTMENT: Int = 2
        private val DB_TABLE_CUSTODAIN_RP: Int = 3
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$DB_TABLE")
        val CONTENT_URI2: Uri = Uri.parse("content://$AUTHORITY/$DB_TABLE2")
        val CONTENT_URI3: Uri = Uri.parse("content://$AUTHORITY/$DB_TABLE3")
        private var sUriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH) //初始化不匹配任何返回碼
        private val crTBsql: String = "CREATE TABLE $DB_TABLE (" +
                                      "id INTEGER PRIMARY KEY," +
                                      "fix_code INTEGER NOT NULL," +
                                      "fix_name TEXT NOT NULL," +
                                      "fix_spec TEXT NOT NULL," +
                                      "naj_dept_id INTEGER," +
                                      "dept_id INTEGER NOT NULL," +
                                      "dept_name TEXT NOT NULL," +
                                      "empl_id INTEGER NOT NULL," +
                                      "empl_name TEXT NOT NULL," +
                                      "location TEXT NOT NULL," +
                                      "fix_status TEXT NOT NULL," +
                                      "site TEXT NOT NULL," +
                                      "qty INTEGER NOT NULL," +
                                      "fix_inventory TEXT NOT NULL," +
                                      "queryfix_inventory TEXT NOT NULL," +
                                      "cycle_date TEXT," +
                                      "cycle_status TEXT)"

        private val crTBsql2: String = "CREATE TABLE $DB_TABLE2 (" +
                                       "id INTEGER PRIMARY KEY," +
                                       "naj_dept_id INTEGER NOT NULL," +
                                       "naj_dept_name TEXT NOT NULL," +
                                       "dept_no TEXT," +
                                       "dept_name TEXT)"

        private val crTBsql3: String = "CREATE TABLE $DB_TABLE3 (" +
                                       "id INTEGER PRIMARY KEY," +
                                       "fix_code INTEGER NOT NULL," +
                                       "fix_name TEXT NOT NULL," +
                                       "fix_spec TEXT NOT NULL," +
                                       "naj_dept_id INTEGER," +
                                       "dept_id INTEGER NOT NULL," +
                                       "dept_name TEXT NOT NULL," +
                                       "empl_id INTEGER NOT NULL," +
                                       "empl_name TEXT NOT NULL," +
                                       "location TEXT NOT NULL," +
                                       "fix_status TEXT NOT NULL," +
                                       "site TEXT NOT NULL," +
                                       "qty INTEGER NOT NULL," +
                                       "fix_inventory TEXT NOT NULL," +
                                       "queryfix_inventory TEXT NOT NULL," +
                                       "cycle_date TEXT," +
                                       "cycle_status TEXT)"
    }

    @SuppressLint("Recycle")
    override fun onCreate(): Boolean {
        sUriMatcher.addURI(AUTHORITY, DB_TABLE, DB_TABLE_CUSTODAIN_NJ)
        sUriMatcher.addURI(AUTHORITY, DB_TABLE2, DB_TABLE_DEPARTMENT)
        sUriMatcher.addURI(AUTHORITY, DB_TABLE3, DB_TABLE_CUSTODAIN_RP)

        // ---宣告 使用Class DbOpenHelper.java 作為處理SQLite介面
        // Content Provider 就是 data Server, 負責儲存及提供資料, 他允許任何不同的APP使用
        // 共同的資料(不同的APP用同一個SQLite).
        val mDbOpenHelper: DbOpenHelper = DbOpenHelper(context, DB_NANE,null,1)
        mCustodian = mDbOpenHelper.writableDatabase

        // 檢查資料表是否已經存在，如果不存在，就建立一個。
        try {
            val cursor: Cursor = mCustodian.rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master " +
                    "WHERE tbl_name = '$DB_TABLE'",null)
            val cursor2: Cursor = mCustodian.rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master " +
                    "WHERE tbl_name = '$DB_TABLE2'",null)
            val cursor3: Cursor = mCustodian.rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master " +
                    "WHERE tbl_name = '$DB_TABLE3'",null)

            //沒有資料表，要建立一個資料表。
            if (cursor.count == 0)
                mCustodian.execSQL(crTBsql)

            if (cursor2.count == 0)
                mCustodian.execSQL(crTBsql2)

            if (cursor3.count == 0)
                mCustodian.execSQL(crTBsql3)

            cursor.close()
            cursor2.close()
            cursor3.close()

        }catch (e: Exception){
            Log.d(TAG,"error" + e.toString())
        }

        return true
    }

    override fun getType(uri: Uri?): String {

        return when(sUriMatcher.match(uri)){
            DB_TABLE_CUSTODAIN_NJ -> "" + CONTENT_URI
            DB_TABLE_DEPARTMENT -> "" + CONTENT_URI2
            DB_TABLE_CUSTODAIN_RP -> "" + CONTENT_URI3
            else -> throw IllegalAccessException("Unknown URI=$uri")
        }

    }

    //查詢
    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?,
                       selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        var c: Cursor? = null

        when(sUriMatcher.match(uri)){
            DB_TABLE_CUSTODAIN_NJ -> {
                c = mCustodian.query(true, DB_TABLE, projection, selection, selectionArgs,
                        null,null, sortOrder,null) //"ASC DESC";
                c.setNotificationUri(context.contentResolver, uri)
            }

            DB_TABLE_DEPARTMENT -> {
                c = mCustodian.query(true, DB_TABLE2, projection, selection, selectionArgs,
                        null,null, sortOrder,null) //"ASC DESC";
                c.setNotificationUri(context.contentResolver, uri)
            }

            DB_TABLE_CUSTODAIN_RP -> {
                c = mCustodian.query(true, DB_TABLE3, projection, selection, selectionArgs,
                        null,null, sortOrder,null) //"ASC DESC";
                c.setNotificationUri(context.contentResolver, uri)
            }
        }

        return c
    }

    //新增
    override fun insert(uri: Uri?, values: ContentValues?): Uri? {

        when(sUriMatcher.match(uri)){
            DB_TABLE_CUSTODAIN_NJ -> {
                val rowId: Long = mCustodian.insert(DB_TABLE,null, values)

                if (rowId > 0){
                    // 在已有的 Uri的後面追加ID數據
                    val insertedRowUri: Uri = ContentUris.withAppendedId(CONTENT_URI, rowId)

                    // 通知數據已經改變
                    context.contentResolver.notifyChange(insertedRowUri,null)

                    return insertedRowUri
                }
            }

            DB_TABLE_DEPARTMENT -> {
                val rowId: Long = mCustodian.insert(DB_TABLE2,null, values)

                if (rowId > 0){
                    // 在已有的 Uri的後面追加ID數據
                    val insertedRowUri: Uri = ContentUris.withAppendedId(CONTENT_URI2, rowId)

                    // 通知數據已經改變
                    context.contentResolver.notifyChange(insertedRowUri,null)

                    return insertedRowUri
                }
            }

            DB_TABLE_CUSTODAIN_RP -> {
                val rowId: Long = mCustodian.insert(DB_TABLE3,null, values)

                if (rowId > 0){
                    // 在已有的 Uri的後面追加ID數據
                    val insertedRowUri: Uri = ContentUris.withAppendedId(CONTENT_URI3, rowId)

                    // 通知數據已經改變
                    context.contentResolver.notifyChange(insertedRowUri,null)

                    return insertedRowUri
                }
            }
        }

        return null
    }

    //批次新增(處理大量資料)
    override fun bulkInsert(uri: Uri?, values: Array<out ContentValues>?): Int {
        var numinsert: Int = 0

        when(sUriMatcher.match(uri)){
            DB_TABLE_CUSTODAIN_NJ -> {
                mCustodian.beginTransaction() //啟動加速/交易功能

                try {
                    numinsert = values!!.size - 1 //!!:不得為空

                    for (i in 0..(numinsert - 1)){
                        val rowId: Long = mCustodian.insert(DB_TABLE,null, values[i])

                        // 在已有的 Uri的後面追加ID數據
                        ContentUris.withAppendedId(CONTENT_URI, rowId)

                        // 通知數據已經改變
                        context.contentResolver.notifyChange(uri,null)

                        if (rowId < 0)
                            throw UnsupportedOperationException("unsupported uri:$uri")
                    }

                    mCustodian.setTransactionSuccessful() //加速成功

                }catch (e: Exception){
                    Log.d(TAG,"error=" + e.toString())

                }finally {
                    mCustodian.endTransaction() //關閉加速
                }
            }

            DB_TABLE_CUSTODAIN_RP -> {
                mCustodian.beginTransaction() //啟動加速/交易功能

                try {
                    numinsert = values!!.size - 1 //!!:不得為空

                    for (i in 0..(numinsert - 1)){
                        val rowId: Long = mCustodian.insert(DB_TABLE3,null, values[i])

                        // 在已有的 Uri的後面追加ID數據
                        ContentUris.withAppendedId(CONTENT_URI3, rowId)

                        // 通知數據已經改變
                        context.contentResolver.notifyChange(uri,null)

                        if (rowId < 0)
                            throw UnsupportedOperationException("unsupported uri:$uri")
                    }

                    mCustodian.setTransactionSuccessful() //加速成功

                }catch (e: Exception){
                    Log.d(TAG,"error=" + e.toString())

                }finally {
                    mCustodian.endTransaction() //關閉加速
                }
            }
        }

        return numinsert
    }

    //修改
    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        var rowsAffected: Int = 0

        when(sUriMatcher.match(uri)){
            DB_TABLE_CUSTODAIN_NJ -> rowsAffected = mCustodian.update(DB_TABLE, values, selection,null)
            DB_TABLE_DEPARTMENT -> rowsAffected = mCustodian.update(DB_TABLE2, values, selection,null)
            DB_TABLE_CUSTODAIN_RP -> rowsAffected = mCustodian.update(DB_TABLE3, values, selection,null)
        }

        return rowsAffected
    }

    //刪除
    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        var rowsAffected: Int = 0

        when(sUriMatcher.match(uri)){
            DB_TABLE_CUSTODAIN_NJ -> rowsAffected = mCustodian.delete(DB_TABLE, selection,null)
            DB_TABLE_DEPARTMENT -> rowsAffected = mCustodian.delete(DB_TABLE2, selection,null)
            DB_TABLE_CUSTODAIN_RP -> rowsAffected = mCustodian.delete(DB_TABLE3, selection,null)
        }

        return rowsAffected
    }

}