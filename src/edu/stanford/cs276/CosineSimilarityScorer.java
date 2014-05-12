package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CosineSimilarityScorer extends AScorer
{
	public CosineSimilarityScorer(Map<String,Double> idfs)
	{
		super(idfs);
	}
	
	///////////////weights///////////////////////////
    double urlweight = 2.0;
    double titleweight  = 0.1;
    double bodyweight = 0.1;
    double headerweight = 0.3;
    double anchorweight = 5.0;
    
    double smoothingBodyLength = 500;
    //////////////////////////////////////////
	
	public double getNetScore(Map<String,Map<String, Double>> tfs, Query q, Map<String,Double> tfQuery,Document d)
	{
		double score = 0.0;
		
		/*
		 * @//TODO : Your code here
		 */
		score += urlweight * getCosineDis(tfs.get("url"), tfQuery);
		score += titleweight * getCosineDis(tfs.get("title"), tfQuery);
		score += bodyweight * getCosineDis(tfs.get("body"), tfQuery);
		score += headerweight * getCosineDis(tfs.get("header"), tfQuery);
		score += anchorweight * getCosineDis(tfs.get("anchor"), tfQuery);
		return score;
	}
	
	public double getCosineDis(Map<String, Double> doc, Map<String, Double> query){
		double output = 0;
		Iterator<String> itr = doc.keySet().iterator();
		while(itr.hasNext()){
			String word = itr.next();
			output = output + doc.get(word) * query.get(word);
		}

		return output;
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

	public Map<String, Double> multipleIDFS(Map<String, Double> map) {
		Map<String, Double> new_map = new HashMap<String, Double>();
		Iterator<String> itr = map.keySet().iterator();
		while(itr.hasNext()) {
			String word = itr.next();
			double count = map.get(word);
			if(idfs.containsKey(word)){
				new_map.put(word, Math.log(count * idfs.get(word)));
			} else {
				new_map.put(word, Math.log(count * 1));//98998
			}
			
		}
		return new_map;
	}
	
	public Map<String, Double> normalize(double length, Map<String, Double> map) {
		Map<String, Double> new_map = new HashMap<String, Double>();
		Iterator<String> itr = map.keySet().iterator();
		while(itr.hasNext()){
			String word = itr.next();
			double count = map.get(word) / length;
			new_map.put(word, count);
		}
		return new_map;
	}
	
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
		
		double length = d.body_length+smoothingBodyLength;
		length = Math.log(length);

		tfs.put("url", normalize(length, tfs.get("url")));
		tfs.put("title", normalize(length, tfs.get("title")));
		tfs.put("body", normalize(length, tfs.get("body")));
		tfs.put("header", normalize(length, tfs.get("header")));
		tfs.put("anchor", normalize(length, tfs.get("anchor")));
	}

	@Override
	public double getSimScore(Document d, Query q) 
	{
		
		Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
		this.normalizeTFs(tfs, d, q);
		Map<String,Double> tfQuery = getQueryFreqs(q);
		tfQuery = toSublinear(tfQuery);
		tfQuery = multipleIDFS(tfQuery);
        return getNetScore(tfs,q,tfQuery,d);
	}

}
