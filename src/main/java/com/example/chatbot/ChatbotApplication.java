package com.example.chatbot;

import com.ChatbotController.ChatbotController;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
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

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

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

		//Mongo stuff
		MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://admin:chatbot123@ec2-54-198-1-3.compute-1.amazonaws.com:27017/?authSource=admin&authMechanism=SCRAM-SHA-1"));
		//This will flag as deprecated but it's fine I promise
		DB database = mongoClient.getDB("chatbot");
		//Specify the collection we're using (it's called threads in this)
		DBCollection collection = database.getCollection("threads");

		ArrayList<ArrayList<String>> documents = new ArrayList<ArrayList<String>>();
		ArrayList<mongoDocument> mongoDocuments = new ArrayList<>();

		//GET ALL FILES FROM MONGO
		DBCursor cursor = collection.find(new BasicDBObject());
		for (int i = 0; i < cursor.size(); i++) {
			DBObject theObj = cursor.next();
			String body = (String)theObj.get("body");
			if (body != null) {
				ArrayList<String> newDoc = new ArrayList<>();
				for (String word : body.split(" ")) {
					newDoc.add(word);
				}
				documents.add(newDoc);
				mongoDocument monDoc = new mongoDocument();
				monDoc.body = body;
				JSONObject obj = new JSONObject(theObj.get("_id").toString());
				String threadid = obj.get("thread_id").toString();
				String subid = obj.get("sub_id").toString();
				String subthreadid = (String)theObj.get("subthread");
				String qa = (String)theObj.get("qa");

				monDoc.threadid = threadid;
				globals.threadList.add(threadid);
				monDoc.subid = subid;
				monDoc.qa = qa;
				monDoc.subthreadid = subthreadid;
				monDoc.keyWords = new ArrayList<>();
				monDoc.date = (String) theObj.get("date");
				mongoDocuments.add(monDoc);
			}
		}
		//Set up globals
		globals.mongoDocuments = mongoDocuments;

		//Calculate all keywords for the mongo entries
		TFIDFCalc keywordCalc = new TFIDFCalc();
		int ind = 0;
		for (int testIndex = 0; testIndex < documents.size(); testIndex++) {
			//System.out.println(documents.get(testIndex));
			HashMap<String, Double> keyWords = new HashMap<>();
			double avg = 0;
			int i = 0;
			for (String word : documents.get(testIndex)) {
				double freq = keywordCalc.tfidf(documents, documents.get(testIndex), word);
				//System.out.println("TF-IDF of " + word + " = " + freq);
				i++;
				avg += freq;
				if (!keyWords.containsKey(word)) {
					keyWords.put(word, freq);
				}
			}
			avg = avg/i;
			//This flag is the global average for all of the keywords. I use this as a threshold.
			globals.averageTF = avg;

			ArrayList<String> tempKeyWords = new ArrayList<>();
			for (String word : keyWords.keySet()) {
				//Threshold is here, change accordingly (average of tf-idf seems to be around
				//1.5-2.5 depending on the document, but this will change when we add more.
				if (keyWords.get(word) > avg) {
				    String keyWord = word.toLowerCase().replaceAll("[^a-zA-Z0-9]","");
				    if (keyWord != null) {
						tempKeyWords.add(keyWord);
					}
				}
			}
			DBObject query = new BasicDBObject("_id", new BasicDBObject("thread_id", globals.threadList.get(ind))
														.append("sub_id", mongoDocuments.get(ind).subid));
			DBObject update = new BasicDBObject("keywords", tempKeyWords);
			collection.update(query, update);
			globals.mongoDocuments.get(testIndex).keyWords = tempKeyWords;
			globals.keyList.add(tempKeyWords);
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
		ArrayList<String> wordList = new ArrayList<>(newDocument);
		documents.add(new ArrayList<>(wordList));
		ArrayList<String> keyWords = new ArrayList<>();
		for (String w : wordList) {
			double freq = keywordCalc.tfidf(documents, wordList, w);
			//Threshold is here, change accordingly (average of tf-idf seems to be around
			//1.5-2.5 depending on the document, but this will change when we add more.
			if (freq > threshold) {
				keyWords.add(w.toLowerCase().replaceAll("[^a-zA-Z0-9]",""));
			}
		}
	   	return keyWords;
	}

	/**
	 * @param folder 	where you want to search
	 * @return 		 	All files inside the folder
	 */
	public ArrayList<ArrayList<String>> listFilesForFolder(final File folder) {
		ArrayList<ArrayList<String>> documents = new ArrayList<ArrayList<String>>();
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
			    //read each file read into document structure
				ArrayList<String> document = new ArrayList<String>();
				try {
					BufferedReader br = new BufferedReader(new FileReader(fileEntry));

					String st;
					while ((st = br.readLine()) != null) {
						document.add(st);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				documents.add(document);
				//System.out.println(fileEntry.getName());
			}
		}
		return documents;
	}

	public static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			double d = Double.parseDouble(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

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

	public void adminIO(DBCollection collection, ArrayList<ArrayList<String>> documents) throws JSONException {
		//handle admin
		boolean quit = false;
		System.out.println("BOT> Welcome admin user! What do you want to do?");
		while (!quit) {
			Scanner in = new Scanner(System.in);
			String s = in.nextLine();
			if (s.equalsIgnoreCase("add")) {
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
					//TODO check if input is actually a date
					if(!isValidDate(date)) {
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
				while(!valid) {
					System.out.println("BOT> Is this a question (q) or an answer (a)?");
					qa = in.nextLine().toLowerCase();
					if (qa.equals("q") || qa.equals("a")) {
						valid = true;
					} else {
						System.out.println("BOT> Please enter either 'q' or 'a'");
					}
				}

				ArrayList<String> convBody = new ArrayList<String>();
				for (String word : body.split(" ")) {
					convBody.add(word.toLowerCase().replaceAll("[^a-zA-Z0-9]", ""));
				}

				ArrayList<String> keyWords = getKeyWords(documents,convBody, globals.averageTF);
				int subid = getThreadSize(collection, threadid);

				//TODO process body into keywords
				DBObject newEntry = new BasicDBObject("_id", new BasicDBObject("thread_id", threadid)
                        									.append("sub_id",subid))
						.append("subthread", subthreadId)
						.append("date", date)
						.append("body", body)
						.append("qa", qa)
						.append("keywords", keyWords);
				collection.insert(newEntry);
				globals.keyList.add(keyWords);
				globals.threadList.add(threadid);
				// Recalculate keywords
				updateKeywords(collection);
				System.out.println("BOT> Inserted new value! Do you want to do anything else?");
			} else if (s.equals("quit")) {
				System.out.println("BOT> Ok, bye. [RETURNING TO MAIN USER]");
				//TODO update locally held records??
				quit = true;
				normalIO(documents, collection);
			} else if (s.equals("help")) {
				System.out.println("BOT> Type 'add' to add a new record, or quit to return to the main user");
			} else {
				System.out.println("BOT> Did not recognise this command. Try again.");
			}
		}
	}

	public void normalIO(ArrayList<ArrayList<String>> documents, DBCollection collection) throws JSONException {
		boolean quit = false;
		boolean admin = false;

		DBCursor cursor = collection.find(new BasicDBObject());
		if (cursor.size() == 0) {
			System.out.println("BOT> Database is empty, enabling admin mode");
			quit = true;
			admin = true;
		} else {
			getMongoDocuments();
			System.out.println("BOT> Hi, how can I help?");
		}
		TFIDFCalc keyWordCalc = new TFIDFCalc();
		while (!quit) {
			Scanner in = new Scanner(System.in);
			String s = in.nextLine();
			String[] sList = s.split(" ");
			if (s.equals("quit")) {
				quit = true;
			} else if (s.equals("admin")) {
				quit = true;
				admin = true;
			} else if (s.toLowerCase().equals("help") || s.toLowerCase().equals("?")) {
				System.out.println("BOT> Ask me a question or type admin to enter admin mode!");
			} else {
			    ArrayList<String> newDocument = new ArrayList<>();
				for (String word : sList) {
					newDocument.add(word.replaceAll("[^a-zA-Z0-9]", ""));
				}
				ArrayList<String> wordList = new ArrayList<>(newDocument);
				documents.add(new ArrayList<>(wordList));
				ArrayList<String> keyWords = new ArrayList<>();
				keyWords = getKeyWords(documents,newDocument, 1);
				int index;
				index = 0;
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
					if (matching > threshold && !foundThreads.contains(globals.mongoDocuments.get(index).threadid)) {
						indices.add(index);
						foundThreads.add(globals.mongoDocuments.get(index).threadid);
					}
					index++;
				}
				if (indices.size() > 0) {
				    ArrayList<String> answers = new ArrayList<>();
				    String threadid = "";
				    String subthread = "";
				    if (indices.size() == 1) {
				        //Bot only found 1 match
						//TODO PRINT OUT ANSWER
                        threadid = globals.mongoDocuments.get(indices.get(0)).threadid;
                        subthread = globals.mongoDocuments.get(indices.get(0)).subthreadid;
						answers = getAnswers(collection, threadid);
					} else {
				    	//Bot finds 2 matches
						System.out.println("BOT> I found some information in these threads!");
						int ind = 0;
						for (int i : indices) {
							System.out.println(ind + ") " + globals.mongoDocuments.get(i).threadid);
							ind++;
						}
						System.out.println("BOT> " + ind++ + ") None of the above");
						boolean valid = false;
						while (!valid) {
							System.out.println("BOT> Select an option.");
							String ansLine = in.nextLine();
							Integer selectAns = toInt(ansLine);
							if (selectAns > ind && selectAns >= 0) {
								System.out.println("BOT> Choose one of the options provided");
							} else {
								valid = true;
								if (selectAns == ind) {
									System.out.println("BOT> Ok, consider opening a new thread on Blackboard.");
									System.out.println("BOT> Can I help with anything else?");
								} else {
									Integer docIndex = indices.get(selectAns);
									threadid = globals.mongoDocuments.get(docIndex).threadid;
									answers = getAnswers(collection,threadid);
								}
							}
						}
					}
					if (answers.size() > 0) {
						int ansIndex = 1;
						System.out.println("BOT> I found these answers:");
						for (String a : answers) {
							System.out.println("BOT> Answer " + ansIndex + " -- " + a);
							ansIndex++;
						}
						System.out.println("BOT> For more information, check the '" + threadid + "' thread");
					} else {
						System.out.println("BOT> I can't find an answer for this question because it hasn't been answered yet.");
					}
					System.out.println("BOT> Can I help with anything else?");
				} else {
					System.out.println("BOT> Sorry, I don't have any information on that. Do you want to try again?");
				}
			}
		}


		//handle admin
		if (admin) {
			adminIO(collection,documents);
		}
	}

	public Integer getThreadSize(DBCollection collection, String threadid) {
		DBCursor cursor = collection.find(new BasicDBObject("_id.thread_id",threadid));
		return cursor.size();
	}

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

	//TODO optimise??
	public void updateKeywords(DBCollection collection) {
		DBCursor cursorGather = collection.find(new BasicDBObject());
		ArrayList<ArrayList<String>> documents = new ArrayList<>();
		for (int i = 0; i < cursorGather.size(); i++) {
			DBObject theObj = cursorGather.next();
			String word = (String) theObj.get("body");
			ArrayList<String> convBody = new ArrayList<String>();
			for (String w : word.split(" ")) {
				convBody.add(w.toLowerCase().replaceAll("[^a-zA-Z0-9]", ""));
			}
			documents.add(convBody);
		}
		DBCursor cursor = collection.find(new BasicDBObject());
		for (int i = 0; i < cursor.size(); i++) {
			DBObject theObj = cursor.next();

			String[] document = ((String) theObj.get("body")).split(" ");
			//TODO change threshold

			ArrayList<String> keywords = getKeyWords(documents, new ArrayList<String>(Arrays.asList(document)),1.5);
			theObj.put("keywords", keywords);
			DBObject newEntry = new BasicDBObject("_id", theObj.get("_id"))
					.append("subthread", theObj.get("subthreadId"))
					.append("date", theObj.get("date"))
					.append("body", theObj.get("body"))
					.append("qa", theObj.get("qa"))
					.append("keywords", keywords);
			collection.remove(new BasicDBObject(new BasicDBObject("_id",theObj.get("_id"))));
			collection.insert(newEntry);
		}
	}

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

	public void getMongoDocuments() throws JSONException {
		FindIterable iterable = collectionAmazon.find();
		MongoCursor cursor = iterable.cursor();
		while(cursor.hasNext()) {
			Document theObj = (Document) cursor.next();
			System.out.println(theObj);
			JSONObject jsonObject = new JSONObject(theObj);
			System.out.println(jsonObject.get("_id"));
			try {
				JSONObject id = new JSONObject(jsonObject.getString("_id"));
				System.out.println(id.get("thread_id"));
			} catch (Exception ex) {

			}

			//String threadid = id.getString("thread_id");
			//if (threadid != null) {
			//	System.out.println(id.get("thread_id"));
			//}

		}

	}
}
