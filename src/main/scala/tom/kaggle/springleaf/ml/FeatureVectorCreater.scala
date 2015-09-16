package tom.kaggle.springleaf.ml

import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.DecimalType
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.sql.types.LongType
import org.apache.spark.sql.types.StructField

import tom.kaggle.springleaf.ApplicationContext
import tom.kaggle.springleaf.SchemaInspector

case class FeatureVectorCreater(df: DataFrame) {
  private val schemaInspector = SchemaInspector(df)
  private val labelIndex = df.schema.fieldIndex(ApplicationContext.labelFieldName)

  def getFeatureVector: RDD[LabeledPoint] = {
    df.map { row => LabeledPoint(getLabel(row), getNumericalValues(row)) }
  }

  private def getLabel(row: Row): Double = row.getInt(labelIndex).toDouble

  private def getNumericalValues(row: Row): Vector = {
    val numericalVariables = schemaInspector.getProcessedNumericalVariables(row.schema)
    val sparseValues = for {
      (column, index) <- numericalVariables.zipWithIndex
      optionalValue <- extractNumericalValue(row, column)
      value <- Some(optionalValue)
    } yield (index, value)
    Vectors.sparse(row.size, sparseValues)
  }

  private def extractNumericalValue(row: Row, column: StructField): Option[Double] = {
    val index = row.fieldIndex(column.name)
    if (row.isNullAt(index)) None
    column.dataType match {
      case DecimalType() =>
        val value = row.getDecimal(index)
        if (value == null) None
        else Some(value.doubleValue())
      case IntegerType   => Some(row.getAs[Integer](index).toDouble)
      case LongType      => Some(row.getLong(index).toDouble)
      case DoubleType    => Some(row.getDouble(index))
      case default => {
        println("for %s type not recognized: %s".format(column.name, column.dataType))
        None
      }
    }
  }

}