package de.uni_trier.wi2.base;

import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.model.base.AggregateClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityMeasure;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.impl.SMObjectEqualImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityMeasureImpl;
import org.junit.Before;

public abstract class SimilarityModelTestBase extends TestBase{


    @Override
    public void initialize() {
        super.initialize();

        // data classes
        DataClass data = model.getDataSystemClass();

        DataClass sc0 = model.getStringSystemClass();
        DataClass sc1 = sc0.createSubclass("SC1");
        DataClass sc2 = sc0.createSubclass("SC2");

        DataClass ac0 = model.getAggregateSystemClass();
        DataClass ac1 = ac0.createSubclass("AC1");
        DataClass ac2 = ac0.createSubclass("AC2");

        ((AggregateClass) ac1).addAttribute("x", model.getStringSystemClass());
        ((AggregateClass) ac1).addAttribute("y", model.getStringSystemClass());

        ((AggregateClass) ac2).addAttribute("x", model.getStringSystemClass());
        ((AggregateClass) ac2).addAttribute("y", model.getStringSystemClass());

        sc1.setAbstract(false);
        sc2.setAbstract(false);
        ac1.setAbstract(false);
        ac2.setAbstract(false);

        sc1.finishEditing();
        sc2.finishEditing();
        ac1.finishEditing();
        ac2.finishEditing();

        // similarity measures
        SimilarityMeasureImpl smObjectEqual = new SMObjectEqualImpl();

        SimilarityMeasureImpl sms0 = createConstantSimilarityMeasure("SMS0", 0.1, sc0);
        SimilarityMeasureImpl sms1 = createConstantSimilarityMeasure("SMS1", 0.2, sc1);
        SimilarityMeasureImpl sms2 = createConstantSimilarityMeasure("SMS2", 0.3, sc2);

        SimilarityMeasureImpl sma0 = createConstantSimilarityMeasure("SMA0", 0.6, ac0);
        SimilarityMeasureImpl sma1 = createConstantSimilarityMeasure("SMA1", 0.4, ac1);
        SimilarityMeasureImpl sma2 = createConstantSimilarityMeasure("SMA2", 0.5, ac2);


        // similarity model
        addSimilarityMeasureToSimilarityModel(smObjectEqual, data);

        addSimilarityMeasureToSimilarityModel(sms0, sc0);
        addSimilarityMeasureToSimilarityModel(sms1, sc1);
        addSimilarityMeasureToSimilarityModel(sms2, sc2);

        addSimilarityMeasureToSimilarityModel(sma0, ac0);
        addSimilarityMeasureToSimilarityModel(sma1, ac1);
        addSimilarityMeasureToSimilarityModel(sma2, ac2);




    }

    private SimilarityMeasureImpl createConstantSimilarityMeasure(String name, double value, DataClass dataClass){
        return new SimilarityMeasureImpl() {
            @Override
            public Similarity compute(DataObject dataObject, DataObject dataObject1, SimilarityValuator similarityValuator) {
                return new SimilarityImpl(this, dataObject, dataObject1, value);
            }

            @Override
            public DataClass getDataClass() {
                return dataClass;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public boolean isSimilarityFor(DataClass dataClass, String s) {
                return dataClass == getDataClass();
            }

            @Override
            public String getSystemName() {
                return name;
            }

            @Override
            public boolean isForceOverride() {
                return false;
            }

            @Override
            public void setForceOverride(boolean b) {

            }

            @Override
            public boolean isReusable() {
                return true;
            }
        };
    }

}
