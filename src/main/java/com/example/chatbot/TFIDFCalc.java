package com.example.chatbot;

import java.util.ArrayList;

//Method of analysing how frequently each word occurs and in what 'document' (thread)
//Returned value will be higher if it appears often in the current document, and not often in others
//Returned value will be lower if it appears in a lot of documents (ie is not unique)
public class TFIDFCalc {

    /** TERM FREQUENCY
     * @param document  Whole sentence of the thread
     * @param word      Word we want the tf of
     * @return          Frequency of word in document (case insensitive)
     */
    public int tf(ArrayList<String> document, String word) {
        int result = 0;
        for (String w : document)  {
            if (word.equalsIgnoreCase(w)) {
                result++;
            }
        }

        return result;
    }

    /**
     * INVERSE DOCUMENT FREQUENCY
     * @param documents List of documents to check
     * @param word      Word we want to test the idf of
     * @return          Relative frequency of word in other documents
     */
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

    /**
     * FULL FUNCTION
     * @param documents List of documents
     * @param document  Document containing the word to check
     * @param word      Word to check
     * @return          Full TF-IFD of a word in a document with respect to some documents
     */
    public double tfidf(ArrayList<ArrayList<String>> documents, ArrayList<String> document, String word) {
        return tf(document, word) * idf(documents, word);
    }
}
