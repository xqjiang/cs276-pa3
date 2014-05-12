package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class AScorer {

	Map<String, Double> idfs;
	String[] TFTYPES = { "url", "title", "body", "header", "anchor" };

	public AScorer(Map<String, Double> idfs) {
		this.idfs = idfs;
	}

	// scores each document for each query
	public abstract double getSimScore(Document d, Query q);

	// handle the query vector
	public Map<String, Double> getQueryFreqs(Query q) {
		Map<String, Double> tfQuery = new HashMap<String, Double>();

		/*
		 * @//TODO : Your code here
		 */
		for (int i = 0; i < q.queryWords.size(); i++) {
			String word = q.queryWords.get(i);
			if (tfQuery.containsKey(word)) {
				double count = tfQuery.get(word) + 1;
				tfQuery.put(word, count);
			} else {
				tfQuery.put(word, 1.0);
			}
		}

		return tfQuery;
	}

	// //////////////////Initialization/Parsing Methods/////////////////////

	/*
	 * @//TODO : Your code here
	 */

	private Map<String, Double> initializeMap(
			Map<String, Map<String, Double>> tfs, String name, Query q) {
		Map<String, Double> output = new HashMap<String, Double>();
		for (int i = 0; i < q.queryWords.size(); i++) {
			output.put(q.queryWords.get(i), 0.0);
		}
		tfs.put(name, output);
		return output;
	}

	// //////////////////////////////////////////////////////

	/*
	 * / Creates the various kinds of term frequences (url, title, body, header,
	 * and anchor) You can override this if you'd like, but it's likely that
	 * your concrete classes will share this implementation
	 */
	public Map<String, Map<String, Double>> getDocTermFreqs(Document d, Query q) {
		// map from tf type -> queryWord -> score
		Map<String, Map<String, Double>> tfs = new HashMap<String, Map<String, Double>>();

		Map<String, Double> url_map = initializeMap(tfs, "url", q);
		Map<String, Double> title_map = initializeMap(tfs, "title", q);
		Map<String, Double> body_map = initializeMap(tfs, "body", q);
		Map<String, Double> header_map = initializeMap(tfs, "header", q);
		Map<String, Double> anchor_map = initializeMap(tfs, "anchor", q);
		// //////////////////Initialization/////////////////////

		/*
		 * @//TODO : Your code here
		 */

		// //////////////////////////////////////////////////////

		// ////////handle counts//////

		// loop through query terms increasing relevant tfs
		for (String queryWord : q.queryWords) {
			/*
			 * @//TODO : Your code here
			 */
			if(d.url.contains(queryWord)){
				double count = url_map.get(queryWord) + 1;
				url_map.put(queryWord, count);
			}

			if(d.title.contains(queryWord)){
				double count = title_map.get(queryWord) + 1;
				title_map.put(queryWord, count);
			}
			if(d.body_hits!=null){
				if(d.body_hits.containsKey(queryWord)){
					double count = body_map.get(queryWord) +d.body_hits.get(queryWord).size();
					body_map.put(queryWord, count);
				}
			}

			if(d.headers!=null){
				Iterator<String> itr = d.headers.iterator();
				while(itr.hasNext()){
					String header = itr.next();
					if(header.contains(queryWord)){
						double count = header_map.get(queryWord) + 1;
						header_map.put(queryWord, count);
					}
				}
			}

			if(d.anchors!=null && d.anchors.containsKey(queryWord)){
				double count = anchor_map.get(queryWord) + 1;
				anchor_map.put(queryWord, count);
			}
		}
		return tfs;
	}

}
