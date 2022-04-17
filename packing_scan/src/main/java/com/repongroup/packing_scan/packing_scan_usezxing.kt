package com.repongroup.packing_scan

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.view.KeyEvent
import android.view.View
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.repongroup.repon_android.Repon_android.Companion.langstart
import kotlinx.android.synthetic.main.packing_scan_usezxing.*

class packing_scan_usezxing : Activity(), DecoratedBarcodeView.TorchListener {

    private lateinit var capture: CaptureManager
    private var handler = Handler()
    private val context = this
    private lateinit var thisview: View

    //自動補光相關宣告-----------------------------------------------------------
    private lateinit var sensor_manager: SensorManager
    private lateinit var sensorlister: MySenesorEventListener
    private var light_code = 0 //標記閃光燈：0=關,1=開,3=自動補光
    private var autolight = 0 //自動補光標記：0=關,1=開
    private var exitTime: Long = 0
    //--------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.packing_scan_usezxing)
        langstart(context) //起始語言配置

        zxing_barcode_scanner.setTorchListener(this)
        if (!hasFlash()) switch_flashlight.visibility = View.GONE
        capture = CaptureManager(this, zxing_barcode_scanner)
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode()
        checkautolight() //檢查是否使用自動補光
    }

    //檢查是否使用自動補光
    private fun checkautolight() {
        val textsetdata = getSharedPreferences("TEXT_SET",0)
        light_code = textsetdata.getInt("Light_mode",0)

        if (light_code == 2){ //使用自動補光
            switch_flashlight.text = getString(R.string.auto_flashlight)
            switch_flashlight.setTextColor(ContextCompat.getColor(context, R.color.floralwhite))
            switch_flashlight.setBackgroundColor(ContextCompat.getColor(context, R.color.deepskyblue))
            autolight = 0 //使用前歸零

            //Light傳感器--------------------------------------------------------------------------------
            sensor_manager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val sensor = sensor_manager.getDefaultSensor(Sensor.TYPE_LIGHT)
            sensorlister = MySenesorEventListener()
            sensor_manager.registerListener(sensorlister, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            //------------------------------------------------------------------------------------------

        }else if (light_code == 1) handler.postDelayed(lightsave(),500) //開燈狀態
    }

    //載入開關燈工作包
    private fun lightsave(): Runnable? = Runnable {
        thisview = window.decorView
        switchFlashlight(thisview)
//        switch_flashlight.performClick()
    }

    //開燈工作包
    private fun autolighton(): Runnable = Runnable {
        thisview = window.decorView
        autobtn(thisview)
//        auto_button.performClick()
    }

    //關燈工作包
    private fun autolightoff(): Runnable = Runnable {
        thisview = window.decorView
        autobtn(thisview)
//        auto_button.performClick()
    }

    /**
     * Check if the device's camera has a Flashlight.
     * @return true if there is Flashlight, otherwise false.
     */
    private fun hasFlash(): Boolean {
        return applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    //閃光燈開關
    fun switchFlashlight(view: View){

        //儲存掃描模式----------------------
        val textsetdata = getSharedPreferences("TEXT_SET",0)

        when(switch_flashlight.text){
            getString(R.string.turn_on_flashlight) -> {
                zxing_barcode_scanner.setTorchOn()
                light_code = 1
            }

            getString(R.string.turn_off_flashlight) -> {
                zxing_barcode_scanner.setTorchOff()
                light_code = 2
            }

            getString(R.string.auto_flashlight) -> {
                light_code = 0
                zxing_barcode_scanner.setTorchOff()
            }
        }

        textsetdata.edit().putInt("Light_mode", light_code).apply()
        if (light_code == 2) checkautolight()
    }

    //自動補光隱藏按鈕
    fun autobtn(view: View){

        autolight = if (autolight == 0){
            zxing_barcode_scanner.setTorchOn()
            1

        }else{
            zxing_barcode_scanner.setTorchOff()
            0
        }

    }

    override fun onTorchOn() {

        if (light_code != 2){ //非自動模式
            switch_flashlight.text = getString(R.string.turn_off_flashlight)
            switch_flashlight.setTextColor(ContextCompat.getColor(context, R.color.white))
            switch_flashlight.setBackgroundColor(ContextCompat.getColor(context, R.color.orangered))
        }

    }

    override fun onTorchOff() {

        if (light_code != 2){ //非自動模式
            switch_flashlight.text = getString(R.string.turn_on_flashlight)
            switch_flashlight.setTextColor(ContextCompat.getColor(context, R.color.black))
            switch_flashlight.setBackgroundColor(ContextCompat.getColor(context, R.color.greenyellow))
        }

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
        if (light_code == 2) sensor_manager.unregisterListener(sensorlister) //有起用自動補光:註銷光線感應器監聽
    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
        handler.removeCallbacks(autolighton())
        handler.removeCallbacks(autolightoff())
        handler.removeCallbacks(lightsave())
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return zxing_barcode_scanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

    @SuppressLint("Registered")
    inner class MySenesorEventListener: SensorEventListener { //inner => 重要!!表示為此kt的內部內別，當然可以使用此kt內所有的參數跟fun
        override fun onSensorChanged(event: SensorEvent) {

            //Android的Light Sensor照度偵測內容只有values[0]有意義!
            val lux = event.values[0]
            val msg = "偵測器sensor:${event.sensor.name}\n流明照度值:${lux}Lux\n"

            //更新UI
            runOnUiThread {

                if (lux <= 40){ //流明小於40時

                    if (autolight == 0){ //閃光燈是關的

                        if (System.currentTimeMillis() - exitTime > 3000){ //經過三秒後再執行
                            exitTime = System.currentTimeMillis() //控制關閉時間限制
                            handler.postDelayed(autolighton(),500) //開執行緒實現自動點擊事件(touch與click監聽同時存在衝突時使用)
                        }

                    }

                }else{

                    if (autolight == 1){

                        if (System.currentTimeMillis() - exitTime > 3000) { //經過三秒後再執行
                            exitTime = System.currentTimeMillis() //控制關閉時間限制
                            handler.postDelayed(autolightoff(),500) //開執行緒實現自動點擊事件(touch與click監聽同時存在衝突時使用)
                        }

                    }

                }

            }

        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

}
