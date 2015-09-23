package tom.kaggle.springleaf

import java.io.File

import com.redis.RedisClient
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import tom.kaggle.springleaf.analysis._

class ApplicationContext(configFilePath: String) {
  val conf = new SparkConf()
    .setAppName("Kaggle Springleaf")
    .setMaster("local[*]")
    .set("spark.executor.memory", "8g")
    .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
  val sc = new SparkContext(conf)
  val ONE_GB = 1024 * 1024 * 1024
  sc.hadoopConfiguration.setInt("parquet.block.size", ONE_GB)

  val sqlContext = new SQLContext(sc)

  val config = ConfigFactory.parseFile(new File(configFilePath))
  val dataFolderPath = config.getString("data.folder")
  val fraction = config.getDouble("fraction")
  val trainFeatureVectorPath = dataFolderPath + "/train-feature-vector" + fraction
  val cachedInferrededTypesPath = dataFolderPath + "/predicted-types"

  val dataImporter = new DataImporter(dataFolderPath, fraction, sc, sqlContext)

  val redisHost = config.getString("redis.host")
  val redisPort = config.getInt("redis.port")
  lazy val redis = new RedisClient(redisHost, redisPort)

  lazy val df = {
    val result = dataImporter.readSample
    result.registerTempTable(ApplicationContext.tableName)
    result
  }

  val typeInference = new ColumnTypeInference
  val statistics = new DataStatistics(sqlContext, ApplicationContext.tableName)
  val cachedAnalysis: ICachedAnalysis = RedisCacheAnalysis(this, statistics)
}

object ApplicationContext {

  val tableName = "xxx"
  val labelFieldName = "target"
  
  val prefixOfIndexedString = "ISTR"
  val prefixOfString = "STR"
  val prefixOfDecimal = "DEC"
  val prefixOfBool = "BOOL"
}
