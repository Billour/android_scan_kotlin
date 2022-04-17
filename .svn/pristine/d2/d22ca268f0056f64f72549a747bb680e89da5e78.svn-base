package com.repongroup.packing_scan.providers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.repongroup.packing_scan.packing_scan_main.Companion.TAG
import com.repongroup.packing_scan.providers.Packing_scanContentProvider.Companion.DB_NANE
import com.repongroup.packing_scan.providers.Packing_scanContentProvider.Companion.DB_TABLE4
import com.repongroup.packing_scan.providers.Packing_scanContentProvider.Companion.DB_VERSION

class DbOpenHelper(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) :
        SQLiteOpenHelper(context, DB_NANE,null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {}

    //資料庫升版：系統自動偵測調用
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "db=$db,oldVersion=$oldVersion,newVersion=$newVersion")

        if (oldVersion == 1 && newVersion == 2){ //舊版為1，新版為2
            db.execSQL("ALTER TABLE packing_scan_SP RENAME TO $DB_TABLE4") //修改南俊商貿資料表名稱
            db.execSQL("UPDATE $DB_TABLE4 SET site = 'SZ' WHERE site = 'SP'") //變更廠別代號為SZ
        }
    }

    //資料庫降版：系統自動偵測調用
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG,"db=$db,oldVersion=$oldVersion,newVersion=$newVersion")

        //舊版資料庫比新版資料庫還新時：使用者安裝舊版本
        if (newVersion == 1){ //回到第一版資料庫時
            db.execSQL("ALTER TABLE $DB_TABLE4 RENAME TO packing_scan_SP") //修改資料表名稱
            db.execSQL("UPDATE packing_scan_SP SET site = 'SP' WHERE site = 'SZ'") //修改廠別代號
            db.execSQL("PRAGMA main.user_version = 1") //設定資料庫版本號：設定版本號為1
        }
    }

}