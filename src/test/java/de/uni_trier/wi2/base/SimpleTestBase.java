package de.uni_trier.wi2.base;

import de.uni_trier.wi2.extension.similarity.measure.collection.*;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.*;
import de.uni_trier.wi2.procake.similarity.base.impl.SMObjectEqualImpl;
import de.uni_trier.wi2.procake.similarity.base.numeric.impl.SMNumericLinearImpl;
import de.uni_trier.wi2.procake.similarity.base.string.impl.SMStringEqualImpl;
import de.uni_trier.wi2.procake.similarity.base.string.impl.SMStringLevenshteinImpl;


public abstract class SimpleTestBase extends TestBase {



    @Override
    public void initialize() {
        super.initialize();
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

        addSimilarityMeasureToSimilarityModel(new SMCollectionIsolatedMappingImpl(),    model.getSetSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMCollectionMappingImpl(),            model.getSetSystemClass());

        addSimilarityMeasureToSimilarityModel(new SMCollectionIsolatedMappingImpl(),    model.getListSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMCollectionMappingImpl(),            model.getListSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMListMappingImpl(),                  model.getListSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMListSWAImpl(),                      model.getListSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMListDTWImpl(),                      model.getListSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMListCorrectnessImpl(),              model.getListSystemClass());

        addSimilarityMeasureToSimilarityModel(new SMCollectionIsolatedMappingImplExt(), model.getSetSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMCollectionMappingImplExt(),         model.getSetSystemClass());

        addSimilarityMeasureToSimilarityModel(new SMCollectionIsolatedMappingImplExt(), model.getListSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMCollectionMappingImplExt(),         model.getListSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMListMappingImplExt(),               model.getListSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMListSWAImplExt(),                   model.getListSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMListDTWImplExt(),                   model.getListSystemClass());
        addSimilarityMeasureToSimilarityModel(new SMListCorrectnessImplExt(),           model.getListSystemClass());


        addSimilarityMeasureToSimilarityModel(new SMCollectionIsolatedMappingImplExt(), model.getNESTSequentialWorkflowClass());
        addSimilarityMeasureToSimilarityModel(new SMCollectionMappingImplExt(),         model.getNESTSequentialWorkflowClass());
        addSimilarityMeasureToSimilarityModel(new SMListMappingImplExt(),               model.getNESTSequentialWorkflowClass());
        addSimilarityMeasureToSimilarityModel(new SMListSWAImplExt(),                   model.getNESTSequentialWorkflowClass());
        addSimilarityMeasureToSimilarityModel(new SMListDTWImplExt(),                   model.getNESTSequentialWorkflowClass());
        addSimilarityMeasureToSimilarityModel(new SMListCorrectnessImplExt(),           model.getNESTSequentialWorkflowClass());
    }
}
