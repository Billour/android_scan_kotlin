package com.repongroup.inventory

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import com.repongroup.inventory.Custodian_main.Companion.RPCOLUMN
import com.repongroup.inventory.Custodian_main.Companion.TAG
import com.repongroup.inventory.Custodian_main.Companion.myorder
import com.repongroup.inventory.Custodian_main.Companion.myselecion
import com.repongroup.inventory.Custodian_main.Companion.singleThreadExecutor
import com.repongroup.inventory.Custodian_main.Companion.tablecolumn
import com.repongroup.inventory.Custodian_main.Companion.uri
import com.repongroup.inventory.DBConnector.Companion.code
import com.repongroup.inventory.DBConnector.Companion.updateconnect_ip
import com.repongroup.inventory.providers.CustodianContentProvider
import com.repongroup.repon_android.Repon_android.Companion.catchdb
import com.repongroup.repon_android.Repon_android.Companion.checklink
import com.repongroup.repon_android.Repon_android.Companion.mobiletoday
import com.repongroup.repon_android.Repon_android.Companion.reponlog
import com.repongroup.repon_android.Repon_android.Companion.wifilink
import kotlinx.android.synthetic.main.custodian_list.*
import org.json.JSONArray
import org.json.JSONObject

class Custodian_list: AppCompatActivity() {

    private var exitTime: Long = 0
    private lateinit var mContRes: ContentResolver
    private var reclist: ArrayList<String> = ArrayList() //暫存bundle過來的arraylist值
    private var recSet: ArrayList<String> = ArrayList() //暫存arraylist的值
    private var recquery: ArrayList<String> = ArrayList() //暫存arraylist的值
    private var tcount_all: Int = 0 //儲存資料筆數
    private var tcount_ok: Int = 0 //儲存已盤點筆數
    private var tcount_no: Int = 0 //儲存等待盤點筆數
    private var tcount_error: Int = 0 //儲存盤點更新失敗筆數
    private var site_id: String = "" //儲存bundle過來的廠別代號
    private var naj_dept_id: String = "" //儲存bundle過來的管理部門代號
    private var layoutview: Int = 0 //定義使用的layout值:0=m_rel_list, 1=m_rel_inventory

    //定義使用的list值:0=m_custodian_list_L_menu, 1=m_custodian_list_L_menudetail,
    // 2=m_inventory_query_L_menu(0過來的), 3=m_inventory_query_L_menu(1過來的)
    private var listview: Int = 0
    private var clickitem: Int = 0 //記錄按下的item項數
    private var uimsg: Int = 0 //儲存worker thread執行時回傳的數值
    private var queryuse: Int = 0 //記錄查詢盤點功能使用情形:1表示有使用
    private var updateclick: Int = 0 //記錄是否點擊上傳鈕　
    private var sitechoice: Int = 0 //預設為NJ
    private var inventory_ok: Int = 0 //標記盤點成功
    private var fix_status: String = "" //資產狀況中文顯示
    private var today: String = "" //記錄今天的年月日
    private var inventorydate: String = "" //記錄盤點日期
    private val context: Context = this
    private val classname: String = "Custodian_list" //kt name


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custodian_list)
        catchdb() //抓資料庫執行緒的資料萬用法
        setupViewComponent() //自定義
    }

    //自定義
    private fun setupViewComponent() {
        setlayoutstart() //layout重設

        //從Bundle中取出資料
        val it: Intent = this.intent
        site_id = it.getStringExtra("site_id") //接收丟過來的廠別id

        if (site_id == "RP"){
            sitechoice = 1 //改為RP
            uri = CustodianContentProvider.CONTENT_URI3 //選擇RP
            tablecolumn = RPCOLUMN //選擇RP
        }

        val dep: String = it.getStringExtra("Management_dep_name") //接收丟過來的管理部門名稱
        naj_dept_id = it.getStringExtra("Management_dep_id") //接收丟過來的管理部門ID
        reclist = it.getStringArrayListExtra("Dep_inventory_data") //接收丟過來的部門盤點資料
        custodian_list_T_dep.text = dep //設定盤點部門
        dbsqlite() //載入盤點資料
        today = mobiletoday("yyyy-M-d") //今天日期
    }

    //layout重設
    private fun setlayoutstart() {
        rel_list.visibility = View.VISIBLE //盤點資訊清單layout
        rel_inventory.visibility = View.GONE //連續盤點layout
        rel_inventory_query.visibility = View.GONE //盤點查詢layout
    }

    //將盤點資料放入listview
    @SuppressLint("SetTextI18n")
    private fun dbsqlite() {
        val mList: ArrayList<Map<String, Any>> = ArrayList()

        if (listview == 0){ //listmenu
            tcount_all = reclist.size

            for (i in 0..(tcount_all - 1)){
                val item: HashMap<String, Any> = HashMap<String, Any>()
                val fld: Array<String> = reclist[i].split("#").toTypedArray()

                if (fld[13] == "Y") tcount_ok += 1

                item["textView"] = "固資編號： " + fld[1] + "\n固資名稱： " + fld[2] + "\n保管人姓名： " +
                                    fld[8] + "\n存放地點： " + fld[9]
                mList.add(item)
            }

            textmsg_menu()

        }else if (listview == 1) { //listmenudetail
            custodian_list_T_num.text = " 資料筆數：共 $tcount_all 筆，顯示第 ${(clickitem + 1)} 筆資料" //訊息框
            val item: HashMap<String, Any> = HashMap()
            val fld: Array<String> = reclist[clickitem].split("#").toTypedArray()
            fix_status = "" //使用前清空
            inventorydate = ""
            fix_status_change(fld[10]) //資產狀況中文轉換

            inventorydate = if (fld[15] == "null") "尚未盤點" else fld[15]

            item["textView"] = "固資編號： " + fld[1] + "\n固資名稱： " + fld[2] + "\n固資規格： " +
                                fld[3] + "\n管理部門： " + fld[4] + "\n部門代號： " + fld[5] +
                               "\n部門名稱： " + fld[6] + "\n保管人工號： " + fld[7] + "\n保管人姓名： " +
                                fld[8] + "\n存放地點： " + fld[9] + "\n資產狀態： " + fix_status +
                               "\n廠別： " + fld[11] + "\n資產數量： " + fld[12] + "\n盤點狀態： " +
                                fld[13] + "\n盤點日期： " + inventorydate
            mList.add(item)
        }

        //===========設定Listview==========//
        val adapter: SimpleAdapter = SimpleAdapter(
                this,
                mList,
                R.layout.list_item,
                arrayOf("textView"),
                intArrayOf(R.id.textView)
        )
        //--------------------
        adapter.notifyDataSetChanged() //更新UI?

        if (listview == 0){ //listmenu設定
            custodian_list_L_menu.adapter = adapter
            custodian_list_L_menu.isEnabled = true
            custodian_list_L_menu.onItemClickListener = listviewOnitemcliklis()

        }else if (listview == 1){ //listmenudetail設定
            custodian_list_L_menudetail.adapter = adapter
            custodian_list_L_menudetail.isEnabled = true
        }
    }

    //設定盤點資料狀態
    private fun textmsg_menu() {
        val msg: String = " 資料筆數：共 $tcount_all 筆，已盤點 $tcount_ok 筆"
        val msgbluestart: String = " 資料筆數：共 $tcount_all 筆，已盤點 "

        val bluestart: Int = msgbluestart.length
        val blueend: Int = Integer.toString(tcount_ok).length

        val msgcolor: SpannableString = SpannableString(msg)
        msgcolor.setSpan(ForegroundColorSpan(Color.BLUE), bluestart,bluestart + blueend,0) //已盤點藍色表示

        custodian_list_T_num.setText(msgcolor, TextView.BufferType.SPANNABLE) //設定狀態框字串顏色
    }

    //資產狀態中文轉換
    private fun fix_status_change(str: String) {
        val fix_status_no: Array<String> = arrayOf("00","01","03","04") //資產狀態代號
        val fix_status_ct: Array<String> = arrayOf("正常使用","折舊完畢","報廢","外送") //代號對應中文意思

        for (i in 0..(fix_status_no.size)){

            if (fix_status_no[i] == str){
                fix_status = fix_status_ct[i]
                break
            }
        }
    }

    //listmenu選項監聽
    private fun listviewOnitemcliklis(): AdapterView.OnItemClickListener? = AdapterView.OnItemClickListener { _, _, position, _ ->
        val s: String = "你按下第 " + Integer.toString(position + 1) + " 筆"
        custodian_list_L_menu.visibility = View.GONE //殺掉listmenu
        custodian_list_L_menudetail.visibility = View.VISIBLE //顯示listmenudetail
        listview = 1
        clickitem = position
        dbsqlite()
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show()
    }

    //盤點按鈕
    fun invientory(view: View){
        layoutview = 1 //標記使用盤點layout
        rel_list.visibility = View.GONE //殺掉盤點layout
        rel_inventory.visibility = View.VISIBLE //顯示連續盤點layout
        inventory_ok_queryall() //查詢盤點狀態
        custodian_inventory_E_fixnum.requestFocus() //取得焦點
        custodian_inventory_E_fixnum.addTextChangedListener(fixnumclick())
    }

    //上傳按鈕
    fun updatedata(view: View){
        checklink("Custodian_list", context) //偵測網路連線

        if (updateclick == 0 && wifilink == 1){

            //開worker thread run需大量計算的工作,run完會自動關掉
            singleThreadExecutor.execute {

                var data_inventory_ok: Int = 0 //儲存部門盤點完成筆數
                var updateok: Int = 0 //儲存上傳成功筆數
                var updateerror: Int = 0 //儲存上傳失敗筆數
                var query_inventory_ok: Int = 0 //儲存例外盤點完成筆數
                var queryupdateok: Int = 0 //儲存列外盤點上傳成功筆數
                var queryupdateerror: Int = 0 //儲存列外盤點上傳失敗筆數
                var queryupdateend: Int = 0 //例外盤點上傳狀況:傳完改1
                var unlink: Int = 0 //記錄網路無連線時

                updateclick = 1 //記錄點擊

                //這邊是背景thread在運作, 這邊可以處理比較長時間或大量的運算
                mContRes = contentResolver //使用前get
                myselecion = "naj_dept_id = '$naj_dept_id' AND fix_inventory = 'Y'"

                try {
                    val c: Cursor = mContRes.query(uri, tablecolumn, myselecion,null,null)
                    c.moveToFirst()
                    data_inventory_ok = c.count //記錄撈到的部門盤點完成資料筆數

                    if (data_inventory_ok > 0){
                        uimsg = 2 //執行上傳

                        while (!c.isAfterLast){
                            val qty: String = "?qty=" + c.getString(12) //固資盤點數量
                            val site: String = "&site=$site_id" //+ c.getString(11) //固資廠別
                            val fix_no: String = "&fix_no=" + c.getString(1) //固資編號
                            val cycle_date: String = "&cycle_date=" + c.getString(15) //盤點時間
                            val cycle_status: String = "&cycle_status=" + c.getString(16) //盤點完給DB的狀態
                            val into: String = qty + site + fix_no + cycle_date + cycle_status //傳PSOT參數(跟在網址列後面的)
                            val result: String = DBConnector.executeQuery(updateconnect_ip + into)

                            try {
                                val jsonArray: JSONArray = JSONArray(result)

                                for (i in 0..(jsonArray.length() - 1)){
                                    val jsonData: JSONObject = jsonArray.getJSONObject(i)
                                    code = jsonData.getInt("Count")
                                    val msg: String = jsonData.getString("Msg")

                                    if (code > 0)
                                        updateok++
                                    else
                                        updateerror++
                                }

                            }catch (e: Exception){
                                Log.d(Custodian_main.TAG,"error=" + e.toString())
                                val errormsg = "updatedata()部門盤點上傳異常:" + e.toString()
                                reponlog(classname, errormsg,0, context) //寫log
                            }

                            c.moveToNext()
                        }

                        c.close() //用完關掉
                        uimsg = 1 //上傳完了標記

                    }else
                        c.close()

                }catch (e: Exception){
                    Log.d(TAG,"error=" + e.toString())
                    val errormsg = "updatedata()部門盤點撈資料異常:" + e.toString()
                    reponlog(classname, errormsg,0, context) //寫log
                }

                if (queryuse == 1){ //有使用查詢盤點功能一起上傳
                    mContRes = contentResolver
                    myselecion = "queryfix_inventory = 'Y'"

                    try {
                        val cur: Cursor = mContRes.query(uri, tablecolumn, myselecion,null,null)
                        cur.moveToFirst()
                        query_inventory_ok = cur.count //記錄撈到的部門盤點完成資料筆數

                        if (query_inventory_ok > 0){
                            uimsg = 2 //執行上傳

                            while (!cur.isAfterLast){
                                val qty: String = "?qty=" + cur.getString(12) //固資盤點數量
                                val site: String = "&site=$site_id" //+ cur.getString(11); //固資廠別
                                val fix_no: String = "&fix_no=" + cur.getString(1) //固資編號
                                val cycle_date: String = "&cycle_date=" + cur.getString(15) //盤點時間
                                val cycle_status: String = "&cycle_status=" + cur.getString(16) //盤點完給DB的狀態
                                val into: String = qty + site + fix_no + cycle_date + cycle_status //傳PSOT參數(跟在網址列後面的)
                                val result: String = DBConnector.executeQuery(updateconnect_ip + into)

                                try {
                                    val jsonArray: JSONArray = JSONArray(result)

                                    if (jsonArray.length() > 0){

                                        for (i in 0..(jsonArray.length() - 1)){
                                            val jsonData: JSONObject = jsonArray.getJSONObject(i)
                                            code = jsonData.getInt("Count")
                                            val msg: String = jsonData.getString("Msg")

                                            if (code > 0){ //接到多少就是上傳多少筆
                                                queryupdateok++ //不管實際上傳幾筆都是只加一筆

                                                //上傳完成後設定例外盤點狀態為N
                                                val contVal: ContentValues = ContentValues()
                                                contVal.put("queryfix_inventory", "N") //記錄例外狀況盤點成功的固資
                                                val whereClause = "fix_code = '${cur.getString(1)}'" //設定欲修改的資料以甚麼條件找到該資料(SQL語法)
                                                mContRes.update(uri, contVal, whereClause, emptyArray())

                                            }else
                                                queryupdateerror++
                                        }

                                    }

                                }catch (e: Exception){
                                    Log.d(Custodian_main.TAG,"error=" + e.toString())
                                    val errormsg = "updatedata() 查詢盤點上傳資料異常:" + e.toString()
                                    reponlog(classname, errormsg,0, context) //寫log
                                }

                                cur.moveToNext()
                            }

                            cur.close()
                            uimsg = 1 //上傳完了標記

                        }else{
                            cur.close()
                            queryupdateend = 1 //無例外盤點資料可上傳
                        }

                    }catch (e: Exception){
                        Log.d(TAG,"error=" + e.toString())
                        val errormsg = "updatedata() 查詢盤點撈資料異常:" + e.toString()
                        reponlog(classname, errormsg,0, context) //寫log
                    }
                }

                //更新ui
                runOnUiThread {

                    //這邊是呼叫main thread handler幫我們處理UI部分
                    var msg: String = "" //Toast用msg

                    when{
                        data_inventory_ok > 0 && uimsg == 1 && queryuse == 0 && unlink == 0 -> {
                            msg = "已盤點完成${data_inventory_ok}筆\n上傳${updateok}筆\n未上傳${updateerror}筆"
                            uimsg = 0 //用完重設
                            updateclick = 0 //用完重設
                        }

                        data_inventory_ok > 0 || (query_inventory_ok> 0 && uimsg == 1 &&
                                queryuse == 1 && queryupdateend == 0 && unlink == 0) -> {
                            msg = "已盤點完成${data_inventory_ok}筆\n上傳${updateok}筆\n未上傳" +
                                  "${updateerror}筆\n\n非本管理部門盤點完成${query_inventory_ok}筆" +
                                  "\n上傳${queryupdateok}筆\n未上傳${queryupdateerror}筆"
                        }

                        data_inventory_ok > 0 || (query_inventory_ok > 0 && uimsg == 1 &&
                                queryuse == 1 && queryupdateend == 0 && unlink == 1) -> { //連接到錯誤的網路
                            msg = "上傳失敗，請檢查網路是否連線至內網"
                        }

                        data_inventory_ok > 0 && uimsg == 1 && queryuse == 1 && queryupdateend == 1
                                && unlink == 0 -> {
                            msg = "已盤點完成${data_inventory_ok}筆\n上傳${updateok}筆\n未上傳" +
                                  "${updateerror}筆"
                        }

                        data_inventory_ok == 0 -> {
                            msg = "無盤點完成資料"
                            updateclick = 0 //用完重設
                        }

                        unlink == 1 -> {
                            msg = "請檢查網路是否連線至內網"
                            unlink = 0 //用完重設
                        }

                    }

                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()

                    if (data_inventory_ok > 0 || uimsg == 1){
                        uimsg = 0 //用完重設
                        updateclick = 0 //用完重設
                    }

                }

            }

        }else if (updateclick == 1 && wifilink == 1)
            Toast.makeText(context,"資料正在上傳中...", Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(context,"上傳失敗!", Toast.LENGTH_SHORT).show()
    }

    //查詢部門盤點資料狀態
    private fun inventory_ok_queryall() {
        mContRes = contentResolver //使用前GET
        myselecion = "naj_dept_id = '$naj_dept_id' AND fix_inventory = 'Y'"

        try {
            val c: Cursor = mContRes.query(uri, tablecolumn, myselecion,null,null)
            c.moveToFirst()
            tcount_ok = c.count
            tcount_no = tcount_all - tcount_ok //等待盤點數
            textmsg() //設定盤點資料狀態
            c.close()

        }catch (e: Exception){
            Log.d(TAG,"error=" + e.toString())
            val errormsg = "inventory_ok_queryall() 撈SOLite資料異常:" + e.toString()
            reponlog(classname, errormsg,0, context) //寫log
        }
    }

    //設定盤點資料狀態
    private fun textmsg() {
        val msg: String = " 資料筆數：共 $tcount_all 筆，已盤點 $tcount_ok 筆，尚有 $tcount_no 筆未盤。"
        val msgbluestart: String = " 資料筆數：共 $tcount_all 筆，已盤點 "
        val msgredstart = " 資料筆數：共 $tcount_all 筆，已盤點 $tcount_ok 筆，尚有 "

        val bluestart: Int = msgbluestart.length
        val blueend: Int = Integer.toString(tcount_ok).length
        val redstart: Int = msgredstart.length
        val redend: Int = Integer.toString(tcount_no).length

        val msgcolor: SpannableString = SpannableString(msg)
        msgcolor.setSpan(ForegroundColorSpan(Color.BLUE), bluestart,bluestart + blueend,0) //已盤點藍色表示
        msgcolor.setSpan(ForegroundColorSpan(Color.RED), redstart,redstart + redend,0) //未盤點紅色表示

        custodian_inventory_T_num.setText(msgcolor, TextView.BufferType.SPANNABLE)
    }

    //監聽fixnum內容變化
    private fun fixnumclick(): TextWatcher? = object: TextWatcher {

        var data: Int = 0
        var datacontext: String = ""
        var fix_num: String = "" //存放資產編號

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            if (custodian_inventory_E_fixnum.length() > 11){
                fix_num = custodian_inventory_E_fixnum.text.toString().trim().toUpperCase() //toUpperCase():轉成大寫

                //先查詢此筆資料盤點狀態
                mContRes = contentResolver //使用前先GET
                myselecion = "fix_code = '$fix_num' AND naj_dept_id = '$naj_dept_id'" //過濾掉別的部門的盤點資料

                try {
                    val c: Cursor = mContRes.query(uri, tablecolumn, myselecion,null,null)
                    c.moveToFirst()
                    data = c.count

                    if (data > 0){ //有盤點資料
                        datacontext = c.getString(13) //取得盤點狀態

                        if (datacontext == "N"){
                            //設定盤點狀態為Y
                            val contVal: ContentValues = ContentValues()
                            contVal.put("fix_inventory", "Y") //欲修改的SQLite項目及值
                            contVal.put("cycle_date", today) //記錄盤點當下時間
                            contVal.put("cycle_status", "FM") //記錄盤點後寫進資料庫的狀態:FM
                            val whereClause: String = "fix_code = '$fix_num'"; //設定欲修改的資料以甚麼條件找到該資料(SQL語法)
                            val i: Int = mContRes.update(uri, contVal, whereClause,null)

                            if (i > 0){
                                c.close()

                                if (data != i)
                                    tcount_error = Math.abs(data - 1) //取絕對值
                                else
                                    tcount_ok += 1

                                inventory_ok = 1 //盤點成功

                            }else
                                tcount_error += 1 //更新失敗

                            if (inventory_ok == 1){
                                Toast.makeText(context,"盤點完成", Toast.LENGTH_SHORT).show()
                                inventory_ok_query() //重新查詢SQLite部門已完成盤點的資料

                            }else{
                                Toast.makeText(context,"已查到此筆資料，但無法更新資料，請記下固資" +
                                        "號並連絡資訊部處理", Toast.LENGTH_SHORT).show()
                                updateerrortextmsg() //更新失敗顯示資料

                                //設定更新失敗的listview內容
                                recSet = ArrayList() //重設recSet的值為空
                                val columnCount = c.columnCount

                                while (!c.isAfterLast){
                                    var fldSet = ""

                                    for (ii in 0..(columnCount - 1)){
                                        fldSet += c.getString(ii)

                                        if (ii < columnCount)
                                            fldSet += "#"
                                    }

                                    recSet.add(fldSet) //存放到arraylist中
                                    c.moveToNext()
                                }

                                c.close() //用完關掉
                                dbsqlite_inventory() //設定盤點完成的listview
                            }

                        }else {
                            c.close()
                            inventory_ok_query() //查詢盤點過該固資的資料
                            Toast.makeText(context,"盤點過了", Toast.LENGTH_SHORT).show()
                        }

                        datacontext = "" //回復預設值
                        inventory_ok = 0

                    }else {
                        c.close() //關封包
                        Toast.makeText(context,"無此筆資料!",Toast.LENGTH_SHORT).show()
                    }

                }catch (e: Exception){
                    Log.d(TAG,"error=" + e.toString())
                    val errormsg = "fixnumclick() 撈SQLite盤點資料異常:" + e.toString()
                    reponlog(classname, errormsg,0, context) //寫log
                }

                data = 0 //回復預設值
                fix_num = "" //回復預設值
                datacontext = "" //回復預設值
                custodian_inventory_E_fixnum.setText("") //回復為空值
                custodian_inventory_E_fixnum.clearFocus() //清除焦點
                custodian_inventory_E_fixnum.requestFocus() //取得焦點
            }
        }

        //查詢SQLite部門完成盤點資料
        private fun inventory_ok_query() {
            mContRes = contentResolver
            myselecion = "fix_code = '$fix_num' AND naj_dept_id = '$naj_dept_id' AND fix_inventory = 'Y'"

            try {
                val c: Cursor = mContRes.query(uri, tablecolumn, myselecion,null,null)
                c.moveToFirst()
                tcount_no = tcount_all - tcount_ok //等待盤點數
                textmsg()

                //設定listview內容
                recSet = ArrayList() //重設recSet的值為空
                val columnCount: Int = c.columnCount

                while (!c.isAfterLast){
                    var fldSet: String = ""

                    for (ii in 0..(columnCount - 1)){
                        fldSet += c.getString(ii)

                        if (ii < columnCount)
                            fldSet += "#"
                    }

                    recSet.add(fldSet) //存放到arraylist中
                    c.moveToNext()
                }

                c.close()

            }catch (e: Exception){
                Log.d(TAG,"error=" + e.toString())
                val errormsg = "inventory_ok_query() 撈SQLite資料異常:" + e.toString()
                reponlog(classname, errormsg,0, context) //寫log
            }

            dbsqlite_inventory() //設定盤點完成的listview
        }

        //將盤點完成資料放入listview
        private fun dbsqlite_inventory() {
            val mList: ArrayList<Map<String, Any>> = ArrayList()
            val item: HashMap<String, Any> = HashMap()
            val fld: Array<String> = recSet.get(0).split("#").toTypedArray() //只放入第一筆資料
            fix_status = "" //使用前清空
            inventorydate = ""
            fix_status_change(fld[10]) //資產狀況中文轉換

            if (fld[15] == "null")
                inventorydate = "尚未盤點"
            else
                inventorydate = fld[15]

            item["textView"] = "固資編號： " + fld[1] + "\n固資名稱： " + fld[2] + "\n固資規格： " +
                                fld[3] + "\n管理部門： " + fld[4] + "\n部門代號： " + fld[5] +
                               "\n部門名稱： " + fld[6] + "\n保管人工號： " + fld[7] + "\n保管人姓名： " +
                                fld[8] + "\n存放地點： " + fld[9] + "\n資產狀態： " + fix_status +
                               "\n廠別： " + fld[11] + "\n資產數量： " + fld[12] + "\n盤點狀態： " +
                                fld[13] + "\n盤點日期： " + inventorydate
            mList.add(item)

            //===========設定Listview==========//
            val adapter: SimpleAdapter = SimpleAdapter(
                    context,
                    mList,
                    R.layout.list_item,
                    arrayOf("textView"),
                    intArrayOf(R.id.textView)
            )
            //--------------------
            adapter.notifyDataSetChanged() //通知UI更新數據
            custodian_inventory_L_menu.adapter = adapter
            custodian_inventory_L_menu.isEnabled = true
        }

        //更新失敗時顯示失敗資料
        private fun updateerrortextmsg() {
            val querymsg: String = " 此筆資料之固資編號為 $fix_num 。"
            val querymsgredstart: String = " 此筆資料之固資編號為 "
            val querymsgredend: String = " 此筆資料之固資編號為 $fix_num"

            val queryredstart: Int = querymsgredstart.length
            val queryredend: Int = querymsgredend.length

            val querymsgcolor: SpannableString = SpannableString(querymsg)
            querymsgcolor.setSpan(ForegroundColorSpan(Color.RED), queryredstart, queryredend,0) //更新失敗固資以紅色表示
            custodian_inventory_T_num.setText(querymsgcolor, TextView.BufferType.SPANNABLE)
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    //創造右上角選單列
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.list_menu, menu)
        return true
    }

    //右上角選單對應
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId

        if (id == R.id.Custodian_inventory_query){ //盤點資料查詢

            if (layoutview == 0)
                layoutview = 2
            else if (layoutview == 1)
                layoutview = 3

            Toast.makeText(context,"開啟盤點查詢功能",Toast.LENGTH_SHORT).show()
            rel_list.visibility = View.GONE //盤點清單layout
            rel_inventory.visibility = View.GONE //連續盤點layout
            rel_inventory_query.visibility = View.VISIBLE //盤點查詢layout
            inventory_query_E_fixnum.requestFocus() //取得焦點
            inventory_query_E_fixnum.addTextChangedListener(fixnumquery())

            if (queryuse == 0){ //初次使用
                inventory_query_T_num.text = " 請掃描或輸入固資編號!"
                queryuse = 1 //記錄使用查詢盤點功能
            }

        }else this.finish() //結束

        return super.onOptionsItemSelected(item)
    }

    //監聽fixnum內容變化(固資查詢+盤點)
    private fun fixnumquery(): TextWatcher? = object : TextWatcher{

        var dataquery: Int = 0
        var dataquerycontext: String = ""
        var dataqueryinventory: String = ""
        var fix_num_query: String = "" //存放資產編號

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            if (inventory_query_E_fixnum.length() > 11){ //固資編號長度 > 11
                fix_num_query = inventory_query_E_fixnum.text.toString().trim().toUpperCase()

                //先查詢此筆資料盤點狀態
                mContRes = contentResolver
                myselecion = "fix_code = '$fix_num_query'" //過濾掉別的部門的盤點資料

                try {
                    val c: Cursor = mContRes.query(uri, tablecolumn, myselecion,null,null)
                    c.moveToFirst()
                    dataquery = c.count

                    if (dataquery > 0){ //有此筆盤點資料
                        dataquerycontext = c.getString(4) //取得管理部門
                        dataqueryinventory = c.getString(13) //取得盤點狀態

                        if (dataqueryinventory == "N"){

                            //設定盤點狀態為Y，例外盤點狀態為Y
                            val contVal: ContentValues = ContentValues()
                            contVal.put("fix_inventory", "Y") //欲修改的SQLite項目及值
                            contVal.put("queryfix_inventory", "Y") //記錄例外狀況盤點成功的固資
                            contVal.put("cycle_date", today) //記錄盤點當下時間
                            contVal.put("cycle_status", "FM") //記錄盤點後寫進資料庫的狀態:FM
                            val i: Int = mContRes.update(uri, contVal, myselecion, emptyArray())

                            if (i > 0){
                                c.close()

                                if (dataquery == i){
                                    Toast.makeText(context,"盤點完成", Toast.LENGTH_SHORT).show()
                                    querytextmsg()
                                    queryinventory_ok() //重新查詢SQLite部門已完成盤點的資料

                                }else
                                    tcount_error += Math.abs(dataquery - i) //取絕對值記錄更新失敗

                                inventory_ok = 1 //盤點成功

                            }else {
                                tcount_error += i //更新失敗
                                Toast.makeText(context,"已查到此筆資料，但無法更新資料，請記下固資" +
                                        "編號並連絡資訊部處理", Toast.LENGTH_LONG).show()
                                updateerrortextmsg() //更新失敗顯示資料

                                //設定更新失敗的listview內容
                                recquery = ArrayList() //重設recquery的值為空
                                val columnCount: Int = c.columnCount

                                while (!c.isAfterLast){
                                    var fldSet: String = ""

                                    for (ii in 0..(columnCount - 1)){
                                        fldSet += c.getString(ii)

                                        if (ii < columnCount)
                                            fldSet += "#"
                                    }

                                    recquery.add(fldSet) //存放到arraylist中
                                    c.moveToNext()
                                }

                                c.close() //用完關掉
                                dbsqlite_inventory_query() //設定更新失敗的listview
                            }

                        }else {
                            c.close()
                            queryinventory_ok() //查詢盤點過該固資的資料
                            Toast.makeText(context,"盤點過了",Toast.LENGTH_SHORT).show()
                        }

                        dataqueryinventory = "" //回復預設值
                        inventory_ok = 0

                    }else {
                        c.close() //關封包
                        Toast.makeText(context,"查無此筆盤點資料!",Toast.LENGTH_SHORT).show()
                    }

                }catch (e: Exception){
                    Log.d(TAG,"error=" + e.toString())
                    val errormsg = "fixnumquery() 撈SQLite資料異常:" + e.toString()
                    reponlog(classname, errormsg,0, context) //寫log
                }

                inventory_query_E_fixnum.setText("") //回復為空值
                inventory_query_E_fixnum.clearFocus() //清除焦點
                inventory_query_E_fixnum.requestFocus() //重新取得焦點
            }
        }

        //設定盤點資料狀態
        private fun querytextmsg() {
            val querymsg: String = " 此筆資料之管理部門為 $dataquerycontext 。"
            val querymsgbluestart: String = " 此筆資料之管理部門為 "
            val querymsgblueend: String = " 此筆資料之管理部門為 $dataquerycontext"

            val querybluestart: Int = querymsgbluestart.length
            val queryblueend: Int = querymsgblueend.length

            val querymsgcolor: SpannableString = SpannableString(querymsg)
            querymsgcolor.setSpan(ForegroundColorSpan(Color.BLUE), querybluestart, queryblueend,0)
            inventory_query_T_num.setText(querymsgcolor, TextView.BufferType.SPANNABLE)
        }

        //查詢SQLite部門完成盤點資料
        private fun queryinventory_ok() {
            mContRes = contentResolver //使用前先取得
            myselecion = "fix_code = '$fix_num_query'"

            try {
                val c: Cursor = mContRes.query(uri, tablecolumn, myselecion,null,null)
                c.moveToFirst()

                //設定listview內容
                recquery = ArrayList() //使用前清空
                val columnCount: Int = c.columnCount

                while (!c.isAfterLast){
                    var fldSet = ""

                    for (ii in 0..(columnCount - 1)){
                        fldSet += c.getString(ii)

                        if (ii < columnCount)
                            fldSet += "#"
                    }

                    recquery.add(fldSet) //存放到arraylist中
                    c.moveToNext()
                }

                c.close()

            }catch (e: Exception){
                Log.d(TAG,"error=" + e.toString())
                val errormsg = "queryinventory_ok() 撈SQLite資料異常:" + e.toString()
                reponlog(classname, errormsg,0, context) //寫log
            }

            dbsqlite_inventory_query() //設定盤點完成的listview
        }

        //更新失敗顯示資料
        private fun updateerrortextmsg() {
            val querymsg: String = " 此筆資料之固資編號為 $fix_num_query 。"
            val querymsgredstart: String = " 此筆資料之固資編號為 "
            val querymsgredend: String = " 此筆資料之固資編號為 $fix_num_query"

            val queryredstart: Int = querymsgredstart.length
            val queryredend: Int = querymsgredend.length

            val querymsgcolor: SpannableString = SpannableString(querymsg)
            querymsgcolor.setSpan(ForegroundColorSpan(Color.RED), queryredstart, queryredend,0) //失敗固資以紅色表示
            inventory_query_T_num.setText(querymsgcolor, TextView.BufferType.SPANNABLE) //盤點筆數
        }

        //將盤點完成資料放入listview
        private fun dbsqlite_inventory_query() {
            val mList: ArrayList<Map<String, Any>> = ArrayList()
            val item: HashMap<String, Any> = HashMap()
            val fld: Array<String> = recquery[0].split("#").toTypedArray() //只放入第一筆資料
            fix_status = "" //使用前清空
            inventorydate = ""
            fix_status_change(fld[10]) //資產狀況中文轉換

            if (fld[15] == "null")
                inventorydate = "尚未盤點"
            else
                inventorydate = fld[15]

            item["textView"] = "固資編號： " + fld[1] + "\n固資名稱： " + fld[2] + "\n固資規格： " +
                                fld[3] + "\n管理部門： " + fld[4] + "\n部門代號： " + fld[5] +
                               "\n部門名稱： " + fld[6] + "\n保管人工號： " + fld[7] + "\n保管人姓名： " +
                                fld[8] + "\n存放地點： " + fld[9] + "\n資產狀態： " + fix_status +
                               "\n廠別： " + fld[11] + "\n資產數量： " + fld[12] + "\n盤點狀態： " +
                                fld[13] + "\n例外盤點： " + fld[14] + "\n盤點日期： " + inventorydate
            mList.add(item)

            //===========設定Listview==========//
            val adapter: SimpleAdapter = SimpleAdapter(
                    context,
                    mList,
                    R.layout.list_item,
                    arrayOf("textView"),
                    intArrayOf(R.id.textView)
            )
            //--------------------
            adapter.notifyDataSetChanged() //通知UI更新數據
            inventory_query_L_menu.adapter = adapter
            inventory_query_L_menu.isEnabled = true
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if (keyCode == KeyEvent.KEYCODE_BACK){

            if (System.currentTimeMillis() - exitTime > 2000){
                exitTime = System.currentTimeMillis()

                when(layoutview){
                    1 -> {
                        rel_inventory.visibility = View.GONE //殺掉連續盤點layout
                        rel_list.visibility = View.VISIBLE //顯示清單layout
                        textmsg_menu() //更新資料筆數
                        layoutview = 0
                    }

                    2 ->{
                        rel_inventory.visibility = View.GONE //殺掉連續盤點layout
                        rel_inventory_query.visibility = View.GONE //殺掉盤點查詢layout
                        rel_list.visibility = View.VISIBLE //顯示清單layout
                        textmsg_menu() //更新資料筆數
                        layoutview = 0
                    }

                    3 -> {
                        rel_list.visibility = View.GONE //殺掉清單layout
                        rel_inventory_query.visibility = View.GONE //殺掉盤點查詢layout
                        rel_inventory.visibility = View.VISIBLE //顯示連續盤點layout
                        layoutview = 1
                    }

                    0 -> {

                        if (listview == 0){ //在list頁面且listmenu

                            if (uimsg == 2)
                                Toast.makeText(context,"目前正在上傳盤點完成資料，\n上傳完" +
                                        "即可重新選擇盤點部門。", Toast.LENGTH_SHORT).show()
                            else
                                Toast.makeText(context,"再按一次重新選擇部門", Toast.LENGTH_SHORT).show()

                        }else if (listview == 1){ //在list頁面且listmenudetail
                            custodian_list_L_menudetail.visibility = View.GONE //殺掉menudetail
                            custodian_list_L_menu.visibility = View.VISIBLE //顯示menu
                            textmsg_menu() //更新資料筆數
                            listview = 0
                        }

                    }

                }

            }else{

                if (layoutview == 0 && listview == 0 && uimsg == 0){ //在list頁面且listmenu
                    val it: Intent = Intent()
                    it.setClass(context, Custodian_main::class.java)
                    startActivity(it)
                    Toast.makeText(context,"請重新選擇部門。", Toast.LENGTH_SHORT).show()
                    this.finish()
                }

            }

            return true
        }

        return super.onKeyDown(keyCode, event)
    }

}