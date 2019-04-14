

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.google.cloud.language.v1.*;
import com.google.cloud.language.v1.Document.Type;
import java.util.List;
import java.io.*;

/**
 * Servlet implementation class YelpScrape
 */
@WebServlet("/YelpScrape")
public class YelpScrape extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	
	String SummaryContent;
	String BriefSummary;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
		
		request.getSession().setAttribute("toScrape", this.SummaryContent);
		request.getSession().setAttribute("scrape", this.SummaryContent);
		
	}
	
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		this.SummaryContent = request.getParameter("toScrape");
		System.out.println(this.SummaryContent);
		
		try 
		{
			this.m(this.SummaryContent);
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		request.setAttribute("toScrape", this.SummaryContent);
		request.setAttribute("scrape", this.BriefSummary);
		
		request.getRequestDispatcher("summary.jsp").forward(request, response);
    }
	
	public void m(String text) throws Exception
	{
		//Parse sentences in different way
    	String[] sentences = this.SummaryContent.toString().split("(?<=[a-z])\\.\\s+");
    	
		List<Tuple<String, String>> adj_nouns = new java.util.LinkedList<Tuple<String, String>>();
		
		ReviewSummarizer rs = new ReviewSummarizer(this.SummaryContent.toString());
    	
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
    		
    		this.BriefSummary = "Summary:\n";
    		this.BriefSummary += ReviewSummarizer.createSummary(adj_nouns);
    		this.BriefSummary += "Overall attitude: " + rs.getAttitude();
    		
    		System.out.println("Summary: ");
        	//System.out.println(ReviewSummarizer.createSummary(rs.filterSummaryPoints(adj_nouns)));
        	System.out.println(ReviewSummarizer.createSummary(adj_nouns));
        	System.out.println("Overall attitude: " + rs.getAttitude());
    	}
	}
	
	String getBooty()
	{
		return "HI";
	}
	
	
	public String[] pageReviews(String url, int numberOfReviews) { //returns a string array of reviews, numberOfReviews is capped at 20
    	Document yelpPage;
    	String[] reviewlist = new String[numberOfReviews];
    	try {
    		yelpPage = Jsoup.connect(url).get();
    		Elements reviews = yelpPage.select("div.review-content");
    		String reviewHolder;
    		for (int i = 0; i < numberOfReviews ; i++) {
    			reviewHolder = parseReview(reviews.get(i).text());
    			System.out.println("REVIEW "+(i+1)+": " + reviewHolder + "\n");	
    		}
    		
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	
    	return reviewlist;
    }
    
    public String[] pageReviews(String url) {	//returns a string array of the first 5 reviews on a yelp page
    	Document yelpPage;
    	String[] reviewlist = new String[5];
    	try {
    		yelpPage = Jsoup.connect(url).get();
    		Elements reviews = yelpPage.select("div.review-content");
    		String reviewHolder;
    		for (int i = 0; i < 5 ; i++) {
    			reviewHolder = parseReview(reviews.get(i).text());
    			System.out.println("REVIEW "+(i+1)+": " + reviewHolder + "\n");	
    		}
    		
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	
    	return reviewlist;
    }
    
    public String parseReview(String review) {
    	return review.replaceFirst("./../.... ", "")
				   .replaceFirst("../../.... ", "")
				   .replaceFirst("././.... ", "")
				   .replaceFirst(".././.... ", "")
				   .replaceFirst(".. check-ins ","")
				   .replaceFirst(". check-ins ","")
				   .replaceFirst(". check-in ","");
    }

}
