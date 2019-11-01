package com.example.chatbot;



import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.FileInputStream;
import java.lang.Object;
import java.util.Iterator;
import org.apache.poi.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

@SpringBootApplication
public class ChatbotApplication {

	public static void main(String[] args) {
		//SpringApplication.run(ChatbotApplication.class, args);
		questionAnswerReader();
	}

	public static Map<String, String> questionAnswerReader(){

		Map<String, String> questionsandAnswers = new HashMap<>();

		try {
			FileInputStream file = new FileInputStream(new File("testfile.xlsx"));
			XSSFWorkbook book = new XSSFWorkbook(file);
			XSSFSheet sheet = book.getSheetAt(0);

			Iterator<Row> rowIterator = sheet.iterator();

			while(rowIterator.hasNext()){
				Row currentRow = rowIterator.next();
				Iterator<Cell> cellIterator = currentRow.cellIterator();
				String question = "";
				String answer = "";

				while(cellIterator.hasNext()){
					Cell currentCell = cellIterator.next();

					if(question.equals("")){
						question = currentCell.getStringCellValue();
					} else{
						answer = currentCell.getStringCellValue();
					}

				}
				questionsandAnswers.put(question, answer);


			}
		} catch(Exception f){
			f.printStackTrace();
		}

		for (Map.Entry<String, String> entry : questionsandAnswers.entrySet()){
			System.out.println("Question: " + entry.getKey() + "\n" + "Answer: " + entry.getValue() + "\n");

		}
		return questionsandAnswers;
	}

}
