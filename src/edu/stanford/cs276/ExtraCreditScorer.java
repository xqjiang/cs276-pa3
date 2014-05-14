package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExtraCreditScorer extends BM25Scorer
{
	
	public ExtraCreditScorer(Map<String,Double> idfs,Map<Query, Map<String, Document>> queryDict)
	{
		super(idfs, queryDict);
	}
	
	public String processStr(String s){
		String cur =s;
		String cur_es =s;
		String cur_s = s;
		if(s.length()>2){
		if (s.substring(s.length()-2).equals("es")&& s.length()!=2){
			cur_es= s.substring(0,s.length()-2);
			//System.out.println("here is good");
			//cur = cur_es;
		}else{
			if (s.substring(s.length()-1).equals("s")){
				if (s.equals("address")) {System.out.println("hello address");}
				cur_s= s.substring(0,s.length()-1);
				//System.out.println(cur_s+" here is good");
				//cur = cur_s;
				}
			}	
		}
		
		if(idfs.containsKey(cur_es)){
			if(idfs.get(cur_es)/idfs.get(cur) > 1){
				cur = cur_es;			
			}
		}
		
		if(idfs.containsKey(cur_s)){
			if (idfs.get(cur_s)/idfs.get(cur) >1){
				cur = cur_s;
			}
		}
		return cur;
	}
	
	@Override
	public double getSimScore(Document d, Query q) {		
		
		double score = 0;
		
		super.urlweight = 0.3;
		super.titleweight = 10;
		super.bodyweight = 0.1;
		super.anchorweight = 6;
		super.headerweight = 0.5;
		
		super.burl = 1;
		super.bbody=0.1;
		super.bheader=0.7;
		super.btitle=0.3;
		super.banchor=0.5;
		
		super.k1 = 2.5;
		super.pageRankLambda=2;
		
		
		ArrayList<String> queries = new ArrayList<String>();
		for (int i = 0; i< q.queryWords.size(); i++){
			queries.add(q.queryWords.get(i));
		}
		
		
		if(queries.size()>1){
		for (int j = 0; j< queries.size()-1; j++){
			String cur = queries.get(j);
			String cur_x = processStr(cur);
			String next = queries.get(j+1);	 
			String next_x = processStr(next);
			
			Query new_q = new Query(cur_x+" "+next_x);
			System.out.println(new_q.queryWords);
			
			score += super.getSimScore(d, new_q);
			
			}
		}
		//System.out.println(score);
		return score/q.queryWords.size();
	}
			
}


