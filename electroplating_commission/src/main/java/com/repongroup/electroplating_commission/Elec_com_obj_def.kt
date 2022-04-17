package com.repongroup.electroplating_commission

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentResolver
import android.text.SpannableString
import android.view.View
import com.repongroup.electroplating_commission.providers.Elec_comContentProvider
import java.util.concurrent.Executors

class Elec_com_obj_def {

    //login.main裡共用的宣告
    companion object {
        val TAG = "repongroup=>" //log用
        var sitechoice = 0 //廠別:0=雲科廠,1=台北廠
        var kindchoice = 0 //種類:0=委外,1=廠內
        val singleThreadExecutor = Executors.newSingleThreadExecutor() //創建單一執行緒緒程池;
        var msg = "" //Toast用訊息
        lateinit var progressDialog: ProgressDialog //宣告dialog
        lateinit var progressDialog2: ProgressDialog
        lateinit var msgcolor: SpannableString //宣告字串顏色方法變數
        val handler = android.os.Handler()
        var islongclick = false //記錄是否執行長按動作

        @SuppressLint("StaticFieldLeak")
        lateinit var myview: View //宣告dialog會用到的自定義view
        var exitTime: Long = 0
        var today = "" //記錄今天的日期

        //連線及SQLite相關宣告------------------------------------------------------------------------
        lateinit var mContRes: ContentResolver
        val uri = Elec_comContentProvider.CONTENT_URI2; //NP
        val uri2 = Elec_comContentProvider.CONTENT_URI2; //下載資料
        val uri3 = Elec_comContentProvider.CONTENT_URI3; //預設為RP
        val uris = arrayOf(uri, uri2, uri3) //uri陣列
        val NJCOLUMN = arrayOf("id","barcode","process_seq","rec_qty","app_scan_date",
                "kind","site","type","combine","insert_date",
                "update_flag")
        val DATACOLUMN = arrayOf("id","so_date","so_nbr","so_nbr_step","sub_sof_item_no",
                "so_um","sub_soi_item_no","so_qty","issue_qty","so_status")
        val RPCOLUMN = arrayOf("id","barcode","process_seq","rec_qty","app_scan_date",
                "kind","site","type","combine","insert_date",
                "update_flag")
        val tablecolumn = RPCOLUMN //預設為RP
        val table_name = arrayOf("Elec_com_RP","Elec_com_NJ")
        var myselecion = ""
        var myorder = "id DESC" //排序欄位
        //------------------------------------------------------------------------------------------

    }

}