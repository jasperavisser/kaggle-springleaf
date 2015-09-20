package tom.kaggle.springleaf

import com.redis.RedisClient
import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import scaldi.{Injectable, Injector}
import tom.kaggle.springleaf.analysis._

class ApplicationContext(implicit injector: Injector) extends Injectable {

  val sc = inject[SparkContext]
  val sqlContext = inject[SQLContext]
  val redis = inject[RedisClient]
  val statistics = inject[DataStatistics]
  val fraction = inject[Double]("fraction")

  val config = ConfigFactory.load()
  val dataFolderPath = config.getString("data.folder")
  val trainFeatureVectorPath = dataFolderPath + "/train-feature-vector" + fraction
  val cachedInferredTypesPath = dataFolderPath + "/predicted-types"

  val dataImporter = new DataImporter(dataFolderPath, fraction, sc, sqlContext)

  val df = {
    val result = dataImporter.readSample
    result.registerTempTable(ApplicationContext.tableName)
    result
  }

  val typeInference = new ColumnTypeInference
}

object ApplicationContext {

  val tableName = "xxx"
  val labelFieldName = "target"
}
