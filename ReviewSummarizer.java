import com.google.cloud.language.v1.*;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.PartOfSpeech.Tag;

import java.util.List;
import java.util.ArrayList;
import java.io.*;

public class ReviewSummarizer 
{
	private final String REVIEW;
	public Entity  LARGEST_SALIENCE_ENTITY;
	
	public ReviewSummarizer(String rev)
	{
		this.REVIEW = rev;
		
		try
		{
			this.LARGEST_SALIENCE_ENTITY = this.getLargestSalienceEntity();
		}
		catch(Exception e)
		{
			this.LARGEST_SALIENCE_ENTITY = null;
		}
	}
	
	String getAttitude() throws Exception
	{
		Document doc = Document.newBuilder().setContent(this.REVIEW).setType(Type.PLAIN_TEXT).build();
		
		try (LanguageServiceClient language = LanguageServiceClient.create())
		{
			Sentiment sentiment = language.analyzeSentiment(doc).getDocumentSentiment();
			
			Float sentiment_score = sentiment.getScore();
			
			if(sentiment_score < -0.3f)
				return "Negative";
			else if(sentiment_score >= -0.3f && sentiment_score <= 0.3f)
				return "Neutral";
			else
				return "Positive";
		}
	}
	
	public Float getSalienceRelativeToDocument(Entity e) throws Exception
	{
		Document doc = Document.newBuilder().setContent(this.REVIEW).setType(Type.PLAIN_TEXT).build();
		
		try (LanguageServiceClient language = LanguageServiceClient.create())
		{
			AnalyzeEntitySentimentRequest request = AnalyzeEntitySentimentRequest.newBuilder()
					.setDocument(doc)
					.setEncodingType(EncodingType.UTF16).build();
			
			AnalyzeEntitySentimentResponse response = language.analyzeEntitySentiment(request);
			
			for(Entity ent : response.getEntitiesList())
			{
				if(ent.getName().equals(e.getName()))
					return ent.getSalience();
			}
			
			return -1.0f;
		}
	}
	
	private Entity getLargestSalienceEntity() throws Exception
	{
		Document doc = Document.newBuilder().setContent(this.REVIEW).setType(Type.PLAIN_TEXT).build();
		
		try (LanguageServiceClient language = LanguageServiceClient.create())
		{
			AnalyzeEntitySentimentRequest request = AnalyzeEntitySentimentRequest.newBuilder()
					.setDocument(doc)
					.setEncodingType(EncodingType.UTF16).build();
			
			
			AnalyzeEntitySentimentResponse response = language.analyzeEntitySentiment(request);
			
			List<Entity> entity_list = response.getEntitiesList();
			
			//response.
			Entity mostRelevant = null;
			
			for (Entity entity : entity_list) 
			{
				if(mostRelevant == null)
					mostRelevant = entity;
				else
				{
					if(entity.getSalience() > mostRelevant.getSalience())
						mostRelevant = entity;
				}
			}
			
			return mostRelevant;
		}
	}
	
	List<Token> getAdjectivesInSentence(String sentence) throws Exception
	{
		Document doc = Document.newBuilder().setContent(sentence).setType(Type.PLAIN_TEXT).build();
		
		try (LanguageServiceClient language = LanguageServiceClient.create())
		{
			AnalyzeSyntaxRequest syntax_request = 
			AnalyzeSyntaxRequest.newBuilder()
	    		      			.setDocument(doc)
	    		      			.setEncodingType(EncodingType.UTF16)
	    		      			.build();
	    	// analyze the syntax in the given text
	    	AnalyzeSyntaxResponse syntax_response = language.analyzeSyntax(syntax_request);
	    	// print the response
	    	
	    	List<Token> tokens = syntax_response.getTokensList();
	    	
	    	List<Token> adjectives = new ArrayList<Token>();
	    	
	    	for(Token token : tokens)
	    	{
	    		if(token.getPartOfSpeech().getTag() == Tag.ADJ)
	    			adjectives.add(token);
	    		/*
	    		else if(token.getPartOfSpeech().getTag() == Tag.ADV)
	    		{
	    			if(adjectives.size() > 0 && adjectives.get(adjectives.size() - 1).getPartOfSpeech().getTag() == Tag.ADJ)
	    				adjectives.add(token);
	    		}
	    		*/
	    	}
	    	
	    	return adjectives;
		}
	}
	
	Entity getSubjectInSentence(String sentence) throws Exception
	{
		try (LanguageServiceClient language = LanguageServiceClient.create())
		{
			Document doc = Document.newBuilder()
			          .setContent(sentence).setType(Type.PLAIN_TEXT).build();
			
			AnalyzeEntitySentimentRequest request = AnalyzeEntitySentimentRequest.newBuilder()
					.setDocument(doc)
					.setEncodingType(EncodingType.UTF16).build();
			
			AnalyzeEntitySentimentResponse response = language.analyzeEntitySentiment(request);
			
			List<Entity> entity_list = response.getEntitiesList();
			Entity mostRelevant = null;
			
			for (Entity entity : entity_list) 
			{
				if(mostRelevant == null)
					mostRelevant = entity;
				else
				{
					if(entity.getSalience() > mostRelevant.getSalience())
						mostRelevant = entity;
				}
			}
			
			return mostRelevant;
		}
	}
	
	//Returns Tuple[] with format -> <Category name, Category confidence>
	public List<Tuple<String, String>> getClassifications() throws Exception
	{
		try (LanguageServiceClient language = LanguageServiceClient.create())
		{
			Document doc = Document.newBuilder()
			          .setContent(this.REVIEW).setType(Type.PLAIN_TEXT).build();
			
			ClassifyTextRequest classify_request = ClassifyTextRequest.newBuilder()
	  		      .setDocument(doc)
	  		      .build();
	  	
			// detect categories in the given text
			ClassifyTextResponse classify_response = language.classifyText(classify_request);
			
			List<Tuple<String, String>> classifications = new java.util.LinkedList<Tuple<String, String>>();
			
			for (ClassificationCategory category : classify_response.getCategoriesList()) 
				classifications.add(new Tuple(category.getName(), category.getConfidence()));
			
			
			return classifications;
		}
	}
	
	static List<Tuple<String, String>> classify(String text) throws Exception
	{
		try (LanguageServiceClient language = LanguageServiceClient.create())
		{
			Document doc = Document.newBuilder()
			          .setContent(text).setType(Type.PLAIN_TEXT).build();
			
			ClassifyTextRequest classify_request = ClassifyTextRequest.newBuilder()
	  		      .setDocument(doc)
	  		      .build();
	  	
			// detect categories in the given text
			ClassifyTextResponse classify_response = language.classifyText(classify_request);
			
			List<Tuple<String, String>> classifications = new java.util.LinkedList<Tuple<String, String>>();
			
			for (ClassificationCategory category : classify_response.getCategoriesList()) 
				classifications.add(new Tuple(category.getName(), category.getConfidence()));
			
			return classifications;
		}
	}
	
	//Tuple is in accordance with format: <Adj, Noun>
	static String createSummary(List<Tuple<String, String>> vals)
	{
		StringBuilder summary = new StringBuilder();
		
		for(int i = 0; i < vals.size(); i++)
		{
			//If there is an adjective and noun
			if(vals.get(i).x != null && vals.get(i).y != null)
				summary.append("-" + vals.get(i).x + " " + vals.get(i).y + "\n");
		}
		
		return summary.toString();
	}
	
	List<Tuple<String, String>> filterSummaryPoints(List<Tuple<String, String>> vals) throws Exception
	{
		try 
		{
			//Classifications in form <Name, weight>
			List<Tuple<String, String>> doc_Classifications = this.getClassifications();
			
			List<Tuple<String, String>> filtered_Summaries = new java.util.LinkedList<Tuple<String, String>>();
			
			for(Tuple<String, String> t: vals)
			{
				int matches = 0;
				
				//Combine adjective + noun into phrase
				String cString = t.x + " " + t.y;
				
				List<Tuple<String, String>> cString_classifications = ReviewSummarizer.classify(cString);
				
				for(Tuple<String, String> t1: doc_Classifications)
				{
					for(Tuple<String, String> c: cString_classifications)
					{
						//Only if confidence is above 0.5
						if(Float.parseFloat(t1.y) > 0.5)
						{
							if(c.x.equals(t1.x))
								matches++;
						}
					}
				}
				
				if( matches >= 1 )
					filtered_Summaries.add(t);
			}
			
			return filtered_Summaries;
		}
		finally
		{
			
		}
	}
}
