// Imports the Google Cloud client library
/*
import com.google.cloud.language.v1.AnalyzeEntitySentimentResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.EntityMention;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.cloud.language.v1beta2.AnalyzeEntitySentimentRequest;
*/
import com.google.cloud.language.v1.*;
import com.google.cloud.language.v1.Document.Type;
import java.util.List;
import java.io.*;

public class Main 
{

	public static void main(String... args) throws Exception 
	{
		FileInputStream in = null;
    	StringBuilder doc_text = new StringBuilder();
    	
    	try
    	{
    		int c;
    		
    		in = new FileInputStream(args[0]);
    		
    		while((c = in.read()) != -1)
    		{
    			doc_text.append((char)c);
    		}
    	}
    	finally
    	{
    		if( in != null )
    			in.close();
    		else
    		{
    			System.out.println("Error reading from input file");
    			return;
    		}
    	}
    	
    	//Parse sentences in different way
    	String[] sentences = doc_text.toString().split("(?<=[a-z])\\.\\s+");
    	
		List<Tuple<String, String>> adj_nouns = new java.util.LinkedList<Tuple<String, String>>();
		
		ReviewSummarizer rs = new ReviewSummarizer(doc_text.toString());
    	
    	for(int sentence_counter = 0; sentence_counter < sentences.length; sentence_counter++)
    	{
    		try
    		{
    			//Print only once
    			if(sentence_counter == 0)
    			{	
        			System.out.println("MOST RELEVANT ENTITY IN DOCUMENT: " + ((rs.LARGEST_SALIENCE_ENTITY == null) ? "None" : rs.LARGEST_SALIENCE_ENTITY.getName()));
        			System.out.println("value: " + ((rs.LARGEST_SALIENCE_ENTITY == null) ? "None" : rs.LARGEST_SALIENCE_ENTITY.getSalience()));
    			}
    			
    			Entity subject = rs.getSubjectInSentence(sentences[sentence_counter]);
    			System.out.println("Sentence " + sentence_counter + ":");
    			
    			if(subject != null)
    			{
	    			Float  salience_subject_relative = rs.getSalienceRelativeToDocument(subject);
	    			
	    			System.out.println("--Subject of sentence: " + ((subject == null) ? "None" : subject.getName()));
	    			System.out.println("Salience of this entity relative to document: " + ((subject == null) ? "None" : salience_subject_relative));
    			}
    			else
    			{
    				System.out.println("No subject content");
    			}
    			
    			System.out.print("--Adjectives: ");
    			
    			List<Token> adjectives = rs.getAdjectivesInSentence(sentences[sentence_counter]);
    			
    			if(adjectives.isEmpty())
    				System.out.print("None\n");
    			else
    			{
    				for(Token adjective : adjectives)
    					System.out.print(adjective.getText().getContent() + ", ");
    				
    				System.out.println();
    			}
    			
    			Float min_Salience = rs.LARGEST_SALIENCE_ENTITY.getSalience() / 4;
    			
    			//if(subject.getSentiment().getScore() >= 0)
    			
    			StringBuilder adj_descriptor = new StringBuilder();
    			
    			for(int i = 0; i < adjectives.size(); i++)
    			{
    				adj_descriptor.append(adjectives.get(i).getText().getContent());
    				
    				if(i != (adjectives.size() - 1))
    					adj_descriptor.append(", ");
    			}
    			
    			//adj_nouns.add(new Tuple((adjectives.isEmpty()) ? null : adjectives.get(0).getText().getContent(), (subject == null) ? null : subject.getName()));
    			
    			adj_nouns.add(new Tuple((adj_descriptor.length() == 0) ? null : adj_descriptor.toString(), (subject == null) ? null : subject.getName()));
    		}
    		finally
    		{
    			
    		}
    	}
    	
    	System.out.println("Summary: ");
    	//System.out.println(ReviewSummarizer.createSummary(rs.filterSummaryPoints(adj_nouns)));
    	System.out.println(ReviewSummarizer.createSummary(adj_nouns));
    	System.out.println("Overall attitude: " + rs.getAttitude());
    	
    	
		/*
	    // Instantiates a client
	    try (LanguageServiceClient language = LanguageServiceClient.create()) 
	    {
	    	
	    	FileInputStream in = null;
	    	StringBuilder doc_text = new StringBuilder();
	    	
	    	try
	    	{
	    		int c;
	    		
	    		in = new FileInputStream(args[0]);
	    		
	    		while((c = in.read()) != -1)
	    		{
	    			doc_text.append((char)c);
	    		}
	    	}
	    	finally
	    	{
	    		if( in != null )
	    			in.close();
	    		else
	    		{
	    			System.out.println("Error reading from input file");
	    			return;
	    		}
	    	}
	    	
	    	if(doc_text.length() == 0)
	    		return;
	    	
	    	Document doc = Document.newBuilder()
	          .setContent(doc_text.toString()).setType(Type.PLAIN_TEXT).build();

	    	// Detects the sentiment of the text
	    	Sentiment sentiment = language.analyzeSentiment(doc).getDocumentSentiment();
	    	
	    	AnalyzeEntitySentimentRequest request = AnalyzeEntitySentimentRequest.newBuilder()
	    											.setDocument(doc)
	    											.setEncodingType(EncodingType.UTF16).build();
	    	
	    	AnalyzeEntitySentimentResponse response = language.analyzeEntitySentiment(request);
	    	// Print the response
	    	  for (Entity entity : response.getEntitiesList()) 
	    	  {
	    	    System.out.printf("Entity: %s\n", entity.getName());
	    	    System.out.printf("Salience: %.3f\n", entity.getSalience());
	    	    System.out.printf("Sentiment : %s\n", entity.getSentiment());
	    	    System.out.printf("Descriptor: %s\n", entity.getDescriptor().getFullName());
	    	    
	    	    for (EntityMention mention : entity.getMentionsList()) 
	    	    {
	    	      System.out.printf("Begin offset: %d\n", mention.getText().getBeginOffset());
	    	      System.out.printf("Content: %s\n", mention.getText().getContent());
	    	      System.out.printf("Magnitude: %.3f\n", mention.getSentiment().getMagnitude());
	    	      System.out.printf("Sentiment score : %.3f\n", mention.getSentiment().getScore());
	    	      System.out.printf("Type: %s\n\n", mention.getType());
	    	    }
	    	  }

	    	System.out.printf("Text: %s%n", doc_text.toString());
	    	System.out.printf("Sentiment: %s, %s%n", sentiment.getScore(), sentiment.getMagnitude());
	    	
	    	ClassifyTextRequest classify_request = ClassifyTextRequest.newBuilder()
	    		      .setDocument(doc)
	    		      .build();
	    	
	    	// detect categories in the given text
	    	ClassifyTextResponse classify_response = language.classifyText(classify_request);

	    	for (ClassificationCategory category : classify_response.getCategoriesList()) 
	    	{
	    		    System.out.printf("Category name : %s, Confidence : %.3f\n",
	    		        category.getName(), category.getConfidence());
	    	}
	    	
	    	  AnalyzeSyntaxRequest syntax_request = AnalyzeSyntaxRequest.newBuilder()
	    		      .setDocument(doc)
	    		      .setEncodingType(EncodingType.UTF16)
	    		      .build();
	    		  // analyze the syntax in the given text
	    		  AnalyzeSyntaxResponse syntax_response = language.analyzeSyntax(syntax_request);
	    		  // print the response
	    		  for (Token token : syntax_response.getTokensList()) {
	    		    System.out.printf("\tText: %s\n", token.getText().getContent());
	    		    System.out.printf("\tBeginOffset: %d\n", token.getText().getBeginOffset());
	    		    System.out.printf("Lemma: %s\n", token.getLemma());
	    		    System.out.printf("PartOfSpeechTag: %s\n", token.getPartOfSpeech().getTag());
	    		    System.out.printf("\tAspect: %s\n", token.getPartOfSpeech().getAspect());
	    		    System.out.printf("\tCase: %s\n", token.getPartOfSpeech().getCase());
	    		    System.out.printf("\tForm: %s\n", token.getPartOfSpeech().getForm());
	    		    System.out.printf("\tGender: %s\n", token.getPartOfSpeech().getGender());
	    		    System.out.printf("\tMood: %s\n", token.getPartOfSpeech().getMood());
	    		    System.out.printf("\tNumber: %s\n", token.getPartOfSpeech().getNumber());
	    		    System.out.printf("\tPerson: %s\n", token.getPartOfSpeech().getPerson());
	    		    System.out.printf("\tProper: %s\n", token.getPartOfSpeech().getProper());
	    		    System.out.printf("\tReciprocity: %s\n", token.getPartOfSpeech().getReciprocity());
	    		    System.out.printf("\tTense: %s\n", token.getPartOfSpeech().getTense());
	    		    System.out.printf("\tVoice: %s\n", token.getPartOfSpeech().getVoice());
	    		    System.out.println("DependencyEdge");
	    		    System.out.printf("\tHeadTokenIndex: %d\n", token.getDependencyEdge().getHeadTokenIndex());
	    		    System.out.printf("\tLabel: %s\n\n", token.getDependencyEdge().getLabel());
	    		  }
	    }
	    */
	    
	  }
	
	  public static String makeReview(List<Entity> entity)
	  {
		  return "";
	  }
}
