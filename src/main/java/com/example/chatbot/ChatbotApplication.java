package com.example.chatbot;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;
import com.mongodb.MongoCredential;
import com.mongodb.MongoClientOptions;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

@SpringBootApplication
public class ChatbotApplication implements CommandLineRunner{

	@Autowired
	private CustomerRepository repository;

	public static void main(String[] args) {
		SpringApplication.run(ChatbotApplication.class, args);
	}

	public void run(String... args) throws Exception {

		//repository.deleteAll();

		// save a couple of customers
		//repository.save(new Customer("Alice", "Smith"));
		//repository.save(new Customer("Bob", "Smith"));

		// fetch all customers
		//System.out.println("Customers found with findAll():");
		//System.out.println("-------------------------------");
		//for (Customer customer : repository.findAll()) {
		//	System.out.println(customer);
		//}
		//System.out.println();

		// fetch an individual customer
		//System.out.println("Customer found with findByFirstName('Alice'):");
		//System.out.println("--------------------------------");
		//System.out.println(repository.findByFirstName("Alice"));

		//System.out.println("Customers found with findByLastName('Smith'):");
		//System.out.println("--------------------------------");
		//for (Customer customer : repository.findByLastName("Smith")) {
		//	System.out.println(customer);
		//}

		// Keyword stuff
		//loop through each document and calculate td-idf of each keyword
		// TODO: parse into thread id, date, etc. This part of the algorithm should only receive
		// TODO: the body of each thread to get keywords out

		

		ArrayList<ArrayList<String>> documents = new ArrayList<ArrayList<String>>();

		//collect the filenames of all of the files inside the './threads/' folder
		final File folder = new File("./threads/");
		documents = listFilesForFolder(folder);

		//remove punctuation from each word in the document (makes things easier to process)
		int i = 0;
		ArrayList<String> newDocument = new ArrayList<String>();
		for (ArrayList<String> document : documents) {
			for (String paras : document) {
				String[] wordList = paras.split(" ");
				for (String word : wordList) {
					newDocument.add(word.replaceAll("[^a-zA-Z0-9]", ""));
				}
			}
			documents.set(i, new ArrayList<>(newDocument));
			newDocument.clear();
			i++;
		}

		ArrayList<ArrayList<String>> keyList = new ArrayList<ArrayList<String>>();
		TFIDFCalc keywordCalc = new TFIDFCalc();
		for (int testIndex = 0; testIndex < documents.size(); testIndex++) {
			//System.out.println(documents.get(testIndex));
			ArrayList<String> keyWords = new ArrayList<>();
			double avg = 0;
			int n = 0;
			for (String word : documents.get(testIndex)) {
				double freq = keywordCalc.tfidf(documents, documents.get(testIndex), word);
				//System.out.println("TF-IDF of " + word + " = " + freq);
				avg += freq;
				n++;
				//Threshold is here, change accordingly (average of tf-idf seems to be around
				//1.5-2.5 depending on the document, but this will change when we add more.
				if (freq > 2) {
					keyWords.add(word);
				}
			}
			ArrayList<String> tempKeyWords = new ArrayList<>();
			for (String word : keyWords) {
				if (!tempKeyWords.contains(word)) {
					tempKeyWords.add(word.toLowerCase());
				}
			}
			keyList.add(tempKeyWords);
		}
		boolean quit = false;
		System.out.println("Hi, how can I help?");
		while (!quit) {
			Scanner in = new Scanner(System.in);
			String s = in.nextLine();
			String[] sList = s.split(" ");
			if (s.equals("quit")) {
				quit = true;
			} else {
				for (String word : sList) {
					newDocument.add(word.replaceAll("[^a-zA-Z0-9]", ""));
				}
				ArrayList<String> wordList = new ArrayList<>(newDocument);
				documents.add(new ArrayList<>(wordList));
				ArrayList<String> keyWords = new ArrayList<>();
				for (String w : wordList) {
					double freq = keywordCalc.tfidf(documents, wordList, w);
					//Threshold is here, change accordingly (average of tf-idf seems to be around
					//1.5-2.5 depending on the document, but this will change when we add more.
					if (freq > 1) {
						keyWords.add(w.toLowerCase());
					}
				}
				int currentMax, index, maxMatch;
				index = currentMax = maxMatch = 0;
				for (ArrayList<String> keyWordsi : keyList) {
					int matching = 0;
					for (String w : keyWords) {
						if (keyWordsi.contains(w.toLowerCase())) {
							matching++;
						}
					}
					if (matching > maxMatch) {
						currentMax = index;
					}
					index++;
				}
				System.out.println("most closely matching thread is " + documents.get(currentMax));
			}
		}
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

}
