package com.repongroup.packing_scan

import android.os.Environment
import android.util.Log
import com.repongroup.packing_scan.packing_scan_main.Companion.TAG
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.*

class DBConnector {

    //宣告類別變數以方便存取，並判斷是否連線成功
    companion object {
        var httpstate = 0
        private var result = ""
        private val timeoutConnection = 3000 //設定超時三秒
        private val timeoutSocket = 5000 //設定超時五秒
        var code = 0 //接收伺服器回傳更新值
        val connect_ip = "http://170.168.110.49:8888/WMS/asrs-j/insertJ.aspx" //連公司內網上傳掃描資料
        val apktxt_ip = "http://170.168.110.49:8888/AndroidVersion/packing_scan/" //存放apktxt網址
        val apk_ip = "http://170.168.110.49:8888/AndroidVersion/packing_scan/packing_scan.zip" //存放apk網址
        val File_NAME = "packing_scan.zip"
        lateinit var file: File //記錄下載完檔案解壓縮後的路徑及名稱

        //判斷外部儲存空間是否可寫入
        private fun isExternalStorageWritable(): Boolean{
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

        //判斷外部儲存空間是否可讀取
        private fun isExternalStorageReadable(): Boolean{
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
        }

        //創建存放apk公有資料夾路徑
        private fun getExtoragePublicDir(albumName: String): File{
            val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            //如果該路徑不存在，創建文件夾
            if (!file.mkdir()){
                val f = File(file, albumName)
                if (f.mkdir()) return f
            }

            return File(file, albumName)
        }

        //GET傳遞給公司內網
        fun executedata(s: String): String{

            //全域變數使用前重設(程式中有重複使用時要特別注意!!)
            result = ""
            httpstate = 0
            //---------------------------------------------

            try {
                val url = URL(s)
                val conn = url.openConnection() as HttpURLConnection

                //設定連接屬性、連接、下載數據
                conn.connectTimeout = timeoutConnection //連線等待時間
                conn.readTimeout = timeoutSocket //讀取等待時間
                conn.useCaches = true //快取開關
                conn.requestMethod = "GET" //設定GET傳遞
                conn.connect()
                httpstate = conn.responseCode //取得連線代碼:200表成功

                if (httpstate == HttpURLConnection.HTTP_OK){
                    val resultData = StringBuffer()
                    val inputStream = conn.inputStream
                    val input = InputStreamReader(inputStream,"utf-8")
                    val buffer = BufferedReader(input)
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

        //GET傳遞給公司內網網址下載apk
        fun executeapkdownload(): Int {
            var i = 0
            var f: File? = null
            val BUFFER_SIZE = 4096 //設定緩衝區:一定要設不然會出錯

            if (isExternalStorageWritable() && isExternalStorageReadable())
                f = File(getExtoragePublicDir("scan_app"), File_NAME) //創建私有資料夾

            try {

                //獲得輸入的檔案並下載到指定位置
                val input = URL(apk_ip).openStream()
                val output = FileOutputStream(f)

                input.use { _ ->
                    output.use { _ ->
                        input.copyTo(output)
                    }
                }

                output.close() //用完關掉
                input.close()

                //開始解壓縮
                val zip = FileInputStream(f)
                val zipStream = ZipInputStream(zip)
                val zipEntry = zipStream.nextEntry

                if (zipEntry != null) {
                    val zipEntryname = zipEntry.name
                    file = File(getExtoragePublicDir("scan_app"), zipEntryname) //設定儲存路徑
                    val buffer = ByteArray(BUFFER_SIZE)
                    val unzip = FileOutputStream(file) //開始儲存
                    val bos = BufferedOutputStream(unzip, BUFFER_SIZE)
                    var count = 0


                    while (zipStream.read(buffer,0, BUFFER_SIZE).apply { count = this } > 0) {
                        bos.write(buffer, 0, count)
                    }

                    bos.flush()
                    bos.close()
                    unzip.close()

                    if (f!!.exists()) f.delete() //解壓縮完畢，zip檔案存在則刪除
                }

                zipStream.close()
                zip.close()

                i = 1 //下載成功!

            }catch (e: ZipException){
                Log.d(TAG,"error=" + e.toString())
                i = 2 //出現未知錯誤
            }

            return i
        }

    }

}