package com.example.chatbot;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@SpringBootTest
public class ChatbotApplicationTests {

	@Autowired
	private ChatbotApplication app;


	@Test
	public void threadDeletion() {

		//insert dummy thread
		dummyThreadadd("144");

		//delete dummy thread
		dummyThreadDelete("144");

		//verify dummy thread has been deleted
		assert(!doesThreadExist("144"));
	}

	@Test
	public void addThreadTest(){
		Boolean addedorNot = false;
		dummyThreadadd("69");
		if(doesThreadExist("69")){
			addedorNot = true;
		}
		dummyThreadDelete("69");

		assert(addedorNot);

	}

	@Test
	public void emptyQuestionTest(){
		//bot should respond with no answer if the question is empty
		try {
			JSONObject response = app.normalIO(app.globals.documents, app.globals.collection, "");
			assert(response.get("type").equals("no-answer"));
		} catch (Exception e){
			System.out.println("Either normalIO execption or JSON execption");
		}


	}

	//adds dummy thread to AWS database using application function.
	public void dummyThreadadd(String id){
		DateFormat df = new SimpleDateFormat("dd/MM/yy");
		Date dateobj = new Date();
		try {
			app.adminAdd(app.globals.collection, app.globals.documents, id, "0", "dummy thread", df.format(dateobj), "a");
		} catch (Exception e) {
			System.out.println("Adding dummy thread failed");
		}
	}

	//uses chatbot application function to delete the specified thread
	public void dummyThreadDelete(String id){

		try {
			JSONObject ids = new JSONObject();
			ids.put("thread_id", id);
			ids.put("sub_id", 0);

			JSONArray jsonThreads = new JSONArray();
			jsonThreads.put(ids);

			for (int j = 0; j < jsonThreads.length(); j++) {
				JSONObject object = jsonThreads.getJSONObject(j);
				app.adminDelete(app.globals.collection, object);
			}

		} catch (Exception e) {
			System.out.println("Deletion failed");
		}
	}
	//uses chatbot application function to obtain all threads and verify that a specified thread exists or not
	public Boolean doesThreadExist(String id){
		Boolean exist = false;
		ArrayList<String> threadidlist = new ArrayList<>();
		try {
			threadidlist = app.getAllThreads(app.globals.collection);
		} catch (Exception e) {

		}

		for (String thread : threadidlist) {
			if(thread.equals(id)){
				exist= true;
			}
		}
		return exist;
	}
}

