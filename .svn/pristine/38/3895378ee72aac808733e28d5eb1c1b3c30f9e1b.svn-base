package com.repongroup.packing_scan

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import android.widget.*
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.packing_scan_main.*
import com.repongroup.packing_scan.DBConnector.Companion.apktxt_ip
import com.repongroup.packing_scan.DBConnector.Companion.code
import com.repongroup.packing_scan.DBConnector.Companion.connect_ip
import com.repongroup.packing_scan.DBConnector.Companion.file
import com.repongroup.packing_scan.providers.Packing_scanContentProvider
import com.repongroup.packing_scan.providers.Packing_scanContentProvider.Companion.CONTENT_URI
import com.repongroup.packing_scan.providers.Packing_scanContentProvider.Companion.CONTENT_URI3
import com.repongroup.packing_scan.providers.Packing_scanContentProvider.Companion.CONTENT_URI4
import com.repongroup.packing_scan.providers.Packing_scanContentProvider.Companion.db_Packing_scan
import com.repongroup.repon_android.ReponAlertDialog
import com.repongroup.repon_android.Repon_android.Companion.apkname
import com.repongroup.repon_android.Repon_android.Companion.checklink
import com.repongroup.repon_android.Repon_android.Companion.hidekeyboard
import com.repongroup.repon_android.Repon_android.Companion.langnum
import com.repongroup.repon_android.Repon_android.Companion.langstart
import com.repongroup.repon_android.Repon_android.Companion.locale_CN
import com.repongroup.repon_android.Repon_android.Companion.locale_TW
import com.repongroup.repon_android.Repon_android.Companion.locale_save
import com.repongroup.repon_android.Repon_android.Companion.reponlog
import com.repongroup.repon_android.Repon_android.Companion.toast
import com.repongroup.repon_android.Repon_android.Companion.toggleSoftInput
import com.repongroup.repon_android.Repon_android.Companion.wifilink
import com.repongroup.repon_android.Repon_list
import org.json.JSONArray
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.jar.Manifest
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class packing_scan_main: AppCompatActivity() {

    private lateinit var m_text_T_set: TextView
    private var exitTime: Long = 0
    private lateinit var thisview: View

    //SQL資料庫相關----------------------------------------------------------------------------------
    private lateinit var mContRes: ContentResolver
    private val uri = CONTENT_URI //NJ
    private val uri2 = CONTENT_URI3 //RP
    private val uri3 = CONTENT_URI4 //SZ
    private val uris = arrayOf(uri,uri2,uri3)
    private val NJCOLUMN = arrayOf("id","qrcode","check_in_date","insert_date",
                                                 "update_flag","write_flag","site")
    private val RPCOLUMN = arrayOf("id","qrcode","check_in_date","insert_date",
                                                 "update_flag","write_flag","site")
    private val SZCOLUMN = arrayOf("id","qrcode","check_in_date","insert_date",
                                                 "update_flag","write_flag","site")
    private val tablecolume_id = arrayOf(NJCOLUMN, RPCOLUMN, SZCOLUMN) //廠別table代號陣列
    private val table_name = arrayOf("packing_scan_NJ","packing_scan_RP","packing_scan_SZ") //table表名
    private var recSet: ArrayList<String> = ArrayList() //暫存arraylist的值
    private var reclist: ArrayList<String> = ArrayList() //暫存arraylist的值
    private var mList: ArrayList<Map<String, Any>> = ArrayList() //儲存arraylist的值
    private var wList: ArrayList<Map<String, Any>> = ArrayList() //儲存arraylist的值
    //----------------------------------------------------------------------------------------------

//    private lateinit var Site_spinner: ArrayAdapter<String>
    private val site_id = arrayOf("NJ","RP","SZ") //廠別代號陣列
    private var sitechoice = 0 //預設為NJ
    private var today = "" //記錄今天日期
    private var today_time = "" //記錄現在日期與時間
    private var lastday = "" //記錄前一天有資料的日期
    private var secondlastday = "" //記錄前兩天有資料的日期
    private var scan_ok = 0 //記錄當日掃描成功筆數
    private var scannum = "" //記錄掃描到的條碼
    private var subscannum = "" //儲存提取的掃描條碼部分內容
    private var updateclick = 0 //記錄上傳按鈕點擊:1=點擊
    private var uimsg = 0 //儲存worker thread執行時回傳的數值
    private var createlist = 0 //儲存list清單產生狀態:0=未產生,1=產生中
    private var up_i = 0 //update時傳給ui即時更新的訊息數字
    private var updateok = 0 //儲存上傳成功筆數
    private var updateerror = 0 //儲存上傳失敗筆數
    private var updateokrepeat = 0 //儲存已上傳過的筆數
    private var lastupdateok = 0 //記錄昨日沒傳到的資料今日上傳成功
    private var lastupdateerror = 0 //記錄昨日沒傳到的資料今日上傳失敗
    private var textset = 0 //設定字體大小：0=預設(大),1=中,2=小

    //spinner自定義選項layout切換使用-------------------------------------------------------------------------------
    private val item_style = intArrayOf(R.layout.main_simple_spinner_item,
                                                R.layout.main_simple_spinner_item2,
                                                R.layout.main_simple_spinner_item3) //字體大小改變用：0=選項大,1=選項中,2=選項小
    private val dropdown_item_style = intArrayOf(R.layout.dropdown_spinner_item,
                                                         R.layout.dropdown_spinner_item2,
                                                         R.layout.dropdown_spinner_item3) //字體大小改變用：0=下拉選項大,1=下拉選項中,2=下拉選項小
    //------------------------------------------------------------------------------------------------------------

    private lateinit var msgcolor: SpannableString //宣告字串顏色方法變數
    private var textchangestart = 0 //標記字體大小改變:0=未使用,1=使用
    private var scanuse = 0 //字體大小改變用：記錄單筆掃描成功
    private var updateuse = 0 //字體大小改變用：0=未使用update按鈕,1=使用過update按鈕且有設定訊息框(字體大小變更完再復歸)

    //list_item_layout切換使用:item資料的size:0=大(預設),1=中,2=小-----------------------------------------------
    private val list_layout = intArrayOf(R.layout.list_item, R.layout.list_item2, R.layout.list_item3)
    private val item_text_id = intArrayOf(R.id.data_show_large,R.id.data_show_middle,R.id.data_show_small)
    private val write_img_id = intArrayOf(R.id.write_large,R.id.write_middle,R.id.write_small)
    //--------------------------------------------------------------------------------------------------------

    private lateinit var alertDialog: ReponAlertDialog //宣告dialog
    private lateinit var alertDialog2: ReponAlertDialog //宣告dialog
    private lateinit var myview: View //宣告dialog會用到的自定義view
    private var msg = "" //儲存執行緒要給UI Thread的訊息
//    private val locale_CN = Locale.SIMPLIFIED_CHINESE //多國語言：簡體
//    private lateinit var locale: Locale //宣告目前使用的value語系
//    private var locale_save = "" //儲存value語系
//    private var langnum = 0 //標記使用的語言：0=預設繁體,1=簡體
    private var scanrepeat = 0 //標記使用連續掃描模式:1=啟用
    private var no_update = 0 //記錄尚未上傳的資料筆數
    private var writenum = 0 //記錄手動輸入的資料數量
    private var write_flag = 0 //記錄手動輸入標記:0=未使用,1=手動輸入
    private var positionsave = -1 //item選項用
    private var write_page = 0 //標記手動輸入layout:0=未使用,1=在手動輸入頁面
    private var write_edit = 0 //標記手動輸入編輯功能:0=未使用,1=使用中
    private var del_insert_time = "" //記錄要刪除的qrcode資料輸入時間(從list選項取得)
    private var getqrcode = "" //暫存選擇到的qrcode
    private var del_qrcode = "" //暫存選擇欲刪除的qrcode
    private val context = this
    private var islongclick = false //記錄是否執行長按動作
    private var starttime: Long = 0 //記錄長按時間
    private var updateall = 0 //全部資料上傳標記:1=全部重傳
    private val singleThreadExecutor = Executors.newSingleThreadExecutor() //創建單一執行緒緒程池
    private val handler = Handler()
    private val classname: String = "packing_scan_main"

    companion object {
        val PERMISSIONS_REQUEST_CAMERA = 101
        var myselecion = ""
        var myorder = "id DESC" //排序欄位

        //字體值=T:textview_size,B:button_size,L:listitem_size,AT:apktextview
        var T = 0
        var B = 0
        var L = 0
        var AT = 0
        //--------------------------------------------------------------------

        val TAG = "repongroup=>" //Log用
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.packing_scan_main)
        langstart(context) //起始語言配置
        setupViewComponent() //自定義
    }

    //自定義
    private fun setupViewComponent() {
        val i = apkname(context) //撈取apk訊息
        reponlog(classname,"packing_scan_main start.",1, context) //寫log

        //讀取資料-----------------------------------------------------------------------------------
        val textsetdata = getSharedPreferences("TEXT_SET",0)
        sitechoice = textsetdata.getInt("site_set",0) //讀取儲存的廠別選項

        if (sitechoice == 0) packing_scan_main_T_sl.text = getString(R.string.packing_scan_main_t_sl) //NJ
        else if (sitechoice == 1) packing_scan_main_T_sl.text = "SU" //RP

        textsetstart() //載入字體大小及掃描模式設定
        site_spinner() //廠別spinner項目生成
        u_checkcamara() //確認相機權限
        mobiletoday() //取得手機日期
        cleardata() //SQLite僅保留七天資料，其餘清掉
        packing_scan_main_B_listshow.performClick() //使用自動點擊顯示掃描清單
        checklink(classname, context) //偵測是否連網

        if (wifilink == 1 && i == 1) checkapk() //有連上wifi再執行apk版本檢查

        packing_scan_main_S_site.onItemSelectedListener = siteselect()
    }

    //廠別選項監聽
    private fun siteselect(): AdapterView.OnItemSelectedListener = object: AdapterView.OnItemSelectedListener{

        @SuppressLint("SetTextI18n")
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            sitechoice = position //儲存使用者選擇第幾項

            if (sitechoice == 0) packing_scan_main_T_sl.text = getString(R.string.packing_scan_main_t_sl) //NJ
            if (sitechoice == 1) packing_scan_main_T_sl.text = "SU" //RP

            if (write_page == 0){ //不是在手動輸入頁面
                scantoday() //刷新訊息框
                packing_scan_main_B_listshow.performClick() //刷新掃描清單

            }else{
                del_qrcode = "" //變更掃描廠別復歸
                packing_scan_main_B_writedelete.visibility = View.GONE //殺掉SQLite資料刪除按鈕
                packing_scan_main_B_check.visibility = View.VISIBLE //顯示SQLite資料確認按鈕
                thisview = window.decorView
                writebtn(thisview)
//                m_write_B_btn.performClick() //刷新手動輸入清單
            }

            //儲存資料
            val textsetdata = getSharedPreferences("TEXT_SET",0)
            textsetdata.edit().putInt("site_set", sitechoice).apply()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    //SQLite撈掃描資料
    private fun scantoday() {

        //開工作執行緒撈SQLite掃描資料
        singleThreadExecutor.execute {
            mContRes = contentResolver //使用前先GET
            myselecion = "insert_date LIKE '$today'"
            myorder = "id DESC"

            try {
                val cur = mContRes.query(uris[sitechoice], tablecolume_id[sitechoice], myselecion,
                        null, myorder)
                cur.moveToFirst()
                val data = cur.count
                cur.close() //用完關掉

                if (data > 0){ //有資料
                    scan_ok = data //設定筆數
                    myselecion = "insert_date LIKE '$today' AND update_flag LIKE 0" //尚未上傳資料

                    try {
                        val c = mContRes.query(uris[sitechoice], tablecolume_id[sitechoice],
                                myselecion,null, myorder)
                        c.moveToFirst()
                        no_update = c.count
                        c.close() //用完關掉

                    }catch (e: Exception){
                        Log.d(TAG,"error=" + e.toString())
                        val errormsg = "scantoday() 撈SQLite今日掃描且未上傳資料異常:" + e.toString()
                        reponlog(classname, errormsg,0, context) //寫log
                    }

                }else{
                    scan_ok = 0 //無資料復歸
                    no_update = 0 //無資料復歸
                }

            }catch (e: Exception){
                Log.d(TAG,"error=" + e.toString())
                val errormsg = "scantoday() 撈SQLite今日掃描資料異常:" + e.toString()
                reponlog(classname, errormsg,0, context) //寫log
            }

            //更新UI
            runOnUiThread { textmsg() } //設定訊息框
        }

    }

    //設定字串裡字體的顏色
    private fun textmsg() {
        var msg = "本日已掃描${scan_ok}筆資料，尚有${no_update}筆未上傳"
        val msgbluestart = "本日已掃描"
        val msgredstart = "本日已掃描${scan_ok}筆資料，尚有"

        if (langnum == 1) msg = "本日已扫描${scan_ok}笔资料，尚有${no_update}笔未上传" //簡體

        val bluestart = msgbluestart.length
        val blueend = Integer.toString(scan_ok).length
        val redstart = msgredstart.length
        val redend = Integer.toString(no_update).length

        msgcolor = SpannableString(msg)
        msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),
                bluestart, bluestart + blueend,0) //已掃描藍色表示
        msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.red)),
                redstart, redstart + redend,0) //未上傳紅色表示

        packing_scan_main_T_num.setText(msgcolor, TextView.BufferType.SPANNABLE)
    }

    //檢查apk版本
    private fun checkapk() {

        //開工作執行緒檢查apk更新
        singleThreadExecutor.execute {

            //取得目前的apk文件訊息
            val pm = packageManager
            val pi = pm.getPackageInfo(packageName,0)
            val versionname = pi.versionName
            val versioncode = pi.versionCode
//            Log.d(TAG,"版本名稱：$versionname,版本號碼：$versioncode")

            try {
                val result = DBConnector.executedata(apktxt_ip + "version.txt") //連線至伺服器查詢是否有新版本
                val jsonArray = JSONArray(result) //取得資料開始比對

                if (jsonArray.length() > 0){

                    for (i in 0 until jsonArray.length()){ //0 start to < jsonArray.length()
                        val jsonData = jsonArray.getJSONObject(i)
                        code = jsonData.getInt("versionCode")
                        val name = jsonData.getString("versionName")
                        val newversiontime = jsonData.getString("newversiontime")

                        if (code > versioncode && name != versionname){
                            msg = if (langnum == 1) "发现${newversiontime}更新的新版本!\n按一下开始更新。"
                            else "發現${newversiontime}更新的新版本!\n按一下開始更新。"
                            uiupdate(4)

                        }else Log.d(TAG,"已是最新版本：$versionname。")
                    }

                }

            }catch (e: Exception){
                Log.d(TAG,"error=" + e.toString())
                val errormsg = "checkapk() error:" + e.toString()
                reponlog(classname, errormsg,0,context) //寫log
            }

        }

    }

    //執行緒執行時傳遞更新UI的msg
    private fun uiupdate(i: Int) {

        runOnUiThread {

            when(i){
                0,2 -> { //Toast訊息
                    val str = if (i == 0){
                        if (langnum == 1) "资料上传中..." else "資料上傳中..."
                    }else{
                        if (langnum == 1) "查无资料!" else "查無資料!"
                    }

                    Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
                }

                1 -> reponlog(classname,"上傳失敗!",0, context) //寫lag

                //以下為checkapk()使用----------------------------------------------------------------
                3 -> packing_scan_main_T_checkapk.text = msg //apk檢查更新訊息框

                4 -> {
                    packing_scan_main_T_checkapk.visibility = View.VISIBLE //發現新版本時顯示版本訊息框
                    packing_scan_main_T_checkapk.text = msg
                    packing_scan_main_T_checkapk.setBackgroundColor(ContextCompat.getColor(context,
                            R.color.lightpink)) //新版本改底色為粉紅色

                    //設定textview點擊監聽
                    packing_scan_main_T_checkapk.setOnClickListener {
                        val view = layoutInflater.inflate(R.layout.packing_scan_main_apkupdate,null) //自定義Layout:dialog

                        //設定app更新視窗
                        alertDialog = ReponAlertDialog(context)
                        alertDialog.setView(view,0,0,0,
                                0) //設定自定義layout
                        val m_text_T_message2 = view.findViewById(R.id.text_T_message2) as TextView
                        m_text_T_message2.append("\n\n" + getString(R.string.text_T_message2_add))

                        //設定分段字體顏色------------------------------------------------------------------
                        val msg = m_text_T_message2.text.toString()
                        val msgredstart = getString(R.string.text_T_message2_add)
                        val redend = msg.length
                        val redstart = redend - msgredstart.length

                        msgcolor = SpannableString(msg)
                        msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context,
                                R.color.deeppink)), redstart, redend,0) //注意以紅色表示
                        m_text_T_message2.setText(msgcolor, TextView.BufferType.SPANNABLE)
                        //---------------------------------------------------------------------------------

                        alertDialog.show() //務必先show出來才能設定參數

                        //自定義Dialog視窗參數
                        val params = alertDialog.window.attributes //取得dialog參數對象
                        params.width = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog寬度包裹內容
                        params.height = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog高度包裹內容
                        params.gravity = Gravity.CENTER //設置dialog重心
                        alertDialog.window.attributes = params //dialog參數綁定
                    }
                }

                5,7,8 -> { //5:最新版本,7:無法下載,8:錯誤
                    packing_scan_main_T_checkapk.text = msg
                    if (i == 7 || i == 8) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    handler.postDelayed(apktextkill(),5000) //五秒後執行
                }

                6 -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() //無回應
                9 -> packing_scan_main_T_checkapk.visibility = View.GONE

                10,11,12 -> { //10:apk下載成功,11:apk下載失敗,12:出現錯誤
                    if (i == 10) msg = if (langnum == 1) "新版本已下载。" else "新版本已下載。"
                    if (i == 11) msg = if (langnum == 1) "app下载失败!" else "app下載失敗!"
                    if (i == 12) msg = if (langnum == 1) "连接伺服器错误!" else "連接伺服器錯誤!"

                    packing_scan_main_T_checkapk.text = msg
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    handler.postDelayed(apktextkill(),5000) //五秒後執行
                }
            }

        }

    }

    //工作包：殺掉apk檢查更新訊息框
    private fun apktextkill(): Runnable = Runnable {
        uiupdate(9)
    }

    //載入字體大小及掃描模式設定
    private fun textsetstart() {

        //讀取資料----------------------------------------------------------------------------
        val textsetdata = getSharedPreferences("TEXT_SET",0)
        T = textsetdata.getInt("T_size",0)
        B = textsetdata.getInt("B_size",0)
        L = textsetdata.getInt("L_size",0)
        AT = textsetdata.getInt("AT_size",0)
        scanrepeat = textsetdata.getInt("SCAN_type",0) //掃描模式
        //-----------------------------------------------------------------------------------

        if (T == 0 && B == 0 && L == 0){ //無已存的資料時字體為預設:大
            T = 30
            B = 50
            L = 0
            AT = 26
        }

        packing_scan_main_T_site.textSize = T.toFloat() //掃描廠別
        packing_scan_main_T_scan.textSize = T.toFloat() //掃描日期
        packing_scan_main_T_date.textSize = T.toFloat() //今天日期
        packing_scan_main_T_num.textSize = T.toFloat() //訊息顯示框
        packing_scan_main_B_update.textSize = B.toFloat() //上傳
        packing_scan_main_B_scan.textSize = B.toFloat() //掃描
        packing_scan_main_B_scanrepeat.textSize = B.toFloat() //連續掃描
        packing_scan_main_B_check.textSize = B.toFloat() //手動輸入確認按鈕
        packing_scan_main_B_listshow.textSize = B.toFloat() //列表
        packing_scan_main_T_sl.textSize = B.toFloat() //手動輸入固定開頭文字：SU
        packing_scan_main_E_write.textSize = B.toFloat() //手動輸入框
        packing_scan_main_B_writecancel.textSize = B.toFloat() //手動輸入取消按鈕
        packing_scan_main_B_writedelete.textSize = B.toFloat() //手動輸入刪除按鈕
        packing_scan_main_T_checkapk.textSize = AT.toFloat() //apk檢查更新訊息框

        if (scanrepeat == 1){ //連續掃描模式
            packing_scan_main_B_scan.visibility = View.GONE //殺掉單次掃描按鈕
            packing_scan_main_B_scanrepeat.visibility = View.VISIBLE //顯示連續掃描按鈕
        }
    }

    //廠別spinner項目生成
    private fun site_spinner() {
        val Site_spinner = ArrayAdapter(context, item_style[L],
                resources.getStringArray(R.array.packing_scan_firstuse_s_sitelist))
        Site_spinner.setDropDownViewResource(dropdown_item_style[L])
        Site_spinner.notifyDataSetChanged() //綁定更新
        packing_scan_main_S_site.adapter = Site_spinner
        packing_scan_main_S_site.setSelection(sitechoice,true) //預設為雲科廠
    }

    //相機權限確認
    private fun u_checkcamara() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(android.Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), PERMISSIONS_REQUEST_CAMERA)
        }
    }

    //取得手機日期
    @SuppressLint("SimpleDateFormat")
    private fun mobiletoday(){
        val formatter = SimpleDateFormat("yyyy-M-d")
        today = formatter.format(Date()) //今天日期
        Log.d(TAG,"today=$today")
        packing_scan_main_T_date.text = today //今天日期

        //開工作執行緒撈SQLite掃描資料取得昨天日期:上傳完整理SQLite_db用
        singleThreadExecutor.execute {

            //SQLite撈有資料的最近三天,取出存入陣列
            mContRes = contentResolver
//            myselecion = ""
            myorder = "id DESC" //降冪排列

            try {
                val cur = mContRes.query(uris[sitechoice], tablecolume_id[sitechoice],
                        null,null, myorder)
                cur.moveToFirst()
                val data = cur.count

                if (data > 0){ //有資料

                    //設定暫存Array內容
                    reclist = ArrayList() //清空
                    var str = "" //記錄上一比string

                    while (!cur.isAfterLast){
                        val fldSet = cur.getString(3)

                        if (str != fldSet){ //insert日期不同時再紀錄
                            reclist.add(fldSet)
                            str = fldSet
                        }

                        cur.moveToNext()
                    }

                    cur.close() //用完關掉

                    if (reclist.size >= 2){
                        lastday = reclist[1] //記錄前一天有資料的日期

                        if (reclist.size > 2) //大於2時才有第二個工作天掃描資料
                            secondlastday = reclist[2] //記錄前兩天有資料的日期
                    }
                    Log.d(TAG,"lastday=$lastday,secondlastday=$secondlastday")

                }else cur.close()

            }catch (e: Exception){
                Log.d(TAG,"error=" + e.toString())
                val errormsg = "mobiletoday() 撈SQLite資料錯誤:" + e.toString()
                reponlog(classname, errormsg,0, context) //寫log
            }

        }

    }

    //SQLite僅保留七天資料，其餘清掉
    private fun cleardata() {

        //開單一執行緒在背景執行清資料
        singleThreadExecutor.execute {
            val sqlite = "SELECT insert_date FROM ${table_name[sitechoice]} ORDER BY id DESC"

            try {
                val cur = Packing_scanContentProvider.rawquery(context, uris[sitechoice],
                        db_Packing_scan, sqlite, emptyArray())
                cur.moveToFirst()
                val data = cur.count

                if (data > 0){

                    //設定暫存Array內容
                    reclist = ArrayList() //清空
                    var str = "" //記錄上一筆string

                    while (!cur.isAfterLast){
                        val fldSet = cur.getString(0)

                        if (str != fldSet){ //insert日期不同時再紀錄
                            reclist.add(fldSet) //存放到arraylist中
                            str = fldSet
                        }

                        cur.moveToNext()
                    }

                    val j = reclist.size

                    if (j > 7){
                        mContRes = contentResolver //使用前先get

                        for (i in 7..(j - 1)){
                            myselecion = "insert_date = '${reclist[i]}'"
                            val k = mContRes.delete(uris[sitechoice], myselecion, emptyArray())

                            if (k == 0) break //出現錯誤就停止
                        }
                    }

                }

                cur.close()

            }catch (e: Exception){
                Log.d(TAG,"error=" + e.toString())
                val errormsg = "cleardata() 撈SQLite資料異常:" + e.toString()
                reponlog(classname, errormsg,0, context) //寫log
            }

        }

    }

    //改變字體dialog取消按鈕
    fun canclebtn(view: View){
        val str = if (langnum == 1) "字体大小未变更" else "字體大小未變更" //1:簡體
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
        alertDialog2.dismiss() //關閉dialog
    }

    //改變字體dialog確認按鈕
    fun checkbtn(view: View){
        textchange() //改變字體大小
        val str = if (langnum == 1) "字体大小设定完成！" else "字體大小設定完成！" //1:簡體
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
        alertDialog2.dismiss()
    }

    //dialog取消按鈕2
    fun canclebtn2(view: View){
        handler.postDelayed(apktextkill(),2000)
        packing_scan_main_T_checkapk.isEnabled = false //取消監聽
        toast(context,"下次再更新。","short")
        alertDialog.dismiss() //關閉dialog
    }

    //dialog更新按鈕
    fun updateapkbtn(view: View){
        msg = if (langnum == 1) "开始下载更新档，请稍后..." else "開始下載更新檔，請稍後..." //1:簡體
        packing_scan_main_T_checkapk.text = msg
        packing_scan_main_T_checkapk.isEnabled = false //取消監聽
        toast(context, msg,"short")
        alertDialog.dismiss() //關閉dialog
        downloadapk() //下載新版apk
    }

    //dialog改變字體大小按鈕
    fun textbtn(view: View){
        myview = layoutInflater.inflate(R.layout.packing_scan_main_menu,null) //自定義Layout:dialog
        m_text_T_set = myview.findViewById(R.id.text_T_set) //注意要帶view表示自定義layout

        //設定字體變更視窗
        alertDialog2 = ReponAlertDialog(context)
        alertDialog2.setView(myview,0,0,0,0) //設定自定義layout
        alertDialog2.show() //務必先show出來才能設定參數

        //自定義Dialog視窗參數
        val params = alertDialog2.window.attributes //取得dialog參數對象
        params.width = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog寬度包裹內容
        params.height = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog高度包裹內容
        params.gravity = Gravity.CENTER //設置dialog重心
        alertDialog2.window.attributes = params //dialog參數綁定

        alertDialog.dismiss() //關閉menu_choice dialog
    }

    @SuppressLint("SetTextI18n")
    //選擇字體"小"的按鈕
    fun smallbtn(view: View){
        textset = 2
        m_text_T_set.text = getString(R.string.text_t_set) + 26 //設定字體sp
        val str = if (langnum == 1) "设定字体为-小" else "設定字體為-小" //1:簡體
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("SetTextI18n")
    //選擇字體"中"的按鈕
    fun middlebtn(view: View){
        textset = 1
        m_text_T_set.text = getString(R.string.text_t_set) + 38 //設定字體sp
        val str = if (langnum == 1) "设定字体为-中" else "設定字體為-中" //1:簡體
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("SetTextI18n")
    //選擇字體"大"的按鈕
    fun largebtn(view: View){
        textset = 0
        m_text_T_set.text = getString(R.string.text_t_set) + 50 //設定字體sp
        val str = if (langnum == 1) "设定字体为-大" else "設定字體為-大" //1:簡體
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
    }

    //dialog變更語言按鈕
    fun langbtn(view: View){
        myview = layoutInflater.inflate(R.layout.packing_scan_main_menu_text,null) //自定義Layout:dialog

        //設定變更語言視窗
        alertDialog2 = ReponAlertDialog(context)
        alertDialog2.setView(myview,0,0,0,0) //設定自定義layout
        alertDialog2.show()

        //自定義Dialog視窗參數
        val params = alertDialog2.window.attributes //取得dialog參數對象
        params.width = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog寬度包裹內容
        params.height = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog高度包裹內容
        params.gravity = Gravity.CENTER //設置dialog重心
        alertDialog2.window.attributes = params //dialog參數綁定

        alertDialog.dismiss() //關閉menu_choice dialog
    }

    //dialog繁體中文按鈕
    fun TWbtn(view: View){

        if (locale_save != locale_TW.toString()){ //不等於繁體中文時

            //變更設定檔
            val resources = resources
            val config = resources.configuration

            //依照版本不同執行：JELLY_BEAN_MR1 = 4.3
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
                val wrapper = ContextThemeWrapper()
                config.setLocale(locale_TW)
                wrapper.applyOverrideConfiguration(config)

            }else {
                val dm = resources.displayMetrics
                config.locale = Locale.TRADITIONAL_CHINESE //設定為繁體中文
                resources.updateConfiguration(config, dm)
            }

            //儲存語言選擇---------------------------------------------------------------------------
            val textsetdata = getSharedPreferences("TEXT_SET",0)
            textsetdata.edit().putString("locale_set", locale_TW.toString()).apply()
            //--------------------------------------------------------------------------------------

            val it = Intent()
            it.setClass(context, packing_scan_main::class.java)
            startActivity(it)
            this.finish()
        }

        Toast.makeText(this, "已變更語言為繁體中文", Toast.LENGTH_SHORT).show()
        alertDialog2.dismiss() //關閉menu_text dialog
    }

    //dialog簡體中文按鈕
    fun CNbtn(view: View){

        if (locale_save != locale_CN.toString()){ //不等於簡體中文時

            //變更設定檔
            val resources = resources
            val config = resources.configuration

            //依照版本不同執行：JELLY_BEAN_MR1 = 4.3
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
                val wrapper = ContextThemeWrapper()
                config.setLocale(locale_CN)
                wrapper.applyOverrideConfiguration(config)

            }else {
                val dm = resources.displayMetrics
                config.locale = Locale.SIMPLIFIED_CHINESE //設定為簡體中文
                resources.updateConfiguration(config, dm)
            }

            //儲存語言選擇---------------------------------------------------------------------------
            val textsetdata = getSharedPreferences("TEXT_SET",0)
            textsetdata.edit().putString("locale_set", locale_CN.toString()).apply()
            //--------------------------------------------------------------------------------------

            val it = Intent()
            it.setClass(context, packing_scan_main::class.java)
            startActivity(it)
            this.finish()
        }

        toast(context,"已变更于言为简体中文","short")
        alertDialog2.dismiss() //關閉menu_text dialog
    }

    //更新說明按鈕
    fun release_notesbtn(view: View){
        myview = layoutInflater.inflate(R.layout.packing_scan_main_update_note,
                null) //自定義Layout:dialog

        //設定變更語言視窗
        alertDialog2 = ReponAlertDialog(context)
        alertDialog2.setView(myview,0,0,0,0) //設定自定義layout
        val m_text_T_msg = myview.findViewById<TextView>(R.id.text_T_msg) //宣告dialog上的物件
        var newmsg: String = ""

        if (langnum == 1) { //簡體
            newmsg = "2018/04/26更新：\n修正全新安装APP使用第二天发生程式闪退问题\n\n"
            msg = newmsg
            msg += "2018/04/20新增：\n(1)重新上传资料于右上角选单中\n(2)修正部分程式错误\n\n"
            msg += "2018/03/28更新：\n(1)选择厂别时更新扫描清单(含手动输入模式)\n(2)修正部分程式错误\n\n"
            msg += "2018/03/15新增：\n厂别选单，可选择要扫描的厂别\n\n"
            msg += "2018/02/27更新：\n修正扫描日期显示错误及部分程式问题\n\n"
            msg += "2018/02/26更新：\n(1)手动输入模式输入框，数字对多输入五位\n(2)修正自动下载APK提示字串\n\n"
            msg += "2018/02/23新增：\n手动输入模式于右上角的选单中\n\n"
            msg += "2018/02/06更新：\n列表清单仅显示未上传的资料、上传时检查上个工作日未上传的资料一并上传\n\n"
            msg += "2018/01/26新增：\n开闪光灯，关闪光灯，自动补光三种模式\n\n"
            msg += "2018/01/23新增：\n连续扫描按钮于右上角选单中\n\n"
            msg += "2018/01/22新增：\n说明按钮于右上角选单中\n\n"
            msg += "2018/01/19新增：\n多国语言功能，透过右上角选单钮可进行语言切换\n\n"
            msg += "2018/01/18新增：\napp线上更新功能\n"
        } else {
            newmsg = "2018/04/26更新：\n修正全新安裝APP使用第二天發生程式閃退問題\n\n"
            msg = newmsg
            msg += "2018/04/20新增：\n(1)重新上傳資料於右上角選單中\n(2)修正部分程式錯誤\n\n"
            msg += "2018/03/28更新：\n(1)選擇廠別時更新掃描清單(含手動輸入模式)\n(2)修正部分程式錯誤\n\n"
            msg += "2018/03/15新增：\n廠別選單，可選擇要掃描的廠別\n\n"
            msg += "2018/02/27更新：\n修正掃描日期顯示錯誤及部分程式問題\n\n"
            msg += "2018/02/26更新：\n(1)手動輸入模式輸入框，數字對多輸入五位\n(2)修正自動下載apk提示字串\n\n"
            msg += "2018/02/23新增：\n手動輸入模式於右上角的選單中\n\n"
            msg += "2018/02/06更新：\n列表清單僅顯示未上傳的資料、上傳時檢查上個工作日未上傳的資料一併上傳\n\n"
            msg += "2018/01/26新增：\n開閃光燈、關閃光燈、自動補光三種模式\n\n"
            msg += "2018/01/23新增：\n連續掃描按鈕於右上角選單中\n\n"
            msg += "2018/01/22新增：\n說明按鈕於右上角選單中\n\n"
            msg += "2018/01/19新增：\n多國語言功能，透過右上角選單鈕可進行語言切換\n\n"
            msg += "2018/01/18新增：\napp線上更新功能\n"
        }

        val blueend = newmsg.length

        msgcolor = SpannableString(msg)
        msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),0,
                blueend,0) //已掃描藍色表示
        m_text_T_msg.setText(msgcolor, TextView.BufferType.SPANNABLE)
        alertDialog2.show() //務必先show出來才能設定參數

        //自定義Dialog視窗參數
        val params = alertDialog2.window.attributes //取得dialog參數對象
        params.width = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog寬度包裹內容
        params.height = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog高度包裹內容
        params.gravity = Gravity.CENTER //設置dialog重心
        alertDialog2.window.attributes = params //dialog參數綁定

        alertDialog.dismiss() //關閉menu_choice dialog
    }

    //更新說明確認紐
    fun okbtn(view: View){
        alertDialog2.dismiss() //關閉main_update_note dialog
    }

    //掃描按鈕
    fun scan(view: View){
        msg = if (langnum == 1) "扫描QR-Code" else "掃描QR-Code" //1:簡體

        /*
        創建一個IntentIntegrator對象並設置相應的屬性，然後調用initiateScan方法即可開啟相機掃描。
        setDesiredBarcodeFormats:設置掃描type，未指定則自動偵測。QR_CODE_TYPES:OR-Code
        setPrompt:設定掃描框最下面的文字
        setCameraId(0):設定相機，0為後鏡頭，1為前鏡頭
        initiateScan():掃描method
        */
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES) //所有格式
        integrator.setPrompt(msg)
        integrator.setCameraId(0) //使用相機前鏡頭或後鏡頭
        integrator.setBeepEnabled(true) //掃描條碼撥放提示音
        if (scanrepeat == 0) integrator.setTimeout(15000) //單次掃描時設定:設定15秒自動關閉
        integrator.captureActivity = packing_scan_usezxing::class.java //使用直式的掃描視窗
        integrator.initiateScan()
        handler.post(apktextkill()) //使用掃瞄功能即關掉新版本下載提示框
    }

    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null){
            val msg: String //訊息
            val msg2: String
            val msg3: String

            if (langnum == 1) { //簡體
                msg = "扫描取消"
                msg2 = "扫描成功"
                msg3 = "扫描失败!请扫描正确的QR-Code"

            } else {
                msg = "掃描取消"
                msg2 = "掃描成功"
                msg3 = "掃描失敗!請掃描正確的QR-Code"
            }

            if (result.contents == null){

                if (scanrepeat == 1){ //啟用連續掃描
                    packing_scan_main_I_scanimg.visibility = View.GONE //殺掉掃描結果圖示
                    packing_scan_main_R_rel.visibility = View.VISIBLE //顯示main_rel
                    scantoday() //重新載入已掃描且未上傳的訊息框
                    packing_scan_main_B_listshow.performClick() //使用自動點擊顯示掃描清單
                }

                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

            }else {
                scannum = "" //使用前清空
                scannum = result.contents
                mobiletoday() //重新獲取今天日期

                //獲取現在時間
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val curDate = Date(System.currentTimeMillis()) //獲取當前時間
                today_time = formatter.format(curDate)

                if (scannum.length == 14){

//                    //寫入大量資料做中介程式及資料庫壓力測試------------------------------------------
//                    for (k in 10 until 17){ //一次寫七天的資料
//                        var str = "SU17010000"
//                        today_time = "2018-04-"
//
//                        str += k.toString()
//                        today = today_time + k.toString()
//                        today_time += k.toString() + " 00:00:00";
//                        scannum = ""
//
//                        for (i in 1 until 200){
//
//                            when{
//                                i < 10 -> scannum = str + "00" + i.toString()
//                                i < 100 -> scannum = str + "0" + i.toString()
//                                else -> scannum = str + i.toString()
//                            }
//
//                            dbinsert() //加資料進SQLite
//                        }
//
//                    }
//                    //----------------------------------------------------------------------------

                    write_flag = 0 //非手動輸入
                    dbinsert() //加資料進SQLite
                    Toast.makeText(this, msg2 + result.contents, Toast.LENGTH_SHORT).show()
                    scantoday() //重新檢查掃描狀態
                    dbsqlite_scan() //列出單筆存進SQLite的資料

                    if (scanrepeat == 1){ //啟用連續掃描
                        packing_scan_main_R_rel.visibility = View.GONE //殺掉main_rel
                        packing_scan_main_I_scanimg.visibility = View.VISIBLE //顯示掃描結果圖示
                        packing_scan_main_I_scanimg.setImageResource(R.drawable.ok_img) //有掃到設定ok圖示
                        handler.postDelayed(scanscan(),1000)
                    }

                }else {
                    Toast.makeText(context, msg3, Toast.LENGTH_SHORT).show()

                    if (scanrepeat == 1){ //啟用連續掃描
                        packing_scan_main_R_rel.visibility = View.GONE //殺掉main_rel
                        packing_scan_main_I_scanimg.visibility = View.VISIBLE //顯示掃描結果圖示
                        packing_scan_main_I_scanimg.setImageResource(R.drawable.ok_img) //有掃到設定ok圖示
                        handler.postDelayed(scanscan(),2000)
                    }

                }

            }

        }else super.onActivityResult(requestCode, resultCode, data)

    }

    //連續掃描模式按鈕
    fun scanrepeatbtn(view: View){
        scanrepeat = 1 //標記連續掃描啟用
        packing_scan_main_B_scan.visibility = View.GONE //殺掉單次掃描按鈕
        packing_scan_main_B_scanrepeat.visibility = View.VISIBLE //顯示連續掃描按鈕
        alertDialog.dismiss() //關掉dialog

        //儲存掃描模式---------------------
        val textsetdata = getSharedPreferences("TEXT_SET",0)
        textsetdata.edit().putInt("SCAN_type", scanrepeat).apply()
        //---------------------------------

        val str = if (langnum == 1) "***启用连续扫描模式***" else "***啟用連續掃描模式***"
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
    }

    //一般掃描模式
    fun scanbtn(view: View){
        scanrepeat = 0 //標記連續掃描關閉
        packing_scan_main_B_scan.visibility = View.VISIBLE //顯示單次掃描按鈕
        packing_scan_main_B_scanrepeat.visibility = View.GONE //殺掉連續掃描按鈕
        packing_scan_main_R_rel.visibility = View.VISIBLE //顯示main_rel
        packing_scan_main_I_scanimg.visibility = View.GONE //殺掉掃描結果圖示
        alertDialog.dismiss() //關掉dialog

        //儲存掃描模式-------------------------------------------------------------------------
        val textsetdata = getSharedPreferences("TEXT_SET",0)
        textsetdata.edit().putInt("SCAN_type", scanrepeat).apply()
        //------------------------------------------------------------------------------------

        val str = if (langnum == 1) "***启用一般扫描模式***" else "***啟用一般掃描模式***"
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
    }

    //列表清單按鈕
    fun scanlist(view: View){

        if (createlist == 0){

            //開工作執行緒撈SQLite掃描資料
            singleThreadExecutor.execute {
                var msgnum = 0
                var data_upno = 0 //記錄有無未上傳資料

                createlist = 1 //標記開始產生清單
                mContRes = contentResolver
                myselecion = "insert_date LIKE '$today'" //撈取今天的掃描資料  AND update_flag LIKE 0
                myorder = "check_in_date DESC"

                try {
                    val cur = mContRes.query(uris[sitechoice], tablecolume_id[sitechoice],
                            myselecion,null, myorder)
                    cur.moveToFirst()
                    val data = cur.count
                    cur.close() //確認有資料即可關閉

                    if (data > 0){ //有資料
                        msgnum = 1 //設定
                        scanuse = 0 //掃描成功並輸出資料復歸(字體變更用)
                        myselecion = "insert_date LIKE '$today' AND update_flag LIKE 0" //撈取今天未上傳的掃描資料
                        myorder = "check_in_date DESC"
                        val c = mContRes.query(uris[sitechoice], tablecolume_id[sitechoice],
                                myselecion,null, myorder)
                        c.moveToFirst()
                        data_upno = c.count

                        if (data_upno > 0){ //有未上傳的資料

                            //設定listview內容
                            recSet = ArrayList() //重設recSet的值為空
                            val columnCount = c.columnCount
                            mList = ArrayList() //重設mList的值為空

                            while (!c.isAfterLast){
                                val fldSet = StringBuilder()

                                for (ii in 0 until columnCount){
                                    fldSet.append(c.getString(ii))
                                    if (ii < columnCount) fldSet.append("#")
                                }

                                recSet.add(fldSet.toString()) //存放到arraylist中
                                c.moveToNext()
                            }

                            val msg = if (langnum == 1) "\n扫描时间：" else "\n掃描時間：" //1:簡體

                            for (i in 0 until recSet.size){
                                val item = HashMap<String, Any>()
                                val fld = recSet[i].split("#").toTypedArray()
                                item["textview"] = fld[1] + msg + fld[2]
                                item["img"] = if (fld[5] == "0") "" else R.drawable.pencil
                                mList.add(item)
                            }

                        }else mList = ArrayList() //都傳過了清空list

                        c.close() //用完關掉

                    }else mList = ArrayList() //重設mList的值為空

                }catch (e: Exception){
                    Log.d(TAG,"error=" + e.toString())
                    val errormsg = "scanlist 撈SQLite資料異常:" + e.toString()
                    reponlog(classname, errormsg,0, context) //寫log
                }

                //更新UI
                runOnUiThread {
                    var msg = "" //訊息
                    var msg2 = ""

                    if (langnum == 1) { //簡體
                        msg = "本日无扫描资料"
                        msg2 = "已更新清单"

                    } else {
                        msg = "本日無掃描資料"
                        msg2 = "已更新清單"
                    }

                    if (msgnum == 1){

                        //===========設定Listview==========//
                        val adapter = SimpleAdapter(
                                context,
                                mList,
                                list_layout[L],
                                arrayOf("textview", "img"),
                                intArrayOf(item_text_id[L], write_img_id[L])
                        )
                        //----------------------------------

                        adapter.notifyDataSetChanged() //通知UI更新數據
                        packing_scan_main_L_menu.adapter = adapter
                        packing_scan_main_L_menu.isEnabled = true

                        if (textchangestart == 1){ //有使用變更字體大小時
                            val str = packing_scan_main_T_num.text.toString().trim()
                            val substr = str.substring(2,3) //抓取現在訊息框中的第三個字

                            if (substr == "已") scantoday() //重新檢查今日掃描狀態
                            else if (substr == "掃" || substr == "扫") textupdatemsg()

                            textchangestart = 0 //使用完復歸

                        }else {
                            if (updateuse == 1){ //有使用上傳功能時
                                textupdatemsg() //設定訊息框
                                updateuse = 0 //設定完復歸

                            }else scantoday() //重新檢查今日掃描狀態
                        }

                    }else {
                        scantoday() //撈取當日掃描成功筆數設定給訊息框

                        //===========設定Listview==========//
                        val adapter = SimpleAdapter(
                                context,
                                mList,
                                list_layout[L],
                                arrayOf("textview", "img"),
                                intArrayOf(item_text_id[L], write_img_id[L])
                        )
                        //--------------------
                        adapter.notifyDataSetChanged() //通知UI更新數據
                        packing_scan_main_L_menu.adapter = adapter
                        packing_scan_main_L_menu.isEnabled = true
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }

                    Toast.makeText(context, msg2, Toast.LENGTH_SHORT).show()
                    createlist = 0 //清單產生結束重設
                }

            }

        }else {
            val str = if (langnum == 1) "扫描清单产生中，请稍后..." else "掃描清單產生中，請稍後..." //1:簡體
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
        }

    }

    //手動輸入模式
    fun writebtn(view: View){
        mobiletoday() //更新今天日期(防止使用者未關機也未關閉程式)

        singleThreadExecutor.execute {
            mContRes = contentResolver //使用前GET
            myorder = "id DESC" //降冪排列(大到小排序)

            try {
                val cur = mContRes.query(uris[sitechoice], tablecolume_id[sitechoice],
                        null,null, myorder)
                cur.moveToFirst()
                val data = cur.count

                if (data > 0){ //有資料

                    //設定更新失敗的listview內容
                    recSet = ArrayList() //重設recSet的值為空
                    val columnCount = cur.columnCount

                    if (!cur.isAfterLast){ //只放入第一筆資料
                        val fldSet = StringBuilder()

                        for (ii in 0 until columnCount){
                            fldSet.append(cur.getString(ii))
                            if (ii < columnCount) fldSet.append("#")
                        }

                        recSet.add(fldSet.toString()) //存放到arraylist中
                    }

                    val fld = recSet[0].split("#").toTypedArray() //只放入第一筆資料
                    scannum = fld[1]
                    subscannum = scannum.substring(2,7) //扣掉SU取前五碼，如:12345

                }else subscannum = "" //無資料復歸

                cur.close() //用完關掉

            }catch (e: Exception){
                Log.d(TAG,"error=" + e.toString())
                val errormsg = "writebtn() 撈SQLite資料異常:" + e.toString()
                reponlog(classname, errormsg,0, context) //寫log
            }

            myselecion = "update_flag LIKE 0 AND write_flag LIKE 1" //條件:未上傳且為手動輸入
            myorder = "id DESC"

            try {
                val c = mContRes.query(uris[sitechoice], tablecolume_id[sitechoice], myselecion,
                        null, myorder)
                c.moveToFirst()
                val data2 = c.count

                if (data2 > 0){ //有資料
                    writenum = data2

                    //設定listview內容
                    recSet = ArrayList() //重設recSet的值為空
                    val columnCount = c.columnCount
                    wList = ArrayList() //重設mList的值為空

                    while (!c.isAfterLast){
                        val fldSet = StringBuilder()

                        for (ii in 0 until columnCount){
                            fldSet.append(c.getString(ii))
                            if (ii < columnCount) fldSet.append("#")
                        }

                        recSet.add(fldSet.toString()) //存放到arraylist中
                        c.moveToNext()
                    }

                    c.close()
                    val msg = if (langnum == 1) "\n输入时间：" else "\n輸入時間："

                    for (i in 0 until recSet.size){
                        val item = HashMap<String, Any>()
                        val fld = recSet[i].split("#").toTypedArray()
                        item["textview"] = fld[1] + msg + fld[2]
                        item["img"] = if (fld[5] == "0") "" else R.drawable.pencil
                        wList.add(item)
                    }

                }else {
                    wList = ArrayList() //重設mList的值為空
                    c.close()
                }

            }catch (e: Exception){
                Log.d(TAG,"error=" + e.toString())
                val errormsg = "writebtn() -> 未上傳且為手動輸入,撈SQLite資料異常:" + e.toString()
                reponlog(classname, errormsg,0, context) //寫log
            }

            //單執行緒執行到最後更新UI thread
            runOnUiThread {
                packing_scan_main_B_scan.visibility = View.GONE
                packing_scan_main_B_scanrepeat.visibility = View.GONE
                packing_scan_main_L_menu.visibility = View.GONE
                packing_scan_main_B_listshow.visibility = View.GONE
                packing_scan_main_B_check.visibility = View.VISIBLE
                packing_scan_main_T_sl.visibility = View.VISIBLE
                packing_scan_main_E_write.visibility = View.VISIBLE
                packing_scan_main_B_writecancel.visibility = View.VISIBLE
                packing_scan_main_L_menuwrite.visibility = View.VISIBLE

                write_page = 1 //標記使用手動輸入頁面

                if (getqrcode != ""){
                    packing_scan_main_E_write.setText(getqrcode) //設定使用者選擇的qrcode
                    getqrcode = "" //設定完復歸

                }else if (subscannum != ""){
                    packing_scan_main_E_write.setText(subscannum)
                    subscannum = "" //設定完復歸
                }

                packing_scan_main_E_write.requestFocus(9) //取得焦點:第8位
                toggleSoftInput(view) //打開軟鍵盤

                var msg = ""
                var msg2 = ""

                if (langnum == 1) { //簡體
                    msg = "请输入QR-Code号码，输入完请按「确认」键！"
                    msg2 = "***启用手动输入模式***"

                } else {
                    msg = "請輸入QR-Code號碼，輸入完請按「確認」鍵！"
                    msg2 = "***啟用手動輸入模式***"
                }

                //對話框內容上色--------------------------------------------------------------
                val bluestart = "請輸入".length
                val blueend = "請輸入QR-Code".length
                val redstart = "請輸入QR-Code號碼，輸入完請按「".length
                val redend = "請輸入QR-Code號碼，輸入完請按「確認".length

                setwritecolor(msg, bluestart, blueend, redstart, redend) //對話框內容上色
                //--------------------------------------------------------------------------

                //================= 設定Listview ==================//
                val adapter = SimpleAdapter(
                        context,
                        wList,
                        list_layout[L],
                        arrayOf("textview", "img"),
                        intArrayOf(item_text_id[L], write_img_id[L])
                )
                //--------------------------------------------------

                adapter.notifyDataSetChanged() //通知UI更新數據
                packing_scan_main_L_menuwrite.adapter = adapter
                packing_scan_main_L_menuwrite.isEnabled = true
                packing_scan_main_L_menuwrite.onItemClickListener = writeitem() //手動輸入選項監聽
                Toast.makeText(context, msg2, Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            }

        }

    }

    //訊息框裡的字串上顏色
    private fun setwritecolor(msg: String, bluestart: Int, blueend: Int, redstart: Int, redend: Int) {
        msgcolor = SpannableString(msg)
        msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),
                bluestart, blueend, 0) //輸入資料藍色表示
        msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.red)),
                redstart, redend, 0) //未上傳紅色表示
        packing_scan_main_T_num.setText(msgcolor, TextView.BufferType.SPANNABLE)
    }

    //手動輸入列表清單選項監聽
    private fun writeitem(): AdapterView.OnItemClickListener? = AdapterView.OnItemClickListener { parent, view, position, _ ->
        write_edit = 1 //標記編輯中
        val msg = if (langnum == 1) "启用资料刪除功能，欲删除资料请按「删除」键！"
        else "啟用資料刪除功能，欲刪除資料請按「刪除」鍵！"

        //對話框內容上色--------------------------------------------------------------
        val bluestart = "啟用".length
        val blueend = "啟用資料刪除".length
        val redstart = "啟用資料刪除功能，欲刪除資料請按「".length
        val redend = "啟用資料刪除功能，欲刪除資料請按「刪除".length

        setwritecolor(msg, bluestart, blueend, redstart, redend) //對話框內容上色
        //--------------------------------------------------------------------------

        val getvalue = wList[position]["textview"].toString().split("\n").toTypedArray()
        del_insert_time = getvalue[1].substring(5) //儲存此筆資料輸入時間=掃描時間(刪除用),substring(int i):擷取字串從第幾位開始到最後
        packing_scan_main_E_write.setText(getvalue[0].substring(2,10)) //設定給edittext:扣除SU取後面八位數字
        del_qrcode = getvalue[0] //暫存欲刪除的qrcode

        packing_scan_main_B_check.visibility = View.GONE //殺掉SQLite資料確認按鈕
        packing_scan_main_B_writedelete.visibility = View.VISIBLE //顯示SQLite資料刪除按鈕

        parent.getChildAt(position).setBackgroundColor(ContextCompat.getColor(context, R.color.deeppink)) //選中的加外框:深粉紅色

        if (positionsave != -1 && positionsave != position && parent.getChildAt(positionsave) != null)
            parent.getChildAt(positionsave).setBackgroundColor(ContextCompat.getColor(context,
                    R.color.paleturquoise)) //非選中的取消外框回復底色

        positionsave = position
        hidekeyboard(view)
    }

    @SuppressLint("SimpleDateFormat")
    //手動輸入模式的確認按鈕
    fun writecheckbtn(view: View){
        val writestr = packing_scan_main_E_write.text.toString().trim()
        var msg = "" //訊息框用
        var msg2 = ""
        mobiletoday() //重新獲取今天日期

        //獲取現在時間
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val curDate = Date(System.currentTimeMillis()) //獲取當前時間
        today_time = formatter.format(curDate)

        if (writestr != "" && writestr.length == 8 && writestr.matches("[0-9]{8}".toRegex())){
            writenum++
            var str = "SL" //預設台北廠
            if (site_id[sitechoice] == "RP") str = "SU"
            scannum = str + writestr + "0001"
            subscannum = scannum.substring(2,7) //取前五碼，如:12345
            write_flag = 1 //手動輸入
            dbinsert() //資料寫入SQLite

            if (langnum == 1) { //簡體
                msg = "已扫描${writenum}笔资料，欲编辑资料请「按一下」该笔资料。"
                msg2 = "\n输入时间："

            } else {
                msg = "已掃描${writenum}筆資料，欲編輯資料請「按一下」該筆資料。"
                msg2 = "\n輸入時間："
            }

            //對話框內容上色--------------------------------------------------------------
            val bluestart = "已掃描".length
            val blueend = "已掃描$writenum".length
            val redstart = "已掃描${writenum}筆資料，欲編輯資料請「".length
            val redend = "已掃描${writenum}筆資料，欲編輯資料請「按一下".length

            setwritecolor(msg, bluestart, blueend, redstart, redend) //對話框內容上色
            //--------------------------------------------------------------------------

            val item = HashMap<String, Any>()
            item["textview"] = scannum + msg2 + today_time
            item["img"] = R.drawable.pencil
            wList.add(item)

            //================= 設定Listview ==================//
            val adapter = SimpleAdapter(
                    context,
                    wList,
                    list_layout[L],
                    arrayOf("textview", "img"),
                    intArrayOf(item_text_id[L], write_img_id[L])
            )
            //--------------------------------------------------

            adapter.notifyDataSetChanged() //通知UI更新數據
            packing_scan_main_L_menuwrite.adapter = adapter
            packing_scan_main_L_menuwrite.isEnabled = true
            packing_scan_main_L_menuwrite.onItemClickListener = writeitem()
            Toast.makeText(context,"新增完成！", Toast.LENGTH_SHORT).show()
            packing_scan_main_E_write.setText("") //使用完先清空
            packing_scan_main_E_write.clearFocus() //取消焦點
            hidekeyboard(view) //檢查系統輸入法鍵盤是否有打開，有打開則關閉

            if (subscannum != ""){
                packing_scan_main_E_write.setText(subscannum)
                subscannum = "" //設定完復歸
            }

            packing_scan_main_E_write.requestFocus() //取得焦點

        }else if (writestr != "" && writestr.length != 8){
            val writenum = writestr.length

            if (langnum == 1) { //簡體
                msg = "QR-Code需为8位数，目前输入${writenum}位数，请输入正确的QR码！"
                msg2 = "请输入正确的QR-Code！"

            } else {
                msg = "QR-Code需為8位數，目前輸入${writenum}位數，請輸入正確的QR-Code!"
                msg2 = "請輸入正確的QR-Code！"
            }

            //對話框內容上色--------------------------------------------------------------
            val bluestart = "QR-Code需為".length
            val blueend = "QR-Code需為8".length
            val redstart = "QR-Code需為8位數，目前輸入".length
            val redend = "QR-Code需為8位數，目前輸入$writenum".length

            setwritecolor(msg, bluestart, blueend, redstart, redend) //對話框內容上色
            //--------------------------------------------------------------------------

            Toast.makeText(this, msg2, Toast.LENGTH_SHORT).show()
        }

    }

    //加資料進SQLite
    private fun dbinsert() {
        mContRes = contentResolver //ContentResolver使用前一定要寫
        val newRow = ContentValues()
        newRow.put("qrcode", scannum)
        newRow.put("check_in_date", today_time)
        newRow.put("insert_date", today)
        newRow.put("update_flag", 0) //上傳標記:0=表未上傳
        newRow.put("write_flag", write_flag) //手動輸入標記:1=表手動輸入
        newRow.put("site", site_id[sitechoice])
        mContRes.insert(uris[sitechoice], newRow)
    }

    //手動輸入刪除按鈕
    fun writedeletebtn(view: View){
        myview = layoutInflater.inflate(R.layout.packing_scan_main_write_delete,
                null) //自定義Layout:dialog

        //設定字體變更視窗
        alertDialog = ReponAlertDialog(context)
        alertDialog.setView(myview,0,0,0,0) //設定自定義layout
        alertDialog.show() //務必先show出來才能設定參數

        //自定義Dialog視窗參數
        val params = alertDialog.window.attributes //取得dialog參數對象
        params.width = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog寬度包裹內容
        params.height = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog高度包裹內容
        params.gravity = Gravity.CENTER //設置dialog重心
        alertDialog.window.attributes = params //dialog參數綁定
    }

    //手動輸入刪除dialog:確認紐
    fun write_delete_check(view: View){
        mContRes = contentResolver
        myselecion = "qrcode LIKE '$del_qrcode' AND check_in_date LIKE '$del_insert_time' AND " +
                     "write_flag LIKE 1"; //確定是手動輸入才能刪除(防止刪除正常掃描的資料)
        val i = mContRes.delete(uris[sitechoice], myselecion,null)

        if (i > 0){

            //開單一執行緒重刷清單
            singleThreadExecutor.execute {
                myselecion = "" //清空
                myorder = "id DESC" //降冪排列(大到小排序)

                try {
                    val cur = mContRes.query(uris[sitechoice], tablecolume_id[sitechoice],
                            myselecion,null, myorder)
                    cur.moveToFirst()
                    val data = cur.count

                    if (data > 0){ //有資料

                        //設定更新失敗的listview內容
                        recSet = ArrayList() //重設recSet的值為空
                        val columnCount = cur.columnCount

                        if (!cur.isAfterLast){ //只放入第一筆資料
                            val fldSet = StringBuilder()

                            for (ii in 0 until columnCount){
                                fldSet.append(cur.getString(ii))
                                if (ii < columnCount) fldSet.append("#")
                            }

                            recSet.add(fldSet.toString()) //存放到arraylist中
                        }

                        val fld = recSet[0].split("#").toTypedArray()
                        scannum = fld[1]
                        subscannum = scannum.substring(2,7) //扣掉SU取前五碼，如:12345

                    }else scannum = "" //無資料復歸

                    cur.close() //用完關掉

                    myselecion = "write_flag LIKE 1"; //手動輸入
                    myorder = "id DESC" //降冪排列(大到小排序)
                    val c = mContRes.query(uris[sitechoice], tablecolume_id[sitechoice],
                            myselecion,null, myorder)
                    c.moveToFirst()
                    val data2 = c.count

                    if (data2 > 0){ //有資料
                        writenum = data2
                        c.close()
                        myselecion = "update_flag LIKE 0 AND write_flag LIKE 1"; //未上傳且為手動輸入
                        myorder = "id DESC" //降冪排列(大到小排序)
                        val c2 = mContRes.query(uris[sitechoice], tablecolume_id[sitechoice],
                                myselecion,null, myorder)
                        c2.moveToFirst()
                        val data3 = c2.count

                        if (data3 > 0){

                            //設定listview內容
                            recSet = ArrayList() //重設recSet的值為空
                            val columnCount = c2.columnCount
                            wList = ArrayList() //重設mList的值為空

                            while (!c2.isAfterLast){
                                val fldSet = StringBuilder()

                                for (ii in 0 until columnCount){
                                    fldSet.append(c2.getString(ii))
                                    if (ii < columnCount) fldSet.append("#")
                                }

                                recSet.add(fldSet.toString())
                                c2.moveToNext()
                            }

                            c2.close() //用完關掉

                            val msg = if (langnum == 1) "\n输入时间：" else "\n輸入時間：" //1:簡體

                            for (iii in 0 until recSet.size){
                                val item = HashMap<String, Any>()
                                val fld = recSet[iii].split("#").toTypedArray()
                                item["textview"] = fld[1] + msg + fld[2]
                                item["img"] = if (fld[5] == "0") "" else R.drawable.pencil
                                wList.add(item)
                            }

                        }else {
                            wList = ArrayList() //重設mList的值為空
                            c2.close()
                        }

                    }else {
                        wList = ArrayList() //重設mList的值為空
                        writenum = 0 //無資料復歸
                        c.close()
                    }

                }catch (e: Exception){
                    Log.d(TAG,"error=" + e.toString())
                    val errormsg = "write_delete_check() 撈SQLite資料異常:" + e.toString()
                    reponlog(classname, errormsg,0, context) //寫log
                }

                //單執行緒執行到最後更新UI thread
                runOnUiThread {

                    if (subscannum != ""){
                        packing_scan_main_E_write.setText(subscannum)
                        subscannum = "" //設定完復歸
                    }

                    packing_scan_main_E_write.requestFocus(6) //取得焦點:第六位
                    var msg = ""
                    var msg2 = ""
                    var msg3 = ""

                    if (langnum == 1) { //簡體
                        msg = "手动输入${writenum}笔资料，已删除1笔资料"
                        msg2 = "已成功删除此笔资料！"
                        msg3 = "请输入QR-Code号码，输入完请按「确认」键！"

                    } else {
                        msg = "手動輸入${writenum}筆資料，已刪除1筆資料"
                        msg2 = "已成功刪除此筆資料!"
                        msg3 = "請輸入QR-Code號碼，輸入完請按「確認」鍵！"
                    }

                    //對話框內容上色--------------------------------------------------------------
                    val bluestart = "手動輸入".length
                    val blueend = "手動輸入$writenum".length
                    val redstart = "手動輸入${writenum}筆資料，已刪除".length
                    val redend = "手動輸入${writenum}筆資料，已刪除1".length

                    setwritecolor(msg, bluestart, blueend, redstart, redend) //對話框內容上色
                    //--------------------------------------------------------------------------

                    //================= 設定Listview ==================//
                    val adapter = SimpleAdapter(
                            context,
                            wList,
                            list_layout[L],
                            arrayOf("textview", "img"),
                            intArrayOf(item_text_id[L], write_img_id[L])
                    )
                    //--------------------------------------------------

                    adapter.notifyDataSetChanged() //通知UI更新數據
                    packing_scan_main_L_menuwrite.adapter = adapter
                    packing_scan_main_L_menuwrite.isEnabled = true
                    packing_scan_main_L_menuwrite.onItemClickListener = writeitem() //手動輸入選項監聽
                    Toast.makeText(context, msg2, Toast.LENGTH_SHORT).show()

                    if (writenum == 0 || wList.toString() == "[]"){ //無手動輸入資料時
                        packing_scan_main_B_check.visibility = View.VISIBLE
                        packing_scan_main_B_writedelete.visibility = View.GONE
                        write_edit = 0 //退出編輯模式

                        //對話框內容上色--------------------------------------------------------------
                        val bluestart2 = "請輸入".length
                        val blueend2 = "請輸入QR-Code".length
                        val redstart2 = "請輸入QR-Code號碼，輸入完請按「".length
                        val redend2 = "請輸入QR-Code號碼，輸入完請按「確認".length

                        setwritecolor(msg3, bluestart2, blueend2, redstart2, redend2) //對話框內容上色
                        //--------------------------------------------------------------------------
                    }

                }

            }

        }else {
            val str = if (langnum == 1) "删除失败，查无此笔资料！" else "刪除失敗，查無此筆資料！" //1:簡體
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
        }

        alertDialog.dismiss() //關閉dialog
    }

    //手動輸入刪除dialog:取消紐
    fun write_delete_cancle(view: View){
        val str = if (langnum == 1) "取消删除此笔资料" else "取消刪除此筆資料" //1:簡體
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
        alertDialog.dismiss() //關閉dialog
    }

    //手動輸入取消按鈕
    fun writecancel(view: View){

        //1:使用編輯功能中
        if (write_edit == 1) write_edit_end() //結束刪除頁面
        else write_page_end(view) //結束手動輸入頁面
    }

    //menu_choice_dialog：強制重新上傳所有資料按鈕
    fun updateallbtn(view: View){
        updateall = 1 //全部重傳辣
        updatedata(view) //呼叫上傳method
        Toast.makeText(context, "重新上傳所有資料！", Toast.LENGTH_SHORT).show()
        alertDialog.dismiss() //關閉menu_choice_dialog
    }

    //上傳按鈕
    fun updatedata(view: View) {

        if (System.currentTimeMillis() - exitTime > 3000){ //點擊間隔時間設定大於三秒，避免連續按
            exitTime = System.currentTimeMillis()
            checklink(classname, context) //偵測網路連線
            mobiletoday() //更新今天的日期(防止使用者未關機也未結束本程式)

            if (wifilink == 1 && updateclick == 0){

                //開worker thread run需大量計算的工作,run完會自動關掉
                singleThreadExecutor.execute {
                    var data = 0 //記錄query資料數
                    var data_scan = 0 //儲存掃描資料筆數
                    val unlink = 1 //記錄網路有無連線:1=連線中

                    updateclick = 1 //記錄點擊
                    updateok = 0 //使用前清空
                    updateerror = 0 //使用前清空
                    updateokrepeat = 0 //使用前清空
                    lastupdateok = 0 //使用前清空

                    for (s in 0 until 2){ //跑兩次

                        //查詢SQLite是否有資料
                        mContRes = contentResolver
                        myselecion = "insert_date LIKE '$today'" //撈取近兩天的掃描資料

                        if (s == 1 && lastday != ""){
                            myselecion = "insert_date LIKE '$lastday'" //撈取前一天的掃描資料

                            if (secondlastday != "")
                                myselecion = "insert_date between '$secondlastday' AND '$lastday'" //撈取前兩個工作天的所有資料

                            if (updateall == 1)
                                myselecion = "insert_date < '$today'" //撈取除了今天以外所有的資料
                        }

                        myorder = "id DESC"

                        try {
                            val c = mContRes.query(uris[sitechoice], tablecolume_id[sitechoice],
                                    myselecion,null, myorder)
                            c.moveToFirst()
                            data = c.count

                            if (data > 0){
                                uimsg = 1 //執行上傳
                                up_i = 0
                                uiupdate(up_i) //傳送更新UI訊息
                                if (s == 0) data_scan = data //限今天

                                while (!c.isAfterLast){
                                    val qrcode = "?qrcode=" + c.getString(1) //QR-code
                                    var scan_date = "" //掃描日期時間

                                    try { //記錄時間的資料裡有空格要用URLEncoder進行轉碼:空格轉碼
                                        scan_date = "&check_in_date=" + URLEncoder
                                                .encode(c.getString(2),"UTF-8")
                                    }catch (e: Exception){
                                        e.printStackTrace()
                                    }

                                    val site = "&site=" + c.getString(6) //廠別
                                    val into = qrcode + scan_date + site //傳GET參數(跟在網址列後面的)
                                    val result = DBConnector.executedata(connect_ip + into)

                                    try {
                                        val jsonArray = JSONArray(result)

                                        if (jsonArray.length() > 0){

                                            for (i in 0 until jsonArray.length()){
                                                val jsonData = jsonArray.getJSONObject(i)
                                                code = jsonData.getInt("Count")
                                                val msg = jsonData.getString("Msg")

                                                if (code > 0)
                                                    if (s == 0) updateok++ else lastupdateok++ //其他工作日沒傳到的今日上傳成
                                                else
                                                    if (s == 0) updateokrepeat++ //限今日

                                                //更新SQLite裡的update_flag
                                                mContRes = contentResolver //ContentResolver使用前一定要寫
                                                myselecion = "id LIKE '" + c.getString(0) +
                                                        "' AND qrcode LIKE '" + c.getString(1) + "'"
                                                val newRow = ContentValues()
                                                newRow.put("update_flag", 1) //上傳標記:1=已上傳
                                                mContRes.update(uris[sitechoice], newRow, myselecion,null)
                                            }
                                        }

                                    }catch (e: Exception){
                                        Log.d(TAG,"error=" + e.toString())
                                        if (s == 0) updateerror++ else lastupdateerror++
                                        up_i = 1 //上傳失敗
                                        uiupdate(up_i)
                                    }

                                    c.moveToNext()
                                }

                                c.close() //用完關掉
                                uimsg = 0 //上傳完了標記

                            }else{
                                c.close()
                                if (s == 0) uimsg = 2 //只限今日,無資料標記
                            }

                        }catch (e: Exception){
                            Log.d(TAG,"error=" + e.toString())
                            val errormsg = "updatedata 撈SQLite資料異常:" + e.toString()
                            reponlog(classname, errormsg,0, context) //寫log
                        }

                    }

                    //這邊是呼叫main thread handler幫我們處理UI部分
                    runOnUiThread {

                        when{
                            updateclick == 1 && unlink == 1 && uimsg == 0 -> {
                                packing_scan_main_B_listshow.performClick() //刷新listview
                                updateuse = 1 //記錄按下update
                                var msg = ""

                                if (langnum == 1) { //簡體
                                    msg = "本日扫描资料${data_scan}笔\n上传${updateok}笔\n${updateokrepeat}笔传过了"

                                    if (lastupdateok != 0)
                                        msg += "\n找到其他工作日未上传资料${lastupdateok}笔\n成功上传${lastupdateok}笔"

                                } else {
                                    msg = "本日掃描資料${data_scan}筆\n上傳${updateok}筆\n${updateokrepeat}筆傳過了"

                                    if (lastupdateok != 0)
                                        msg += "\n找到其他工作日未上傳資料${lastupdateok}筆\n成功" +
                                               "上傳${lastupdateok}筆"
                                }

                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                cleardata() //上傳完畢整理手機資料庫(防止使用者都不關機也不關程式)
                                updateclick = 0 //用完重設
                                updateall = 0 //傳完復歸
                            }

                            updateclick == 1 && unlink == 0 && uimsg == 0 -> {
                                updatewarn() //上傳失敗訊息框
                                val str = if (langnum == 1) "上传失败!\n请检查网路是否连线至内网" //1:簡體
                                else "上傳失敗!\n請檢查網路是否連線至內網"
                                Toast.makeText(context, str,Toast.LENGTH_SHORT).show()
                                updateclick = 0 //用完重設
                                updateall = 0 //傳完復歸
                            }

                            uimsg == 2 -> {
                                val str = if (langnum == 1) "手机资料库无资料" //1:簡體
                                else "手機資料庫無資料"
                                Toast.makeText(context, str,Toast.LENGTH_SHORT).show()
                                updateclick = 0 //用完重設
                                uimsg = 0 //用完重設
                                updateall = 0 //傳完復歸
                            }
                        }

                    }

                }

            }else if (wifilink == 0 && updateclick == 0){
                val str = if (langnum == 1) "未连接到网路" //1:簡體
                else "未連接到網路"
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show()

            }else if (updateclick == 1){
                val str = if (langnum == 1) "资料上传中请稍后..." //1:簡體
                else "資料上傳中請稍後..."
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
            }

        }else{
            val str = if (langnum == 1) "请勿连续点击上传按钮!" //1:簡體
            else "請勿連續點擊上傳按鈕!"
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
        }

    }

    //設定上傳失敗訊息框
    private fun updatewarn() {
        val msg = if (langnum == 1) "上传失败!\n请检查网路是否连线至内网" //1:簡體
        else "上傳失敗!\n請檢查網路是否連線至內網"
        val redend = "上傳失敗!".length

        msgcolor = SpannableString(msg)
        msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.red)),0,
                redend,0) //失敗紅色表示
        msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)), redend,
                msg.length,0) //網路藍色表示
        packing_scan_main_T_num.setText(msgcolor, TextView.BufferType.SPANNABLE) //設定給訊息框
    }

    //結束刪除頁面，返回手動輸入頁面
    private fun write_edit_end() {
        getqrcode = packing_scan_main_E_write.text.toString().trim() //暫存選擇的qrcode
        packing_scan_main_B_check.visibility = View.VISIBLE
        packing_scan_main_B_writedelete.visibility = View.GONE
        thisview = window.decorView
        writebtn(thisview)
//        m_write_B_btn.performClick() //按一下重刷清單
        write_edit = 0 //用完復歸
    }

    //結束手動輸入頁面，返回主畫面
    private fun write_page_end(v: View) {

        if (scanrepeat == 0){ //使用一般掃描模式
            packing_scan_main_B_scan.visibility = View.VISIBLE
            packing_scan_main_B_scanrepeat.visibility = View.GONE

        }else {
            packing_scan_main_B_scan.visibility = View.GONE
            packing_scan_main_B_scanrepeat.visibility = View.VISIBLE
        }

        packing_scan_main_B_listshow.visibility = View.VISIBLE
        packing_scan_main_L_menu.visibility = View.VISIBLE
        packing_scan_main_B_check.visibility = View.GONE
        packing_scan_main_T_sl.visibility = View.INVISIBLE
        packing_scan_main_E_write.visibility = View.GONE
        packing_scan_main_B_writecancel.visibility = View.GONE
        packing_scan_main_L_menuwrite.visibility = View.GONE

        write_page = 0 //復歸:跳回主頁面

        packing_scan_main_E_write.setText("") //清空
        packing_scan_main_E_write.clearFocus() //取消焦點

        hidekeyboard(v) //檢查系統輸入法鍵盤是否有打開，有打開則關閉
        mobiletoday() //重抓今天日期
        packing_scan_main_B_listshow.performClick() //重載今日掃描清單
        val str = if (langnum == 1) "***取消手动输入***" else "***取消手動輸入***"
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
    }

    //下載新版apk並執行安裝
    private fun downloadapk() {

        //開工作執行緒下載apk
        singleThreadExecutor.execute {

            try{
                val i = DBConnector.executeapkdownload()

                when (i) {
                    1 -> { //下載成功後先判斷安裝權限
                        val fileUri = if (Build.VERSION.SDK_INT >= 24)
                            FileProvider.getUriForFile(context,"com.repongroup.packing_scan.providers.fileprovider", file)
                        else Uri.fromFile(file)

                        //轉跳至安裝apk畫面
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) //給予URI讀取權限
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) //給予新線程權限
                        intent.setDataAndType(fileUri,"application/vnd.android.package-archive")
                        startActivity(intent)
                        uiupdate(10) //下載成功
                    }
                    0 -> uiupdate(11) //下載失敗
                    else -> uiupdate(12) //出現錯誤
                }

            }catch (e: Exception){
                Log.d(TAG,"error=" + e.toString())
                uiupdate(12) //出現錯誤
            }

        }

    }

    //變更layout字體大小
    private fun textchange(){
        textchangestart = 1 //標記執行字體變更

        when(textset){
            0 -> { //大
                T = 30
                B = 50
                L = 0
                AT = 26
            }

            1 -> { //中
                T = 24
                B = 38
                L = 1
                AT = 20
            }

            2 -> { //小
                T = 18
                B = 26
                L = 2
                AT = 14
            }
        }

        packing_scan_main_T_site.textSize = T.toFloat()
        packing_scan_main_T_scan.textSize = T.toFloat()
        packing_scan_main_T_date.textSize = T.toFloat()
        packing_scan_main_T_num.textSize = T.toFloat()
        packing_scan_main_B_update.textSize = B.toFloat()
        packing_scan_main_B_scan.textSize = B.toFloat()
        packing_scan_main_B_listshow.textSize = B.toFloat()
        packing_scan_main_B_scanrepeat.textSize = B.toFloat()
        packing_scan_main_B_check.textSize = B.toFloat()
        packing_scan_main_T_sl.textSize = B.toFloat()
        packing_scan_main_E_write.textSize = B.toFloat()
        packing_scan_main_B_writecancel.textSize = B.toFloat()
        packing_scan_main_B_writedelete.textSize = B.toFloat()
        packing_scan_main_T_checkapk.textSize = AT.toFloat()

        if (scanuse == 1){ //掃描成功並輸出資料
            dbsqlite_scan() //重載掃描成功的單筆資料

            if (updateuse == 1){ //有使用過上傳功能(字體變更用)
                textupdatemsg()
                updateuse = 0 //字體變更完復歸
            }

            textchangestart = 0 //字體變更結束復歸
        }else
            packing_scan_main_B_listshow.performClick() //重載SQLite清單列表 →走獨立執行緒(注意!)

        site_spinner() //重載spinner

        //儲存字體大小資料-------------------
        val textsetdata = getSharedPreferences("TEXT_SET",0)
        textsetdata.edit()
                   .putInt("T_size", T)
                   .putInt("B_size", B)
                   .putInt("L_size", L)
                   .putInt("AT_size", AT)
                   .apply()
        //---------------------------------
    }

    //將掃描完成資料放入listview
    private fun dbsqlite_scan() {
        var msg = "" //訊息
        var msg2 = ""

        //先查詢此筆資料盤點狀態
        mContRes = contentResolver
        myselecion = "qrcode = '$scannum' AND check_in_date = '$today_time'"
        myorder = "id DESC"

        try {
            val c = mContRes.query(uris[sitechoice], tablecolume_id[sitechoice], myselecion,
                    null, myorder)
            c.moveToFirst()
            val data = c.count

            if (langnum == 1){ //簡體
                msg = "\n扫描时间："
                msg2 = "手机资料库无此笔资料！"

            }else{
                msg = "\n掃描時間："
                msg2 = "手機資料庫無此筆資料！"
            }

            if (data > 0){ //有資料
                scanuse = 1 //記錄掃描成功並輸出資料(字體變更用)

                //設定更新失敗的listview內容
                recSet = ArrayList() //重設recSet的值為空
                val columnCount = c.columnCount

                while (!c.isAfterLast){
                    val fldSet = StringBuilder()

                    for (ii in 0 until columnCount){
                        fldSet.append(c.getString(ii))
                        if (ii < columnCount) fldSet.append("#")
                    }

                    recSet.add(fldSet.toString()) //存放到arraylist中
                    c.moveToNext()
                }

                c.close() //用完關掉
                val mList = ArrayList<Map<String, Any>>()
                val item = HashMap<String, Any>()
                val fld = recSet[0].split("#").toTypedArray()
                item.put("textview", fld[1] + msg + fld[2])
                mList.add(item)

                //===========設定Listview==========//
                val adapter = SimpleAdapter(
                        context,
                        mList,
                        list_layout[L],
                        arrayOf("textview"),
                        intArrayOf(item_text_id[L])
                )
                //--------------------
                adapter.notifyDataSetChanged() //通知UI更新數據
                packing_scan_main_L_menu.adapter = adapter
                packing_scan_main_L_menu.isEnabled = true

            }else {
                c.close()
                Toast.makeText(context, msg2, Toast.LENGTH_SHORT).show()
            }

        }catch (e: Exception){
            Log.d(TAG,"error=" + e.toString())
            val errormsg = "dbsqlite_scan() 撈SQLite掃描完成資料異常:" + e.toString()
            reponlog(classname, errormsg,0, context) //寫log
        }

    }

    //設定上傳成功後的訊息框
    @SuppressLint("SimpleDateFormat")
    private fun textupdatemsg() {

        //獲取現在時間
        val formatter = SimpleDateFormat("MM-dd HH:mm:ss")
        val curDate = Date(System.currentTimeMillis()) //獲取上傳時間
        val update_time = formatter.format(curDate)

        val bluestart = "本日掃描資料已於".length
        val blueend = update_time.length
        val bluestart2 = "本日掃描資料已於${update_time}上傳：".length
        val blackstart = "本日掃描資料已於${update_time}上傳：成功".length

        //下面都會用到故設置區域變數---
        var blueend2 = 0
        var redstart = 0
        var redend = 0
        var blackend = 0
        var bluestart3 = 0
        var blueend3 = 0
        var redstart2 = 0
        var redend2 = 0
        var blackstart2 = 0
        var blackend2 = 0
        var redstart3 = 0
        var redend3 = 0
        //--------------------------

        var msg = ""

        if (updateokrepeat > 0){ //資料上傳過了

            if (langnum == 1){ //簡體
                msg = "本日扫描资料已于${update_time}上传：成功${updateok}笔，${updateokrepeat}笔传过了"

                if (lastupdateok != 0)
                    msg += "\n找到其他工作日未上传资料${lastupdateok}笔，成功上传${lastupdateok}笔"

            }else {
                msg = "本日掃描資料已於${update_time}上傳：成功${updateok}筆，${updateokrepeat}筆傳過了"

                if (lastupdateok != 0)
                    msg += "\n找到其他工作日未上傳資料${lastupdateok}筆，成功上傳${lastupdateok}筆"
            }

            blueend2 = "本日掃描資料已於${update_time}上傳：成功${updateok}筆".length
            blackend = Integer.toString(updateok).length
            redstart = "本日掃描資料已於${update_time}上傳：成功${updateok}筆，".length
            redend = "${updateokrepeat}筆傳過了".length

            msgcolor = SpannableString(msg)
            msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),
                    bluestart,bluestart + blueend,0) //上傳時間藍色表示
            msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),
                    bluestart2, blueend2,0) //成功藍色表示
            msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.black)),
                    blackstart,blackstart + blackend,0) //成功數字黑色表示
            msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.red)),
                    redstart,redstart + redend,0) //傳過紅色表示

            if (lastupdateok != 0){ //有昨天資料上傳成功
                bluestart3 = ("本日掃描資料已於${update_time}上傳：成功${updateok}筆，$updateokrepeat" +
                              "筆傳過了\n找到其他工作日未上傳資料${lastupdateok}筆，").length
                blueend3 = msg.length
                redstart2 = ("本日掃描資料已於${update_time}上傳：成功${updateok}筆，$updateokrepeat" +
                             "筆傳過了\n找到其他工作日未上傳資料").length
                redend2 = Integer.toString(lastupdateok).length
                blackstart2 = ("本日掃描資料已於${update_time}上傳：成功${updateok}筆，$updateokrepeat" +
                               "筆傳過了\n找到其他工作日未上傳資料${lastupdateok}筆，成功上傳").length
                blackend2 = Integer.toString(lastupdateok).length

                msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),
                        bluestart3, blueend3,0) //成功藍色表示
                msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.red)),
                        redstart2, redstart2 + redend2,0) //昨日資料紅色表示
                msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.black)),
                        blackstart2, blackstart2 + blackend2,0) //成功數字黑色表示
            }

        }else {

            if (up_i == 1){ //上傳失敗

                if (langnum == 1) { //簡體
                    msg = "本日扫描资料已于${update_time}上传：成功${updateok}笔，失败：${updateerror}笔"

                    if (lastupdateok != 0 || lastupdateerror != 0)
                        msg += "\n找到其他工作日未上传资料${lastupdateok}笔，成功上传${lastupdateok}笔" +
                               "，失败：${lastupdateerror}笔"

                } else {
                    msg = "本日掃描資料已於${update_time}上傳：成功${updateok}筆，失敗：${updateerror}筆"

                    if (lastupdateok != 0 || lastupdateerror != 0)
                        msg += "\n找到其他工作日未上傳資料${lastupdateok}筆，成功上傳${lastupdateok}筆" +
                               "，失敗：${lastupdateerror}筆"
                }

                blueend2 = "本日掃描資料已於${update_time}上傳：成功${updateok}筆".length
                redstart = "本日掃描資料已於${update_time}上傳：成功${updateok}筆，".length
                redend = "本日掃描資料已於${update_time}上傳：成功${updateok}筆，失敗：${updateerror}筆".length

                //找到昨日未上傳資料時設定-----------------------------------------------------------------
                bluestart3 = ("本日掃描資料已於${update_time}上傳：成功${updateok}筆，失敗：$updateerror" +
                              "筆\n找到其他工作日未上傳資料${lastupdateok}筆，").length
                blueend3 = msg.length
                redstart2 = ("本日掃描資料已於${update_time}上傳：成功${updateok}筆，失敗：$updateerror" +
                             "筆\n找到其他工作日未上傳資料").length
                redend2 = Integer.toString(lastupdateok).length
                blackstart2 = ("本日掃描資料已於${update_time}上傳：成功${updateok}筆，失敗：$updateerror" +
                               "筆\n找到其他工作日未上傳資料${lastupdateok}筆，成功上傳").length
                blackend2 = Integer.toString(lastupdateok).length
                redstart3 = ("本日掃描資料已於${update_time}上傳：成功${updateok}筆，失敗：$updateerror" +
                             "筆\n找到其他工作日未上傳資料${lastupdateok}筆，成功上傳${lastupdateok}筆，").length
                redend3 = msg.length
                //---------------------------------------------------------------------------------------

            }else {

                if (langnum == 1) { //簡體
                    msg = "本日扫描资料已于${update_time}上传：成功${updateok}笔"

                    if (lastupdateok != 0)
                        msg += "\n找到其他工作日未上传资料${lastupdateok}笔，成功上传${lastupdateok}笔"

                } else {
                    msg = "本日掃描資料已於${update_time}上傳：成功${updateok}筆"

                    if (lastupdateok != 0)
                        msg += "\n找到其他工作日未上傳資料${lastupdateok}筆，成功上傳${lastupdateok}筆"
                }

                blueend2 = "本日掃描資料已於${update_time}上傳：成功${updateok}筆".length

                //找到昨日未上傳資料時設定-------------------------------------------------------------
                bluestart3 = ("本日掃描資料已於${update_time}上傳：成功${updateok}筆\n找到其他工作日未" +
                              "上傳資料${lastupdateok}筆，").length
                blueend3 = msg.length
                redstart2 = ("本日掃描資料已於${update_time}上傳：成功${updateok}筆\n找到其他工作日未" +
                             "上傳資料").length
                redend2 = Integer.toString(lastupdateok).length
                blackstart2 = ("本日掃描資料已於${update_time}上傳：成功${updateok}筆\n找到其他工作日" +
                               "未上傳資料${lastupdateok}筆，成功上傳").length
                blackend2 = Integer.toString(lastupdateok).length
                //----------------------------------------------------------------------------------
            }

            blackend = Integer.toString(updateok).length

            msgcolor = SpannableString(msg)
            msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),
                    bluestart, bluestart + blueend, 0) //上傳時間藍色表示
            msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),
                    bluestart2, blueend2, 0) //成功藍色表示
            msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.black)),
                    blackstart, blackstart + blackend, 0) //成功數字綠色表示
            msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.red)),
                    redstart, redend, 0) //失敗紅色表示

            if (lastupdateok != 0 || lastupdateerror != 0) { //有昨天資料上傳成功
                msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),
                        bluestart3, blueend3, 0) //成功藍色表示
                msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.red)),
                        redstart2, redstart2 + redend2, 0) //昨日資料紅色表示
                msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.black)),
                        blackstart2, blackstart2 + blackend2, 0) //成功數字黑色表示
            }

            if (lastupdateerror != 0) { //有昨天資料上傳失敗
                msgcolor.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.red)),
                        redstart3, redend3, 0) //昨日上傳失敗資料紅色表示
            }

        }

        packing_scan_main_T_num.setText(msgcolor, TextView.BufferType.SPANNABLE) //設定給訊息框
    }

    //activity設定檔
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
    }

    //activity無動作時會進入暫停階段
    override fun onPause() {
        super.onPause()
        mobiletoday() //重新取得今天日期
    }

    override fun onStop() {
        super.onStop()
        mobiletoday() //重新取得今天日期
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(apktextkill()) //移除閒置的工作包
        handler.removeCallbacks(scanscan())
        singleThreadExecutor.shutdownNow() //強制關掉執行緒
    }

    private fun scanscan(): Runnable? = Runnable {
        scantoday()
        packing_scan_main_B_scanrepeat.performClick() //按下掃描紐
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when(id){
            R.id.app_M_finish -> {
                val str = if (langnum == 1) "谢谢您的使用!" else "謝謝您的使用!"
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
                this.finish()
            }

            R.id.menu_M_list -> {
                myview = layoutInflater.inflate(R.layout.packing_scan_main_menu_choice,null) //自定義Layout:dialog

                //設定選單選擇視窗
                alertDialog = ReponAlertDialog(context)
                alertDialog.setView(myview,0,0,0,
                        0) //設定自定義layout
                val m_text_T_title3 = myview.findViewById<TextView>(R.id.text_T_title3) //dialog選單title
                val m_scanrepeat_B_btn = myview.findViewById<Button>(R.id.scanrepeat_B_btn) //dialog連續掃描
                val m_scan_B_btn = myview.findViewById<Button>(R.id.scan_B_btn) //dialog一般掃描

                if (scanrepeat == 1){ //啟用連續掃描
                    m_scanrepeat_B_btn.visibility = View.INVISIBLE //隱藏dialog連續掃描按鈕(因layout跟隨的關係不使用GONE)
                    m_scan_B_btn.visibility = View.VISIBLE //顯示dialog一般掃描按鈕

                }else {
                    m_scanrepeat_B_btn.visibility = View.VISIBLE //顯示dialog連續掃描按鈕
                    m_scan_B_btn.visibility = View.GONE //殺掉dialog一般掃描按鈕
                }

                alertDialog.show() //務必先show出來才能設定參數

                //自定義Dialog視窗參數
                val params = alertDialog.window.attributes //取得dialog參數對象
                params.width = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog寬度包裹內容
                params.height = WindowManager.LayoutParams.WRAP_CONTENT //設置dialog高度包裹內容
                params.gravity = Gravity.CENTER //設置dialog重心
                alertDialog.window.attributes = params //dialog參數綁定

                //長按監聽
                m_text_T_title3.setOnLongClickListener {
                    islongclick = true //長按開始
                    starttime = System.currentTimeMillis() //記錄長按發生時間
                    return@setOnLongClickListener true
                }

                //觸碰監聽
                m_text_T_title3.setOnTouchListener { _, event ->

                    if (event.action == MotionEvent.ACTION_UP){

                        if (System.currentTimeMillis() - starttime > 10000 && islongclick) { //長按大於10秒時執行
                            starttime = 0 //復歸長按時間
                            val it = Intent()
                            it.setClass(context, Repon_list::class.java)
                            startActivity(it)
                            alertDialog.dismiss() //關掉dialog
                        }

                        islongclick = false //設定長按結束
                    }

                    return@setOnTouchListener false
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        //訊息
        var msg = ""
        var msg2 = ""
        var msg3 = ""
        var msg4 = ""

        if (keyCode == KeyEvent.KEYCODE_BACK){

            if (langnum == 1) { //簡體
                msg = "再按一次结束程式"
                msg2 = "再按一次回上一页"
                msg3 = "资料上传中，请不要结束程式"
                msg4 = "谢谢您的使用！"

            } else {
                msg = "再按一次結束程式"
                msg2 = "再按一次回上一頁"
                msg3 = "資料上傳中，請不要結束程式"
                msg4 = "謝謝您的使用!"
            }

            if (System.currentTimeMillis() - exitTime > 2000){
                exitTime = System.currentTimeMillis()

                when{
                    uimsg == 0 && updateclick == 0 && write_page == 0 ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

                    uimsg == 0 && updateclick == 0 && write_page == 1 && write_edit == 0 ->
                        Toast.makeText(context, msg2, Toast.LENGTH_SHORT).show()

                    uimsg == 0 && updateclick == 0 && write_page == 1 && write_edit == 1 ->
                        write_edit_end() //結束刪除頁面

                    else -> Toast.makeText(context, msg3, Toast.LENGTH_SHORT).show()
                }

            }else {

                if (uimsg == 0 && updateclick == 0 && write_page == 0){
                    Toast.makeText(this, msg4, Toast.LENGTH_SHORT).show()
                    this.finish()

                }else if (uimsg == 0 && updateclick == 0 && write_page == 1){
                    thisview = window.decorView
                    write_page_end(thisview) //結束手動輸入頁面
                }

            }

            return true
        }

        return super.onKeyDown(keyCode, event)
    }

}