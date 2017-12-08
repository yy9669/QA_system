package src;

import org.apdplat.word.recognition.PersonName;
import org.apdplat.word.segmentation.Word;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class evidencefinder {
	
//public static void main(String[] args) {
public static Question localEvidence(Question question) {
	question.evidences.clear();
	//Question question = new Question() ;
	//question.question = "哪个国家是世界上领土面积最大的国家" ;
	//question.question = "三国时期孙权字什么" ;
	question.question = question.question.replace("什么","") ;
    ArrayList<String> querywords = new ArrayList<>();
    List<Word> questionwords = WordParser.parseWithoutStopWords(question.question) ;
    float maxlen = 0 ;
    String firstword = "到不找" ;
    String lastword = "到不找" ;

    if(querywords.size()>=2) lastword = querywords.get(querywords.size()-1) ;
	for( Word word : questionwords )
	{
			int len = word.getText().length() ;
			if( len >=maxlen )
			{
				maxlen = len ;
				firstword = word.getText() ;
			}
	}
	querywords.add(firstword);
    for( Word word : questionwords )
    {
		String a=word.getPartOfSpeech().getPos().toLowerCase();
		if(a.equals("nz")||a.equals("nt")||a.equals("ns")||a.equals("nr")){
			int len = word.getText().length() ;
			if( len >=maxlen )
			{
				maxlen = len ;
				firstword = word.getText() ;
			}
			querywords.add(word.getText());
		}
    }
	for( Word word : questionwords ) {
		if (word.getPartOfSpeech().getPos().toLowerCase().equals("nr") ) {
			if (PersonName.is(word.getText())) {
				firstword=word.getText();
			}
		}
	}

    System.out.println(firstword);
    if( question.question.indexOf("《") != -1 && question.question.indexOf("》") != -1 )
	{
		if(!querywords.contains(question.question.substring(question.question.indexOf("《")+1, question.question.indexOf("》")))) {
			querywords.add(question.question.substring(question.question.indexOf("《") + 1, question.question.indexOf("》")));
		}
			firstword = question.question.substring(question.question.indexOf("《") + 1, question.question.indexOf("》"));
	}
    if( question.question.indexOf("“") != -1 && question.question.indexOf("”") != -1 )
	{
		if(!querywords.contains(question.question.substring(question.question.indexOf("“")+1, question.question.indexOf("”")))) {
			querywords.add(question.question.substring(question.question.indexOf("“") + 1, question.question.indexOf("”")));
		}
		firstword = question.question.substring(question.question.indexOf("“")+1, question.question.indexOf("”")) ;
	}
    ArrayList<String> wikis = new ArrayList<String>();
    ArrayList<Evidence> evds = new ArrayList<Evidence>() ;
    
	File file1=new File("C:\\Users\\Ann\\Desktop\\key.txt");

	try{
		FileWriter fileWriter = new FileWriter(file1,true);
		fileWriter.write(firstword);
		fileWriter.close();
	}
	catch (IOException e) {e.printStackTrace();}
	
    try {
    	String outputfile = "C:\\Users\\Ann\\Desktop\\AnswerRobot\\resource\\database\\text\\result"+question.num;
    	FileWriter outdata = new FileWriter(outputfile);
    	PrintWriter outfile = new PrintWriter(outdata);
    	
    	for( int i = 0 ; i <= 67 ; i ++ )
    	{
    		String queryword = firstword ;
    		String inputfile = "C:\\Users\\Ann\\Desktop\\AnswerRobot\\resource\\database\\text\\wiki_";
    		
    		inputfile = inputfile + i/10 + i%10 ;
    		InputStream is = new FileInputStream(inputfile);  
    		
    		BufferedReader reader = new BufferedReader(new InputStreamReader(is));  
    		String sentence = "初始化" ; 
    		String title = "初始化" ;
    		int istitle = 0 ;
    		while (true) {  
    			 sentence = reader.readLine();  
    		     if( sentence == null ) break; 
    		     if( sentence.length() <= 12 ) continue ;
    		     if( istitle == 1 )
    		     {
    		         istitle = 0 ;
    		         title = sentence ;
    		     }
    		     if(sentence.indexOf("title") != -1 )
    		     {
    		      	 istitle = 1 ;
    		     }
				/*
    		     for( String word : querywords )
    		     {
    		    	 if(sentence.indexOf(word) != -1) // || sentence.indexOf(lastword) != -1) 
    		    	 {
    		    		 wikis.add(sentence) ;
    		    		 wikis.add(title) ;
    		    		 break ;
    		    	 }
    		     }
    		     */
				int nn=0;
				int bb=querywords.size();
				int havefirst=0;
				for( String word : querywords )
				{
					if(sentence.indexOf(word) != -1) // || sentence.indexOf(lastword) != -1)
					{
						nn+=1;
						if(word==firstword) {
							havefirst=1;
						}
					}
				}
				if(nn>=2||havefirst==1) {
					wikis.add(sentence);
					wikis.add(title);
				}
    		}            
    		is.close(); 
    		System.out.println(i);
    	}
    	
    	//构造证据
    	int istitle = 0 ;
    	Evidence evd = null ;
    	for ( String sentence : wikis )
    	{
    		if( istitle == 0 )
    		{
    			evd = new Evidence() ;
    			evd.source = 0 ;
    			evd.snippet = sentence ;
    			istitle = 1 ;
    			continue ;
    		}
    		if( istitle == 1 )
    		{
    			evd.title = sentence ;
    			istitle = 0 ;
    			evds.add(evd) ;
    		}
    	}
    	
    	//统计证据
    	double maxscore = 0, minscore = 1 , maxfinal = 0 , num = 0 ;
    	
    	//用标题评分证据
    	double titlenum = 0 ;
    	for( String queryword : querywords )
        {   
    		for( Evidence evidence : evds )  
        	{
    			if( evidence.title.indexOf(queryword) != -1 ) 
    			{   				
    				titlenum ++ ;
    			}
        	}
        }
    	for( String queryword : querywords )
        {   
    		for( Evidence evidence : evds )  
        	{
    			if( evidence.title.indexOf(queryword) != -1 ) 
    			{   				
    				evidence.score += 1/Math.sqrt(Math.sqrt(titlenum)) ;
    				if( evidence.score > maxscore ) maxscore = evidence.score ;
    			}
    			if( evidence.title.indexOf(queryword) == 0 && evidence.title.length() == queryword.length() ) 
    			{   				
    				evidence.score += 1/Math.sqrt(Math.sqrt(titlenum)) ;
    				if( evidence.score > maxscore ) maxscore = evidence.score ;
    			}
        	}
        }
    	
    	//用词频评分证据
    	Map<String,Double> map=new HashMap<String,Double>(); 
    	for( String queryword : querywords )
        {   	
    		num = 0 ;
    		System.out.println(queryword);
    		for( Evidence evidence : evds )  
        	{
    			if( evidence.snippet.indexOf(queryword) != -1 )
        		{
        			num ++ ;
        		}
        	}
    		map.put(queryword, num) ;
    		System.out.println(num);
        }
    	for( String queryword : querywords )
        {   
    		for( Evidence evidence : evds )  
        	{
    			if( evidence.snippet.indexOf(queryword) != -1 )
        		{
        			if( queryword == firstword ) evidence.score += 50/Math.sqrt(map.get(queryword)) ;
        			else evidence.score += 1/Math.sqrt(map.get(queryword)) ;
        		}
        	}
        }
    	for( Evidence evidence : evds )  
    	{
			if( evidence.score > maxscore ) maxscore = evidence.score ;
    	}
    	for( Evidence evidence : evds )  
    	{
			evidence.score = evidence.score / maxscore ;
    	}
    	for( Evidence evidence : evds )  
    	{
    		if( evidence.score > maxscore ) maxscore = evidence.score ;
    	}
    	//用词频筛选证据
    	int cnt = 0 , cnttemp = 0 ;
    	double score = maxscore ;
    	while( cnt <= 20000 && score >= 0  )
    	{	
    		score -= 0.01  ;
    		cnttemp = cnt ;
    		cnt = 0 ;
    		for( Evidence evidence : evds )  
    		{
    			if( evidence.score > score )
    			{
    				cnt ++ ;
    				if( cnt >= 50000 ) break ;
    			}
    		}
    	}
    	minscore = score ;
    	score += 0.01 ;
    	outfile.println(score);
    	outfile.println(maxscore);
    	//score = score * score ;
    	
    	//用匹配筛选证据
    	List<String> patterns = new ArrayList<>();
        for (int i = 0; i < querywords.size() - 2; i++) {
            String pattern = querywords.get(i) + "." + querywords.get(i + 2);
            patterns.add(pattern);
        }
        for (int i = 0; i < querywords.size() - 1; i++) {
            String pattern = querywords.get(i) + querywords.get(i + 1);
            patterns.add(pattern);
        }
        double maxbiscore = 0 ;
        for( Evidence evidence : evds )
        {
        	if(evidence.score < score) continue ;
        	String text = evidence.title + evidence.snippet;
        	double bigramscore = 0;
        	for (String pattern : patterns) {
        		int count = 0 ;
        		Pattern p = Pattern.compile(pattern);
        		Matcher matcher = p.matcher(text);
        		while (matcher.find()) {
        			count++;
        		}
        		if (count > 0) {
        			bigramscore = bigramscore + count ;
        		}
        	}
        	bigramscore = bigramscore/Math.sqrt(evidence.snippet.length()) ;
        	if( bigramscore > maxbiscore ) maxbiscore = bigramscore ;
        }
        outfile.println(maxbiscore);
        if( maxbiscore == 0 ) maxbiscore = 1 ;
        for( Evidence evidence : evds )
        {
        	if(evidence.score < score) continue ;
        	String text = evidence.title + evidence.snippet;
        	double bigramscore = 0;
        	for (String pattern : patterns) {
        		int count = 0 ;
        		Pattern p = Pattern.compile(pattern);
        		Matcher matcher = p.matcher(text);
        		while (matcher.find()) {
        			count++;
        		}
        		if (count > 0) {
        			bigramscore = bigramscore + count ;
        		}
        	}
        	bigramscore = bigramscore/Math.sqrt(evidence.snippet.length()) ;
        	evidence.score += 0.3 * (maxscore-minscore) * Math.sqrt(bigramscore / maxbiscore) ;
        	if( evidence.snippet.indexOf(firstword) != -1 ) evidence.score += 0.1/ Math.sqrt(8+evidence.snippet.indexOf(firstword));
        	if( evidence.score > maxfinal ) maxfinal = evidence.score ;
        }
        for( Evidence evidence : evds )
        {
        	evidence.score = evidence.score / maxfinal ;
        }
        cnt = 0 ; cnttemp = 0 ;
    	score = 1 ;
    	while( cnt <= 10 && score >= 0 )
    	{	
    		score -= 0.01  ;
    		cnttemp = cnt ;
    		cnt = 0 ;
    		for( Evidence evidence : evds )  
    		{
    			if( evidence.score > score )
    			{
    				cnt ++ ;		
    			}
    		}
    		if(cnt >= 20) continue ;
    	}
    	score += 0.01 ;
    	for( Evidence evidence : evds )  
    	{
    		if( evidence.score >= score )
    		{
    			outfile.println(evidence.score);
    			outfile.println(evidence.snippet); // 输出String
        		outfile.flush(); // 输出缓冲区的数据 
    			System.out.println(evidence.title);
    			question.evidences.add(evidence) ;
    		}
    	}
    	outfile.close();
    	System.out.println(score);
    	//question.evidences.addAll(baidu.searchWeb(question.question)) ;
    }catch (Exception e) {  
	    e.printStackTrace();
    }
    return question ;
  }
	public static Question webEvidence(Question question) {
		question.evidences.clear();
		try{
		question.evidences.addAll(baidu.searchWeb(question.question)) ;
	}catch (Exception e) {
		e.printStackTrace();
	}
	return question;
	}
}