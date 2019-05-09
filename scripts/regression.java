import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.REPTree;
import weka.classifiers.bayes.NaiveBayes;

public class regression {

	public static void main(String[] args) throws Exception{
		
			String folderPath = "/Users/eric/Desktop/CS590U/project/";
			String filePath = folderPath + "dataset.csv";
			 
			Instances dataset = getDataset(filePath);
	
			//LinearRegression model = new LinearRegression();
			//MultilayerPerceptron model = new MultilayerPerceptron();
			//GaussianProcesses model = new GaussianProcesses();
			//IBk model = new IBk(8);
			REPTree model = new REPTree();
			
			
			// Evaluation eval = new Evaluation(dataset);
			 // model, data, #folds, random seed
			 //eval.crossValidateModel(model, dataset, 10, new Random(1));
			 //System.out.println(eval.rootMeanSquaredError());
			
			model.buildClassifier(dataset);
			
			
			double[] data = {70.4,25.44,19.0};
			DenseInstance instance = new DenseInstance(1.0, data); 
			double prediction = model.classifyInstance(instance);
			
			System.out.println(prediction); // actual 3.02865
			
			weka.core.SerializationHelper.write(folderPath+"model.pt", model);
			// to read in:
			//Classifier cls = (Classifier) weka.core.SerializationHelper.read(folderPath+"model.pt");
			//prediction = cls.classifyInstance(instance);
			//System.out.println(prediction); // actual 3.02865
			//prediction = model.classifyInstance(instance);
			
			 

			
		}
	
	private static Instances getDataset(String filePath) throws Exception {
		DataSource source;

		source = new DataSource(filePath);
		Instances dataset = source.getDataSet();
		if (dataset.classIndex() == -1)
			   dataset.setClassIndex(dataset.numAttributes() - 1);
		
		return dataset;
		
	}

}