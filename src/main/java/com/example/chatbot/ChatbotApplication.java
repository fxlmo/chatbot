package com.example.chatbot;


import com.ChatbotController.ChatbotController;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.MongoDbFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import static com.example.chatbot.context.*;


//amazon mongo stuff
//local mongo stuff

@SpringBootApplication
@ComponentScan(basePackageClasses = ChatbotController.class)
public class ChatbotApplication implements CommandLineRunner {
	private final MongoDbFactory mongo;
	private final MongoDatabase databaseAmazon;
	private final MongoCollection<Document> collectionAmazon;

	@Autowired
	public ChatbotApplication(MongoDbFactory mongo) {
		this.mongo = mongo;
		this.databaseAmazon = mongo.getDb("test");
		this.collectionAmazon = databaseAmazon.getCollection("threads");
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
		DBCollection collection = database.getCollection("threads");

		//Set up containers
		ArrayList<ArrayList<String>> documents = new ArrayList<ArrayList<String>>();

		//GET ALL FILES FROM MONGO
		DBCursor cursor = collection.find(new BasicDBObject());
		//Iterate through documents
		for (int i = 0; i < cursor.size(); i++) {
			DBObject currentDoc = cursor.next();
			String body = (String)currentDoc.get("body");
			if (body != null) {
				ArrayList<String> newDoc = new ArrayList<>();
				for (String word : body.split(" ")) {
					newDoc.add(word);
				}
				documents.add(newDoc);
				//id is a composite string, so set up JSON reader to split into the two parts
				JSONObject obj = new JSONObject(currentDoc.get("_id").toString());
				String threadid = obj.get("thread_id").toString();
				//Add thread to global thread list
				globals.threadList.add(threadid);
			}
		}
		globals.entries = new ArrayList<>();
		getEntries(collection);

		//Calculate all keywords for the mongo entries
		TFIDFCalc keywordCalc = new TFIDFCalc();
		for (int testIndex = 0; testIndex < documents.size(); testIndex++) {
			HashMap<String, Double> keyWords = new HashMap<>();
			double avg = 0;
			int i = 0;
			for (String word : documents.get(testIndex)) {
				double freq = keywordCalc.tfidf(documents, documents.get(testIndex), word);
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

		//Begin interaction with user
		normalIO(documents, collection);
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
	 * @param collection	The collection
	 * @param documents		Current documents
	 * @throws JSONException
	 */
	public void adminIO(DBCollection collection, ArrayList<ArrayList<String>> documents) throws JSONException {
		boolean quit = false;
		System.out.println("BOT> Welcome admin user! What do you want to do?");
		globals.context = none;
		while (!quit) {
			Scanner in = new Scanner(System.in);
			String s = in.nextLine();
			if (globals.context.equals(list)) {
				if (s.equalsIgnoreCase("")) {
					for (entry ent : globals.entries) {
						System.out.println("===================");
						System.out.println(ent);
					}
					System.out.println("===================");
					System.out.println("BOT> Can I help with anything else?");
					globals.context = admin_else;
				} else if (s.equalsIgnoreCase("quit") || s.equalsIgnoreCase("q") || s.equalsIgnoreCase("exit")) {
					System.out.println("BOT> Ok, can I help with anything else?");
					globals.context = admin_else;
				} else {
					System.out.println("BOT> Couldn't find this thread id. Try again, or type quit to cancel");
				}
			} else {
				if (s.equalsIgnoreCase("add")) {
					globals.context = none;
					Boolean valid = false;
					String threadid = "";
					String subthreadId = "";
					while (!valid) {
						System.out.println("BOT> Specify thread id");
						threadid = in.nextLine();
						if (threadid == null) {
							System.out.println("BOT> Sorry, invalid thread id, please try again");
						} else {
							valid = true;
						}
					}
					valid = false;

					while (!valid) {
						System.out.println("BOT> Specify subthread id");
						subthreadId = in.nextLine();
						if (subthreadId == null) {
							System.out.println("BOT> Sorry, invalid subthread id, please try again");
						} else {
							valid = true;
						}
					}
					valid = false;

					String date = "";
					while (!valid) {
						System.out.println("BOT> Enter date submitted (dd-MM-yyyy)");
						date = in.nextLine();
						if (!isValidDate(date)) {
							System.out.println("BOT> Sorry, invalid date, try entering again");
						} else {
							valid = true;
						}
					}

					String body = "";
					valid = false;
					while (!valid) {
						System.out.println("BOT> Enter the body of the thread");
						body = in.nextLine();
						if (body.equals("")) {
							System.out.println("BOT> The body cannot be empty");
						} else {
							valid = true;
						}
					}
			String qa = "";
					valid = false;
					while (!valid) {
						System.out.println("BOT> Is this a question (q) or an answer (a)?");
						qa = in.nextLine().toLowerCase();
						if (qa.equals("q") || qa.equals("a")) {
							valid = true;
						} else {
							System.out.println("BOT> Please enter either 'q' or 'a'");
						}
					}

					//converts body of thread to lowercase and removes all punctuation
					ArrayList<String> convBody = convDoc(body.split(" "));
					ArrayList<String> keyWords = getKeyWords(documents, convBody, globals.averageTF);
					int subid = getThreadSize(collection, threadid);

					//create container for new entry
					DBObject newEntry = new BasicDBObject("_id", new BasicDBObject("thread_id", threadid)
							.append("sub_id", subid))
							.append("subthread", subthreadId)
							.append("date", date)
							.append("body", body)
							.append("qa", qa)
							.append("keywords", keyWords);
					collection.insert(newEntry);

					globals.keyList.add(keyWords);
					globals.threadList.add(threadid);
					entry ent = new entry();
					// Recalculate keywords
					globals.entries.add(ent.add(threadid,subthreadId,subid,date,qa,keyWords,body));
					updateKeywords(collection);
					getEntries(collection);
					System.out.println("BOT> Inserted new value! Do you want to do anything else?");
					globals.context = admin_else;
				} else if (s.equalsIgnoreCase("quit") || globals.context.equals(admin_else) && s.equalsIgnoreCase("no")) {
					System.out.println("BOT> Ok, bye. [RETURNING TO MAIN USER]");
					//TODO update locally held records??
					quit = true;
					//normalIO(documents, collection);
				} else if (s.equalsIgnoreCase("help") || s.equals("?")) {
					System.out.println("BOT> Type 'add' to add a new record, or quit to return to the main user");
					globals.context = none;
				} else if (s.equalsIgnoreCase("list")) {
					System.out.println("BOT> Type a threadid to list all entries from that thread, or leave blank to show all");
					globals.context = list;
				} else if (s.equalsIgnoreCase("recalc") || s.equalsIgnoreCase("recalculate")) {
					updateKeywords(collection);
					System.out.println("BOT> Done! Can I help with anything else?");
					globals.context = admin_else;
				} else if (s.equalsIgnoreCase("remove all")) {
					//add confirmation
					DBCursor cursor = collection.find(new BasicDBObject());
					for (int i = 0; i <= cursor.size(); i++) {
						collection.remove(cursor.next());
					}
					System.out.println("BOT> Removed all entries. Can I help with anything else?");
					globals.context = admin_else;
				//TODO add delete, list, and edit functionality
				} else {
					System.out.println("BOT> Did not recognise this command. Try again.");
					globals.context = none;
				}
			}
		}
	}

	/**
	 * Normal user interaction
	 * @param documents
	 * @param collection
	 * @throws JSONException
	 */

	public JSONObject normalIO(ArrayList<ArrayList<String>> documents, DBCollection collection, String questionAsked) throws JSONException {
		boolean quit = false;
		boolean admin = false;

		DBCursor cursor = collection.find(new BasicDBObject());
		JSONObject out = new JSONObject();
		if (cursor.size() == 0) {
			//if the database is empty on start, enable admin mode to add some records (this should never really run in theory)
			//System.out.println("BOT> Database is empty, enabling admin mode");
			out.put("type","error");
			out.put("content","empty");
			//quit = true;
			//admin = true;
		} else {
			getEntries(collection);
			//System.out.println("BOT> Hi, how can I help?");
		}
		//check input and match to given string
		//while (!quit) {
			String[] sList = questionAsked.split(" ");
			if (questionAsked.equals("quit") || questionAsked.equals("exit") || questionAsked.equals("q")) {
				quit = true;
			} else if (questionAsked.equals("admin")) {
				quit = true;
				admin = true;
			} else if (questionAsked.toLowerCase().equals("help") || questionAsked.toLowerCase().equals("?")) {
				System.out.println("BOT> Ask me a question or type admin to enter admin mode!");
			} else {
			    ArrayList<String> newDocument = new ArrayList<>();
				for (String word : sList) {
					newDocument.add(word.replaceAll("[^a-zA-Z0-9]", ""));
				}
				ArrayList<String> wordList = new ArrayList<>(newDocument);
				documents.add(new ArrayList<>(wordList));
				ArrayList<String> keyWords = new ArrayList<>();
				keyWords = getKeyWords(documents,newDocument, globals.averageTF);
				int index = 0;
				int threshold = (int) (newDocument.size()*0.05);
				ArrayList<String> foundThreads = new ArrayList<>();
				ArrayList<Integer> indices = new ArrayList<>();
				for (ArrayList<String> keyWordsi : globals.keyList) {
					int matching = 0;
					for (String w : keyWords) {
						if (keyWordsi.contains(w.toLowerCase())) {
							matching++;
						}
					}
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
				        //Bot only found 1 match
						//TODO PRINT OUT ANSWER
                        threadid = globals.entries.get(indices.get(0)).threadid;
						answers = getAnswers(collection, threadid);
					} else {
				    	//Bot finds 2 matches
						System.out.println("BOT> I found some information in these threads!");
						int ind = 0;
						for (int i : indices) {
							System.out.println(ind + ") " + globals.entries.get(i).threadid);
							ind++;
						}
						System.out.println("BOT> " + ind++ + ") None of the above");
						boolean valid = false;
						//while (!valid) {
						//	System.out.println("BOT> Select an option.");
							//String ansLine = in.nextLine();
							//Integer selectAns = toInt(ansLine);
							//if (selectAns > ind && selectAns >= 0) {
							//	System.out.println("BOT> Choose one of the options provided");
							//} else {
							//	valid = true;
							//	if (selectAns == ind) {
							//		System.out.println("BOT> Ok, consider opening a new thread on Blackboard.");
							//		System.out.println("BOT> Can I help with anything else?");
							//		globals.context = user_else;
							//	} else {
							//		Integer docIndex = indices.get(selectAns);
							//		threadid = globals.entries.get(docIndex).threadid;
							//		answers = getAnswers(collection,threadid);
							//	}
						//	}
						//}
					}
					if (answers.size() > 0) {
						int ansIndex = 1;
						System.out.println("BOT> I found these answers:");
						JSONObject out = new JSONObject();
						out.put("type","answer");
						out.put("content",answers);
						for (String a : answers) {
							System.out.println("BOT> Answer " + ansIndex + " -- " + a);
							ansIndex++;
						}
						System.out.println("BOT> For more information, check the '" + threadid + "' thread");
					} else {
						System.out.println("BOT> I can't find an answer for this question because it hasn't been answered yet.");
					}
					System.out.println("BOT> Can I help with anything else?");
					globals.context = user_else;
				} else {
					out.put("type","no-answer");
					out.put("content","");
					System.out.println("BOT> Sorry, I don't have any information on that. Do you want to try again?");
				}
			}
		//}

		//goto admin interaction (check password goes here)
		if (admin) {
			adminIO(collection,documents);
		}
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

	//TODO optimise?? also fix
	public void updateKeywords(DBCollection collection) throws JSONException {
		System.out.println("Updating keywords");
		DBCursor cursorGather = collection.find(new BasicDBObject());
		ArrayList<ArrayList<String>> documents = new ArrayList<>();
		for (int i = 0; i < cursorGather.size(); i++) {
			DBObject theObj = cursorGather.next();
			String word = (String) theObj.get("body");
			//System.out.println(theObj.get("_id"));
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
				DBObject newEntry = new BasicDBObject("_id", theObj.get("_id"))
						.append("subthread", theObj.get("subthread"))
						.append("date", theObj.get("date"))
						.append("body", theObj.get("body"))
						.append("qa", theObj.get("qa"))
						.append("keywords", keywords);
				collection.remove(new BasicDBObject(new BasicDBObject("_id",theObj.get("_id"))));
				collection.insert(newEntry);
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

	public void getEntries(DBCollection collection) throws JSONException {
	    //clear entries to readd from mongo
		globals.entries.clear();
		DBCursor cursor = collection.find(new BasicDBObject());
		for (int i = 0; i < cursor.size(); i++) {
			DBObject currentDoc = cursor.next();
			String body = (String)currentDoc.get("body");
			//System.out.println(currentDoc);
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
			globals.entries.add(ent);
		}
	}
}
