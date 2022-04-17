package com.repongroup.inventory

import android.app.ProgressDialog
import android.content.*
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.content.DialogInterface.BUTTON_POSITIVE
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.repongroup.inventory.DBConnector.Companion.connect_ip
import com.repongroup.inventory.DBConnector.Companion.depconnect_ip
import com.repongroup.inventory.DBConnector.Companion.httpstate
import com.repongroup.inventory.R.id.Custodian_main_menu_update
import com.repongroup.inventory.providers.CustodianContentProvider
import com.repongroup.repon_android.ReponAlertDialog
import com.repongroup.repon_android.Repon_android.Companion.catchdb
import com.repongroup.repon_android.Repon_android.Companion.checklink
import com.repongroup.repon_android.Repon_android.Companion.reponlog
import com.repongroup.repon_android.Repon_android.Companion.wifilink
import kotlinx.android.synthetic.main.custodian_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class Custodian_main: AppCompatActivity() {

    private lateinit var mContRes: ContentResolver
    private var naj_dept_id: ArrayList<String> = ArrayList()
    private var naj_dept_name: ArrayList<String> = ArrayList()
    private var dep_sqlite: ArrayList<String> = ArrayList()
    private var naj_dept_name_id: ArrayList<String> = ArrayList()
    private var jsondatanum: Int = 0 //記錄json資料筆數
    private var depdataerror: Int = 0 //記錄部門錯誤筆數
    private var exitTime: Long = 0
    private lateinit var Site_spinner: ArrayAdapter<String> //廠別
    private val site_id: Array<String> = arrayOf("NJ","RP") //宣告對應的廠別ID
    private var sitechoice: Int = 0 //宣告選擇廠別:0為NJ,1為RP
    private lateinit var Dep_spinner: ArrayAdapter<String> //盤點部門
    private var Management_dep_name: String = "" //記錄盤點部門
    private var Management_dep_id: Int = 0 //記錄部門ID
    private var msgsave: String = "" //訊息
    private var msgtext: String = "" //訊息
    private lateinit var progressDialog: ProgressDialog //讀取進度條
    private var i: Int = 0 //記錄進度條跑到第幾個資料
    private var pgress_num: Int = 0 //記錄進度條數字
    private var idnum: Int = 0 //記錄使用者選擇的部門是第幾項
    private var datadownload: Int = 0 //記錄下載盤點資料:0=未下載,1=下載中
    private val handler: Handler = Handler() //執行緒管理員
    private val classname: String = "Custodian_main"
    private val context: Context = this

    companion object { //等同於stataic用法
        var uri: Uri = CustodianContentProvider.CONTENT_URI //預設為NJ
        val NJCOLUMN: Array<String> = arrayOf("id","fix_code","fix_name","fix_spec","naj_dept_id",
                "dept_id","dept_name","empl_id","empl_name","location",
                "fix_status","site","qty","fix_inventory",
                "queryfix_inventory","cycle_date","cycle_status")
        val DEPCOLUMN: Array<String> = arrayOf("id","naj_dept_id","naj_dept_name","dept_no","dept_name")
        val RPCOLUMN: Array<String> = arrayOf("id","fix_code","fix_name","fix_spec","naj_dept_id",
                "dept_id","dept_name","empl_id","empl_name","location",
                "fix_status","site","qty","fix_inventory",
                "queryfix_inventory","cycle_date","cycle_status")
        var tablecolumn: Array<String> = NJCOLUMN
        var myselecion: String = ""
        var myorder: String = "id ASC" //排序欄位
        val TAG: String = "repongroup=>"
        val singleThreadExecutor: Executor = Executors.newSingleThreadExecutor() //創建單一執行緒緒程池;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custodian_main)

        catchdb() //抓資料庫執行緒的資料萬用法
        setupViewComponent() //自定義
    }

    //自定義
    private fun setupViewComponent() {
        //讀取儲存的資料-----------------------------------------------------------------------------
        val custodian_data: SharedPreferences = getSharedPreferences("custodian_set",0)
        sitechoice = custodian_data.getInt("sitechoice",0) //預設為台北廠
        idnum = custodian_data.getInt("idnum",0) //預設未選
        //-----------------------------------------------------------------------------------------

        site_spinner() //廠別spinner項目生成
        dep_spinner() //部門spinner項目生成
        pgdialog() //進度條生成

        custodian_main_T_msg.movementMethod = ScrollingMovementMethod.getInstance() //textview啟用捲動
    }

    //廠別spinner選單
    private fun site_spinner() {
        Site_spinner = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line,
                resources.getStringArray(R.array.custodian_main_s_sitelist))
        Site_spinner.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        Site_spinner.notifyDataSetChanged() //綁定更新
        custodian_main_S_site.adapter = Site_spinner
        custodian_main_S_site.setSelection(sitechoice,true) //預設為台北廠
        sitechoice = custodian_main_S_site.selectedItemId.toInt() //儲存使用者選擇的廠別項目
    }

    //部門spinner項目生成
    private fun dep_spinner() {
        Dep_spinner = ArrayAdapter(this,android.R.layout.simple_dropdown_item_1line,
                resources.getStringArray(R.array.custodian_main_s_dep))

        //setDropDownViewResource設定spinner顯示方式，記得layout的spinnerMode不要設定
        Dep_spinner.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        custodian_main_S_dep.adapter = Dep_spinner
        custodian_main_S_dep.setSelection(0,true) //部門預設值

        mContRes = contentResolver
        val cur: Cursor = mContRes.query(CustodianContentProvider.CONTENT_URI2, DEPCOLUMN,
                null,null,null)
        cur.moveToFirst() // 一定要寫，不然會出錯

        if (cur.count == 0){ //找到的資料筆數
            cur.close()
            checklink(classname,this) //監測網路連線

            if (wifilink == 1){
                handler.removeCallbacks(checkrepeat()) //避免任務還在執行故移除
                dbdep() //下載部門資料

            }else{
                scvnew("行動裝置無部門項目資料\n正在嘗試連接網路下載部門資料...")
                handler.postDelayed(checkrepeat(),5000) // 五秒後重try
            }

        }else{
            val columnCount: Int = cur.columnCount //每筆資料欄位數
            var sqlbeforid: Int = 0 //儲存上一筆id
            var sqlgetid: Int = 0 //儲存此筆id

            while (!cur.isAfterLast){ //確認cur不是在最後一筆，解決第一筆資料不見的問題
                var fldSet: String = ""
                sqlgetid = Integer.parseInt(cur.getString(1))

                if (sqlbeforid != sqlgetid){ //過濾重複的部門id

                    for (ii in 0..(columnCount - 1)){
                        fldSet += cur.getString(ii)

                        if (ii < columnCount)
                            fldSet += "#"
                    }

                    sqlbeforid = sqlgetid
                    dep_sqlite.add(fldSet)
                }

                cur.moveToNext()
            }

            cur.close()
            arrayliststart() //SQLite載入項目前先初始化

            for (i in 0..(dep_sqlite.size - 1)){
                val fld: Array<String> = dep_sqlite[i].split("#").toTypedArray()
                naj_dept_id.add(fld[1])
                naj_dept_name.add(fld[2])
                naj_dept_name_id.add(fld[2] + "(" + fld[1] + ")")
            }

            Dep_spinner = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line,
                    naj_dept_name_id)
            Dep_spinner.notifyDataSetChanged() //宣告綁定更新Spinner項目(只綁定最先宣告的)
            custodian_main_S_dep.adapter = Dep_spinner
            custodian_main_S_dep.setSelection(idnum,true) //部門預設值
            custodian_main_B_download.text = "開始盤點"
            custodian_main_B_download.visibility = View.VISIBLE //顯示按鈕
        }

    }

    //剛裝好app網路未連接時，循環try下載部門
    private fun checkrepeat(): Runnable = Runnable {
        dep_spinner()
    }

    //撈取伺服器部門資料
    private fun dbdep(){

        //新增worker thread run 大量運算
        singleThreadExecutor.execute {
            msgtext = "" //清空值

            try {
                msgtext = "正在嘗試連接伺服器..."
                dbdepui_update(0) //更新ui
                val result: String? = DBConnector.executeQuery(depconnect_ip)

                //存取類別成員DBConnector.httpstate 判定是否回應200(連線要求成功)
                if (httpstate == 200){
                    msgtext = "伺服器連接成功"
                    dbdepui_update(0) //更新ui

                    val r: String = result?.trim() ?: null.toString()

                    if (r.length > 5) { //非null

                        /*******************************************************************************************
                         * SQL 結果有多筆資料時使用JSONArray 只有一筆資料時直接建立JSONObject物件 JSONObject
                         * jsonData = new JSONObject(result);
                         *******************************************************************************************/
                        val jsonArray: JSONArray = JSONArray(result)
                        jsondatanum = jsonArray.length()

                        if (jsondatanum > 0){ //有資料
                            msgtext = "伺服器部門資料下載完畢，正在存入行動裝置..."
                            dbdepui_update(0)
                            uri = CustodianContentProvider.CONTENT_URI2
                            mContRes.delete(uri,null,null) //匯入前,刪除資料
                            arrayliststart() //下載項目載入前先設定arraylist

                            var beforeid: Int = 0 //儲存上一筆管理部門id
                            var getid: Int = 0 //儲存此筆管理部門id

                            // 處理JASON 傳回來的每筆資料
                            for (i in 0..(jsondatanum - 1)){
                                val jsonData: JSONObject = jsonArray.getJSONObject(i)
                                val newRow: ContentValues = ContentValues()

                                if (jsonData.getString("naj_dept_id") == "" ||
                                        jsonData.getString("naj_dept_name") == ""){
                                    depdataerror++

                                }else {
                                    getid = Integer.parseInt(jsonData.getString("naj_dept_id"))

                                    if (beforeid != getid){ //過濾重複的管理部門id
                                        naj_dept_id.add(jsonData.getString("naj_dept_id"))
                                        naj_dept_name.add(jsonData.getString("naj_dept_name"))
                                        naj_dept_name_id.add((jsonData.getString("naj_dept_name")
                                                + "(" + jsonData.getString("naj_dept_id") + ")"))
                                        beforeid = getid
                                    }

                                    // ---(2) 使用固定已知欄位---------------------------
                                    newRow.put("naj_dept_id", jsonData.getString("naj_dept_id"))
                                    newRow.put("naj_dept_name", jsonData.getString("naj_dept_name"))
                                    newRow.put("dept_no", jsonData.getString("dept_no"))
                                    newRow.put("dept_name", jsonData.getString("dept_name"))
                                    // -------------------加入SQLite---------------------------------------
                                    mContRes.insert(CustodianContentProvider.CONTENT_URI2, newRow)
                                }
                            }

                            msgtext = "行動裝置已存入部門資料：共${jsondatanum}筆，已下載" +
                                    "${(jsondatanum - depdataerror)}筆資料，錯誤資料${depdataerror}筆。"
                            dbdepui_update(0) //更新ui
                            val c: Cursor = mContRes.query(CustodianContentProvider.CONTENT_URI2,
                                    DEPCOLUMN,null,null,null)
                            c.moveToFirst()
                            val i: Int = c.count
                            c.close()

                            if (i == 0){
                                msgtext = "伺服器與本機資料庫不合,請檢查伺服器路徑!"
                                dbdepui_update(0)

                            }else{
                                msgtext = "已完成由伺服器下載部門資料"
                                dbdepui_update(1)
                            }

                        }else {
                            msgtext = "主機資料庫無資料"
                            dbdepui_update(0)
                        }

                    }else{
                        msgtext = "伺服器無部門資料"
                        dbdepui_update(0)
                    }

                }else{
                    msgtext = "伺服器無回應,請檢查網路是否連接至內網"
                    dbdepui_update(0)
                    handler.postDelayed(checkrepeat(),5000) //五秒後重try
                }

            }catch (e: Exception){
                Log.d(TAG,"error=" + e.toString())
                msgtext = "網路問題或查無資料"
                dbdepui_update(0)
                reponlog(classname,"dbdep() error:" + e.toString(),0, context) //寫log
            }
        }

    }

    //設定arraylist第一個項目值
    private fun arrayliststart() {
        naj_dept_id.add("0")
        naj_dept_name.add("請選擇部門")
        naj_dept_name_id.add("請選擇部門")
    }

    //執行緒執行時傳遞更新UI的msg
    private fun dbdepui_update(progress: Int) {

        runOnUiThread {
            when(progress){
                0 -> scvnew(msgtext) //更新textview狀態

                1 -> { //dbdep資料下載完畢
                    scvnew(msgtext)

                    //設定部門選單
                    Dep_spinner = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line,
                            naj_dept_name_id)
                    Dep_spinner.notifyDataSetChanged() //綁定更新Spinner項目
                    custodian_main_S_dep.adapter = Dep_spinner
                    custodian_main_S_dep.setSelection(0,true) //部門預設值
                    scvnew("開始下載盤點資料\n請稍候...")
                    dbsql() //下載盤點資料
                }

                2 -> { //dbsql資料下載完畢
                    custodian_main_B_download.text = "開始盤點"
                    progressDialog.setMessage("行動裝置資料載入完成。")
                    datadownload = 0 //下載完重設
                    Thread.sleep(500) //睡一下
                    progressDialog.dismiss() //關閉進度條
                    i = 0 //恢復預設值
                    pgdialog() //進度條重置
                }

                3 -> { //dbsql資料下載結束
                    custodian_main_B_download.visibility = View.VISIBLE //顯示按鈕
                    progressDialog.dismiss() //關閉進度條
                    datadownload = 0 //下載完重設
                    pgdialog() //進度條重置
                }

                4 -> progressDialog.show() //dbsql連接成功顯示進度條

                5 -> { //設定進度條資訊
                    progressDialog.setMessage("盤點資料正在存入行動裝置...")
                    progressDialog.max = jsondatanum
                }

                6 -> { //更新進度條
                    pgress_num = i / jsondatanum * 100 //設定進度條每次跑多少%
                    progressDialog.progress = pgress_num //更新進度條(%)
                    progressDialog.incrementProgressBy(i) //更新insert的資料量(右邊下載進來的資料量)
                }

                7 -> { //確認盤點部門，bundle資料並跳頁
                    val it: Intent = Intent()
                    it.putExtra("site_id", site_id[sitechoice]) //丟廠別ID過去
                    it.putExtra("Management_dep_name", Management_dep_name) //丟部門名稱過去
                    it.putExtra("Management_dep_id", naj_dept_id[idnum]) //丟管理部門ID過去(注意要是String格式)
                    it.putStringArrayListExtra("Dep_inventory_data", dep_sqlite) //丟部門盤點資料過去
                    it.setClass(this, Custodian_list::class.java)
                    startActivity(it)
                    this.finish() //殺掉此頁
                }

                8 -> { //重新下載盤點資料
                    scvnew(msgtext)
                    custodian_main_B_download.visibility = View.GONE //下載資料時先殺掉
                    dbsql() //重新下載資料
                }
            }
        }

    }

    //設定進度條
    private fun pgdialog() {
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("執行中")
        progressDialog.setMessage("正在連接伺服器...")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.setCancelable(false)
    }

    //撈伺服器所有盤點資料
    private fun dbsql() {

        //新增worker thread run 大量運算
        singleThreadExecutor.execute {

            if (sitechoice == 1){ //選擇RP
                tablecolumn = RPCOLUMN
                uri = CustodianContentProvider.CONTENT_URI3

            }else{
                tablecolumn = NJCOLUMN
                uri = CustodianContentProvider.CONTENT_URI
            }

            try {
                dbdepui_update(4)
                val dep: String = site_id[sitechoice] //部門
                val into: String = "?site=$dep" // + "&fix_no=" + item; "&dept_id="部門代號"
                val result: String? = DBConnector.executeQuery(connect_ip + into) //連公司內網時

                //存取類別成員DBConnector.httpstate 判定是否回應200(連線要求成功)
                if (httpstate == 200){
                    msgtext = "伺服器連接成功，開始下載資料..."
                    dbdepui_update(0) //更新ui

                    /*******************************************************************************************
                     * SQL 結果有多筆資料時使用JSONArray 只有一筆資料時直接建立JSONObject物件 JSONObject
                     * jsonData = new JSONObject(result);
                     *******************************************************************************************/
                    val jsonArray: JSONArray = JSONArray(result)

                    if (jsonArray.length() > 0){ //連結成功有資料
                        jsondatanum = jsonArray.length()
                        mContRes.delete(uri,null,null) //匯入前,刪除資料
                        depdataerror = 0 //清空筆數
                        dbdepui_update(5)

                        //處理JASON傳回來的每筆資料，先過濾空值得資料數確保正確要寫入的資料筆數
                        for (e in 0..(jsondatanum - 1)){
                            val jsonData: JSONObject = jsonArray.getJSONObject(e)

                            if (jsonData.getString("fix_code") == "")
                                depdataerror++
                        }

                        val bulkToInsert: Array<ContentValues?> = arrayOfNulls<ContentValues>(jsondatanum - depdataerror) //注意資料寫入是從0開始

                        // 處理JASON 傳回來的每筆資料
                        var error_num: Int = 0 //記錄發生錯誤數量
                        var ok_num: Int = 0 //記錄正確寫入SQLite的筆數

                        for (ii in 0..(jsondatanum - 1)){
                            i = ii
                            val jsonData: JSONObject = jsonArray.getJSONObject(i)

                            //空值的過濾掉
                            if (jsonData.getString("fix_code") == "")
                                error_num++
                            else{
                                val newRow: ContentValues = ContentValues()

                                // ---(2) 使用固定已知欄位---------------------------
                                newRow.put("fix_code", jsonData.getString("fix_code"))
                                newRow.put("fix_name", jsonData.getString("fix_name"))
                                newRow.put("fix_spec", jsonData.getString("fix_spec"))
                                newRow.put("naj_dept_id", jsonData.getString("naj_dept_id"))
                                newRow.put("dept_id", jsonData.getString("dept_id"))
                                newRow.put("dept_name", jsonData.getString("dept_name"))
                                newRow.put("empl_id", jsonData.getString("empl_id"))
                                newRow.put("empl_name", jsonData.getString("empl_name"))
                                newRow.put("location", jsonData.getString("location"))
                                newRow.put("fix_status", jsonData.getString("fix_status"))
                                newRow.put("site", jsonData.getString("site"))
                                newRow.put("qty", "1") //預設盤點物品數量為1
                                newRow.put("fix_inventory", "N") //預設盤點狀態為'N'
                                newRow.put("queryfix_inventory", "N") //預設查詢盤點狀態為'N'

                                if (error_num > 0){ //上一筆以上發生錯誤了(不是我們需要的資料)
                                    ok_num -= error_num
                                    bulkToInsert[ok_num] = newRow
                                    error_num = 0 //復歸

                                }else
                                    bulkToInsert[ok_num] = newRow
                            }

                            ok_num++
                            dbdepui_update(6)
                        }
                        // -------------------加入SQLite---------------------------------------
                        mContRes.bulkInsert(uri, bulkToInsert) //大量寫入
                        msgtext = "已找到伺服器資料：共${jsondatanum}筆，已下載" +
                                  "${(jsondatanum - depdataerror)}筆資料，錯誤資料${depdataerror}筆。"
                        dbdepui_update(0)
                        val c: Cursor = mContRes.query(uri, tablecolumn,null,
                                null,null)
                        c.moveToFirst()
                        val i: Int = c.count
                        c.close()

                        if (i == 0) {
                            msgtext = "伺服器與本機資料庫不合,請檢查伺服器路徑!"
                            dbdepui_update(0)

                        } else {
                            msgtext = "已完成由伺服器下載資料"
                            dbdepui_update(2)
                        }

                    }else{
                        msgtext = "主機資料庫無資料"
                        dbdepui_update(0)
                    }

                }else{
                    msgtext = "伺服器無回應，是否連接至內網"
                    dbdepui_update(0)
                }

            }catch (e: Exception){
                Log.d(TAG,"error=" + e.toString())
                msgtext = "所選部門查無資料，請確認網路訊號或重新選擇部門"
                dbdepui_update(0)
                reponlog(classname,"dbsql() error:" + e.toString(),0, context) //寫log
            }

            dbdepui_update(3)
        }

    }

    //設定捲動訊息維持在最新的幾行
    private fun scvnew(msgget: String) {

        if (msgsave == "")
            msgsave = msgget + "\n"
        else
            msgsave += msgget + "\n"

        //有新訊息進來時設定為藍色
        val alltext: SpannableString = SpannableString(msgsave)
        val i: Int = msgget.length //取得本次加入字串數量
        val j: Int = alltext.length //取得全部字串數量
        alltext.setSpan(ForegroundColorSpan(Color.GRAY),0,j - 1,0) //設定舊訊息
        alltext.setSpan(ForegroundColorSpan(Color.BLUE),j - i -1, j,0) //設定新訊息
        custodian_main_T_msg.setText(alltext, TextView.BufferType.SPANNABLE)

        //設定訊息維持在最新:scrollTo方法
        val offset: Int = custodian_main_T_msg.lineCount * custodian_main_T_msg.lineHeight //文本高度
        val strset: Int = custodian_main_T_msg.height //取得textview視窗高度

        if (offset > strset)
            custodian_main_T_msg.scrollTo(0,offset - strset)
    }

    //下載按鈕事件
    fun downloadlist(view: View){
        sitechoice = custodian_main_S_site.selectedItemId.toInt() //儲存使用者選擇的廠別項目
        Management_dep_name = custodian_main_S_dep.selectedItem.toString().trim() //儲存使用者選擇的部門
        idnum = custodian_main_S_dep.selectedItemId.toInt()
        Management_dep_id = Integer.parseInt(naj_dept_id[idnum]) //管理部門id

        if (sitechoice == 1){ //選擇RP
            uri = CustodianContentProvider.CONTENT_URI3
            tablecolumn = RPCOLUMN

        }else{
            uri = CustodianContentProvider.CONTENT_URI
            tablecolumn = NJCOLUMN
        }

        if (Management_dep_id == 0)
            scvnew("無此部門盤點資料")
        else{
            scvnew("已確認盤點部門\n等待資料載入中...")

            singleThreadExecutor.execute {
                mContRes = contentResolver

                try {
                    val c: Cursor = mContRes.query(uri, tablecolumn,null,null,
                            null)
                    c.moveToFirst() //一定要寫，不然會出錯
                    val a: Int = c.count

                    if (a > 0){ //確認sqlite有無盤點資料
                        c.close()

                        myselecion = "naj_dept_id = '$Management_dep_id'"
                        val cur: Cursor = mContRes.query(uri, tablecolumn, myselecion,null,
                                myorder)
                        cur.moveToFirst()
                        val b: Int = cur.count

                        if (b > 0){ //sqlite有資料
                            msgtext = "本機資料庫已找到此部門盤點資料"
                            dbdepui_update(0)

                            dep_sqlite = ArrayList() //暫存sqlite拉出的部門盤點資料，共用時注意資料一定要清空！！
                            val columnCount: Int = cur.columnCount

                            while (!cur.isAfterLast){
                                var fldSet = ""

                                for (ii in 0..(columnCount - 1)){
                                    fldSet += cur.getString(ii)

                                    if (ii < columnCount)
                                        fldSet += "#"
                                }

                                dep_sqlite.add(fldSet)
                                cur.moveToNext()
                            }

                            cur.close()
                            dbdepui_update(7)

                        }else{
                            cur.close()
                            msgtext = "本機資料庫查無此部門盤點資料，請確認是否為管理部門"
                            dbdepui_update(0)
                        }

                    }else{
                        c.close()
                        msgtext = "手機資料庫無資料，正在重新下載資料...\n請耐心等待。"
                        dbdepui_update(8)
                    }

                }catch (e: Exception){
                    Log.d(TAG,"error=" + e.toString())
                    msgtext = "downloadlist() error:" + e.toString()
                    reponlog(classname, msgtext,0, context) //寫log
                }

            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkrepeat()) //移除工作

        //儲存使用者選擇的廠別及部門資料---------------------------------------------------------------
        val custodian_data: SharedPreferences = getSharedPreferences("custodian_set",0)
        custodian_data.edit()
                      .putInt("sitechoice", sitechoice)
                      .putInt("idnum", idnum)
                      .apply()
        //------------------------------------------------------------------------------------------
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId

        if (id == Custodian_main_menu_update){
            checklink(classname,this) //偵測網路是否連線

            if (datadownload == 0 && wifilink == 1){

                //設定提示視窗
                val reponAlertDialog: ReponAlertDialog = ReponAlertDialog(this)
                reponAlertDialog.setTitle("盤點系統提示視窗")
                reponAlertDialog.setMessage("您確定要更新盤點資料嗎?\n提醒您，重新下載盤點資料會清空" +
                                            "已盤點的資料內容。")
                reponAlertDialog.setIcon(android.R.drawable.star_big_on)
                reponAlertDialog.setButton(BUTTON_POSITIVE, "取消", dialogbtn())
                reponAlertDialog.setButton(BUTTON_NEGATIVE, "確定", dialogbtn())
                reponAlertDialog.show()

            }else if (datadownload == 1 && wifilink == 1)
                Toast.makeText(this,"資料下載中請稍後...", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this,"下載失敗!", Toast.LENGTH_SHORT).show()

        }

        return super.onOptionsItemSelected(item)
    }

    private fun dialogbtn(): DialogInterface.OnClickListener? = object: DialogInterface.OnClickListener{

        override fun onClick(dialog: DialogInterface?, which: Int) {

            when(which){
                BUTTON_POSITIVE -> Toast.makeText(context,"未下載任何資料", Toast.LENGTH_SHORT).show() //取消更新
                BUTTON_NEGATIVE -> {
                    datadownload = 1 //標記下載資料中
                    sitechoice = custodian_main_S_site.selectedItemId.toInt() //儲存使用者選擇的廠別項目
                    val site: String = custodian_main_S_site.selectedItem.toString().trim() //儲存使用者選擇的廠別
                    Toast.makeText(context, "下載 $site 新的盤點資料", Toast.LENGTH_LONG).show()

                    if (sitechoice == 1){ //選擇RP
                        uri = CustodianContentProvider.CONTENT_URI3
                        tablecolumn = RPCOLUMN
                    }else{
                        uri = CustodianContentProvider.CONTENT_URI
                        tablecolumn = NJCOLUMN
                    }

                    dbsql() //下載盤點資料
                }
            }

        }

    }

    //監聽手機返回鍵
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if (keyCode == KeyEvent.KEYCODE_BACK){

            if (System.currentTimeMillis() - exitTime > 2000){
                exitTime = System.currentTimeMillis()

                if (datadownload == 1)
                    Toast.makeText(context,"資料下載中請稍後...", Toast.LENGTH_LONG).show()
                else
                    Toast.makeText(context,"再按一次退出程式", Toast.LENGTH_SHORT).show()
            }else
                this.finish()

            return true
        }

        return super.onKeyDown(keyCode, event)
    }

}