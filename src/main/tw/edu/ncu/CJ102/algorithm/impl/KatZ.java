package tw.edu.ncu.CJ102.algorithm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import tw.edu.ncu.CJ102.algorithm.LinkPrediction;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import edu.uci.ics.jung.algorithms.matrix.GraphMatrixOperations;
import edu.uci.ics.jung.graph.Graph;

public class KatZ<V,E> implements LinkPrediction<V, E> {
	private SparseDoubleMatrix2D adjacencyMatrix;
	ArrayList<V> indexList;
	Graph<V,E> g;
	
	public KatZ(Graph<V,E> g){
		this.g = g;
		adjacencyMatrix = GraphMatrixOperations.graphToSparseMatrix(g);
		indexList = new ArrayList<>(g.getVertices());

	}
	
	@Override
	public double predict(V startNode, V goalNode) {
		//TODO implement this
		int j = this.indexList.indexOf(startNode);
		int k = this.indexList.indexOf(goalNode);
		for(int i = 0;i<=this.adjacencyMatrix.columns();i++){
			this.adjacencyMatrix.get(j, i);
			this.adjacencyMatrix.get(k, i);

		}
		return 0;
	}

}
