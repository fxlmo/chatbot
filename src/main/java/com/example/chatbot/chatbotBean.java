package com.example.chatbot;

import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class chatbotBean {
    private final MongoDbFactory mongo;
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;

    @Autowired
    public chatbotBean(MongoDbFactory mongo) {
        this.mongo = mongo;
        this.database = mongo.getDb("chatbot");
        this.collection = database.getCollection("threads");
    }

    // ...

    public void getMongoDocuments() throws JSONException {
        FindIterable iterable = collection.find();
        MongoCursor cursor = iterable.cursor();
        while(cursor.hasNext()) {
            DBObject theObj = (DBObject) cursor.next();
            String body = (String)theObj.get("body");
            if (body != null) {
                ArrayList<String> newDoc = new ArrayList<>();
                for (String word : body.split(" ")) {
                    newDoc.add(word);
                }
                mongoDocument monDoc = new mongoDocument();
                monDoc.body = body;
                JSONObject obj = new JSONObject(theObj.get("_id").toString());
                String threadid = obj.get("thread_id").toString();
                String subid = obj.get("sub_id").toString();
                String subthreadid = (String)theObj.get("subthread");
                String qa = (String)theObj.get("qa");

                monDoc.threadid = threadid;
                monDoc.subid = subid;
                monDoc.qa = qa;
                monDoc.subthreadid = subthreadid;
                monDoc.keyWords = new ArrayList<>();
                monDoc.date = (String) theObj.get("date");
                System.out.println("chatbotBean> Thread id - " + threadid);
            }
        }

    }

    public void run(String... args) throws JSONException {
        getMongoDocuments();
    }

}
