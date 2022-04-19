package com.repongroup.inventory

import android.util.Log
import com.repongroup.inventory.Custodian_main.Companion.TAG
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class DBConnector {

    companion object {
        var httpstate: Int = 0
        private var result: String = ""
        private val timeoutConnection: Int = 3000 //設定超時三秒
        private val timeoutSocket: Int = 5000 //設定超時五秒
        var code: Int = 0 //接收伺服器回傳更新值
        private val myconnect_ip: String = "http://xxx.com/admin/android_mysql_connect/" //自己手機測試
        val depconnect_ip: String = "http://100.100.110.49:8888/getDept.aspx" //連公司內網下載部門資料
        val connect_ip: String = "http://100.100.110.49:8888/getFixMst.aspx" //連公司內網下載盤點資料
        val updateconnect_ip: String = "http://100.100.110.49:8888/updateFixMst.aspx" //連公司內網上傳盤點資料　?qty=1&site=NJ&

        //連公司內網抓資料
        fun executeQuery(s: String): String{

            //全域變數使用前重設(程式中有重複使用時要特別注意!!)
            result = ""
            httpstate = 0
            //---------------------------------------------

            try {
                val url: URL = URL(s)
                val conn: HttpURLConnection = url.openConnection() as HttpURLConnection

                //設定連接屬性、連接、下載數據
                conn.connectTimeout = timeoutConnection //連線等待時間
                conn.readTimeout = timeoutSocket //讀取等待時間
                conn.useCaches = true //快取開關
                conn.requestMethod = "GET" //設定GET傳遞
                conn.connect()
                httpstate = conn.responseCode //取得連線代碼:200表成功

                if (httpstate == HttpURLConnection.HTTP_OK){
                    val resultData: StringBuffer = StringBuffer()
                    val inputStream: InputStream = conn.inputStream
//                val encoding: String = conn.contentEncoding //取得資料編碼格式
                    val input: InputStreamReader = InputStreamReader(inputStream,"utf-8")
                    val buffer: BufferedReader = BufferedReader(input)
                    buffer.forEachLine { resultData.append(it) } //java -> kotlin抽出資料閃退的關鍵
                    buffer.close()
                    input.close()
                    inputStream.close()
                    result = resultData.toString()
                }

            }catch (e: Exception){
                Log.d(TAG,"error=" + e.toString())
                result = e.toString()
            }

            return result
        }


    }

}
