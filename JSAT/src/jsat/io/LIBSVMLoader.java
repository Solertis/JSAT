package jsat.io;

import java.io.*;
import java.util.*;
import jsat.classifiers.CategoricalData;
import jsat.classifiers.ClassificationDataSet;
import jsat.linear.SparseVector;
import static java.lang.Integer.*;
import static java.lang.Double.*;
import jsat.datatransform.DenseSparceTransform;
import jsat.regression.RegressionDataSet;
import jsat.utils.DoubleList;

/**
 * Loads a LIBSVM data file into a {@link DataSet}. LIVSM files do not indicate 
 * whether or not the target variable is supposed to be numerical or 
 * categorical, so two different loading methods are provided. 
 * <br><br>
 * LIBSVM files do not explicitly specify the length of data vectors. This can 
 * be problematic if loading a testing and training data set, if the data sets 
 * do not include the same highest index as a non-zero value, the data sets will
 * have incompatible vector lengths. To resolve this issue, use the loading 
 * methods that include the optional {@code vectorLength} parameter to specify 
 * the length before hand. 
 * 
 * @author Edward Raff
 */
public class LIBSVMLoader
{

    private LIBSVMLoader()
    {
    }
    
    /*
     * LIBSVM format is sparse
     * <VAL> <1 based Index>:<Value>
     * 
     */
    
    /**
     * Loads a new regression data set from a LIBSVM file, assuming the label is
     * a numeric target value to predict
     * 
     * @param file the file to load
     * @return a regression data set
     * @throws FileNotFoundException if the file was not found
     * @throws IOException if an error occurred reading the input stream
     */
    public static RegressionDataSet loadR(File file) throws FileNotFoundException, IOException
    {
        return loadR(file, 0.5);
    }
    
    /**
     * Loads a new regression data set from a LIBSVM file, assuming the label is
     * a numeric target value to predict
     * 
     * @param file the file to load
     * @param sparseRatio the fraction of non zero values to qualify a data 
     * point as sparse
     * @return a regression data set
     * @throws FileNotFoundException if the file was not found
     * @throws IOException if an error occurred reading the input stream
     */
    public static RegressionDataSet loadR(File file, double sparseRatio) throws FileNotFoundException, IOException
    {
        return loadR(file, sparseRatio, -1);
    }
    
    /**
     * Loads a new regression data set from a LIBSVM file, assuming the label is
     * a numeric target value to predict
     * 
     * @param file the file to load
     * @param sparseRatio the fraction of non zero values to qualify a data 
     * point as sparse
     * @param vectorLength the pre-determined length of each vector. If given a 
     * negative value, the largest non-zero index observed in the data will be 
     * used as the length. 
     * @return a regression data set
     * @throws FileNotFoundException if the file was not found
     * @throws IOException if an error occurred reading the input stream
     */
    public static RegressionDataSet loadR(File file, double sparseRatio, int vectorLength) throws FileNotFoundException, IOException
    {
        return loadR(new FileReader(file), sparseRatio, vectorLength);
    }
    
    /**
     * Loads a new regression data set from a LIBSVM file, assuming the label is
     * a numeric target value to predict
     * 
     * @param isr the input stream for the file to load
     * @param sparseRatio the fraction of non zero values to qualify a data 
     * point as sparse
     * @return a regression data set
     * @throws IOException if an error occurred reading the input stream
     */
    public static RegressionDataSet loadR(InputStreamReader isr, double sparseRatio) throws IOException
    {
        return loadR(isr, sparseRatio, -1);
    }
    
    /**
     * Loads a new regression data set from a LIBSVM file, assuming the label is
     * a numeric target value to predict.
     * 
     * @param isr the input stream for the file to load
     * @param sparseRatio the fraction of non zero values to qualify a data 
     * point as sparse
     * @param vectorLength the pre-determined length of each vector. If given a 
     * negative value, the largest non-zero index observed in the data will be 
     * used as the length. 
     * @returna a regression data set
     * @throws IOException 
     */
    public static RegressionDataSet loadR(InputStreamReader isr, double sparseRatio, int vectorLength) throws IOException
    {
        BufferedReader br = new BufferedReader(isr);
        List<SparseVector> sparseVecs = new ArrayList<SparseVector>();
        List<Double> targets = new DoubleList();
        int maxLen=1;
        
        
        String line;
        
        while((line = br.readLine()) != null)
        {
            String[] split = line.split("\\s+");
            targets.add(parseDouble(split[0]));
            
            maxLen = loadSparseVec(split, maxLen, sparseVecs);
        }
        
        if(vectorLength > 0)
            maxLen = vectorLength;
        
        RegressionDataSet rds = new RegressionDataSet(maxLen, new CategoricalData[0]);
        for(int i = 0; i < sparseVecs.size(); i++)
        {
            SparseVector sv = sparseVecs.get(i);
            sv.setLength(maxLen);
            rds.addDataPoint(sv, new int[0], targets.get(i));
        }
        
        rds.applyTransform(new DenseSparceTransform(sparseRatio));
        
        return rds;
    }
    
    /**
     * Loads a new classification data set from a LIBSVM file, assuming the 
     * label is a nominal target value
     * 
     * @param file the file to load
     * @return a classification data set
     * @throws FileNotFoundException if the file was not found
     * @throws IOException if an error occurred reading the input stream
     */
    public static ClassificationDataSet loadC(File file) throws FileNotFoundException, IOException
    {
        return loadC(new FileReader(file), 0.5);
    }
    
    /**
     * Loads a new classification data set from a LIBSVM file, assuming the 
     * label is a nominal target value
     * 
     * @param file the file to load
     * @param sparseRatio the fraction of non zero values to qualify a data 
     * point as sparse
     * @return a classification data set
     * @throws FileNotFoundException if the file was not found
     * @throws IOException if an error occurred reading the input stream
     */
    public static ClassificationDataSet loadC(File file, double sparseRatio) throws FileNotFoundException, IOException
    {
        return loadC(file, sparseRatio, -1);
    }
    
    /**
     * Loads a new classification data set from a LIBSVM file, assuming the 
     * label is a nominal target value
     * 
     * @param file the file to load
     * @param sparseRatio the fraction of non zero values to qualify a data 
     * point as sparse
     * @param vectorLength the pre-determined length of each vector. If given a 
     * negative value, the largest non-zero index observed in the data will be 
     * used as the length. 
     * @return a classification data set
     * @throws FileNotFoundException if the file was not found
     * @throws IOException if an error occurred reading the input stream
     */
    public static ClassificationDataSet loadC(File file, double sparseRatio, int vectorLength) throws FileNotFoundException, IOException
    {
        return loadC(new FileReader(file), sparseRatio, vectorLength);
    }
    
    /**
     * Loads a new classification data set from a LIBSVM file, assuming the 
     * label is a nominal target value 
     * 
     * @param isr the input stream for the file to load
     * @param sparseRatio the fraction of non zero values to qualify a data 
     * point as sparse
     * @return a classification data set
     * @throws IOException if an error occurred reading the input stream
     */
    public static ClassificationDataSet loadC(InputStreamReader isr, double sparseRatio) throws IOException
    {
        return loadC(isr, sparseRatio, -1);
    }
    
    /**
     * Loads a new classification data set from a LIBSVM file, assuming the 
     * label is a nominal target value 
     * 
     * @param isr the input stream for the file to load
     * @param sparseRatio the fraction of non zero values to qualify a data 
     * point as sparse
     * @param vectorLength the pre-determined length of each vector. If given a 
     * negative value, the largest non-zero index observed in the data will be 
     * used as the length. 
     * @return a classification data set
     * @throws IOException if an error occurred reading the input stream
     */
    public static ClassificationDataSet loadC(InputStreamReader isr, double sparseRatio, int vectorLength) throws IOException
    {
        
        BufferedReader br = new BufferedReader(isr);
        List<SparseVector> sparceVecs = new ArrayList<SparseVector>();
        List<Double> cats = new ArrayList<Double>();
        Map<Double, Integer> possibleCats = new HashMap<Double, Integer>();
        int maxLen=1;
        
        
        String line;
        
        while((line = br.readLine()) != null)
        {
            String[] split = line.split("\\s+");
            double cat = Double.parseDouble(split[0]);
            if(!possibleCats.containsKey(cat))
                possibleCats.put(cat, possibleCats.size());
            cats.add(cat);
            
            maxLen = loadSparseVec(split, maxLen, sparceVecs);
        }
        
        CategoricalData predicting = new CategoricalData(possibleCats.size());
        
        ClassificationDataSet cds = new ClassificationDataSet(maxLen, new CategoricalData[0], predicting);
        for(int i = 0; i < cats.size(); i++)
        {
            SparseVector vec = sparceVecs.get(i);
            vec.setLength(maxLen);
            cds.addDataPoint(vec, new int[0], possibleCats.get(cats.get(i)));
        }
        
        cds.applyTransform(new DenseSparceTransform(sparseRatio));
        
        return cds;
    }

    /**
     * Loads a sparse vector from a split and stores it in the sparse vector 
     * array
     * @param split the array of splits that contains the vector, label should 
     * be left in the array as index 0
     * @param maxLen the current max length seen for a vector
     * @param sparceVecs the list to place the new vector into
     * @return the new max length value
     */
    private static int loadSparseVec(String[] split, int maxLen, List<SparseVector> sparceVecs) 
    {
        SparseVector sv = new SparseVector(1);
        for(int i = 1; i < split.length; i++)
        {
            String[] indxVal = split[i].split(":");
            int index = parseInt(indxVal[0])-1;
            double val = parseDouble(indxVal[1]);
            maxLen = Math.max(maxLen, index+1);
            sv.setLength(maxLen);
            sv.set(index, val);
        }
        sparceVecs.add(sv);
        return maxLen;
    }
}
