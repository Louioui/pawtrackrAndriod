package com.pawtrackr.app.core.search

import kotlin.math.sqrt

object VectorMath {
    fun cosineSimilarity(left: FloatArray, right: FloatArray): Float {
        if (left.isEmpty() || right.isEmpty() || left.size != right.size) return 0f

        var dotProduct = 0.0
        var leftMagnitude = 0.0
        var rightMagnitude = 0.0

        for (index in left.indices) {
            val leftValue = left[index].toDouble()
            val rightValue = right[index].toDouble()
            dotProduct += leftValue * rightValue
            leftMagnitude += leftValue * leftValue
            rightMagnitude += rightValue * rightValue
        }

        if (leftMagnitude == 0.0 || rightMagnitude == 0.0) return 0f

        val score = dotProduct / (sqrt(leftMagnitude) * sqrt(rightMagnitude))
        return score.toFloat().coerceIn(-1f, 1f)
    }
}
