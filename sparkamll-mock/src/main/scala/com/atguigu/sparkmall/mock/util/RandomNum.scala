package com.atguigu.sparkmall.mock.util

import scala.collection.mutable.ListBuffer
import scala.util.Random

/**
  * 随机生成区间范围内的数字(左右包含)
  */

object RandomNum {
    
    def apply(fromNum:Int,toNum:Int):Int={
        
        fromNum+new Random().nextInt(toNum-fromNum+1)
    }
    def multi(fromNum:Int,toNum:Int,amount:Int,delimiter:String,canRepeat:Boolean) ={
        // 实现方法  在fromNum和 toNum之间的 多个数组拼接的字符串 共amount个
       // 用delimiter分割  canRepeat为false则不允许重复
        
        
        if(canRepeat) {
            val numList = new ListBuffer[Int]() //链表
            while (numList.size < amount) {
                numList += apply(fromNum, toNum)
            }
            //分割
            numList.mkString(delimiter)
            //canReport 是否会重复
        }else{
            val numSet = new ListBuffer[Int]() //链表
            while (numSet.size < amount) {
                numSet += apply(fromNum, toNum)
            }
            //分割
            numSet.mkString(delimiter)
            
        }
    }
    
    def main(args: Array[String]): Unit = {
    
    
        println(multi(1, 5, 3, " ", false))
    }
    
    
    
    
}
