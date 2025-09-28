package com.example.telemetrylab

object Compute {
    private const val W = 256
    private const val H = 256
    private val KERNEL = floatArrayOf(
        0.0625f, 0.125f, 0.0625f,
        0.125f, 0.25f, 0.125f,
        0.0625f, 0.125f, 0.0625f
    )
    fun runComputePass(load: Int) {
        val inBuf = FloatArray(W * H) { (it % 255) / 255f }
        val outBuf = FloatArray(W * H)
        repeat(load.coerceAtLeast(1)) {
            convolve3x3(inBuf, outBuf, W, H)
            System.arraycopy(outBuf, 0, inBuf, 0, outBuf.size)
        }
    }
    private fun convolve3x3(inBuf: FloatArray, outBuf: FloatArray, w: Int,
                            h: Int) {
        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                var acc = 0f
                var ki = 0
                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val px = x + kx
                        val py = y + ky
                        acc += inBuf[py * w + px] * KERNEL[ki++]
                    }
                }
                outBuf[y * w + x] = acc
            }
        }
    }
}