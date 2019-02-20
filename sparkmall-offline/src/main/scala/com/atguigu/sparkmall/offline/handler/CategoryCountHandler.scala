package com.atguigu.sparkmall.offline.handler

import com.atguigu.sparkmall.common.model.UserVisitAction
import com.atguigu.sparkmall.common.util.JdbcUtil
import com.atguigu.sparkmall.offline.acc.CategoryAccumulator
import com.atguigu.sparkmall.offline.bean.CategoryCount
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.collection.mutable

object CategoryCountHandler {
    def handle(sparkSession: SparkSession,userVisitActionRDD:RDD[UserVisitAction],taskId:String): Unit ={
    
        //    2)定义累加器,注册累加器
    
        val accumulator = new CategoryAccumulator
        sparkSession.sparkContext.register(accumulator)
    
        //  3)利用累计器进行累加操作
    
        userVisitActionRDD.foreach { userVisitAction =>
            if (userVisitAction.click_category_id != -1L) {
                val key = userVisitAction.click_category_id + "_click"
                accumulator.add(key)
            } else if (userVisitAction.order_category_ids != null && userVisitAction.order_category_ids.length > 0) {
                val orderCids: Array[String] = userVisitAction.order_category_ids.split(",")
                for (cid <- orderCids) {
                    val key: String = cid + "_order"
                    accumulator.add(key)
                }
            } else if (userVisitAction.pay_category_ids != null && userVisitAction.pay_category_ids.length > 0) {
                val payCids: Array[String] = userVisitAction.pay_category_ids.split(",")
                for (cid <- payCids) {
                    val key: String = cid + "_pay"
                    accumulator.add(key)
                }
            }
        }
    
    
        val categoryMap: mutable.HashMap[String, Long] = accumulator.value
        println(s"categoryMap=${categoryMap.mkString("\n")}")
        //     4)累加器.value
    
    
        val categoryGroupCidMap: Map[String, mutable.HashMap[String, Long]] = categoryMap.groupBy({ case (key, count) => key.split("_")(0) })
        println(s"categoryGroupCidMap=${categoryGroupCidMap.mkString("\n")}")
        val categoryCountList: List[CategoryCount] = categoryGroupCidMap.map { case (cid, actionMap) =>
            CategoryCount("", cid, actionMap.getOrElse(cid + "_click", 0L), actionMap.getOrElse(cid + "_order", 0L), actionMap.getOrElse(cid + "_pay", 0L))
        }.toList
    
        // 5)把结果进行排序/截取
        val sortedCategoryCountList :List[CategoryCount]=categoryCountList.sortWith { (categoryCount1, categoryCount2) =>
            //以点击位置 ,点击量相同比较下单,下单相同,比较支付
            //前小后大,升序
            if (categoryCount1.clickCount > categoryCount2.clickCount) {
                true
            } else {
                false
            }
        }.take(10)
        //        6)top10保存到mysql中
    
    
        println(s"sortedCategoryCount=${sortedCategoryCountList.mkString("\n")}")
        val resultList: List[Array[Any]] = sortedCategoryCountList.map {
            CategoryCount => Array(taskId, CategoryCount.categoryId, CategoryCount.clickCount, CategoryCount.orderCount, CategoryCount.payCount)
        
        }
        resultList
    
        JdbcUtil.executeBatchUpdate("insert into category_top10 values(?,?,?,?,?)",resultList)
    
    }
    
}
