package de.uni_trier.wi2.retrieval;

import de.uni_trier.wi2.base.SimpleTestBase;
import de.uni_trier.wi2.extension.retrieval.LinearRetrieverImplExt;
import de.uni_trier.wi2.extension.similarity.measure.collection.SMCollectionIsolatedMappingExt;
import de.uni_trier.wi2.extension.similarity.measure.collection.SMListMappingExt;
import de.uni_trier.wi2.extension.similarity.measure.collection.SMListMappingImplExt;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.IntegerObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.SetObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.data.objectpool.ObjectPoolFactory;
import de.uni_trier.wi2.procake.data.objectpool.WriteableObjectPool;
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

import java.util.ArrayList;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

public class LinearRetrieverExtTest extends SimpleTestBase {

    @Test
    public void correctness_of_retrieval(){
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
        SimilarityMeasureFunc similarityMeasureFunc = (q,c) -> {
            if (q instanceof StringObject   && c instanceof StringObject)   return SMStringLevenshtein.NAME;
            if (q instanceof SetObject      && c instanceof SetObject)      return SMCollectionIsolatedMappingExt.NAME;
            if (q instanceof IntegerObject  && c instanceof IntegerObject)  return SMNumericLinear.NAME;
            return SMObjectEqual.NAME;
        };

        // method invokers function
        MethodInvokersFunc methodInvokersFunc = new MethodInvokersFunc() {
            @Override
            public ArrayList<MethodInvoker> apply(DataObject q, DataObject c) {
                ArrayList<MethodInvoker> methodInvokers = new ArrayList<>();

                if (q instanceof SetObject      && c instanceof SetObject)  {
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

                if (q instanceof IntegerObject  && c instanceof IntegerObject)  {

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

        assertEquals(23./45, similarity.getValue(), delta);

        // - C2 -
        smListMappingImplExt = new SMListMappingImplExt();
        smListMappingImplExt.setContainsInexact();
        smListMappingImplExt.setSimilarityMeasureFunc(similarityMeasureFunc);
        smListMappingImplExt.setMethodInvokersFunc(methodInvokersFunc);
        smListMappingImplExt.setWeightFunc(weightFunc);

        similarity = smListMappingImplExt.compute(Q, C2, simVal);

        assertEquals(29./45, similarity.getValue(), delta);

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


        LinearRetrieverImplExt linearRetrieverImplExt = new LinearRetrieverImplExt();
        linearRetrieverImplExt.setSimilarityModel(similarityModel);
        linearRetrieverImplExt.setObjectPool(objectPool);

        ArrayList<MethodInvoker> globalMethodInvokers = new ArrayList<>();
        globalMethodInvokers.add(new MethodInvoker("setContainsInexact", new Class[]{}, new Object[]{}));

        linearRetrieverImplExt.setGlobalSimilarityMeasure(SMListMappingExt.NAME);
        linearRetrieverImplExt.setGlobalMethodInvokers(globalMethodInvokers);
        linearRetrieverImplExt.setLocalSimilarityMeasureFunc(similarityMeasureFunc);
        linearRetrieverImplExt.setLocalMethodInvokersFunc(methodInvokersFunc);
        linearRetrieverImplExt.setLocalWeightFunc(weightFunc);

        Query query = linearRetrieverImplExt.newQuery();
        query.setQueryObject(Q);
        query.setRetrieveCases(true);

        RetrievalResultList retrievalResults = linearRetrieverImplExt.perform(query);
        Iterator retrievalResultIterator = retrievalResults.iterator();

        assertEquals("C3", ( (RetrievalResult) retrievalResultIterator.next()).getObjectId() );
        assertEquals("C2", ( (RetrievalResult) retrievalResultIterator.next()).getObjectId() );
        assertEquals("C1", ( (RetrievalResult) retrievalResultIterator.next()).getObjectId() );

    }
}
