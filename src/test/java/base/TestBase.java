package base;

import de.uni_trier.wi2.procake.CakeInstance;
import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.object.DataObjectUtils;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.similarity.SimilarityModel;
import de.uni_trier.wi2.procake.similarity.SimilarityModelFactory;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.*;
import de.uni_trier.wi2.procake.similarity.base.impl.SMObjectEqualImpl;
import de.uni_trier.wi2.procake.similarity.base.numeric.impl.SMNumericLinearImpl;
import de.uni_trier.wi2.procake.similarity.base.string.impl.SMStringEqualImpl;
import de.uni_trier.wi2.procake.similarity.base.string.impl.SMStringLevenshteinImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityMeasureImpl;
import de.uni_trier.wi2.procake.utils.exception.NameAlreadyExistsException;
import de.uni_trier.wi2.procake.utils.io.ResourcePaths;
import extension.similarity.measure.collection.*;
import extension.similarity.valuator.SimilarityValuatorImplExt;
import org.junit.Before;


public abstract class TestBase {



    public static DataObjectUtils utils;
    public static SimilarityModel similarityModel;
    public static SimilarityValuatorImplExt simVal;
    public static Model model;

    public final double delta = 0.0000001;

    @Before
    public void initialize(){

        CakeInstance.start(ResourcePaths.PATH_COMPOSITION);

        utils = new DataObjectUtils();
        similarityModel = SimilarityModelFactory.getDefaultSimilarityModel();
        simVal = new SimilarityValuatorImplExt(similarityModel);
        model = ModelFactory.getDefaultModel();

        /*
            similarity measures:

            Name                                        |   DataClass(es)
            --------------------------------------------|-------------------------------------------
            · ObjectEqual                               |   Data
                                                        |
            · StringEqual                               |   String
            · Levenshtein                               |   String
                                                        |
            · NumericLinear                             |   Integer
                                                        |
            · Isolated Mapping  (original & extended)   |   Collection, NESTSequentialWorkflow
            · Mapping  (original & extended)            |   Collection, NESTSequentialWorkflow
            · List Mapping  (original & extended)       |   List, NESTSequentialWorkflow
            · SWA  (original & extended)                |   List, NESTSequentialWorkflow
            · DTW  (original & extended)                |   List, NESTSequentialWorkflow
            · List Correctness  (original & extended)   |   List, NESTSequentialWorkflow
         */

        addSimilarityMeasureToSimilarityModel(new SMObjectEqualImpl(),                  model.getDataSystemClass());

        addSimilarityMeasureToSimilarityModel(new SMStringEqualImpl(),                  model.getStringSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMStringLevenshteinImpl(),            model.getStringSystemClass());

        addSimilarityMeasureToSimilarityModel(new SMNumericLinearImpl(),                model.getIntegerSystemClass());

        addSimilarityMeasureToSimilarityModel(new SMCollectionIsolatedMappingImpl(),    model.getDataSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMCollectionMappingImpl(),            model.getDataSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMListMappingImpl(),                  model.getDataSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMListSWAImpl(),                      model.getDataSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMListDTWImpl(),                      model.getDataSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMListCorrectnessImpl(),              model.getDataSystemClass());

        addSimilarityMeasureToSimilarityModel(new SMCollectionIsolatedMappingImplExt(), model.getDataSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMCollectionMappingImplExt(),         model.getDataSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMListMappingImplExt(),               model.getDataSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMListSWAImplExt(),                   model.getDataSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMListDTWImplExt(),                   model.getDataSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMListCorrectnessImplExt(),           model.getDataSystemClass());

    }

    protected void addSimilarityMeasureToSimilarityModel(SimilarityMeasureImpl sm, DataClass dataClass){

        // puts name and SimilarityMeasure-Object in cache (should be called only once per SM)
        try {
            similarityModel.registerSimilarityMeasureTemplate(sm);
        } catch (NameAlreadyExistsException e) {};

        // sets the DataClass the SM can be applied to
        sm.setDataClass(dataClass);

        //adds 'sm' to the SMs that can be applied to 'dataClass'
        similarityModel.addSimilarityMeasure(sm, sm.getSystemName());

    }

    public final String[] days = new String[]{
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
            "Sunday"
    };

    public ListObject weekdays(){
        ListObject weekdays = (ListObject) utils.createListObject();

        for (int i = 0; i<7; i++){
            weekdays.addValue(utils.createStringObject(days[i]));
        }

        return weekdays;
    }

    public ListObject workdays(){
        ListObject workdays = (ListObject) utils.createListObject();

        for (int i = 0; i<5; i++){
            workdays.addValue(utils.createStringObject(days[i]));
        }

        return workdays;
    }




}
