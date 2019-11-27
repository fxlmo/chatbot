package com.example.chatbot;

import java.util.ArrayList;

//Method of analysing how frequently each word occurs and in what 'document' (thread)
//Returned value will be higher if it appears often in the current document, and not often in others
//Returned value will be lower if it appears in a lot of documents (ie is not unique)
public class TFIDFCalc {

    // TERM FREQUENCY
    // Inputs:
    // - document: Whole sentence of text (thread in our case)
    // - word: Word of which we want the tf (term frequency of
    // Returns:
    // - The frequency the word appears in the document (ignoring the case)
    public int tf(ArrayList<String> document, String word) {
        int result = 0;
        for (String w : document)  {
            if (word.equalsIgnoreCase(w)) {
                result++;
            }
        }

        return result;
    }

    // INVERSE DOCUMENT FREQUENCY
    // Inputs:
    // - documents: List of documents to check
    // - word: Word of which we want to test the idf of
    // Returns:
    // - The relative frequency of a word in other documents (kind of like how often it appears in other documents)
    public double idf(ArrayList<ArrayList<String>> documents, String word) {
        double result = 0;
        int n = 0; //number of documents with word in

        for (ArrayList<String> d : documents) {
            if (tf(d, word) > 0) {
                n++;
            }
        }
        result = Math.log(documents.size()/n);

        return result;
    }

    // FULL FUNCTION
    // Inputs:
    // - documents: List of documents
    // - document: document containing word
    // - word: word that we want to check
    // Returns:
    // - The full tf-ifd of a word in a document with respect to some documents (threads)
    public double tfidf(ArrayList<ArrayList<String>> documents, ArrayList<String> document, String word) {
        return tf(document, word) * idf(documents, word);
    }
}
