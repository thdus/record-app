package com.example.myapplication10

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication10.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity(), OnTimerTickListener {

    companion object {
        private const val REQUEST_RECORD_AUDIO_CODE = 200
    }

    // 릴리즈 -> 녹음중 -> 릴리즈
    // 릴리즈 -> 재생 -> 릴리즈
    private enum class State {
        RELEASE, RECORDING, PLAYING
    }

    private lateinit var timer: Timer

    private lateinit var binding: ActivityMainBinding
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var fileName: String = ""
    private var state: State = State.RELEASE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"
        timer = Timer(this)

        binding.recordButton.setOnClickListener {
            when(state) {
                State.RELEASE -> {
                    record()
                }State.RECORDING ->{
                    onRecord(false)
                }State.PLAYING -> {

                }}
        }
        binding.playButton.setOnClickListener {
            when(state) {
                State.RELEASE -> {
                    onPlay(true)
                } else -> {
                    // do nothing
                }}
        }
        binding.playButton.isEnabled=false
        binding.playButton.alpha = 0.3f

        binding.stopButton.setOnClickListener {
            when(state) {
            State.PLAYING -> {
                onPlay(false)
            }else -> {
                // do nothing
            }
            }
        }
    }

    private fun record() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 실제로 녹음을 시작하면 됨
                onRecord(true)
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.RECORD_AUDIO) -> {
                showPermissionRationalDialog()
            }
            else -> {
                // You can directly ask for the permission.
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_CODE)
            }
        }
    }

    private fun onRecord(start:Boolean)=if (start){
        startRecording()
        }else{
            stopRecording()
        }

    private fun onPlay(start: Boolean) = if(start) startPlaying() else stopPlaying()

        private fun startRecording() {
            state = State.RECORDING

            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(fileName)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                try{
                    prepare()
                }catch (e: IOException){
                    Log.e("APP","prepare() failed $e")
                }

                start()
            }

            binding.waveformView.clearData()

            timer.start()

            binding.recordButton.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.baseline_stop_24
                )
            )
            binding.recordButton.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this,R.color.black))
            binding.playButton.isEnabled = false
            binding.playButton.alpha = 0.3f
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null

        timer.stop()

        state = State.RELEASE

        binding.recordButton.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.baseline_fiber_manual_record_24
            )
        )
        binding.recordButton.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red))
        binding.playButton.isEnabled = true
        binding.playButton.alpha = 1.0f
    }

    private fun startPlaying(){
        state = State.PLAYING

        player = MediaPlayer().apply {
            setDataSource(fileName)
            try{
                prepare()
            } catch (e: IOException) {
                Log.e("APP", "media player prepare fail $e")
            }
            start()
        }

        binding.waveformView.clearWave()

        timer.start()

        player?.setOnCompletionListener {
            stopPlaying()
        }

        binding.recordButton.isEnabled = false
        binding.recordButton.alpha = 0.3f
    }

    private fun stopPlaying(){
        state = State.RELEASE

        player?.release()
        player = null

        timer.stop()

        binding.recordButton.isEnabled = true
        binding.recordButton.alpha
    }

    private fun showPermissionRationalDialog() {
        AlertDialog.Builder(this)
            .setMessage("녹음 권한을 켜주셔야지 앱을 정상적으로 사용할 수 있습니다.")
            .setPositiveButton("권한 허용하기"){ _, _->
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_CODE)
            }.setNegativeButton("취소"){
                dialogInterface, _ -> dialogInterface.cancel()
            }.show()
    }

    private fun showPermissionSettingDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.permission_setting_message))
            .setPositiveButton("권한 변경하러 가기"){ _, _->

            }.setNegativeButton("취소"){
                    dialogInterface, _ -> dialogInterface.cancel()
            }.show()
    }

    private fun navigateToAppSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply{
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val audioRecordPermissionGranted = requestCode == REQUEST_RECORD_AUDIO_CODE
                && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        if (audioRecordPermissionGranted) {
            onRecord(true)
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                showPermissionRationalDialog()
            } else {
                showPermissionSettingDialog()
            }
        }
    }

    override fun onTick(duration: Long) {
        val millisecond = duration % 1000
        val second = (duration / 1000) % 60
        val minute = (duration / 1000 / 60)


        binding.timerTextView.text =
            String.format("%02d:%02d.%02d", minute, second, millisecond / 10)

        if (state == State.PLAYING) {
            binding.waveformView.replayAmplitude()
        } else if (state == State.RECORDING) {

            binding.waveformView.addAmplitude(recorder?.maxAmplitude?.toFloat() ?: 0f)
        }
    }
}