package tom.kaggle.springleaf

import org.apache.spark.sql.DataFrame
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.ml.feature.StringIndexerModel
import scala.annotation.tailrec

case class IndexedCategoricalVariableCreator(df: DataFrame) {
  lazy val categoricalVariables = SchemaInspector(df).getProcessedCategoricalVariables(df.schema)
  lazy val models: Seq[StringIndexerModel] =
    for (v <- categoricalVariables) yield {
      val indexer = new StringIndexer().setInputCol(v.name).setOutputCol(s"${Names.PrefixOfIndexedString}_${v.name}")
      indexer.fit(df)
    }

  lazy val transformedDf: DataFrame = transform(df, models)

  @tailrec
  private def transform(df: DataFrame, models: Seq[StringIndexerModel]): DataFrame = {
    models match {
      case Nil           => df
      case model :: rest => transform(model.transform(df), rest)
    }
  }
}
