package test;

import de.uni_trier.wi2.procake.CakeInstance;
import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.model.base.ListClass;
import de.uni_trier.wi2.procake.data.object.DataObjectUtils;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.similarity.SimilarityModelFactory;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.string.SMStringEqual;
import de.uni_trier.wi2.procake.similarity.base.string.SMStringLevenshtein;
import de.uni_trier.wi2.procake.utils.io.ResourcePaths;
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

        simVal = SimilarityModelFactory.newSimilarityValuator();

        SMStringLevenshtein smStringLevenshtein = (SMStringLevenshtein) simVal.getSimilarityModel().createSimilarityMeasure(SMStringLevenshtein.NAME, ModelFactory.getDefaultModel().getStringSystemClass());
        smStringLevenshtein.setCaseInsensitive();
        smStringLevenshtein.setThreshold(100);
        simVal.getSimilarityModel().addSimilarityMeasure(smStringLevenshtein, "SMStringLevenshtein");

        SMStringEqual smStringEqual = (SMStringEqual) simVal.getSimilarityModel().createSimilarityMeasure(SMStringEqual.NAME, ModelFactory.getDefaultModel().getStringSystemClass());
        smStringEqual.setCaseInsensitive();
        simVal.getSimilarityModel().addSimilarityMeasure(smStringEqual, "SMStringEqual");
    }
}
