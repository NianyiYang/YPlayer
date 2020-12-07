package com.yny.yplayer

import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission
import com.yny.yplayer.decoder.AudioDecoder
import com.yny.yplayer.decoder.VideoDecoder
import com.yny.yplayer.muxer.MP4Repack
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_play).setOnClickListener {
            initPlayer()
        }

        findViewById<Button>(R.id.btn_repack).setOnClickListener {
            repack()
        }

        requestPermission()
    }

    private fun requestPermission() {
        AndPermission.with(this)
            .runtime()
            .permission(
                Permission.Group.STORAGE,
                Permission.Group.MICROPHONE
            )
            .onGranted {

            }
            .onDenied {
                Toast.makeText(this, "请打开权限，否则无法获取本地文件", Toast.LENGTH_SHORT).show()
            }
            .start()
    }

    private fun initPlayer() {
        val path = Environment.getExternalStorageDirectory().absolutePath + "/aaa.mp4"

        // 创建线程池
        val threadPool = Executors.newFixedThreadPool(10)

        // 创建视频解码器
        val videoDecoder = VideoDecoder(path, sfv, null)
        threadPool.execute(videoDecoder)

        // 创建音频解码器
        val audioDecoder = AudioDecoder(path)
        threadPool.execute(audioDecoder)

        // 开启播放
        videoDecoder.resume()
        audioDecoder.resume()
    }

    private fun repack() {
//        val file = File(Environment.getExternalStorageDirectory().absolutePath + "/aaa.mp4")
//        if (!file.exists()) {
//            Log.i(MainActivity::class.java.simpleName, "mp4文件不存在")
//            return
//        }
//        //实例一个MediaExtractor
//        val extractor = MediaExtractor()
//
//        try {
//            extractor.setDataSource(file.absolutePath) //设置添加MP4文件路径
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        val count = extractor.trackCount //获取轨道数量
//
//        Log.e(MainActivity::class.java.simpleName, "轨道数量 = $count")
//        for (i in 0 until count) {
//            val mediaFormat = extractor.getTrackFormat(0)
//            Log.i(
//                MainActivity::class.java.simpleName,
//                i.toString() + "编号通道格式 = " + mediaFormat.getString(MediaFormat.KEY_MIME)
//            )
//        }

        val path = Environment.getExternalStorageDirectory().absolutePath + "/aaa.mp4"
        MP4Repack(path).apply { start() }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
//    external fun stringFromJNI(): String
//
//    companion object {
//        // Used to load the 'native-lib' library on application startup.
//        init {
//            System.loadLibrary("native-lib")
//        }
//    }
}
