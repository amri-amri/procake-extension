package de.uni_trier.wi2.retrieval;

import de.uni_trier.wi2.base.SimpleTestBase;
import de.uni_trier.wi2.conversion.sax.XEStoNESTsAXConverter;
import de.uni_trier.wi2.extension.retrieval.ParallelLinearRetrieverImplExt;
import de.uni_trier.wi2.extension.similarity.measure.collection.SMCollectionIsolatedMappingExt;
import de.uni_trier.wi2.extension.similarity.measure.collection.SMListMappingExt;
import de.uni_trier.wi2.extension.similarity.measure.collection.SMListMappingImplExt;
import de.uni_trier.wi2.parsing.XMLtoMethodInvokersFuncConverter;
import de.uni_trier.wi2.parsing.XMLtoSimilarityMeasureFuncConverter;
import de.uni_trier.wi2.parsing.XMLtoWeightFuncConverter;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.IntegerObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.SetObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.data.objectpool.ObjectPoolFactory;
import de.uni_trier.wi2.procake.data.objectpool.WriteableObjectPool;
import de.uni_trier.wi2.procake.data.objectpool.impl.WriteableObjectPoolImpl;
import de.uni_trier.wi2.procake.retrieval.Query;
import de.uni_trier.wi2.procake.retrieval.RetrievalResult;
import de.uni_trier.wi2.procake.retrieval.RetrievalResultList;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.base.SMObjectEqual;
import de.uni_trier.wi2.procake.similarity.base.numeric.SMNumericLinear;
import de.uni_trier.wi2.procake.similarity.base.string.SMStringLevenshtein;
import de.uni_trier.wi2.utils.MethodInvoker;
import de.uni_trier.wi2.utils.MethodInvokersFunc;
import de.uni_trier.wi2.utils.SimilarityMeasureFunc;
import de.uni_trier.wi2.utils.WeightFunc;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ParallelLinearRetrieverExtTest extends SimpleTestBase {

    @Test
    public void ten_workers() {
        // query object Q
        StringObject q1 = utils.createStringObject("AEI");
        SetObject q2 = utils.createSetObject();
        q2.addValue(utils.createStringObject("B"));
        q2.addValue(utils.createStringObject("C"));
        q2.addValue(utils.createIntegerObject(1));
        ListObject Q = utils.createListObject();
        Q.addValue(q1);
        Q.addValue(q2);

        // case object C1
        StringObject c11 = utils.createStringObject("ABC");
        StringObject c12 = utils.createStringObject("DEF");
        SetObject c13 = utils.createSetObject();
        c13.addValue(utils.createStringObject("A"));
        c13.addValue(utils.createStringObject("B"));
        c13.addValue(utils.createIntegerObject(1));
        c13.addValue(utils.createIntegerObject(2));
        ListObject C1 = utils.createListObject();
        C1.setId("C1");
        C1.addValue(c11);
        C1.addValue(c12);
        C1.addValue(c13);

        // case object C2
        StringObject c21 = utils.createStringObject("GHI");
        SetObject c22 = utils.createSetObject();
        c22.addValue(utils.createStringObject("C"));
        c22.addValue(utils.createIntegerObject(1));
        c22.addValue(utils.createIntegerObject(3));
        ListObject C2 = utils.createListObject();
        C2.setId("C2");
        C2.addValue(c21);
        C2.addValue(c22);

        // case object C3
        SetObject c31 = utils.createSetObject();
        c31.addValue(utils.createStringObject("B"));
        c31.addValue(utils.createStringObject("C"));
        c31.addValue(utils.createIntegerObject(1));
        ListObject C3 = utils.createListObject();
        C3.setId("C3");
        C3.addValue(c31);

        // weight function
        WeightFunc weightFunc = (q) -> {
            if (q.isString()) {
                String qVal = ((StringObject) q).getNativeString();
                if (qVal.equals("AEI") || qVal.equals("B")) return 0.5;
            }
            return 1.;
        };

        // similarity measure function
        SimilarityMeasureFunc similarityMeasureFunc = (q, c) -> {
            if (q instanceof StringObject && c instanceof StringObject) return SMStringLevenshtein.NAME;
            if (q instanceof SetObject && c instanceof SetObject) return SMCollectionIsolatedMappingExt.NAME;
            if (q instanceof IntegerObject && c instanceof IntegerObject) return SMNumericLinear.NAME;
            return SMObjectEqual.NAME;
        };

        // method invokers function
        MethodInvokersFunc methodInvokersFunc = new MethodInvokersFunc() {
            @Override
            public ArrayList<MethodInvoker> apply(DataObject q, DataObject c) {
                ArrayList<MethodInvoker> methodInvokers = new ArrayList<>();

                if (q instanceof SetObject && c instanceof SetObject) {
                    methodInvokers.add(new MethodInvoker(
                            "setSimilarityMeasureFunc",
                            new Class[]{SimilarityMeasureFunc.class},
                            new Object[]{similarityMeasureFunc}));

                    methodInvokers.add(new MethodInvoker(
                            "setMethodInvokersFunc",
                            new Class[]{MethodInvokersFunc.class},
                            new Object[]{this}));

                    methodInvokers.add(new MethodInvoker(
                            "setWeightFunc",
                            new Class[]{WeightFunc.class},
                            new Object[]{weightFunc}
                    ));

                }

                if (q instanceof IntegerObject && c instanceof IntegerObject) {

                    methodInvokers.add(new MethodInvoker(
                            "setMinimum",
                            new Class[]{double.class},
                            new Object[]{0.}
                    ));

                    methodInvokers.add(new MethodInvoker(
                            "setMaximum",
                            new Class[]{double.class},
                            new Object[]{10.}
                    ));

                }

                return methodInvokers;
            }
        };


        // calculation of the three similarities (Q,C1), (Q,C2), (Q,C3)
        SMListMappingImplExt smListMappingImplExt;
        Similarity similarity;

        // - C1 -
        smListMappingImplExt = new SMListMappingImplExt();
        smListMappingImplExt.setContainsInexact();
        smListMappingImplExt.setSimilarityMeasureFunc(similarityMeasureFunc);
        smListMappingImplExt.setMethodInvokersFunc(methodInvokersFunc);
        smListMappingImplExt.setWeightFunc(weightFunc);

        similarity = smListMappingImplExt.compute(Q, C1, simVal);

        assertEquals(23. / 45, similarity.getValue(), delta);

        // - C2 -
        smListMappingImplExt = new SMListMappingImplExt();
        smListMappingImplExt.setContainsInexact();
        smListMappingImplExt.setSimilarityMeasureFunc(similarityMeasureFunc);
        smListMappingImplExt.setMethodInvokersFunc(methodInvokersFunc);
        smListMappingImplExt.setWeightFunc(weightFunc);

        similarity = smListMappingImplExt.compute(Q, C2, simVal);

        assertEquals(29. / 45, similarity.getValue(), delta);

        // - C3 -
        smListMappingImplExt = new SMListMappingImplExt();
        smListMappingImplExt.setContainsInexact();
        smListMappingImplExt.setSimilarityMeasureFunc(similarityMeasureFunc);
        smListMappingImplExt.setMethodInvokersFunc(methodInvokersFunc);
        smListMappingImplExt.setWeightFunc(weightFunc);

        similarity = smListMappingImplExt.compute(Q, C3, simVal);

        assertEquals(1., similarity.getValue(), delta);

        // retrieving the three cases and checking their order
        WriteableObjectPool objectPool = ObjectPoolFactory.newObjectPool();
        objectPool.store(C1);
        objectPool.store(C2);
        objectPool.store(C3);


        ParallelLinearRetrieverImplExt parallelLinearRetrieverImplExt = new ParallelLinearRetrieverImplExt();
        parallelLinearRetrieverImplExt.setSimilarityModel(similarityModel);
        parallelLinearRetrieverImplExt.setObjectPool(objectPool);

        ArrayList<MethodInvoker> globalMethodInvokers = new ArrayList<>();
        globalMethodInvokers.add(new MethodInvoker("setContainsInexact", new Class[]{}, new Object[]{}));

        parallelLinearRetrieverImplExt.setGlobalSimilarityMeasure(SMListMappingExt.NAME);
        parallelLinearRetrieverImplExt.setGlobalMethodInvokers(globalMethodInvokers);
        parallelLinearRetrieverImplExt.setLocalSimilarityMeasureFunc(similarityMeasureFunc);
        parallelLinearRetrieverImplExt.setLocalMethodInvokersFunc(methodInvokersFunc);
        parallelLinearRetrieverImplExt.setLocalWeightFunc(weightFunc);

        parallelLinearRetrieverImplExt.setNumberOfWorkers(10);

        Query query = parallelLinearRetrieverImplExt.newQuery();
        query.setQueryObject(Q);
        query.setRetrieveCases(true);

        RetrievalResultList retrievalResults = parallelLinearRetrieverImplExt.perform(query);
        Iterator retrievalResultIterator = retrievalResults.iterator();

        assertEquals("C3", ((RetrievalResult) retrievalResultIterator.next()).getObjectId());
        assertEquals("C2", ((RetrievalResult) retrievalResultIterator.next()).getObjectId());
        assertEquals("C1", ((RetrievalResult) retrievalResultIterator.next()).getObjectId());

    }

    @Test
    public void test0() throws IOException, ParserConfigurationException, ClassNotFoundException, InvocationTargetException, SAXException, NoSuchMethodException, IllegalAccessException {
        String xes = Files.readString(Path.of("src/test/resources/de/uni_trier/wi2/parallel/log.xes"));

        String[] ids = new String[10];
        for (int i = 0; i < ids.length; i++) ids[i] = "T" + i;

        XEStoNESTsAXConverter converter = new XEStoNESTsAXConverter(model);
        converter.configure(false, false, null, ids);
        List<NESTSequentialWorkflowObject> workflows = converter.convert(xes);
        WriteableObjectPool<NESTSequentialWorkflowObject> pool = new WriteableObjectPoolImpl<>();
        pool.storeAll(workflows);

        ParallelLinearRetrieverImplExt retriever = new ParallelLinearRetrieverImplExt();
        retriever.setSimilarityModel(similarityModel);
        retriever.setNumberOfWorkers(3);
        retriever.setObjectPool(pool);


        String globalSimilarityMeasure = "ListDTWExt";
        ArrayList<MethodInvoker> globalMethodInvokers = new ArrayList<>();
        globalMethodInvokers.add(new MethodInvoker("setHalvingDistancePercentage", new Class[]{double.class}, new Object[]{0.5d}));

        SimilarityMeasureFunc localSimilarityMeasureFunc = XMLtoSimilarityMeasureFuncConverter.getSimilarityMeasureFunc(new File("src/test/resources/de/uni_trier/wi2/parallel/smf.xml"));
        MethodInvokersFunc localMethodInvokersFunc = XMLtoMethodInvokersFuncConverter.getMethodInvokersFunc(new File("src/test/resources/de/uni_trier/wi2/parallel/mif.xml"));
        WeightFunc localWeightFunc = XMLtoWeightFuncConverter.getWeightFunc(new File("src/test/resources/de/uni_trier/wi2/parallel/wf.xml"));


        retriever.setGlobalSimilarityMeasure(globalSimilarityMeasure);
        retriever.setGlobalMethodInvokers(globalMethodInvokers);

        retriever.setLocalSimilarityMeasureFunc(localSimilarityMeasureFunc);
        retriever.setLocalMethodInvokersFunc(localMethodInvokersFunc);
        retriever.setLocalWeightFunc(localWeightFunc);


        Query query = retriever.newQuery();
        query.setQueryObject(workflows.get(0));
        query.setRetrieveCases(false);
        query.setNumberOfResults(10);
        RetrievalResultList retrievalResults = retriever.perform(query);
        retrievalResults.iterator();

    }
}
