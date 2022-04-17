package com.repongroup.packing_scan.providers

import android.content.*
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import com.repongroup.packing_scan.packing_scan_main.Companion.TAG

class Packing_scanContentProvider: ContentProvider() {

    companion object {
        private val AUTHORITY = "com.repongroup.packing_scan.providers.Packing_scanContentProvider"
        val DB_NANE = "packing_scan.db"
        val DB_TABLE = "packing_scan_NJ"
        val DB_TABLE2 = "Department"
        val DB_TABLE3 = "packing_scan_RP"
        val DB_TABLE4 = "packing_scan_SZ"
        val DB_VERSION = 2 //資料庫版本控制：目前資料庫版本
        private val DB_TABLE_PACKING_SCAN_NJ = 1
        private val DB_TABLE_DEPARTMENT = 2
        private val DB_TABLE_PACKING_SCAN_RP = 3
        private val DB_TABLE_PACKING_SCAN_SZ = 4
        val CONTENT_URI = Uri.parse("content://$AUTHORITY/$DB_TABLE")
        val CONTENT_URI2 = Uri.parse("content://$AUTHORITY/$DB_TABLE2")
        val CONTENT_URI3 = Uri.parse("content://$AUTHORITY/$DB_TABLE3")
        val CONTENT_URI4 = Uri.parse("content://$AUTHORITY/$DB_TABLE4")
        private var sUriMatcher = UriMatcher(UriMatcher.NO_MATCH) //初始化不匹配任何返回碼
        lateinit var db_Packing_scan: SQLiteDatabase
        private val crTBsql = "CREATE TABLE $DB_TABLE (" +
                                     "id INTEGER PRIMARY KEY," +
                                     "qrcode INTEGER NOT NULL," +
                                     "check_in_date TEXT NOT NULL," +
                                     "insert_date TEXT NOT NULL," +
                                     "update_flag TEXT NOT NULL," +
                                     "write_flag TEXT NOT NULL," +
                                     "site TEXT NOT NULL)"
        private val crTBsql2 = "CREATE TABLE $DB_TABLE2 (" +
                                      "id INTEGER PRIMARY KEY," +
                                      "naj_dept_id INTEGER NOT NULL," +
                                      "naj_dept_name TEXT NOT NULL," +
                                      "dept_no TEXT,dept_name TEXT)"
        private val crTBsql3 = "CREATE TABLE $DB_TABLE3 (" +
                                      "id INTEGER PRIMARY KEY," +
                                      "qrcode INTEGER NOT NULL," +
                                      "check_in_date TEXT NOT NULL," +
                                      "insert_date TEXT NOT NULL," +
                                      "update_flag TEXT NOT NULL," +
                                      "write_flag TEXT NOT NULL," +
                                      "site TEXT NOT NULL)"
        private val crTBsql4 = "CREATE TABLE $DB_TABLE4 (" +
                                      "id INTEGER PRIMARY KEY," +
                                      "qrcode INTEGER NOT NULL," +
                                      "check_in_date TEXT NOT NULL," +
                                      "insert_date TEXT NOT NULL," +
                                      "update_flag TEXT NOT NULL," +
                                      "write_flag TEXT NOT NULL," +
                                      "site TEXT NOT NULL)"

        //直接傳入SQLite語法做查詢
        fun rawquery(context: Context, uri: Uri?, sqldata: SQLiteDatabase, sql: String, selectionArgs: Array<String>): Cursor{
            val c: Cursor = sqldata.rawQuery(sql, selectionArgs)
            c.setNotificationUri(context.contentResolver, uri)

            return c
        }
    }


    override fun onCreate(): Boolean {
        sUriMatcher.addURI(AUTHORITY, DB_TABLE, DB_TABLE_PACKING_SCAN_NJ)
        sUriMatcher.addURI(AUTHORITY, DB_TABLE2, DB_TABLE_DEPARTMENT)
        sUriMatcher.addURI(AUTHORITY, DB_TABLE3, DB_TABLE_PACKING_SCAN_RP)
        sUriMatcher.addURI(AUTHORITY, DB_TABLE4, DB_TABLE_PACKING_SCAN_SZ)

        // ---宣告 使用Class DbOpenHelper.java 作為處理SQLite介面
        // Content Provider 就是 data Server, 負責儲存及提供資料, 他允許任何不同的APP使用
        // 共同的資料(不同的APP用同一個SQLite).
        //version：資料庫更動時改一下版本號
        val mDbOpenHelper = DbOpenHelper(context, DB_NANE,null, DB_VERSION)
        val last_version = mDbOpenHelper.readableDatabase.version //取得資料庫版本號
        Log.d(TAG,"last_version=$last_version")

        db_Packing_scan = mDbOpenHelper.writableDatabase

        // 檢查資料表是否已經存在，如果不存在，就建立一個。
        val cursor = db_Packing_scan.rawQuery("SELECT DISTINCT tbl_name FROM " +
                "sqlite_master WHERE tbl_name = '$DB_TABLE'", null)
        val cursor2 = db_Packing_scan.rawQuery("SELECT DISTINCT tbl_name FROM " +
                "sqlite_master WHERE tbl_name = '$DB_TABLE2'", null)
        val cursor3 = db_Packing_scan.rawQuery("SELECT DISTINCT tbl_name FROM " +
                "sqlite_master WHERE tbl_name = '$DB_TABLE3'", null)
        val cursor4 = db_Packing_scan.rawQuery("SELECT DISTINCT tbl_name FROM " +
                "sqlite_master WHERE tbl_name = '$DB_TABLE4'", null)

        // 沒有資料表，要建立一個資料表。
        if (cursor.count == 0) db_Packing_scan.execSQL(crTBsql)
        if (cursor2.count == 0) db_Packing_scan.execSQL(crTBsql2)
        if (cursor3.count == 0) db_Packing_scan.execSQL(crTBsql3)
        if (cursor4.count == 0) db_Packing_scan.execSQL(crTBsql4)

        cursor.close()
        cursor2.close()
        cursor3.close()
        cursor4.close()

        return true
    }

    //uri分流
    override fun getType(uri: Uri?): String {

        return when(sUriMatcher.match(uri)){
            DB_TABLE_PACKING_SCAN_NJ -> "" + CONTENT_URI
            DB_TABLE_DEPARTMENT -> "" + CONTENT_URI2
            DB_TABLE_PACKING_SCAN_RP -> "" + CONTENT_URI3
            DB_TABLE_PACKING_SCAN_SZ -> "" + CONTENT_URI4
            else -> throw IllegalArgumentException("Unknown URI=$uri")
        }

    }

    //查詢
    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        var c: Cursor? = null

        when(sUriMatcher.match(uri)){
            DB_TABLE_PACKING_SCAN_NJ -> c = db_Packing_scan.query(true, DB_TABLE, projection,
                        selection, selectionArgs, null, null, sortOrder, null) //"ASC DESC"

            DB_TABLE_DEPARTMENT -> c = db_Packing_scan.query(true, DB_TABLE2, projection,
                        selection, selectionArgs,null,null, sortOrder,null) //"ASC DESC"

            DB_TABLE_PACKING_SCAN_RP -> c = db_Packing_scan.query(true, DB_TABLE3, projection,
                        selection, selectionArgs,null,null, sortOrder,null) //"ASC DESC"

            DB_TABLE_PACKING_SCAN_SZ -> c = db_Packing_scan.query(true, DB_TABLE4, projection,
                        selection, selectionArgs,null,null, sortOrder,null) //"ASC DESC
        }

        return c
    }

    //批次新增(處理大量資料)
    override fun bulkInsert(uri: Uri?, values: Array<out ContentValues>?): Int {
        var numinsert = 0

        when(sUriMatcher.match(uri)){
            DB_TABLE_PACKING_SCAN_NJ -> { //台北廠
                db_Packing_scan.beginTransaction() //啟動加速

                try {
                    numinsert = (values!!.size - 1)

                    for (i in 0..(numinsert - 1)){
                        val rowID = db_Packing_scan.insert(DB_TABLE,null,values[i])
                        ContentUris.withAppendedId(CONTENT_URI, rowID) // 在已有的 Uri的後面追加ID數據
                        context.contentResolver.notifyChange(uri,null) // 通知數據已經改變

                        if (rowID < 0)
                            throw UnsupportedOperationException("unsupported uri: $uri")
                    }

                    db_Packing_scan.setTransactionSuccessful() //加速成功

                }catch (e: Exception){
                    Log.d(TAG,"error=" + e.toString())
                }finally {
                    db_Packing_scan.endTransaction() //關閉加速
                }
            }

            DB_TABLE_PACKING_SCAN_RP -> { //雲科廠
                db_Packing_scan.beginTransaction() //啟動加速

                try {
                    numinsert = (values!!.size - 1)

                    for (i in 0..(numinsert - 1)){
                        val rowID = db_Packing_scan.insert(DB_TABLE3,null,values[i])
                        ContentUris.withAppendedId(CONTENT_URI3, rowID) // 在已有的 Uri的後面追加ID數據
                        context.contentResolver.notifyChange(uri,null) // 通知數據已經改變

                        if (rowID < 0)
                            throw UnsupportedOperationException("unsupported uri: $uri")
                    }

                    db_Packing_scan.setTransactionSuccessful() //加速成功

                }catch (e: Exception){
                    Log.d(TAG,"error=" + e.toString())
                }finally {
                    db_Packing_scan.endTransaction() //關閉加速
                }
            }

            DB_TABLE_PACKING_SCAN_SZ -> { //南俊商貿
                db_Packing_scan.beginTransaction() //啟動加速

                try {
                    numinsert = (values!!.size - 1)

                    for (i in 0..(numinsert - 1)){
                        val rowID = db_Packing_scan.insert(DB_TABLE4,null,values[i])
                        ContentUris.withAppendedId(CONTENT_URI4, rowID) // 在已有的 Uri的後面追加ID數據
                        context.contentResolver.notifyChange(uri,null) // 通知數據已經改變

                        if (rowID < 0)
                            throw UnsupportedOperationException("unsupported uri: $uri")
                    }

                    db_Packing_scan.setTransactionSuccessful() //加速成功

                }catch (e: Exception){
                    Log.d(TAG,"error=" + e.toString())
                }finally {
                    db_Packing_scan.endTransaction() //關閉加速
                }
            }

        }

        return numinsert
    }

    //新增
    override fun insert(uri: Uri?, values: ContentValues?): Uri? {

        when(sUriMatcher.match(uri)){
            DB_TABLE_PACKING_SCAN_NJ -> {
                val rowId = db_Packing_scan.insert(DB_TABLE,null, values)

                if (rowId > 0){
                    val insertedRowUri = ContentUris.withAppendedId(CONTENT_URI, rowId) //在已有的 Uri的後面追加ID數據
                    context.contentResolver.notifyChange(insertedRowUri,null)
                    return insertedRowUri
                }
            }

            DB_TABLE_DEPARTMENT -> {
                val rowId = db_Packing_scan.insert(DB_TABLE2,null, values)

                if (rowId > 0){
                    val insertedRowUri = ContentUris.withAppendedId(CONTENT_URI2, rowId) //在已有的 Uri的後面追加ID數據
                    context.contentResolver.notifyChange(insertedRowUri,null)
                    return insertedRowUri
                }
            }

            DB_TABLE_PACKING_SCAN_RP -> {
                val rowId = db_Packing_scan.insert(DB_TABLE3,null, values)

                if (rowId > 0){
                    val insertedRowUri = ContentUris.withAppendedId(CONTENT_URI3, rowId) //在已有的 Uri的後面追加ID數據
                    context.contentResolver.notifyChange(insertedRowUri,null)
                    return insertedRowUri
                }
            }

            DB_TABLE_PACKING_SCAN_SZ -> {
                val rowId = db_Packing_scan.insert(DB_TABLE4,null, values)

                if (rowId > 0){
                    val insertedRowUri = ContentUris.withAppendedId(CONTENT_URI4, rowId) //在已有的 Uri的後面追加ID數據
                    context.contentResolver.notifyChange(insertedRowUri,null)
                    return insertedRowUri
                }
            }
        }

        return null
    }

    //修改
    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        var rowsAffected = 0

        when(sUriMatcher.match(uri)){
            DB_TABLE_PACKING_SCAN_NJ -> rowsAffected = db_Packing_scan.update(DB_TABLE, values,
                    selection,null)

            DB_TABLE_DEPARTMENT -> rowsAffected = db_Packing_scan.update(DB_TABLE2, values,
                    selection,null)

            DB_TABLE_PACKING_SCAN_RP -> rowsAffected = db_Packing_scan.update(DB_TABLE3, values,
                    selection,null)

            DB_TABLE_PACKING_SCAN_SZ -> rowsAffected = db_Packing_scan.update(DB_TABLE4, values,
                    selection,null)
        }

        return rowsAffected
    }

    //刪除
    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        var rowsAffected = 0

        when(sUriMatcher.match(uri)){
            DB_TABLE_PACKING_SCAN_NJ -> rowsAffected = db_Packing_scan.delete(DB_TABLE, selection,null)
            DB_TABLE_DEPARTMENT -> rowsAffected = db_Packing_scan.delete(DB_TABLE2, selection,null)
            DB_TABLE_PACKING_SCAN_RP -> rowsAffected = db_Packing_scan.delete(DB_TABLE3, selection,null)
            DB_TABLE_PACKING_SCAN_SZ -> rowsAffected = db_Packing_scan.delete(DB_TABLE4, selection,null)
        }

        return rowsAffected
    }

}
