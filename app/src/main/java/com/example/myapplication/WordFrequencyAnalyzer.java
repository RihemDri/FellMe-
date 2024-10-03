package com.example.myapplication;

import com.example.myapplication.model.Tweet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class WordFrequencyAnalyzer {
    private Map<String, Integer> wordFrequencies;

    public WordFrequencyAnalyzer() {
        wordFrequencies = new HashMap<>();
    }


    public static HashMap<String, Integer> getWordFrequency(List<Tweet> tweets) {
        HashMap<String, Integer> wordCountMap = new HashMap<>();

        for (Tweet tweet : tweets) {
            // Extract the tweet text from the formatted string
            String tweetText = tweet.getTweet();

            // Tokenize the tweet text into words
            StringTokenizer tokenizer = new StringTokenizer(tweetText, " \t\n\r,.;:!?()[]{}\"");

            while (tokenizer.hasMoreTokens()) {
                String word = tokenizer.nextToken().toLowerCase(); // Convert to lowercase

                // Update the word count map
                wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);
            }
        }

        return wordCountMap;
    }

    private static String extractTweetText(String tweet) {

        // Extract text between 'tweet=' and the first ','
        int startIndex = tweet.indexOf("tweet='") + 7;
        int endIndex = tweet.indexOf("'", startIndex);
        return tweet.substring(startIndex, endIndex);
    }
}
