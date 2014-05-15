package edu.stanford.cs276;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//doesn't necessarily have to use task 2 (could use task 1, in which case, you'd probably like to extend CosineSimilarityScorer instead)
public class SmallestWindowScorer extends BM25Scorer {

	// ///smallest window specific hyper parameters////////
	double B = 100;
	double boostmod = 1;

	//////////////////////////////

	public SmallestWindowScorer(Map<String, Double> idfs,
			Map<Query, Map<String, Document>> queryDict) {
		super(idfs, queryDict);
	}

	public int getWindow(String[] text, String[] words) {
		int window = Integer.MAX_VALUE;
		HashMap<String, Integer> lasts = new HashMap<String, Integer>();
		for (int i = 0; i < words.length; i++) {
			lasts.put(words[i], -1);
		}

		for (int i = 0; i < text.length; i++) {
			if (lasts.containsKey(text[i])) {
				lasts.put(text[i], i);
				int max = getMaxIndex(lasts, text[i]);
				int min = getMinIndex(lasts, text[i]);
				if (min != -1 && (max - min) < window) {
					window = max - min;
				}
			}
		}
		if (window == Integer.MAX_VALUE) {
			return window;
		}
		return window + 1;
	}

	public int getMinIndex(HashMap<String, Integer> lasts, String word) {
		int output = Integer.MAX_VALUE;
		Iterator<String> itr = lasts.keySet().iterator();
		while (itr.hasNext()) {
			String curWord = itr.next();
			int index = lasts.get(curWord);
			if (index < output) {
				output = index;
			}
		}
		return output;
	}

	public int getMaxIndex(HashMap<String, Integer> lasts, String word) {
		int output = -1;
		Iterator<String> itr = lasts.keySet().iterator();
		while (itr.hasNext()) {
			String curWord = itr.next();
			int index = lasts.get(curWord);
			if (index > output) {
				output = index;
			}
		}
		return output;
	}

	public int getSmallestWindow(Document d, Query q) {
		/*
		 * @//TODO : Your code here
		 */
		int smallest = Integer.MAX_VALUE;
		String words[] = q.queryWords.toArray(new String[q.queryWords.size()]);

		String text[] = d.url.split("/|\\.");
		int temp = getWindow(text, words);
		if (temp < smallest) {
			smallest = temp;
		}

		text = d.url.split("/|\\.");
		temp = getWindow(text, words);
		if (temp < smallest) {
			smallest = temp;
		}

		text = d.title.split(" ");
		temp = getWindow(text, words);
		if (temp < smallest) {
			smallest = temp;
		}
		
		if(d.headers!=null) {
			for (String header : d.headers) {
				text = header.split(" ");
				temp = getWindow(text, words);
				if (temp < smallest) {
					smallest = temp;
				}
			}			
		}
		
		if (d.anchors != null) {
			for (String anchor_text : d.anchors.keySet()) {
				text = anchor_text.split(" ");
				temp = getWindow(text, words);
				if (temp < smallest) {
					smallest = temp;
				}
			}
		}
		return smallest;
	}

	public double getWindowScore(Document d, Query q) {
		double score = 0;
		int window = getSmallestWindow(d, q);
		score = 1.0 / (1.0 + (Math.E/B - Math.E) * Math.exp(-1 * window));
		return score;
	}

	@Override
	public double getSimScore(Document d, Query q) {
//		Map<String, Map<String, Double>> tfs = this.getDocTermFreqs(d, q);
//
//		this.normalizeTFs(tfs, d, q);
//
//		Map<String, Double> tfQuery = getQueryFreqs(q);

		double score = super.getSimScore(d, q);
		score = score * getWindowScore(d, q);
		return score;
	}

}
