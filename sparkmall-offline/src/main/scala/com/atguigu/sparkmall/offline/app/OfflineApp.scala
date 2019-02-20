package com.atguigu.sparkmall.offline.app

import java.util.{Properties, UUID}

import com.alibaba.fastjson.{JSON, JSONObject}
import com.atguigu.sparkmall.common.model.UserVisitAction
import com.atguigu.sparkmall.common.util.PropertiesUtil
import com.atguigu.sparkmall.offline.handler.CategoryCountHandler
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession


object OfflineApp {
    def main(args: Array[String]): Unit = {
        
        val taskId: String = UUID.randomUUID().toString
    
        val sparkConf: SparkConf = new SparkConf().setAppName("sparkmall-offline").setMaster("local[*]")
        val sparkSession: SparkSession = SparkSession.builder().config(sparkConf).enableHiveSupport().getOrCreate()
    
        //1)根据条件,从hive中查询数据
    
        val userVisitActionRDD: RDD[UserVisitAction] = readUserVisitActionToRDD(sparkSession)
       /* userVisitAction.foreach { userVisitAction =>
            println(userVisitAction)*/
        
        //需求1
        CategoryCountHandler.handle(sparkSession,userVisitActionRDD,taskId)
        println("需求1完成")
        
          
       
      
    }
    
        def readUserVisitActionToRDD(sparkSession: SparkSession): RDD[UserVisitAction] = {
            //json工具 fastjson gson jackson
            val properties: Properties = PropertiesUtil.load("conditions.properties")
            val conditionJson: String = properties.getProperty("condition.params.json")
            val conditionJsonObj: JSONObject = JSON.parseObject(conditionJson)
            val startDate: String = conditionJsonObj.getString("startDate")
            val endDate: String = conditionJsonObj.getString("endDate")
            val startAge: String = conditionJsonObj.getString("startAge")
            val endAge: String = conditionJsonObj.getString("endAge")
        
            //
            val sql = new StringBuilder("select v.*  from user_visit_action v,user_info u where v.user_id=u.user_id")
            if (startDate.nonEmpty) {
                sql.append(" and date>='" + startDate + "'")
            }
            if (endDate.nonEmpty) {
                sql.append(" and date<='" + endDate + "'")
            }
            if (startAge.nonEmpty) {
                sql.append(" and age>=" + startAge)
            }
            if (endAge.nonEmpty) {
                sql.append(" and age<= " + endAge)
            }
            
            println(sql)
        
            sparkSession.sql("use sparkmall")
          import sparkSession.implicits._
            val rdd: RDD[UserVisitAction] = sparkSession.sql(sql.toString()).as[UserVisitAction].rdd
            rdd
        }
    
    
}
