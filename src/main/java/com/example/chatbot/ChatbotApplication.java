package com.example.chatbot;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
		// TODO: parse into thread id, date, etc. This part of the algorithm should only receive
		// TODO: the body of each thread to get keywords out
		ArrayList<ArrayList<String>> documents = new ArrayList<ArrayList<String>>();
		final File folder = new File("./threads/");
		documents = listFilesForFolder(folder);

		//loop through each document and calculate td-idf of each keyword (in theory)
		// for now I'm just going to look at one document and see what happens

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

		TFIDFCalc keywordCalc = new TFIDFCalc();
		final int testIndex = 4;
		System.out.println(documents.get(testIndex));
		ArrayList<String> keyWords = new ArrayList<>();
		for (String word : documents.get(testIndex)) {
			double freq = keywordCalc.tfidf(documents, documents.get(testIndex), word);
			System.out.println("TF-IDF of " + word + " = " + freq);
			if (freq > 1.5) {
				keyWords.add(word);
			}
		}

		System.out.println("keywords are " + keyWords);
	}

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
