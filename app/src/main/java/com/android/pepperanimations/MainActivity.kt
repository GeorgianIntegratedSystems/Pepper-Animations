package com.android.pepperanimations

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.`object`.actuation.Animate
import com.aldebaran.qi.sdk.`object`.actuation.Animation
import com.aldebaran.qi.sdk.`object`.touch.TouchSensor
import com.aldebaran.qi.sdk.builder.AnimateBuilder
import com.aldebaran.qi.sdk.builder.AnimationBuilder
import com.aldebaran.qi.sdk.design.activity.RobotActivity

class MainActivity : RobotActivity(), RobotLifecycleCallbacks {

    private var animate: Animate? = null
    var songPlayer: MediaPlayer? = null
    var animateFuture: Future<Void>? = null
    private var headTouchSensor: TouchSensor? = null
    lateinit var solitariesLoop: SolitariesLoop

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        QiSDK.register(this, this)

        findViewById<Button>(R.id.startAnimBtn).setOnClickListener {
            animateFuture = animate?.async()?.run()
        }
        findViewById<Button>(R.id.startSolitariesAnim).setOnClickListener {
            Thread {
                solitariesLoop.start()
            }.start()
        }
    }

    override fun onRobotFocusGained(qiContext: QiContext?) {
        val animation: Animation =
            AnimationBuilder.with(qiContext)
                .withResources(R.raw.fist_bump_anim)
                .build()

        animate = AnimateBuilder.with(qiContext)
            .withAnimation(animation)
            .build()

        animateFuture = animate?.async()?.run()

        solitariesLoop = SolitariesLoop(qiContext!!, 4, 0, 3)
        songPlayer = MediaPlayer.create(this, R.raw.boom)

        animate?.addOnLabelReachedListener { label, time ->
            songPlayer!!.start()
        }
        touchSensors(qiContext)

    }

    override fun onRobotFocusLost() {
        animate?.removeAllOnLabelReachedListeners()
    }

    override fun onRobotFocusRefused(reason: String?) {
    }

    private fun touchSensors(qiContext: QiContext) {
        val touch = qiContext.touch
        headTouchSensor = touch.getSensor("Head/Touch")
        headTouchSensor!!.addOnStateChangedListener { touchState ->
            if (touchState.touched) {
                rood(qiContext)
            }
        }

    }

    private fun rood(qiContext: QiContext) {
        val myAnimation: Animation = AnimationBuilder.with(qiContext)
            .withResources(R.raw.nicereaction_a002)
            .build()
        val btnClick: MediaPlayer = MediaPlayer.create(this, R.raw.laught)
        btnClick.start()
        val animate = AnimateBuilder.with(qiContext)
            .withAnimation(myAnimation)
            .build()
        animate.async().run()
    }

}