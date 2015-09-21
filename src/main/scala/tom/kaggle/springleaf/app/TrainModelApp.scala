package tom.kaggle.springleaf.app

import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import tom.kaggle.springleaf.ApplicationContext
import tom.kaggle.springleaf.ml.GbtReducedFeaturesEvaluator

case class TrainModelApp(ac: ApplicationContext) {

  def run() {
    val trainFeatureVectors = ac.sc.objectFile[LabeledPoint](ac.trainFeatureVectorPath, 16)
    val splits = trainFeatureVectors.randomSplit(Array(0.6, 0.2, 0.2))
    val (trainingSet, testSet, validationSet) = (splits(0), splits(1), splits(2))

    val components = 100
    val numIterations = 100
    println(s"\nTraining model with $components components and $numIterations iterations")
//    val svmTrainer = SvmTrainer(trainingSet, components, numIterations)
//    svmTrainer.reducedFeatures.take(10).foreach(println)
    val svmTrainer = GbtReducedFeaturesEvaluator(trainingSet, components, numIterations)

    println(s"\nCompute raw scores on the test set.")
    val scoreAndLabels = svmTrainer.reduce(testSet).map { point =>
      val score = svmTrainer.model.predict(point.features)
      (score, point.label)
    }

    // Get evaluation metrics.
    val metrics = new BinaryClassificationMetrics(scoreAndLabels)
    println(s"\nArea under ROC = ${metrics.areaUnderROC()}")
    println(s"Evaluation: ${interpretAreaUnderROC(metrics.areaUnderROC())}")
  }

  private def interpretAreaUnderROC(areaUnderROC: Double): String = {
    if (areaUnderROC < 0.6) "FAIL"
    else if (areaUnderROC < 0.7) "poor"
    else if (areaUnderROC < 0.8) "fair"
    else if (areaUnderROC < 0.9) "decent"
    else "excellent"
  }
}

object TrainModelApp {
  def main(args: Array[String]) {
//    val ac = new ApplicationContext()
//    val app = TrainModelApp(ac)
//    app.run()
  }

}
