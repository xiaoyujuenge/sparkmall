package com.atguigu.sparkmall.mock.util

import java.util.{Date, Random}

/**
  * 随机生成某个时间去内的日期
  */
object RandomDate {
    
    def apply(startDate: Date, endDate: Date, step: Int): RandomDate = {
        val randomDate = new RandomDate
        val avgStepTime = (endDate.getTime - startDate.getTime) / step
        randomDate.maxTimeStep = avgStepTime * 2
        randomDate.lastDataTime = startDate.getTime
        randomDate
    }
    
  
    
    class RandomDate {
        var lastDataTime = 0L
        var maxTimeStep = 0L
        
        def getRandomDate() = {
            val timeStep = new Random().nextInt(maxTimeStep.toInt)
            lastDataTime = lastDataTime + timeStep
            
            new Date(lastDataTime)
        }
        
    }
    
}
