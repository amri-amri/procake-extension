package extension;

import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.CollectionObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.SetObject;
import de.uni_trier.wi2.procake.data.objectpool.DataObjectIterator;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityMeasure;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.SMCollectionMapping;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMCollectionImpl;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMCollectionMappingImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import org.apache.commons.collections4.map.MultiKeyMap;
import utils.SimFunc;
import utils.WeightFunc;

import java.security.InvalidParameterException;
import java.util.*;

public class SMCollectionMappingImplExt extends SMCollectionMappingImpl implements SMCollectionMappingExt {
    protected SimFunc similarityToUseFunc;
    protected WeightFunc weightFunc = (a, b) -> 1;

    @Override
    public void setSimilarityToUse(String newValue) {
        super.setSimilarityToUse(newValue);
        similarityToUseFunc = (a, b) -> newValue;
    }

    @Override
    public void setSimilarityToUse(SimFunc similarityToUse){
        similarityToUseFunc = similarityToUse;
    }

    @Override
    public SimFunc getSimilarityToUseFunc() {
        return similarityToUseFunc;
    }

    @Override
    public void setWeightFunction(WeightFunc weightFunc) {
        this.weightFunc = (a, b) -> {
            Double weight = weightFunc.apply(a, b);
            if (weight==null) return 1;
            if (weight<0) return 0;
            if (weight>1) return 1;
            return weight;
        };
    }

    @Override
    public WeightFunc getWeightFunction() {
        return weightFunc;
    }



    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {
        DataObjectIterator qIt, cIt;
        CollectionObject queryCol = (CollectionObject) queryObject;
        CollectionObject caseCol = (CollectionObject) caseObject;

        qIt = queryCol.iterator();
        cIt = caseCol.iterator();

        DataObject[] queryElements = new DataObject[queryCol.size()];
        DataObject[] caseElements = new DataObject[caseCol.size()];

        if (qIt.hasNext()) {
            int qC = 0;
            do {
                queryElements[qC++] = qIt.nextDataObject();
            } while (qIt.hasNext());
        }

        if (cIt.hasNext()) {
            int cC = 0;
            do {
                caseElements[cC++] = cIt.nextDataObject();
            } while (cIt.hasNext());
        }

        ArrayList<Map> mappingList = new ArrayList();
        MappingNode root = new MappingNode(mappingList, queryElements, caseElements);
        ArrayList<MappingNode> leafs = MappingNode.getLeafs(root);
        MappingNode.assignSimilarities(leafs, queryObject, caseObject, this, valuator, similarityToUseFunc, weightFunc);
        ArrayList<MappingNode> maxSimMappings = MappingNode.getMaxSimMappings(leafs);
        MappingNode maxSimMappingNode = maxSimMappings.get(0);
        double maxSim = maxSimMappingNode.sim.getValue();
        ArrayList<Similarity> localSimilarities = (ArrayList<Similarity>) maxSimMappingNode.sim.getLocalSimilarities();




        return new SimilarityImpl(
                this, queryObject, caseObject, maxSim, localSimilarities);
    }

    private class Map{
        final DataObject x;
        final DataObject y;

        Map(final DataObject x, final DataObject y){
            this.x = x;
            this.y = y;
        }
    }

    private class MappingNode{
        final List<Map> mapping;
        final DataObject[] unmappedQueryItems;
        final DataObject[] unmappedCaseItems;
        List<MappingNode> children;
        Similarity sim;

        MappingNode(final List<Map> mapping, final DataObject[] unmappedQueryItems, final DataObject[] unmappedCaseItems){
            this.mapping = mapping;
            this.unmappedQueryItems = unmappedQueryItems;
            this.unmappedCaseItems = unmappedCaseItems;
            children = new ArrayList<>();
            produceChildren();
            sim = null;
        }

        private void produceChildren(){
            if (unmappedQueryItems.length <1) return;

            DataObject newQueryItem = unmappedQueryItems[0];

            DataObject[] newUnmappedQueryItems = new DataObject[unmappedQueryItems.length -1];
            for(int i = 0; i<unmappedQueryItems.length -1; i++){
                newUnmappedQueryItems[i] = unmappedQueryItems[i+1];
            }

            for (int i = 0; i< unmappedCaseItems.length; i++){
                DataObject newCaseItem = unmappedCaseItems[i];

                DataObject[] newUnmappedCaseItems = new DataObject[unmappedCaseItems.length -1];
                for(int j = 0; j<unmappedCaseItems.length; j++){
                    if (j<i) newUnmappedCaseItems[j] = unmappedCaseItems[j];
                    if (j>i) newUnmappedCaseItems[j-1] = unmappedCaseItems[j];
                }

                List newMappingList = new ArrayList();
                for (Map map : mapping) newMappingList.add(map);
                newMappingList.add(new Map(newQueryItem, newCaseItem));

                children.add(new MappingNode(newMappingList, newUnmappedQueryItems, newUnmappedCaseItems));
            }

            List newMappingList = new ArrayList();
            for (Map map : mapping) newMappingList.add(map);
            newMappingList.add(new Map(newQueryItem, null));

            children.add(new MappingNode(newMappingList, newUnmappedQueryItems, unmappedCaseItems));

        }




        private static ArrayList<MappingNode> leafs = null;

        public static ArrayList<MappingNode> getLeafs(MappingNode mappingNode){
            leafs = new ArrayList<>();

            putLeaf(mappingNode);

            return leafs;
        }

        private static void putLeaf(MappingNode mappingNode){
            if (mappingNode.children.isEmpty()) leafs.add(mappingNode);
            else for (MappingNode child : mappingNode.children) putLeaf(child);
        }

        public static void assignSimilarities(ArrayList<MappingNode> mappingNodeList, DataObject queryObject, DataObject caseObject, SimilarityMeasure sm, SimilarityValuator simval, SimFunc similarityToUseFunc, WeightFunc weightFunc){
            for (MappingNode mappingNode : mappingNodeList) {
                ArrayList<Similarity> localSimilarities = new ArrayList<>();
                double nominator = 0;
                double denominator = 0;
                for (Map map : mappingNode.mapping) {
                    Similarity sim;
                    if (map.x != null && map.y != null) {
                        sim = simval.computeSimilarity(map.x, map.y, similarityToUseFunc.apply(map.x, map.y));
                        sim = new SimilarityImpl(sm, map.x, map.y, sim.getValue() * weightFunc.apply(map.x, map.y));
                    } else {
                        sim = new SimilarityImpl(sm, map.x, map.y, 0);
                    }
                    localSimilarities.add(sim);
                    nominator += sim.getValue();
                    denominator += weightFunc.apply(map.x, map.y);
                }
                mappingNode.sim = new SimilarityImpl(sm, queryObject, caseObject, nominator / denominator, localSimilarities);
            }
        }

        public static ArrayList<MappingNode> getMaxSimMappings(ArrayList<MappingNode> mappingNodeList){
            ArrayList<MappingNode> maxSimMappings = new ArrayList<>();
            double maxSim = mappingNodeList.get(0).sim.getValue();
            for (MappingNode mappingNode : mappingNodeList) {
                double simVal = mappingNode.sim.getValue();
                if (simVal >= maxSim) {
                    if (simVal > maxSim) {
                        maxSimMappings = new ArrayList<>();
                        maxSim = simVal;
                    }
                    maxSimMappings.add(mappingNode);
                }
            }
            return maxSimMappings;
        }
    }


}
