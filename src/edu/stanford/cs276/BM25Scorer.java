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
    double urlweight = -1;
    double titleweight  = -1;
    double bodyweight = -1;
    double headerweight = -1;
    double anchorweight = -1;
    
    ///////bm25 specific weights///////////////
    double burl=-1;
    double btitle=-1;
    double bheader=-1;
    double bbody=-1;
    double banchor=-1;

    double k1=-1;
    double pageRankLambda=-1;
    double pageRankLambdaPrime=-1;
    double pageRankLambda1 = -1;
    //////////////////////////////////////////
    
    ////////////bm25 data structures--feel free to modify ////////
    
    Map<Document,Map<String,Double>> lengths;
    Map<String,Double> avgLengths;
    Map<Document,Double> pagerankScores;
    
    //////////////////////////////////////////
    public double V_log(double f){
    	double log_f = f +pageRankLambdaPrime;
    	return Math.log(log_f);
    }
    
    public double V_sigmoid(double f){
    	return 1.0/(pageRankLambdaPrime + Math.exp(-pageRankLambda1*f));
    }
    
    public double V_saturate(double f){
    	return f /(f+pageRankLambdaPrime);
    }
    
    //sets up average lengths for bm25, also handles pagerank
    public void calcAverageLengths()
    {
    	lengths = new HashMap<Document,Map<String,Double>>();
    	avgLengths = new HashMap<String,Double>();
    	pagerankScores = new HashMap<Document,Double>();
    	
		/*
		 * @//TODO : Your code here
		 */
    	// return the pagerank for the Dictionary
    	
    	for(Query q : queryDict.keySet()){
    		for (String url : queryDict.get(q).keySet()){
    			Document new_url = new Document(queryDict.get(q).get(url).url);
    			int pageranks = queryDict.get(q).get(url).page_rank;
    			pagerankScores.put(new_url, (double)pageranks);
    			
    			int url_length = queryDict.get(q).get(url).url.split(" ").length;
    			Map<String, Double> mapUrl = new HashMap<String, Double>();
    			mapUrl.put("url", (double)url_length);
    			lengths.put(new_url,mapUrl);
    			
    			int title_length =queryDict.get(q).get(url).title.length();
    			Map<String, Double> mapTitle = new HashMap<String, Double>();
    			mapUrl.put("title", (double)title_length);
    			lengths.put(new_url,mapTitle);
    			
    			if (queryDict.get(q).get(url).anchors!=null){
    				int anchor_count  = 0;
    				for (String anchor_text : queryDict.get(q).get(url).anchors.keySet()){
    					anchor_count = anchor_count + queryDict.get(q).get(url).anchors.get(anchor_text);
    				} 	
    				Map<String, Double> mapAnchor= new HashMap<String, Double>();
        			mapUrl.put("anchor", (double)anchor_count);
        			lengths.put(new_url,mapAnchor);
    			}
    			
    			int body_length = 0;

    			body_length = queryDict.get(q).get(url).headers.size();
    			Map<String, Double> mapBody = new HashMap<String, Double>();
        		mapUrl.put("body", (double)body_length);
        		lengths.put(new_url,mapBody);
      		
    	}
    	
 
    	
    	
    	//normalize avgLengths
		for (String tfType : this.TFTYPES)
		{
			/*
			 * @//TODO : Your code here
			 */
			// for each different types, try to fetch the 
			for (Document doc : lengths.keySet()){
				double count = 0;
				count =  count + lengths.get(doc).get(tfType);
				avgLengths.put(tfType, (double)count/lengths.keySet().size());					
			}		
		}
    }

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
    ////////////////////////////////////
	Map<String, Double> constructBmap(){
		Map<String, Double> bmap = new HashMap<String, Double>();
		bmap.put("url", burl);
		bmap.put("title", btitle);
		bmap.put("body", bbody);
		bmap.put("header", bheader);
		bmap.put("anchor", banchor);
		
		return bmap;
	}
    public double getNormFTF(String qType, Document d, Map<String, Map<String, Double>> tfs, Map<String, Double> bmap, String t){	
    	//double score = 0.0;
    	return tfs.get(qType).get(t)/(1 + bmap.get(qType)*(lengths.get(d).get(qType)/avgLengths.get(qType)-1));
    	
    }
    
    Map<String, Double> constructWmap(){
		Map<String, Double> Wmap = new HashMap<String, Double>();
		Wmap.put("url", urlweight);
		Wmap.put("title", titleweight);
		Wmap.put("body", bodyweight);
		Wmap.put("header", headerweight);
		Wmap.put("anchor", anchorweight);
		
		return Wmap;
	}
	public double getNetScore(Map<String,Map<String, Double>> tfs, Query q, Map<String,Double> tfQuery,Document d)
	{
		double score = 0.0;
		double score_wdt = 0.0;
		/*
		 * @//TODO : Your code here
		 */
		Map<String, Double> bmap = constructBmap();
		
		for(String s: q.queryWords){
			score_wdt += urlweight * getNormFTF("url",d,tfs,bmap,s);
			score_wdt += titleweight * getNormFTF("title",d,tfs,bmap,s);
			score_wdt += bodyweight * getNormFTF("body",d,tfs,bmap,s);
			score_wdt += headerweight * getNormFTF("header",d,tfs,bmap,s);
			score_wdt += anchorweight * getNormFTF("anchor",d,tfs,bmap,s);	
			int dft = queryDict.get(d).get(d.url).body_hits.get(s).size();
			
			score += score_wdt/(k1+score_wdt)*Math.log(idfs.get(s));
		}
		score += pageRankLambda*V_log(score); ///// will need to be changed later
		return score;
	}

	//do bm25 normalization
	public void normalizeTFs(Map<String,Map<String, Double>> tfs,Document d, Query q)
	{
		/*
		 * @//TODO : Your code here
		 */
		
	}

	
	@Override
	public double getSimScore(Document d, Query q) 
	{
		
		Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
		
		this.normalizeTFs(tfs, d, q);
		
		Map<String,Double> tfQuery = getQueryFreqs(q);
		
		
        return getNetScore(tfs,q,tfQuery,d);
	}

	
	
	
}
