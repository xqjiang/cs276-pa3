package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BM25Scorer extends AScorer
{
	Map<Query,Map<String, Document>> queryDict;

	public BM25Scorer(Map<String,Double> idfs,Map<Query,Map<String, Document>> queryDict)
	{
		super(idfs);
		this.queryDict = queryDict;
		this.calcAverageLengths();
	}

	///////////////weights///////////////////////////
    double urlweight = 0.1;
    double titleweight  = 10;
    double bodyweight = 0.1;
    double headerweight = 0.5;
    double anchorweight = 6;

    ///////bm25 specific weights///////////////
    double burl=0.5;
    double btitle=0.3;
    double bheader=0.7;
    double bbody=0.1;
    double banchor=0.5;

    double k1=2.5;
    double pageRankLambda=2;
	double pageRankLambdaPrime =0.5;
    //////////////////////////////////////////

    ////////////bm25 data structures--feel free to modify ////////

    Map<Document,Map<String,Double>> lengths;
    Map<String,Double> avgLengths;
    Map<Document,Double> pagerankScores;
    
    //////////////////////////////////////////
    
    //sets up average lengths for bm25, also handles pagerank
    public void calcAverageLengths()
    {
    	lengths = new HashMap<Document,Map<String,Double>>();
    	avgLengths = new HashMap<String,Double>();
    	pagerankScores = new HashMap<Document,Double>();

		/*
		 * @//TODO : Your code here
		 */
		int doc_count = 0;
		int url_total_length = 0;
		int title_total_length = 0;
		int body_total_length = 0;
		int header_total_length = 0;
		int anchor_total_length = 0;

		for (Query q : queryDict.keySet()) {
			for (String url : queryDict.get(q).keySet()) {
				doc_count++;
				Document doc = queryDict.get(q).get(url);
				pagerankScores.put(doc, (double) doc.page_rank);
				Map<String, Double> length = new HashMap<String, Double>();
				lengths.put(doc, length);

				double url_length = doc.url.split("/|\\.").length;
				length.put("url", url_length);
				url_total_length += url_length;

				double title_length = doc.title.split(" ").length;
				length.put("title", title_length);
				title_total_length += title_length;

				body_total_length += doc.body_length;
				length.put("body", (double) doc.body_length);

				double header_length = 0;
				if(doc.headers!=null) {
					for (String header : doc.headers) {
						header_length += header.split(" ").length;
					}			
				}

				header_total_length += header_length;
				length.put("header", header_length);

				int anchor_count = 0;
				if (doc.anchors != null) {
					for (String anchor_text : doc.anchors.keySet()) {
						anchor_count = anchor_count
								+ anchor_text.split(" ").length
								* doc.anchors.get(anchor_text);
					}
				}
				anchor_total_length += anchor_count;
				length.put("anchor", (double) anchor_count);
			}

			// normalize avgLengths
			avgLengths.put("url", ((double) url_total_length) / doc_count);
			avgLengths.put("title", ((double) title_total_length) / doc_count);
			avgLengths.put("body", ((double) body_total_length) / doc_count);
			avgLengths
					.put("header", ((double) header_total_length) / doc_count);
			avgLengths
					.put("anchor", ((double) anchor_total_length) / doc_count);
		}

    }
    
    ////////////////////////////////////
    
	public double V_log(double f) {
		double log_f = f + pageRankLambdaPrime;
		return Math.log(log_f);
	}
	
	
	public double getNormFTF(String qType, Document d,
			Map<String, Map<String, Double>> tfs, double b,
			String term) {
		// double score = 0.0;
		return tfs.get(qType).get(term)
				/ (1 + b * (lengths.get(d).get(qType) / avgLengths.get(qType) - 1));

	}

	public double getNetScore(Map<String,Map<String, Double>> tfs, Query q, Map<String,Double> tfQuery,Document d)
	{
		double score = 0.0;
		double score_wdt = 0.0;
		/*
		 * @//TODO : Your code here
		 */
	    
		for (String term : q.queryWords) {
			score_wdt += urlweight * getNormFTF("url", d, tfs, burl, term);
			score_wdt += titleweight * getNormFTF("title", d, tfs, btitle, term);
			score_wdt += bodyweight * getNormFTF("body", d, tfs, bbody, term);
			score_wdt += headerweight * getNormFTF("header", d, tfs, bheader, term);
			score_wdt += anchorweight * getNormFTF("anchor", d, tfs, banchor, term);

			double idf_score;
			if(idfs.containsKey(term)){
				idf_score = idfs.get(term);
			} else {
				idf_score = 1;
			}
			score += score_wdt / (k1 + score_wdt) * idf_score;
		}
		score += pageRankLambda * V_log(pagerankScores.get(d)); 
		return score;
	}

	public Map<String, Double> toSublinear(Map<String, Double> map) {
		Map<String, Double> new_map = new HashMap<String, Double>();
		Iterator<String> itr = map.keySet().iterator();
		while(itr.hasNext()) {
			String word = itr.next();
			double count = map.get(word);
			if(count > 0) {
				new_map.put(word, 1 + Math.log(count));
			} else {
				new_map.put(word, 0.0);
			}
		}
		return new_map;
	}
	
	//do bm25 normalization
	public void normalizeTFs(Map<String,Map<String, Double>> tfs,Document d, Query q)
	{
		/*
		 * @//TODO : Your code here
		 */
		tfs.put("url", toSublinear(tfs.get("url")));
		tfs.put("title", toSublinear(tfs.get("title")));
		tfs.put("body", toSublinear(tfs.get("body")));
		tfs.put("header", toSublinear(tfs.get("header")));
		tfs.put("anchor", toSublinear(tfs.get("anchor")));
	}

	@Override
	public double getSimScore(Document d, Query q) 
	{
		
		Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
		
		this.normalizeTFs(tfs, d, q);
		
		Map<String,Double> tfQuery = getQueryFreqs(q);
		tfQuery = toSublinear(tfQuery);

        return getNetScore(tfs,q,tfQuery,d);
	}

	
	
	
}
