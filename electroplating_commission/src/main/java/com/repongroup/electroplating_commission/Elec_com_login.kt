package com.repongroup.electroplating_commission

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import com.repongroup.electroplating_commission.DBConnector.Companion.apktxt_ip
import com.repongroup.electroplating_commission.DBConnector.Companion.code
import com.repongroup.electroplating_commission.Elec_com_obj_def.Companion.kindchoice
import com.repongroup.electroplating_commission.Elec_com_obj_def.Companion.progressDialog
import com.repongroup.electroplating_commission.Elec_com_obj_def.Companion.singleThreadExecutor
import com.repongroup.electroplating_commission.Elec_com_obj_def.Companion.sitechoice
import com.repongroup.repon_android.Repon_android.Companion.apkname
import com.repongroup.repon_android.Repon_android.Companion.catchdb
import com.repongroup.repon_android.Repon_android.Companion.checklink
import com.repongroup.repon_android.Repon_android.Companion.langstart
import com.repongroup.repon_android.Repon_android.Companion.reponlog
import com.repongroup.repon_android.Repon_android.Companion.wifilink
import kotlinx.android.synthetic.main.elec_com_login.*
import org.json.JSONArray

class Elec_com_login: AppCompatActivity() {

    private var i = 0 //記錄進度條跑到第幾個資料
    private var pgress_num = 0 //記錄進度條數字
    private lateinit var Site_spinner: ArrayAdapter<String>
    private val site_id = arrayOf("RP","NJ") //宣告對應的廠別ID
    private val kind_id = arrayOf("OUT","IN") //宣告對應的種類ID
    private var j = 0 //記錄要下載的資料筆數
    private var userdownload = 0 //使用者強制下載資料
    private var starttime: Long = 0 //記錄長按時間
    private val context = this
    private var textset = 0 //設定字體大小：0=預設(大),1=中,2=小
    private var T = 0 //字體值=T:textview_size,B:button_size,L:listitem_size,AT:apktextview
    private var B = 0
    private var L = 0
    private var AT = 0

    //spinner自定義選項layout切換使用-----------------------------------------------------------------

    //字體大小改變用：0=選項大,1=選項中,2=選項小
    private val item_style = intArrayOf(R.layout.login_simple_spinner_item,
                                                R.layout.login_simple_spinner_item2,
                                                R.layout.login_simple_spinner_item3)

    //字體大小改變用：0=下拉選項大,1=下拉選項中,2=下拉選項小
    private val dropdown_item_style = intArrayOf(R.layout.dropdown_spinner_item,
                                                         R.layout.dropdown_spinner_item2,
                                                         R.layout.dropdown_spinner_item3)
    //----------------------------------------------------------------------------------------------

    private var loginbtnclick = 0 //標記點擊登入按鈕:1=點擊
    private val TAG = "repongroup=>" //log用

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.elec_com_login)

        catchdb() //抓資料庫執行緒的萬用法
        langstart(context) //起始語言配置
        setupViewComponent() //自定義
    }

    //自定義
    private fun setupViewComponent() {
        var i = apkname(context) //撈取apk訊息
        reponlog("Elec_com_login", "Elec_com_login start.", 1, context) //寫log

        //讀取spinner設定資料------------------------------------------------------------------------
        val textsetdata = getSharedPreferences("TEXT_SET",0)
        sitechoice = textsetdata.getInt("site_set",0) //讀取儲存的廠別選項
        kindchoice = textsetdata.getInt("kind_set",0) //讀取儲存的種類
        //------------------------------------------------------------------------------------------

        textsetstart() //載入字體大小及掃描模式設定
        kind_spinner() //型式spinner項目生成
        pgdialog() //進度條生成
        checklink("Elec_com_login", context) //偵測是否連網

        if (wifilink == 1){ //有連上wifi再執行apk版本檢查及下載資料
            if (i == 1) checkapk() //檢查apk版本
//            dbsql() //下載資料
        }

    }

    //載入字體大小及掃描模式設定
    private fun textsetstart() {

        //讀取資料----------------------------------------------------------------------------
        val textsetdata = getSharedPreferences("TEXT_SET",0)
        T = textsetdata.getInt("T_size",0)
        B = textsetdata.getInt("B_size",0)
        L = textsetdata.getInt("L_size",0)
        AT = textsetdata.getInt("AT_size",0)
        //-----------------------------------------------------------------------------------

        if (T == 0 && B == 0 && L == 0){ //無已存的資料時字體為預設:大
            T = 30
            B = 50
            L = 0
            AT = 26
        }

        elec_com_login_B_login.textSize = T.toFloat()
        elec_com_login_T_checkapk.textSize = T.toFloat()
    }

    //種類spinner項目載入
    private fun kind_spinner() {
        Site_spinner = ArrayAdapter(this, item_style[L],
                resources.getStringArray(R.array.elec_com_login_s_style))
        Site_spinner.setDropDownViewResource(dropdown_item_style[L])
        Site_spinner.notifyDataSetChanged() //綁定更新
        elec_com_login_S_kind.adapter = Site_spinner
        elec_com_login_S_kind.setSelection(kindchoice,true) //預設為委外
    }

    //進度條生成
    private fun pgdialog() {
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("執行中")
        progressDialog.setMessage("正在連接伺服器...")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.setCancelable(false)
    }

    //檢查apk版本
    private fun checkapk() {

        //開工作執行緒檢查apk更新
        singleThreadExecutor.execute{
            val newversion = "version.txt" //要比對的伺服器version資料名稱

            try {
                val result = DBConnector.executedata(apktxt_ip + newversion)
                val jsonArray = JSONArray(result)

                if (jsonArray.length() > 0){

                    for (i in 0 until jsonArray.length()){
                        val jsonData = jsonArray.getJSONObject(i)
                        code = jsonData.getInt("versionCode")

                    }

                }

            }catch (e: Exception){
                Log.d(TAG,"error=" + e.toString())
            }

        }

    }

}