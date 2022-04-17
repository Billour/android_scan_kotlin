package com.repongroup.electroplating_commission.providers

import android.content.*
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import com.repongroup.electroplating_commission.Elec_com_obj_def.Companion.TAG

class Elec_comContentProvider: ContentProvider() {

    companion object {
        private val AUTHORITY = "com.repongroup.electroplating_commission.providers.Elec_comContentProvider"
        val DB_NANE = "Elec_com.db"
        val DB_TABLE = "Elec_com_NJ"
        val DB_TABLE2 = "Elec_com_DATA"
        val DB_TABLE3 = "Elec_com_RP"
        private val DB_TABLE_ELEC_COM_NJ = 1
        private val DB_TABLE_ELEC_COM_DATA = 2
        private val DB_TABLE_ELEC_COM_RP = 3
        val CONTENT_URI = Uri.parse("content://$AUTHORITY/$DB_TABLE")
        val CONTENT_URI2 = Uri.parse("content://$AUTHORITY/$DB_TABLE2")
        val CONTENT_URI3 = Uri.parse("content://$AUTHORITY/$DB_TABLE3")
        private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH) //初始化不匹配任何返回碼
        lateinit var melec_com: SQLiteDatabase

        /*SQLite欄位說明： ------------------------------------------------
        [id] => 主鍵,通常用來排序
        [barcode] [nvarchar](25) NOT NULL,
        [process_seq] [nvarchar](12) NULL, => 製程
        [rec_qty] [numeric](12, 3) NOT NULL,=>數量
        [app_scan_date] [datetime] NOT null,   => APP 掃到barcode 時間
        [kind] [nvarchar](4) NOT NULL ,=> out委外,in廠內
        [site] [nvarchar](4) NOT NULL, => RP雲科,NJ台北
        [type] [nvarchar](10) NULL, =>sending 發料,receving 收料
        [combine] => 合籠：0=沒合,1=合籠

        下載資料的欄位說明：
        so_date      委外開單日期
        so_nbr       委外單號
        so_nbr_step  委外序號
        sub_sof_item_no      委外料號
        so_um        委外單位
        sub_soi_item_no      黑胚料號
        so_qty       委外黑胚數量
        issue_qty    黑胚已發料數量
        so_status    委外單狀態：OP=核發、IS=已發料、IN=部份入庫、FC=強迫結案
        ------------------------------------------------------------------*/

        private val crTBsql = "CREATE TABLE $DB_TABLE(" +
                                     "id INTEGER PRIMARY KEY," +
                                     "barcode TEXT NOT NULL," +
                                     "process_seq TEXT NOT NULL," +
                                     "rec_qty INTEGER NOT NULL," +
                                     "app_scan_date TEXT NOT NULL," +
                                     "kind TEXT NOT NULL," +
                                     "site TEXT NOT NULL," +
                                     "type TEXT NOT NULL," +
                                     "combine INTEGER NOT NULL," +
                                     "insert_date TEXT NOT NULL," +
                                     "update_flag TEXT NOT NUL)"

        private val crTBsql2 = "CREATE TABLE $DB_TABLE2(" +
                                      "id INTEGER PRIMARY KEY," +
                                      "so_date TEXT NOT NULL," +
                                      "so_nbr TEXT NOT NULL," +
                                      "so_nbr_step INTEGER NOT NULL," +
                                      "sub_sof_item_no TEXT NOT NULL," +
                                      "so_um TEXT NOT NULL," +
                                      "sub_soi_item_no TEXT NOT NULL," +
                                      "so_qty INTEGER NOT NULL," +
                                      "issue_qty TEXT NOT NULL," +
                                      "so_status TEXT NOT NULL)"

        private val crTBsql3 = "CREATE TABLE $DB_TABLE3(" +
                                      "id INTEGER PRIMARY KEY," +
                                      "barcode TEXT NOT NULL," +
                                      "process_seq TEXT NOT NULL," +
                                      "rec_qty INTEGER NOT NULL," +
                                      "app_scan_date TEXT NOT NULL," +
                                      "kind TEXT NOT NULL," +
                                      "site TEXT NOT NULL," +
                                      "type TEXT NOT NULL," +
                                      "combine INTEGER NOT NULL," +
                                      "insert_date TEXT NOT NULL," +
                                      "update_flag TEXT NOT NULL)"
    }


    override fun onCreate(): Boolean {
        sUriMatcher.addURI(AUTHORITY, DB_TABLE, DB_TABLE_ELEC_COM_NJ)
        sUriMatcher.addURI(AUTHORITY, DB_TABLE2, DB_TABLE_ELEC_COM_DATA)
        sUriMatcher.addURI(AUTHORITY, DB_TABLE3, DB_TABLE_ELEC_COM_RP)

        // ---宣告 使用Class DbOpenHelper.java 作為處理SQLite介面
        // Content Provider 就是 data Server, 負責儲存及提供資料, 他允許任何不同的APP使用
        // 共同的資料(不同的APP用同一個SQLite).
        val mDbOpenHelper = DbOpenHelper(context, DB_NANE,null,1)
        melec_com = mDbOpenHelper.writableDatabase

        //檢查資料表是否已經存在，如果不存在，就建立一個。
        val cursor = melec_com.rawQuery("select DISTINCT tbl_name from sqlite_master" +
                                                    " where tbl_name = '$DB_TABLE'",null)
        val cursor2 = melec_com.rawQuery("select DISTINCT tbl_name from sqlite_master" +
                                                     " where tbl_name = '$DB_TABLE2'",null)
        val cursor3 = melec_com.rawQuery("select DISTINCT tbl_name from sqlite_master" +
                                                     " where tbl_name = '$DB_TABLE3'",null)

        // 沒有資料表，要建立一個資料表。
        if (cursor.count == 0) melec_com.execSQL(crTBsql)
        if (cursor2.count == 0) melec_com.execSQL(crTBsql2)
        if (cursor3.count == 0) melec_com.execSQL(crTBsql3)

        cursor.close()
        cursor2.close()
        cursor3.close()

        return true
    }

    override fun getType(uri: Uri): String? {

        when(sUriMatcher.match(uri)){
            DB_TABLE_ELEC_COM_NJ -> return "" + CONTENT_URI
            DB_TABLE_ELEC_COM_DATA -> return "" + CONTENT_URI2
            DB_TABLE_ELEC_COM_RP -> return "" + CONTENT_URI3
            else -> throw IllegalArgumentException("Unknown URI:$uri")
        }
    }

    //直接傳入SQLite語法做查詢
    fun rawquery(context: Context, uri: Uri, melec_com: SQLiteDatabase, sql: String,
                 selectionArgs: Array<String>): Cursor? {
        var c: Cursor? = null

        try {
            c = Elec_comContentProvider.melec_com.rawQuery(sql, selectionArgs)
            c.setNotificationUri(context.contentResolver, uri)

        }catch (e: Exception){
            Log.d(TAG,"error=" + e.toString())
        }

        return c
    }


    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        var c: Cursor? = null

        when(sUriMatcher.match(uri)){
            DB_TABLE_ELEC_COM_NJ -> {
                c = melec_com.query(true, DB_TABLE, projection, selection, selectionArgs,
                        null,null,null,null) //"ASC DESC"
                c.setNotificationUri(context.contentResolver, uri)
            }

            DB_TABLE_ELEC_COM_DATA -> {
                c = melec_com.query(true, DB_TABLE2, projection, selection, selectionArgs,
                        null,null,null,null) //"ASC DESC"
                c.setNotificationUri(context.contentResolver, uri)
            }

            DB_TABLE_ELEC_COM_RP -> {
                c = melec_com.query(true, DB_TABLE3, projection, selection, selectionArgs,
                        null,null,null,null) //"ASC DESC"
                c.setNotificationUri(context.contentResolver, uri)
            }
        }

        return c
    }

    //批次新增(處理大量資料)
    override fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int {
        var numinsert = 0

        when(sUriMatcher.match(uri)){
            DB_TABLE_ELEC_COM_NJ -> {
                melec_com.beginTransaction()

                try {
                    numinsert = (values.size - 1)

                    for (i in 0 until numinsert){
                        val rowID: Long = melec_com.insert(DB_TABLE,null, values[i])
                        ContentUris.withAppendedId(CONTENT_URI, rowID) //在已有的 Uri的後面追加ID數據
                        context.contentResolver.notifyChange(uri,null) //通知數據已經改變
                        if (rowID < 0) throw UnsupportedOperationException("unsupported uri: $uri")
                    }

                    melec_com.setTransactionSuccessful() //加速成功

                }catch (e: Exception){
                    Log.d(TAG,"error=" + e.toString())
                }finally {
                    melec_com.endTransaction() //關閉加速
                }
            }

            DB_TABLE_ELEC_COM_RP -> {
                melec_com.beginTransaction()

                try {
                    numinsert = (values.size - 1)

                    for (i in 0 until numinsert){
                        val rowID: Long = melec_com.insert(DB_TABLE3,null, values[i])
                        ContentUris.withAppendedId(CONTENT_URI3, rowID) //在已有的 Uri的後面追加ID數據
                        context.contentResolver.notifyChange(uri,null) //通知數據已經改變
                        if (rowID < 0) throw UnsupportedOperationException("unsupported uri: $uri")
                    }

                    melec_com.setTransactionSuccessful() //加速成功

                }catch (e: Exception){
                    Log.d(TAG,"error=" + e.toString())
                }finally {
                    melec_com.endTransaction() //關閉加速
                }
            }
        }

        return super.bulkInsert(uri, values)
    }

    //新增
    override fun insert(uri: Uri, values: ContentValues?): Uri? {

        when(sUriMatcher.match(uri)){
            DB_TABLE_ELEC_COM_NJ -> {
                val rowId: Long = melec_com.insert(DB_TABLE,null, values)

                if (rowId > 0){
                    val insertedRowUri = ContentUris.withAppendedId(CONTENT_URI, rowId) //在已有的 Uri的後面追加ID數據
                    context.contentResolver.notifyChange(insertedRowUri,null)
                    return insertedRowUri
                }
            }

            DB_TABLE_ELEC_COM_DATA -> {
                val rowId: Long = melec_com.insert(DB_TABLE2,null, values)

                if (rowId > 0){
                    val insertedRowUri = ContentUris.withAppendedId(CONTENT_URI2, rowId) //在已有的 Uri的後面追加ID數據
                    context.contentResolver.notifyChange(insertedRowUri,null)
                    return insertedRowUri
                }
            }

            DB_TABLE_ELEC_COM_RP -> {
                val rowId: Long = melec_com.insert(DB_TABLE3,null, values)

                if (rowId > 0){
                    val insertedRowUri = ContentUris.withAppendedId(CONTENT_URI3, rowId) //在已有的 Uri的後面追加ID數據
                    context.contentResolver.notifyChange(insertedRowUri,null)
                    return insertedRowUri
                }
            }
        }

        return null
    }

    //修改
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        var rowsAffected = 0

        when(sUriMatcher.match(uri)){
            DB_TABLE_ELEC_COM_NJ -> rowsAffected = melec_com.update(DB_TABLE, values, selection,null)
            DB_TABLE_ELEC_COM_DATA -> rowsAffected = melec_com.update(DB_TABLE2, values, selection,null)
            DB_TABLE_ELEC_COM_RP -> rowsAffected = melec_com.update(DB_TABLE3, values, selection,null)
        }

        return rowsAffected
    }

    //刪除
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        var rowsAffected = 0

        when(sUriMatcher.match(uri)){
            DB_TABLE_ELEC_COM_NJ -> rowsAffected = melec_com.delete(DB_TABLE, selection,null)
            DB_TABLE_ELEC_COM_DATA -> rowsAffected = melec_com.delete(DB_TABLE2, selection,null)
            DB_TABLE_ELEC_COM_RP -> rowsAffected = melec_com.delete(DB_TABLE3, selection,null)
        }

        return rowsAffected
    }

}