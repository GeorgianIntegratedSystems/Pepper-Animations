package com.android.pepperanimations

import android.util.Log
import android.util.Log.d
import com.aldebaran.qi.Consumer
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.builder.AnimateBuilder
import com.aldebaran.qi.sdk.builder.AnimationBuilder
import com.aldebaran.qi.sdk.util.FutureUtils
import java.util.concurrent.TimeUnit

class SolitariesLoop(
    private val qiContext: QiContext,
    private var loopCounter: Int,
    private var animationType: Int,
    private val delayInSeconds: Int = 0
) {

    companion object {
        private const val TAG = "SolitariesLoop"
    }

    private var animationFuture: Future<Void>? = null

    private var animationNames = arrayOf(
        "hello_a003.qianim",
        "hello_a006.qianim",
        "hello_a010.qianim",
        "salute_right_b001.qianim"
    )


    private var animationNamesQueue = mutableListOf<String>()
    private var lastAnimationName = ""


    private fun buildAndRunAnimate(): Future<Void> {
        d("SolitariesLoop", loopCounter.toString())
        loopCounter -= 1
        return FutureUtils.wait(delayInSeconds.toLong(), TimeUnit.SECONDS)
            .andThenCompose {
                lastAnimationName = chooseAnimationName()
                AnimationBuilder.with(qiContext)
                    .withAssets(lastAnimationName)
                    .buildAsync()
            }
            .andThenCompose { animation ->
                AnimateBuilder.with(qiContext)
                    .withAnimation(animation)
                    .buildAsync()
            }
            .andThenCompose { animate ->
                d(TAG, "blabla")
                animate.async().run()
            }
            .thenCompose {
                if (!it.isCancelled && loopCounter > 0) {
                    buildAndRunAnimate()
                } else {
                    FutureUtils.wait(0, TimeUnit.NANOSECONDS)
                }
            }
    }

    private fun chooseAnimationName(): String {
        if (animationNamesQueue.isEmpty()) {

            animationNamesQueue = when (animationType) {
                0 -> {
                    animationNames.toMutableList()
                }
                else -> {
                    return ""
                }
            }
            animationNamesQueue.shuffle()

            if (animationNamesQueue[0] == lastAnimationName) {
                // Swap with the last item
                val lastIndex = animationNamesQueue.size - 1
                animationNamesQueue[0] = animationNamesQueue[lastIndex]
                animationNamesQueue[lastIndex] = lastAnimationName
            }
        }
        return animationNamesQueue.removeAt(0)
    }

    fun start() {
        Log.i(TAG, "SolitariesLoop starting")
        Log.i(TAG, loopCounter.toString())
        Log.i(TAG, qiContext.toString())
        if (loopCounter > 0)
            animationFuture = buildAndRunAnimate()
    }

    fun stop(): Future<Void> {
        Log.i(TAG, "SolitariesLoop stopping")
        if (animationFuture == null || animationFuture!!.isDone) {
            Log.e(TAG, "Error: trying to stop a SolitariesLoop that hasn't been started")
        } else {
            animationFuture!!.andThenConsume(Consumer {
                animationFuture!!.requestCancellation()
            })
        }
        return animationFuture!!
    }
}