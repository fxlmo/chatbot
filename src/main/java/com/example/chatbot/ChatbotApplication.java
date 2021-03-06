package com.example.chatbot;

import com.ChatbotController.ChatbotController;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static com.example.chatbot.context.*;


//amazon mongo stuff
//local mongo stuff

@SpringBootApplication
@ComponentScan(basePackageClasses = ChatbotController.class)
public class ChatbotApplication implements CommandLineRunner {

	@Autowired
	public ChatbotApplication() {
		globals.keyList = new ArrayList<>();
		globals.threadList = new ArrayList<>();
		globals.context = none;
		globals.stoplist = new ArrayList<>(Arrays.asList("i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours", "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don", "should", "now"));
		globals.entries = new ArrayList<>();
	}

	globals globals = new globals();


	public static void main(String[] args) {
		SpringApplication.run(ChatbotApplication.class, args);
	}


	@Override
	public void run(String... args) throws Exception {
	    globals.keyList = new ArrayList<>();
		globals.threadList = new ArrayList<>();
		globals.context = none;
		globals.stoplist = new ArrayList<>(Arrays.asList("i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours", "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don", "should", "now"));
		//Mongo stuff
		MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://admin:chatbot123@ec2-54-198-1-3.compute-1.amazonaws.com:27017/?authSource=admin&authMechanism=SCRAM-SHA-1"));
		//This will flag as deprecated but it's fine I promise
		DB database = mongoClient.getDB("chatbot");
		//Specify the collection we're using (it's called threads in this)
		globals.collection = database.getCollection("threads");

		//Set up containers

		//GET ALL FILES FROM MONGO
		DBCursor cursor = globals.collection.find(new BasicDBObject());
		//Iterate through documents
		for (int i = 0; i < cursor.size(); i++) {
			DBObject currentDoc = cursor.next();
			String body = (String)currentDoc.get("body");
			if (body != null) {
				ArrayList<String> newDoc = new ArrayList<>();
				for (String word : body.split(" ")) {
					newDoc.add(word);
				}
				globals.documents.add(newDoc);
				//id is a composite string, so set up JSON reader to split into the two parts
				JSONObject obj = new JSONObject(currentDoc.get("_id").toString());
				String threadid = obj.get("thread_id").toString();
				//Add thread to global thread list
				globals.threadList.add(threadid);
			}
		}
		globals.entries = new ArrayList<>();
		getEntries(globals.collection);

		//Calculate all keywords for the mongo entries
		TFIDFCalc keywordCalc = new TFIDFCalc();
		for (int testIndex = 0; testIndex < globals.documents.size(); testIndex++) {
			HashMap<String, Double> keyWords = new HashMap<>();
			double avg = 0;
			int i = 0;
			for (String word : globals.documents.get(testIndex)) {
				double freq = keywordCalc.tfidf(globals.documents, globals.documents.get(testIndex), word);
				i++;
				avg += freq;
				if (!keyWords.containsKey(word)) {
					keyWords.put(word, freq);
				}
			}
			avg = avg/i;
			//This flag is the global average for all of the keywords. I use this as a threshold.
			globals.averageTF = avg;
		}

		//Begin interaction with usr
		//normalIO(documents, globals.collection);
	}

	public ArrayList<String> removeStopWords(ArrayList<String> document) {
		document.removeIf(d -> globals.stoplist.contains(d.toLowerCase()));
		return document;
	}

	/**
	 * Get list of keyword from a given document
	 * @param documents		Document to calculate
	 * @param newDocument	Documents from list
	 * @return				List of keywords in document
	 */
	public ArrayList<String> getKeyWords(ArrayList<ArrayList<String>> documents, ArrayList<String> newDocument, double threshold) {
		TFIDFCalc keywordCalc = new TFIDFCalc();
		HashMap<String, Double> words = new HashMap<>();
		ArrayList<String> wordList = new ArrayList<>(newDocument);
		if(!documents.contains(wordList)) {
			documents.add(new ArrayList<>(wordList));
		}
		ArrayList<String> keyWords = new ArrayList<>();
		wordList.removeAll(globals.stoplist);
		wordList.removeIf(word -> globals.stoplist.contains(word));
		for (ArrayList<String> doc : documents) {
		    doc.removeIf(d -> globals.stoplist.contains(d));
		}
		for (String w : wordList) {
			w = w.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
			double freq = keywordCalc.tfidf(documents, wordList, w);
			//Threshold is here, change accordingly (average of tf-idf seems to be around
			//1.5-2.5 depending on the document, but this will change when we add more.
			if (!words.containsKey(w)) {
			    words.put(w, freq);
			}
		}
		Double avg = 0.0;
		for (Double v : words.values()) {
			avg += v;
		}
		avg = avg/words.values().size();
		for (String w : words.keySet()) {
			if(words.get(w) >= avg) {
				keyWords.add(w);
			}
		}
	   	return keyWords;
	}

	/**
	 * Check if an input is in a valid date formal (dd-mm-yyyy)
	 * @param inDate	Input to check
	 * @return			Returns true if it is a date
	 */
	public static boolean isValidDate(String inDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		dateFormat.setLenient(false);
		try {
			dateFormat.parse(inDate.trim());
		} catch (ParseException pe) {
			return false;
		}
		return true;
	}

	/**
	 * Administrative IO - interaction when entering admin mode
	 * @param collection    The collection
	 * @param documents        Current documents
	 * @throws JSONException
	 * @return
	 */
	public void adminAdd(DBCollection collection, ArrayList<ArrayList<String>> documents, String receivedThreadId, String receivedSubId, String receivedBody, String receivedDate, String receivedQA) throws JSONException {
		globals.context = none;

		//converts body of thread to lowercase and removes all punctuation
		ArrayList<String> convBody = convDoc(receivedBody.split(" "));
		ArrayList<String> keyWords = getKeyWords(documents, convBody, globals.averageTF);
		int subid = 0;
		boolean end = false;

		for (DBObject t : getSubThreads(collection, receivedThreadId)) {
			DBObject id = (DBObject) t.get("_id");
			if ((int) id.get("sub_id") == subid && !end) {
				subid++;
			} else {
				end = true;
			}
		}
		//create container for new entry
		DBObject newEntry = new BasicDBObject("_id", new BasicDBObject("thread_id", receivedThreadId)
				.append("sub_id", subid))
				.append("subthread", receivedSubId)
				.append("date", receivedDate)
				.append("body", receivedBody)
				.append("qa", receivedQA)
				.append("keywords", keyWords);
		collection.insert(newEntry);

		globals.keyList.add(keyWords);
		globals.threadList.add(receivedThreadId);
		entry ent = new entry();
		// Recalculate keywords
		globals.entries.add(ent.add(receivedThreadId, receivedSubId, subid, receivedDate, receivedQA, keyWords, receivedBody));
		updateKeywords(collection);
		getEntries(collection);
		globals.context = admin_else;
	}

	public JSONObject adminList(ArrayList<ArrayList<String>> documents, DBCollection collection, String requestedID) throws JSONException {
		JSONObject out = new JSONObject();
		if (requestedID.equalsIgnoreCase("")) {
			ArrayList<entry> entries = new ArrayList<>();
			out.put("type", "found");
			out.put("content", entries);
			globals.context = admin_else;
		} else {
			//look for thread
			out.put("context", "list");
			out.put("type", "not-found");
			out.put("content", "null");
		}
		return out;
	}

	/**
	 * Shows the percentage match of a word with another word in order
	 * @param word1 First word
	 * @param word2 Second word
	 * @return Percentage match 0.0-1.0
	 */
	public float fuzzyMatch(String word1, String word2) {
		ArrayList<String> bigram1 = bigram(word1);
		ArrayList<String> bigram2 = bigram(word2);
		float total = bigram1.size() + bigram2.size();
		bigram1.retainAll(bigram2);
		float out = (bigram1.size()*2)/total;
		return out;
	}

	/**
	 * Return the bigram of a word (each of the two character possibilities in a word, eg in 'bigram' = 'bi' 'ig' 'gr' 'ra' 'am')
	 */
	public ArrayList<String> bigram(String input) {
		ArrayList<String> bigram = new ArrayList<String>();
		for (int i = 0; i < input.length() - 1; i++) {
			String chars = "";
			chars = chars + input.charAt(i);
			chars = chars + input.charAt(i+1);
			bigram.add(chars);
		}
		return bigram;
	}

	/**
	 * Normal user interaction
	 * @param documents
	 * @param collection
	 * @throws JSONException
	 */
	public JSONObject normalIO(ArrayList<ArrayList<String>> documents, DBCollection collection, String questionAsked) throws JSONException {

		DBCursor cursor = collection.find(new BasicDBObject());
		JSONObject out = new JSONObject();
		if (cursor.size() == 0) {
			//if the database is empty on start, enable admin mode to add some records (this should never really run in theory)
			out.put("type","error");
			out.put("content","empty");
		} else {
			getEntries(collection);
		}
		//check input and match to given string
		questionAsked = questionAsked.substring(1, questionAsked.length() - 1);
		System.out.println("Question received: " + questionAsked);
		String[] sList = questionAsked.split(" ");
		if (globals.context == multi) {
			// If the user has entered a query that matched multiple threads before, go here
			ArrayList<String> answers = new ArrayList<>();
			answers = getAnswers(collection, questionAsked);
			if (answers.size() > 0) {
				int ansIndex = 1;
				out.put("type","answer");
				JSONObject jsonAnswer = new JSONObject();
				int i = 0;
				for (String ans : answers) {
					jsonAnswer.put(String.valueOf(i), ans);
					i++;
				}
				out.put("content",jsonAnswer);
				for (String a : answers) {
					ansIndex++;
				}
			} else {
				out.put("type","no-answer");
				out.put("content","");
			}
			globals.context = none;
			return out;
		}
		ArrayList<String> newDocument = new ArrayList<>();
		for (String word : sList) {
			newDocument.add(word.replaceAll("[^a-zA-Z0-9]", ""));
		}
		ArrayList<String> wordList = new ArrayList<>(newDocument);
		documents.add(new ArrayList<>(wordList));
		ArrayList<String> keyWords = new ArrayList<>();
		// Instead of decompose into keywords, just remove stopwords and query the data with that; the tfidf would
		// get rid of too many of the query words
		keyWords = removeStopWords(newDocument);
		keyWords = new ArrayList<>(new HashSet<>(keyWords));
		keyWords = convDoc(keyWords);
		System.out.println("Keywords of query: " + keyWords);
		int index = 0;
		int threshold = (int) (keyWords.size()*0.5);
		if (keyWords.size() < 2) {
			threshold = 1;
		}
		ArrayList<String> foundThreads = new ArrayList<>();
		ArrayList<Integer> indices = new ArrayList<>();
		globals.keyList = getAllKeyWords(collection, documents);

		//look for matching keywords
		for (ArrayList<String> keyWordsi : globals.keyList) {
			int matching = 0;
			for (String w : keyWords) {
				if (keyWordsi.contains(w)) {
					matching++;
				} else {
					// if no match is found, look at the matches of the 'bigrams' of the each keywords
					boolean match = false;
					for (String kw : keyWordsi) {
						float fzMatch = fuzzyMatch(w.toLowerCase(), kw);
						if (fzMatch > 0.75) {
							match = true;
						}
					}
					if (match) {
						matching++;
					}
				}
			}
			// If matches are above a certain threshold, then return the thread
			System.out.println(matching + " // " + threshold);
			if (matching > threshold && !foundThreads.contains(globals.entries.get(index).threadid)) {
				indices.add(index);
				foundThreads.add(globals.entries.get(index).threadid);
			}
			index++;
		}
		if (indices.size() > 0) {
			ArrayList<String> answers = new ArrayList<>();
			String threadid = "";
			if (indices.size() == 1) {
				//Bot finds one match
				threadid = globals.entries.get(indices.get(0)).threadid;
				answers = getAnswers(collection, threadid);
			} else {
				//Bot finds 2 or more matches
				int ind = 0;
				JSONObject returnedAnswers = new JSONObject();
				for (int i : indices) {
					returnedAnswers.put(String.valueOf(ind), globals.entries.get(i).threadid);
					ind++;
				}
				globals.context = multi;
				out.put("type", "answers");
				out.put("content", returnedAnswers);
				return out;
			}
			if (answers.size() > 0) {
				// If there are answers in the thread:
				int ansIndex = 1;
				out.put("type","answer");
				JSONObject jsonAnswer = new JSONObject();
				int i = 0;
				for (String ans : answers) {
					jsonAnswer.put(String.valueOf(i), ans);
					i++;
				}
				out.put("content",jsonAnswer);
				for (String a : answers) {
					ansIndex++;
				}
			} else {
				// If no answers in thread flag error
				out.put("type","error");
				out.put("content","unanswered");
			}
		} else {
			out.put("type","no-answer");
			out.put("content","");
		}
		//}

		return out;
	}

	/**
	 * Get size of thread (number of entries) for a given thread id
	 * @param collection	The collection of mongodocs
	 * @param threadid		Thread id to check size of
	 * @return				size of thread
	 */
	public Integer getThreadSize(DBCollection collection, String threadid) {
		DBCursor cursor = collection.find(new BasicDBObject("_id.thread_id",threadid));
		return cursor.size();
	}

	/**
	 * Returns all entries that have the 'answer' flag
	 * @param collection	The collection of mongodocs
	 * @param threadid		Thread id to check
	 * @return				Array of answers
	 */
	public ArrayList<String> getAnswers(DBCollection collection, String threadid) {
		ArrayList<String> answers = new ArrayList<>();
		DBCursor cursor = collection.find(new BasicDBObject("_id.thread_id",threadid));
		for (int i = 0; i < cursor.size(); i++) {
		    DBObject theObj = cursor.next();
		    if (theObj.get("qa").equals("a")) {
		    	answers.add((String)theObj.get("body"));
			}
		}

		return answers;
	}

	/**
	 * Update all keywords in the database
	 * @param collection	The collection (database)
	 */
	public void updateKeywords(DBCollection collection) throws JSONException {
		DBCursor cursorGather = collection.find(new BasicDBObject());
		ArrayList<ArrayList<String>> documents = new ArrayList<>();
		for (int i = 0; i < cursorGather.size(); i++) {
			DBObject theObj = cursorGather.next();
			String word = (String) theObj.get("body");
			String[] wList = word.trim().split(" ");
			ArrayList<String> convBody = convDoc(wList);
			documents.add(convBody);
		}
		DBCursor cursor = collection.find(new BasicDBObject());
		for (int i = 0; i < cursor.size(); i++) {
			DBObject theObj = cursor.next();

			String bod = theObj.get("body").toString().trim();
			ArrayList<String> document = convDoc(new ArrayList<>(Arrays.asList(bod.split(" "))));

			ArrayList<String> keywords = getKeyWords(documents, document, globals.averageTF);
			if(!theObj.get("keywords").equals(keywords)) {
				DBObject query = (DBObject) theObj.get("_id");
				DBObject update = new BasicDBObject("$set", new BasicDBObject("keywords", keywords));
				collection.update(query, update);
			}
		}
		getEntries(collection);
	}

	/**
	 * Convert string to (positive) integer
	 * @param string	String to convert
	 * @return			Returns an integer or -1 if string contains non-numeric characters
	 */
	public Integer toInt(String string) {
		Integer output = 0;
		ArrayList<Character> ints = new ArrayList<Character>(Arrays.asList('0','1','2','3','4','5','6','7','8','9'));
		int i = 1;
		boolean error = false;
		for (char c : string.toCharArray()) {
			if (ints.contains(c)) {
				output += i * ints.indexOf(c);
			} else {
				error = true;
			}
			i *= 10;
		}
		if (!error) {
			return output;
		} else {
			return -1;
		}
	}

	/**
	 * Convert document (list of words) to lower case and remove punctuation and numbers
	 * @param document	document to convert
	 */
	public ArrayList<String> convDoc(ArrayList<String> document) {
		ArrayList<String> convBody = new ArrayList<>();
		for (String w: document) {
			convBody.add(w.toLowerCase().replaceAll("[^a-zA-Z0-9]", ""));
		}
		return convBody;
	}

	public ArrayList<String> convDoc(String[] document) {
		ArrayList<String> convBody = new ArrayList<>();
		for (String w: document) {
			convBody.add(w.toLowerCase().replaceAll("[^a-zA-Z0-9]", ""));
		}
		return convBody;
	}

	/**
	 * Get all entries from a given collection (possibly not used)
	 */
	public ArrayList<entry> getEntries(DBCollection collection) throws JSONException {
	    //clear entries to read from mongo
		if(globals.entries != null) {
			globals.entries.clear();
		} else {
			globals.entries = new ArrayList<>();
		}
		ArrayList<entry> entries = new ArrayList<>();
		DBCursor cursor = collection.find(new BasicDBObject());
		for (int i = 0; i < cursor.size(); i++) {
			DBObject currentDoc = cursor.next();
			String body = (String)currentDoc.get("body");
			//id is a composite string, so set up JSON reader to split into the two parts
			JSONObject obj = new JSONObject(currentDoc.get("_id").toString());

			//create new entry and add to global list of entries
			entry ent = new entry();
			ent.threadid = obj.get("thread_id").toString();
			ent.subid = (int) obj.get("sub_id");
			ent.body = body;
			ent.qa = (String)currentDoc.get("qa");
			ent.subthreadid = (String)currentDoc.get("subthread");
			ent.keywords = (ArrayList<String>)currentDoc.get("keywords");
			ent.date = (String) currentDoc.get("date");
			entries.add(ent);
		}
		globals.entries = entries;
		return entries;
	}

	public DBCollection getCollection() {
		MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://admin:chatbot123@ec2-54-198-1-3.compute-1.amazonaws.com:27017/?authSource=admin&authMechanism=SCRAM-SHA-1"));
		//This will flag as deprecated but it's fine I promise
		DB database = mongoClient.getDB("chatbot");
		//Specify the collection we're using (it's called threads in this)
		return database.getCollection("threads");
	}

	public ArrayList<ArrayList<String>> getDocs (DBCollection collection) {
		DBCursor cursorGather = collection.find(new BasicDBObject());
		ArrayList<ArrayList<String>> documents = new ArrayList<>();
		for (int i = 0; i < cursorGather.size(); i++) {
			DBObject theObj = cursorGather.next();
			String word = (String) theObj.get("body");
			String[] wList = word.trim().split(" ");
			ArrayList<String> convBody = convDoc(wList);
			documents.add(convBody);
		}
		return documents;
	}


	public ArrayList<ArrayList<String>> getAllKeyWords(DBCollection collection, ArrayList<ArrayList<String>> documents) {
		DBCursor cursorGather = collection.find(new BasicDBObject());
		ArrayList<ArrayList<String>> keyWords = new ArrayList<>();
		for (int i = 0; i < cursorGather.size(); i++) {
			DBObject theObj = cursorGather.next();
			keyWords.add((ArrayList<String>) theObj.get("keywords"));
		}
		return keyWords;
	}

	/**
	 * Get a list of all threads in the database
	 */
	public ArrayList<String> getAllThreads(DBCollection collection) throws JSONException {
		DBCursor cursorGather = collection.find(new BasicDBObject());
		ArrayList<String> threads = new ArrayList<>();
		for (int i = 0; i < cursorGather.size(); i++) {
			DBObject obj = cursorGather.next();
			DBObject id = (DBObject) obj.get("_id");
			String thread = (String) id.get("thread_id");
			if (!threads.contains(thread)) {
				threads.add(thread);
			}
		}
		return threads;
	}

	public ArrayList<DBObject> getSubThreads(DBCollection collection, String thread_id) throws JSONException {
		DBCursor cursorGather = collection.find(new BasicDBObject());
		ArrayList<DBObject> subthreads = new ArrayList<>();
		for (int i = 0; i < cursorGather.size(); i++) {
			DBObject obj = cursorGather.next();
			DBObject id = (DBObject) obj.get("_id");
			String thread = (String) id.get("thread_id");
			if (thread.equals(thread_id)) {
				subthreads.add(obj);
			}
		}
		return subthreads;
	}

	/**
	 * Admin delete -- Delete an entry from a collection
	 * 
	 * @param collection -- the collection to delete from
	 * @param id         id of entry to delete from (form thread_id, sub_id)
	 * @throws JSONException
	 */
	public void adminDelete(DBCollection collection, JSONObject id) throws JSONException {
		DBObject query = new BasicDBObject("_id", new BasicDBObject("thread_id", id.get("thread_id"))
				.append("sub_id", id.get("sub_id")));
		collection.remove(query);
	}


	/**
	 * Remove all contexts that may be in use (used by frontend when page is reloaded etc.)
	 */
	public void resetAll() {
		globals.context = none;
	}
}
