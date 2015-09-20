package tom.kaggle.springleaf

import com.redis.RedisClient
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import scaldi.Module
import tom.kaggle.springleaf.analysis.{DataStatistics, RedisCacheAnalysis}
import tom.kaggle.springleaf.app.AnalyzeDataApp

class SpringLeafModule extends Module {

  import SpringLeafModule._

  binding identifiedBy "tableName" to "xxx"

  binding to {
    val conf = new SparkConf()
      .setAppName("Kaggle SpringLeaf")
      .setMaster("local[*]")
      .set("spark.executor.memory", "8g")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    val context = new SparkContext(conf)
    context.hadoopConfiguration.setInt("parquet.block.size", OneGigabyte)
    context
  }
  binding to new SQLContext(inject[SparkContext])

  binding to new RedisClient(
    host = inject[String]("redis.host"),
    port = inject[Int]("redis.port")
  )
  binding to injected[KeyHelper]('fraction -> inject[Double]("fraction"))
  binding to injected[RedisCacheAnalysis]
  binding to injected[DataStatistics]('table -> inject[String]("tableName"))

  binding to injected[AnalyzeDataApp]

  // TODO: deprecate me
  binding to new ApplicationContext
}

object SpringLeafModule {
  val OneGigabyte = 1024 * 1024 * 1024
}
