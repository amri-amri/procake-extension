package test;

import de.uni_trier.wi2.procake.CakeInstance;
import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.model.base.CollectionClass;
import de.uni_trier.wi2.procake.data.model.base.ListClass;
import de.uni_trier.wi2.procake.data.object.DataObjectUtils;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.impl.ListObjectImpl;
import de.uni_trier.wi2.procake.similarity.SimilarityMeasure;
import de.uni_trier.wi2.procake.similarity.SimilarityModel;
import de.uni_trier.wi2.procake.similarity.SimilarityModelFactory;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.SMObjectEqual;
import de.uni_trier.wi2.procake.similarity.base.impl.SMObjectEqualImpl;
import de.uni_trier.wi2.procake.similarity.base.numeric.SMNumericLinear;
import de.uni_trier.wi2.procake.similarity.base.string.SMStringEqual;
import de.uni_trier.wi2.procake.similarity.base.string.SMStringLevenshtein;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityMeasureImpl;
import de.uni_trier.wi2.procake.similarity.nest.sequence.SMGraphDTW;
import de.uni_trier.wi2.procake.utils.io.ResourcePaths;
import extension.similarity.measure.*;
import extension.similarity.valuator.SimilarityValuatorImplExt;
import org.junit.*;


public class CollectionSimilarityTest {

    public final String[] days = new String[]{
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
            "Sunday"
    };

    public DataObjectUtils utils;
    public SimilarityValuator simVal;
    final String LIST_CLASS_NAME = "customListClass";

    public final double delta = 0.0000001;

    public ListObject weekdays(){
        ListObject weekdays = (ListObject) ModelFactory.getDefaultModel().createObject(LIST_CLASS_NAME);

        weekdays.addValue(utils.createStringObject(days[0]));
        weekdays.addValue(utils.createStringObject(days[1]));
        weekdays.addValue(utils.createStringObject(days[2]));
        weekdays.addValue(utils.createStringObject(days[3]));
        weekdays.addValue(utils.createStringObject(days[4]));
        weekdays.addValue(utils.createStringObject(days[5]));
        weekdays.addValue(utils.createStringObject(days[6]));

        return weekdays;
    }

    public ListObject workdays(){
        ListObject workdays = (ListObject) ModelFactory.getDefaultModel().createObject(LIST_CLASS_NAME);

        workdays.addValue(utils.createStringObject(days[0]));
        workdays.addValue(utils.createStringObject(days[1]));
        workdays.addValue(utils.createStringObject(days[2]));
        workdays.addValue(utils.createStringObject(days[3]));
        workdays.addValue(utils.createStringObject(days[4]));

        return workdays;
    }

    @Before
    public void initialize(){
        CakeInstance.start(ResourcePaths.PATH_COMPOSITION);

        ListClass listClass = (ListClass) ModelFactory.getDefaultModel().getListSystemClass();
        ListClass customListClass = (ListClass) listClass.createSubclass(LIST_CLASS_NAME);
        customListClass.setElementClass(ModelFactory.getDefaultModel().getStringSystemClass());
        customListClass.finishEditing();

        utils = new DataObjectUtils();

        simVal = new SimilarityValuatorImplExt(SimilarityModelFactory.getDefaultSimilarityModel()   );

        SimilarityModel model = simVal.getSimilarityModel();

        SMStringLevenshtein smStringLevenshtein = (SMStringLevenshtein) simVal.getSimilarityModel().createSimilarityMeasure(SMStringLevenshtein.NAME, ModelFactory.getDefaultModel().getDataSystemClass());
        smStringLevenshtein.setCaseInsensitive();
        smStringLevenshtein.setThreshold(100);
        model.addSimilarityMeasure(smStringLevenshtein, SMStringLevenshtein.NAME);

        SMStringEqual smStringEqual = (SMStringEqual) simVal.getSimilarityModel().createSimilarityMeasure(SMStringEqual.NAME, ModelFactory.getDefaultModel().getDataSystemClass());
        smStringEqual.setCaseInsensitive();
        model.addSimilarityMeasure(smStringEqual, SMStringEqual.NAME);

        SMObjectEqual smObjectEqual = (SMObjectEqual) simVal.getSimilarityModel().createSimilarityMeasure(SMObjectEqual.NAME, ModelFactory.getDefaultModel().getDataSystemClass());
        model.addSimilarityMeasure(smObjectEqual, SMObjectEqual.NAME);

        SMNumericLinear smNumericLinear  = (SMNumericLinear) model.createSimilarityMeasure(SMNumericLinear.NAME, ModelFactory.getDefaultModel().getIntegerSystemClass());
        smNumericLinear.setForceOverride(true);
        model.addSimilarityMeasure(smNumericLinear, SMNumericLinear.NAME);
        model.setDefaultSimilarityMeasure(ModelFactory.getDefaultModel().getIntegerSystemClass(), SMNumericLinear.NAME);

        SimilarityMeasure measure = model.getSimilarityMeasure(ModelFactory.getDefaultModel().getListSystemClass(), SMObjectEqual.NAME);

        //add(model, new SMObjectEqualImpl(), "SMObjectEqual");
        add(model, new SMCollectionIsolatedMappingImplExt(), SMCollectionIsolatedMappingExt.NAME);
        add(model, new SMCollectionMappingImplExt(), SMCollectionMappingExt.NAME);
        add(model, new SMListCorrectnessImplExt(), SMListCorrectnessExt.NAME);
        add(model, new SMListDTWImplExt(), SMListDTWExt.NAME);
        add(model, new SMListSWAImplExt(), SMListSWAExt.NAME);
        add(model, new SMListMappingImplExt(), SMListMappingExt.NAME);

    }

    public void add(SimilarityModel model, SimilarityMeasureImpl sm, String name){
        sm.setForceOverride(true);
        sm.setName(name);
        sm.setDataClass(ModelFactory.getDefaultModel().getDataSystemClass());
        model.addSimilarityMeasure(sm, name);
        model.setDefaultSimilarityMeasure(ModelFactory.getDefaultModel().getDataSystemClass(), name);
    }
}
