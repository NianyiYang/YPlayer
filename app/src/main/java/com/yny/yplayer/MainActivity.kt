package com.yny.yplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission
import com.yny.yplayer.decoder.AudioDecoder
import com.yny.yplayer.decoder.VideoDecoder
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_play).setOnClickListener {
            initPlayer()
        }

        requestPermission()
    }

    private fun requestPermission() {
        val permissions = Permission.Group.STORAGE
        AndPermission.with(this)
            .runtime()
            .permission(Permission.Group.MICROPHONE)
            .permission(permissions)
            .onGranted {

            }
            .onDenied {
                Toast.makeText(this, "请打开权限，否则无法获取本地文件", Toast.LENGTH_SHORT).show()
            }
            .start()
    }

    private fun initPlayer() {
        val path =
            Environment.getExternalStorageDirectory().absolutePath + "/VID_20201031_101628.mp4"

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

    override fun onDestroy() {
        super.onDestroy()
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
