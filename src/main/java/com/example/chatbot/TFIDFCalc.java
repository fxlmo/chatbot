package com.example.chatbot;

import java.util.List;

//Method of analysing how frequently each word occurs and in what 'document' (thread)
public class TFIDFCalc {

    // TERM FREQUENCY
    // Inputs:
    // - document: Whole sentence of text (thread in our case)
    // - word: Word of which we want the tf (term frequency of
    // Returns:
    // - The frequency the word appears in the document (ignoring the case)
    public int tf(List<String> document, String word) {
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
    public int idf(List<List<String>> documents, String word) {
        int result = 0;
        int n = 0; //number of documents with word in

        for (List<String> d : documents) {
            if (tf(d, word) > 0) {
                n++;
            }
        }
        result = (int)Math.log(documents.size()/n);

        return result;
    }

    // FULL FUNCTION
    // Inputs:
    // - documents: List of documents
    // - document: document containing word
    // - word: word that we want to check
    // Returns:
    // - The full tf-ifd of a word in a document with respect to some documents (threads)
    public int tfidf(List<List<String>> documents, List<String> document, String word) {
        return tf(document, word) * idf(documents, word)
    }
}
