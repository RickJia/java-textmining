/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package textmining;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rick Wu Diego Wu
 */
public class Textmining {
    
    Statement st;
    static Properties properties = new Properties();
    static public Map<String, TfDfIdfCount> corpus;
    static public StringBuffer wholeData;
    static List<TfDfIdfCount>sortBy_tf_idf;
    static List<TfDfIdfCount>remove_subStringList = new ArrayList<TfDfIdfCount>();
    static List<TfDfIdfCount> temp = new ArrayList<TfDfIdfCount>();
    public int ngram_begin = 0;
    public int ngram_end = 0;
    static int N=0;
   
    public Textmining(){
        
        try {
            properties.load(new FileInputStream("C:\\Users\\Rick\\Documents\\NetBeansProjects\\textmining\\src\\textmining\\config.properties"));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return;
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        ngram_begin = Integer.parseInt(properties.getProperty("ngram_begin"));
        ngram_end = Integer.parseInt(properties.getProperty("ngram_end"));
        corpus = (Map<String, TfDfIdfCount>) new HashMap<String , TfDfIdfCount>();
        connectAccessDB();
    }
    
    public void connectAccessDB() {
      try {
        // Load MS accces driver class
        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        Connection conn=DriverManager.getConnection(properties.getProperty("DataBaseFilepath"));
        st =conn.createStatement();
        System.out.println("Connection Succesfull");
        } catch (Exception e) {
        System.err.println("Got an exception! ");
        System.err.println(e.getMessage());
        }
      
      try{
        ResultSet rs=st.executeQuery(properties.getProperty("SQL_Finace"));
        wholeData = new StringBuffer();
        int i =0;
        while(rs.next()){
              System.out.println(rs.getString("source")+"  "+ rs.getString("section")+ "  " +rs.getString("title"));
              String content = rs.getString("content");
              ngramOperation(regularExpression(content));
           i++;
       }
        System.out.println("How many rows in database after querying: " + i );
        st.close();
      }catch(Exception e){
          e.printStackTrace();
      }
    }
    
    public String regularExpression(String original_text){ 
       
        String processed_text="";
        processed_text=original_text.replaceAll("[0-9，．<>●。()「」﻿；：？、. （）,!:&+-/~／》《※★◎□◆    ]", "");
        return processed_text;
    }
    
    public void ngramOperation(String processed_text)
    {
      int ngram = ngram_begin;
      Map<String, Integer> temp_hash_for_df = new HashMap<String , Integer>();
      
      while(ngram<=ngram_end){
         for(int i = 0; i<processed_text.length()-ngram+1;i++){
            TfDfIdfCount tf_df_idf = new TfDfIdfCount();
            if(corpus.get(processed_text.substring(i, i+ngram)) == null)
            {  
               tf_df_idf.word =processed_text.substring(i, i+ngram);
               
               tf_df_idf.tf_number = tf_df_idf.tf_number+1;
               temp_hash_for_df.put(processed_text.substring(i,i+ngram), 1);
               corpus.put(processed_text.substring(i,i+ngram), tf_df_idf);
            }
            else{
              int tf_number = corpus.get(processed_text.substring(i,i+ngram)).tf_number+1;
              temp_hash_for_df.put(processed_text.substring(i,i+ngram), 1);
              corpus.get(processed_text.substring(i,i+ngram)).tf_number = tf_number;
              corpus.put(processed_text.substring(i, i+ngram), (TfDfIdfCount)corpus.get(processed_text.substring(i,i+ngram)));
            }
          }
       ngram++;
     }
      for(Object key:temp_hash_for_df.keySet()){
          if(corpus.containsKey(key)){
             corpus.get(key.toString()).df_number=corpus.get(key.toString()).df_number+1;
             calBiggest_N(corpus.get(key.toString()).df_number);
        }
      }
    }
    public void calBiggest_N(int dfCount){
        if(N<=dfCount){
            N= dfCount;
        }
    }
    
    public void tf_idf_weighting(){
        for(Object key: corpus.keySet()){
            corpus.get(key).tf_ifd_number =  ( 1 + Math.log10(corpus.get(key).tf_number) ) * Math.log10( (N / (corpus.get(key).df_number+1) ) );
        }
    }
    
    public void sortBy_tf_idf_weighting(){
        sortBy_tf_idf = new ArrayList<TfDfIdfCount>(corpus.values());
        Collections.sort(sortBy_tf_idf,TfDfIdfCount.tdidfcomparator);
    }
    
    public void printFinalResult(){ 
        int i=0;
        if(properties.getProperty("RemoveSubString").equals("True")){
            getTopKeyword_without_ReomveSubString();
            remove_subString();
            printFinalResult(temp);
        }
        else{
            for(Object key: sortBy_tf_idf){
            if(i<=Integer.parseInt(properties.getProperty("topnumberforlist"))-1){
                System.out.println(i + 1 +"."+sortBy_tf_idf.get(i).word + " tf:" +sortBy_tf_idf.get(i).tf_number + " df:"+ sortBy_tf_idf.get(i).df_number + "  tf-idf:"+sortBy_tf_idf.get(i).tf_ifd_number  );
            }
           i++;
          }
        }
    }
    
    public void printFinalResult(List<TfDfIdfCount> list){
        int i =0;
        for(Object key:list){
            if(i<=Integer.parseInt(properties.getProperty("topnumberforlist"))-1){
                System.out.println(i+1 +"."+list.get(i).word +" tf:"+ list.get(i).tf_number + " df:" + list.get(i).df_number + " tf-idf:" +list.get(i).tf_ifd_number);
            }
           i++; 
       }
    }
    
    public void getTopKeyword_without_ReomveSubString(){
         int i=0;
         for(Object key: sortBy_tf_idf){
            if(i<=Integer.parseInt(properties.getProperty("topnumberforlist"))+250){
               remove_subStringList.add((TfDfIdfCount) key);
            }
           i++;
        }
    }
    
    public void remove_subString(){
        
        int i=0;
         for(i = 0;i<remove_subStringList.size()-1;i++){
             if(remove_subStringList.get(i).tf_number == remove_subStringList.get(i+1).tf_number && remove_subStringList.get(i).df_number == remove_subStringList.get(i+1).df_number){
                int stringforCompare = i; 
                for(int x=i;remove_subStringList.get(x).tf_number != remove_subStringList.get(x+1).tf_number;x++){
                   if(remove_subStringList.get(stringforCompare).word.contains(remove_subStringList.get(x).word)){
                       temp.add(remove_subStringList.get(stringforCompare));
                   }
                   if(remove_subStringList.get(x).word.contains(remove_subStringList.get(stringforCompare).word)){
                       temp.add(remove_subStringList.get(x));
                   }
                }
             }
              else{
                 temp.add(remove_subStringList.get(i));
             }          
          }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        Textmining text = new Textmining();
        System.out.println("Word keySet: "+ corpus.size());
        text.tf_idf_weighting();
        text.sortBy_tf_idf_weighting();
        text.printFinalResult();
      
        
        // TODO code application logic here
    }
}
