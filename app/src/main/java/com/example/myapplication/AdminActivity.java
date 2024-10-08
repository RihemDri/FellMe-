package com.example.myapplication;


import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NavUtils;

import com.ekn.gruzer.gaugelibrary.HalfGauge;
import com.ekn.gruzer.gaugelibrary.Range;
import com.example.myapplication.model.Tweet;
import com.example.myapplication.model.User;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jolenechong.wordcloud.WordCloud;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;



public class AdminActivity extends AppCompatActivity {

    //UI Elements
    ConstraintLayout progress;
    PieChart genderPieChart;
    PieChart positionBarChart;
    HorizontalBarChart tweetPerHourChart;
    BarChart departmentSentimentChart;
    PieChart sentimentPieChart;
    PieChart gaugePieChart;
    LineChart sentimentChart;

    TextView outOfValue, outOfTotle, outOfVerdict;
    ImageView smileyFace;

    FrameLayout wordCloud;
    HalfGauge gauge;

    PieChart femalePieChart;
    PieChart malePieChart;


    // Firebase
    private FirebaseAuth auth;

    private DatabaseReference databaseReference;


    private List<User> users = new ArrayList<>();

    private TextView userCountTextView, tweetCountTextView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        getSupportActionBar().setTitle("Admin Dashbord");
      //  getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        genderPieChart = findViewById(R.id.genderPieChart);
        positionBarChart = findViewById(R.id.departmentsChart);
        progress = findViewById(R.id.progressHolder);
        userCountTextView = findViewById(R.id.numberOfUsers);
        tweetCountTextView = findViewById(R.id.numberOfTweets);
        tweetPerHourChart = findViewById(R.id.tweetPerHourChart);
        sentimentChart = findViewById(R.id.sentimentChart);
        wordCloud = findViewById(R.id.wordCloudView);


        outOfValue = findViewById(R.id.outOfValue);
        outOfTotle = findViewById(R.id.outOfTotle);
        outOfVerdict = findViewById(R.id.outOfVerdict);

        smileyFace = findViewById(R.id.smileyFace);
        departmentSentimentChart = findViewById(R.id.departmentSentimentChart);

        //gaugePieChart = findViewById(R.id.gaugepieChart);
        gauge = findViewById(R.id.halfGauge);

        femalePieChart = findViewById(R.id.femalePieChart);
        malePieChart = findViewById(R.id.maleSentimentChart);


        databaseReference = FirebaseDatabase.getInstance().getReference("tweets");


        loadUserData();
        loadTweetData();

        getUsersList();

        loadDepartmentSentimentData();
        loadGenderSentimentData();
        //loadPiechartfemaleData();
        //loadPiechartmaleData();

        progress.setVisibility(View.INVISIBLE);
        fetchSentimentData();
    }

    private void loadDepartmentSentimentData() {
        DatabaseReference tweetsHolder = FirebaseDatabase.getInstance().getReference("Users");
        tweetsHolder.get().addOnCompleteListener(task -> {
            HashMap<String, User> userIds = new HashMap<>();
            if (task.isSuccessful()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    User value = snapshot.getValue(User.class); // Value of the child
                    String key = snapshot.getKey();
                    userIds.put(key, value);
                }

                DatabaseReference tweetRef = FirebaseDatabase.getInstance().getReference("Tweets");
                tweetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Tweet> tweets = new ArrayList<>();

                        for (DataSnapshot tweetSnapshot : snapshot.getChildren()) {
                            Tweet value = tweetSnapshot.getValue(Tweet.class); // Value of the child
                            tweets.add(value);
                        }

                        // Group tweets by userId and count prediction results
                        Map<String, Map<String, Long>> userPredictionCounts = tweets.stream()
                                .collect(Collectors.groupingBy(Tweet::getUserId, Collectors.groupingBy(Tweet::getPredictionResult, Collectors.counting())));

                        // Group prediction counts by role (department)
                        Map<String, Map<String, Long>> departmentPredictionCounts = new HashMap<>();
                        for (Map.Entry<String, Map<String, Long>> entry : userPredictionCounts.entrySet()) {
                            String userId = entry.getKey();
                            Map<String, Long> predictions = entry.getValue();

                            User user = userIds.get(userId);

                            if (user != null) {
                                String activity = user.getActivity();


                                departmentPredictionCounts.putIfAbsent(activity, new HashMap<>());
                                Map<String, Long> departmentPredictions = departmentPredictionCounts.get(activity);

                                for (Map.Entry<String, Long> predictionEntry : predictions.entrySet()) {
                                    departmentPredictions.merge(predictionEntry.getKey(), predictionEntry.getValue(), Long::sum);
                                }
                            }
                        }

                        List<BarEntry> departmentEntries = new ArrayList<>();
                        List<String> departments = new ArrayList<>();

                        departmentPredictionCounts.forEach((role, predictionMap) -> {

                            // Create a BarEntry for this department with sentiment values
                            departmentEntries.add(new BarEntry(departments.size(), new float[]{
                                    predictionMap.getOrDefault("Very Positive", 0L).floatValue(),
                                    predictionMap.getOrDefault("Positive", 0L).floatValue(),
                                    predictionMap.getOrDefault("Neutral", 0L).floatValue(),
                                    predictionMap.getOrDefault("Negative", 0L).floatValue(),
                                    predictionMap.getOrDefault("Very Negative", 0L).floatValue()
                            }));

                            // Add the department name to the list for labeling
                            departments.add(role);

                            // Print out the prediction results for debugging
                            System.out.println("Department: " + role);
                            predictionMap.forEach((prediction, count) -> {
                                System.out.println(prediction + ": " + count);
                            });
                            System.out.println();
                        });

                        BarDataSet set = new BarDataSet(departmentEntries, "");

                        // TODO: change colors
                        set.setColors(new int[]{Color.MAGENTA, Color.GREEN, Color.BLUE, Color.YELLOW, Color.RED});

                        set.setStackLabels(new String[]{"Very Positive", "Positive", "Neutral", "Negative", "Very Negative"});

                        set.setDrawValues(true);

                        BarData data = new BarData(set);
                        float barWidth = 0.4f; // Example width, adjust as needed
                        data.setBarWidth(barWidth);

                        departmentSentimentChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(departments));
                        departmentSentimentChart.getXAxis().setGranularity(1f);
                        departmentSentimentChart.getXAxis().setGranularityEnabled(true);
                        departmentSentimentChart.getDescription().setEnabled(false);

                        departmentSentimentChart.clear(); // Clear previous data
                        departmentSentimentChart.setData(data); // Set new data
                        departmentSentimentChart.invalidate();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            } else {
                Toast.makeText(AdminActivity.this, "Failed to get Users" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadUserData() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long userCount = snapshot.getChildrenCount();
                userCountTextView.setText(userCount + " users");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

   /* private void loadTweetData() {
        DatabaseReference tweetRef = FirebaseDatabase.getInstance().getReference("Tweets");
        tweetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Tweet> tweets = new ArrayList<>();

                long tweetCount = snapshot.getChildrenCount();
                tweetCountTextView.setText(tweetCount + " tweets");

                List<Calendar> calendars = new ArrayList<>();

                for (DataSnapshot tweetSnapshot : snapshot.getChildren()) {
                    Tweet value = tweetSnapshot.getValue(Tweet.class); // Value of the child
                    tweets.add(value);
                    System.out.println(value);

                    String time = tweetSnapshot.child("time").getValue(String.class);
                    String date = tweetSnapshot.child("date").getValue(String.class);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    try {
                        // Combine the date and time into one string
                        String dateTimeString = date + " " + time;

                        // Parse the combined string into a Date object
                        Date dateTime = dateFormat.parse(dateTimeString);

                        // Create a Calendar object and set the parsed Date
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(dateTime);

                        calendars.add(calendar);
                        System.out.println(calendar.getTime());

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                // Find the most recent date
                Calendar mostRecentDate = getMostRecentDate(calendars);

// Subtract 30 days from the most recent date
                mostRecentDate.add(Calendar.DAY_OF_YEAR, -30);

// Now, 'mostRecentDate' represents the date 30 days before the original most recent date
                System.out.println("Most recent date minus 30 days: " + mostRecentDate.getTime());

                // Calculate the start and end of the month for the most recent date
                Calendar startOfMonth = getStartOfMonth(mostRecentDate);
                Calendar endOfMonth = getEndOfMonth(startOfMonth);
               // Log.i("endOfMonth", "taaastt: " + endOfMonth.toString());
               // Log.i("startOfMonth", "taaastt: " + startOfMonth.toString());



                // Initialize the dayOfWeekCounts with all days of the week set to 0
                Map<DayOfWeek, Integer> dayOfWeekCounts = new EnumMap<>(DayOfWeek.class);
                for (DayOfWeek day : DayOfWeek.values()) {
                    dayOfWeekCounts.put(day, 0);
                }

                // Count occurrences of each day of the week in the last month
                for (Calendar calendar : calendars) {
                    if (calendar.after(startOfMonth) && calendar.before(endOfMonth)) {
                        DayOfWeek dayOfWeek = DayOfWeek.of(calendar.get(Calendar.DAY_OF_WEEK));
                        dayOfWeekCounts.put(dayOfWeek, dayOfWeekCounts.get(dayOfWeek) + 1);
                    }
                }

                // Print results
                for (Map.Entry<DayOfWeek, Integer> entry : dayOfWeekCounts.entrySet()) {
                    System.out.println(entry.getKey().name() + ": " + entry.getValue());
                }

                setupTweetHourBarChart(tweetPerHourChart, dayOfWeekCounts);
                calculateOverallSentiment(tweets);
                setupSentimentLineChart(sentimentChart, tweets, calendars);
                setupWordCloud(tweets);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/
    ///this code fe one month
  private void loadTweetData() {
       DatabaseReference tweetRef = FirebaseDatabase.getInstance().getReference("Tweets");
       tweetRef.addListenerForSingleValueEvent(new ValueEventListener() {
           @RequiresApi(api = Build.VERSION_CODES.O)
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               List<Tweet> tweets = new ArrayList<>();

               long tweetCount = snapshot.getChildrenCount();
               tweetCountTextView.setText(tweetCount + " tweets");

               List<Calendar> calendars = new ArrayList<>();

               for (DataSnapshot tweetSnapshot : snapshot.getChildren()) {
                   Tweet value = tweetSnapshot.getValue(Tweet.class); // Value of the child
                   tweets.add(value);
                   System.out.println(value);

                   String time = tweetSnapshot.child("time").getValue(String.class);
                   String date = tweetSnapshot.child("date").getValue(String.class);

                   SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                   try {
                       // Combine the date and time into one string
                       String dateTimeString = date + " " + time;

                       // Parse the combined string into a Date object
                       Date dateTime = dateFormat.parse(dateTimeString);

                       // Create a Calendar object and set the parsed Date
                       Calendar calendar = Calendar.getInstance();
                       calendar.setTime(dateTime);

                       calendars.add(calendar);
                       System.out.println(calendar.getTime());

                   } catch (ParseException e) {
                       e.printStackTrace();
                   }
               }
               // Find the most recent date
               Calendar mostRecentDate = getMostRecentDate(calendars);
               SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


               // Calculate the start and end of the month for the most recent date
               Calendar startOfMonth = Calendar.getInstance();
               startOfMonth.setTime(mostRecentDate.getTime());
               startOfMonth.add(Calendar.DAY_OF_YEAR, -30);

               Calendar endOfMonth = mostRecentDate;

               // Initialize the dayOfWeekCounts with all days of the week set to 0
               Map<DayOfWeek, Integer> dayOfWeekCounts = new EnumMap<>(DayOfWeek.class);
               for (DayOfWeek day : DayOfWeek.values()) {
                   dayOfWeekCounts.put(day, 0);
               }

               // Count occurrences of each day of the week in the last month
               for (Calendar calendar : calendars) {
                   if (calendar.after(startOfMonth) && calendar.before(endOfMonth)) {
                       Integer dayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;

                        // Ensure dayIndex stays between 1 and 7
                       if (dayIndex < 1) {
                           dayIndex = 7;
                       } else if (dayIndex > 7) {
                           dayIndex = 1;
                       }
                       Log.i("Test","MostRecentDate: " + sdf.format(calendar.getTime()) + " " + DayOfWeek.of(dayIndex));
                       DayOfWeek dayOfWeek = DayOfWeek.of(dayIndex);
                       dayOfWeekCounts.put(dayOfWeek, dayOfWeekCounts.get(dayOfWeek) + 1);
                   }
               }

               // Print results
               for (Map.Entry<DayOfWeek, Integer> entry : dayOfWeekCounts.entrySet()) {
                   System.out.println(entry.getKey().name() + ": " + entry.getValue());
               }

               setupTweetHourBarChart(tweetPerHourChart, dayOfWeekCounts);
               calculateOverallSentiment(tweets);
               setupSentimentLineChart(sentimentChart, tweets, calendars);
               setupWordCloud(tweets);
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });
   }

// ki tenze yatlaa adad louta
   /* private void loadTweetData() {
        DatabaseReference tweetRef = FirebaseDatabase.getInstance().getReference("Tweets");
        tweetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Tweet> tweets = new ArrayList<>();
                long tweetCount = snapshot.getChildrenCount();
                tweetCountTextView.setText(tweetCount + " tweets");

                List<Calendar> calendars = new ArrayList<>();

                for (DataSnapshot tweetSnapshot : snapshot.getChildren()) {
                    Tweet value = tweetSnapshot.getValue(Tweet.class);
                    if (value != null) {
                        tweets.add(value);
                        String time = tweetSnapshot.child("time").getValue(String.class);
                        String date = tweetSnapshot.child("date").getValue(String.class);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                        try {
                            String dateTimeString = date + " " + time;
                            Date dateTime = dateFormat.parse(dateTimeString);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(dateTime);
                            calendars.add(calendar);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                Calendar mostRecentDate = getMostRecentDate(calendars);
                mostRecentDate.add(Calendar.DAY_OF_YEAR, -30);
                Calendar startOfMonth = getStartOfMonth(mostRecentDate);
                Calendar endOfMonth = getEndOfMonth(startOfMonth);

                Map<DayOfWeek, Integer> dayOfWeekCounts = new EnumMap<>(DayOfWeek.class);
                for (DayOfWeek day : DayOfWeek.values()) {
                    dayOfWeekCounts.put(day, 0);
                }

                for (Calendar calendar : calendars) {
                    if (!calendar.before(startOfMonth) && !calendar.after(endOfMonth)) {
                        DayOfWeek dayOfWeek = DayOfWeek.of(calendar.get(Calendar.DAY_OF_WEEK));
                        dayOfWeekCounts.put(dayOfWeek, dayOfWeekCounts.get(dayOfWeek) + 1);
                    }
                }

                for (Map.Entry<DayOfWeek, Integer> entry : dayOfWeekCounts.entrySet()) {
                    System.out.println(entry.getKey().name() + ": " + entry.getValue());
                }

                displayTweetCounts(dayOfWeekCounts);
                setupTweetHourBarChart(tweetPerHourChart, dayOfWeekCounts);

                tweetPerHourChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                    @Override
                    public void onValueSelected(Entry e, Highlight h) {
                        int dayIndex = (int) e.getX(); // Get the index of the day from the entry
                        DayOfWeek day = DayOfWeek.of(dayIndex + 1); // Adjust index to DayOfWeek (1 = Monday)
                        int count = dayOfWeekCounts.get(day);

                        // Display the number of tweets for that day
                        Toast.makeText(getApplicationContext(), day.name() + ": " + count + " tweets", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected() {
                        // Optional: Handle the case when no bar is selected
                    }
                });


                calculateOverallSentiment(tweets);
                setupSentimentLineChart(sentimentChart, tweets, calendars);
                setupWordCloud(tweets);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error fetching tweet data: " + error.getMessage());
            }
        });
    }*/




























    private void setupWordCloud(List<Tweet> tweets) {
        WordCloud wordCloudView = new WordCloud(getApplication(), null);
        wordCloud.addView(wordCloudView);

        wordCloudView.setParagraph(
                "Add some text you'd like to see a word cloud of here",
                10,
                false
        );

        List<String> words = new ArrayList<>();
        /*words.add("human");
        words.add("tasks");
        words.add("tasks");
        words.add("AI");
        words.add("AI");
        words.add("AI");
        words.add("AI");
        words.add("systems");
        words.add("systems");*/
        WordFrequencyAnalyzer wordFrequency = new WordFrequencyAnalyzer();
        HashMap<String, Integer> wordCountMap = new HashMap<>();
        wordCountMap = WordFrequencyAnalyzer.getWordFrequency(tweets);

        Map<String, Integer> newWords = new HashMap<>();

        // Process entries to truncate keys if necessary
        for (Map.Entry<String, Integer> entry : wordCountMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();

            // Limit the key to 10 characters
            if (key.length() > 10) {
                key = key.substring(0, 7) + "..."; // Truncate to 10 characters
            }

            // Add the truncated key and original value to the new map
            newWords.put(key, value);
        }

        for (Map.Entry<String, Integer> entry : newWords.entrySet()) {
            String word = entry.getKey();
            int frequency = entry.getValue();
            words.add(word + "(" + frequency + ")");
        }

        wordCloudView.setWords(words, 10);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setupSentimentLineChart(LineChart sentimentChart, List<Tweet> tweets, List<Calendar> calendars) {

        // Create line data
        LineData lineData = createLineData(tweets);

        // Apply custom ValueFormatter to Y-Axis
        YAxis yAxis = sentimentChart.getAxisLeft();
        yAxis.setValueFormatter(new SentimentValueFormatter());

        // Set data to the chart
        sentimentChart.setData(lineData);
        sentimentChart.getAxisRight().setEnabled(false);
        sentimentChart.getXAxis().setEnabled(true);
        sentimentChart.getDescription().setEnabled(false);
        sentimentChart.getAxisLeft().setDrawAxisLine(true);
        sentimentChart.getAxisLeft().setDrawGridLines(false);

        // Format X-Axis labels
        List<String> dates = getDatesFromTweets(tweets); // Extract dates from tweets
        sentimentChart.getXAxis().setValueFormatter(new XAxisValueFormatter(dates));

        sentimentChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);


        sentimentChart.invalidate(); // Refresh the chart
    }

    private List<String> getDatesFromTweets(List<Tweet> tweets) {
        List<String> dates = new ArrayList<>();
        for (Tweet tweet : tweets) {
            dates.add(tweet.getDate()); // Assuming getDate() returns the date string
        }
        return dates;
    }

    public class XAxisValueFormatter extends ValueFormatter {

        private final SimpleDateFormat dateFormat;
        private final List<String> dates;

        public XAxisValueFormatter(List<String> dates) {
            this.dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            this.dates = dates;
        }

        @Override
        public String getFormattedValue(float value) {
            // Ensure the value is within the bounds of your data
            // Convert float timestamp to milliseconds
            long timestampInMillis = (long) (value);

            // Create a Date object from the timestamp
            Date date = new Date(timestampInMillis);

            // Format the Date object to "1 Sept" format
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM", Locale.ENGLISH);

            // Get the formatted date string
            String formattedDate = dateFormat.format(date);
            return formattedDate;
        }
    }

    // Method to convert sentiment to numerical value
    private static float sentimentToValue(String sentiment) {
        switch (sentiment) {
            case "Very Positive":
                return 4;
            case "Positive":
                return 3;
            case "Neutral":
                return 2;
            case "Negative":
                return 1;
            case "Very Negative":
                return 0;
            default:
                return Float.NaN; // For no prediction or unknown values
        }
    }

    public class SentimentValueFormatter extends ValueFormatter {

        @Override
        public String getFormattedValue(float value) {
            if (Float.isNaN(value)) {
                return ""; // For no prediction or unknown values
            }
            switch ((int) value) {
                case 4:
                    return "Very Positive";
                case 3:
                    return "Positive";
                case 2:
                    return "Neutral";
                case 1:
                    return "Negative";
                case 0:
                    return "Very Negative";
                default:
                    return "";
            }
        }
    }

    // Method to create a list of Entry from tweet data
    @RequiresApi(api = Build.VERSION_CODES.N)
    public LineData createLineData(List<Tweet> tweets) {
        List<Entry> entries = new ArrayList<>();

        // Assuming tweets are sorted by date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (Tweet tweet : tweets) {
            try {
                Date date = dateFormat.parse(tweet.getDate());
                float x = date.getTime(); // Use timestamp as X value
                float y = sentimentToValue(tweet.getPredictionResult());

                System.out.println("sentimentLineChart " + x + " " + y);

                // Check if the sentiment value is valid
                if (!Float.isNaN(y)) {
                    entries.add(new Entry(x, y));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Sort entries by X value (date)
        entries.sort(new EntryXComparator());

        // Create LineDataSet
        LineDataSet dataSet = new LineDataSet(entries, "Sentiment Over Time");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(1);

        dataSet.setValueFormatter(new CustomValueFormatter());

        return new LineData(dataSet);
    }

    public class CustomValueFormatter extends ValueFormatter {

        @Override
        public String getFormattedValue(float value) {
            if (value == 4) {
                return "Very Positive";
            } else if (value == 3) {
                return "Positive";
            } else if (value == 2) {
                return "Neutral";
            } else if (value == 1) {
                return "Negative";
            } else if (value == 0) {
                return "Very Negative";
            }
            return super.getFormattedValue(value);
        }
    }

    private void calculateOverallSentiment(List<Tweet> tweets) {
        int veryPositiveCount = 0;
        int positiveCount = 0;
        int veryNegativeCount = 0;
        int negativeCount = 0;
        int neutralCount = 0;

        for (Tweet tweet : tweets) {
            if (tweet.getPredictionResult().equals("Very Positive")) {
                veryPositiveCount++;
            } else if (tweet.getPredictionResult().equals("Positive")) {
                positiveCount++;
            } else if (tweet.getPredictionResult().equals("Very Negative")) {
                veryNegativeCount++;
            } else if (tweet.getPredictionResult().equals("Negative")) {
                negativeCount++;
            } else if (tweet.getPredictionResult().equals("Neutral")) {
                neutralCount++;
            }

        }

        // Calculate total sentiment count
        int totalCount = positiveCount + negativeCount + neutralCount + veryPositiveCount + veryNegativeCount;

        // Determine which sentiment has the most values
        String mostSentiment = findMostSentiment(veryPositiveCount, positiveCount, neutralCount, negativeCount, veryNegativeCount);
        int mostCount = getMostCount(veryPositiveCount, positiveCount, neutralCount, negativeCount, veryNegativeCount);

        // Format the output
        String result = String.format("%d out of %d sentiments are %s", mostCount, totalCount, mostSentiment);

        // Print the result
        System.out.println(result);

        outOfValue.setText(String.valueOf(mostCount));
        outOfTotle.setText("out of " + totalCount);
        outOfVerdict.setText(mostSentiment);
        if (mostSentiment.equals("Positive") || mostSentiment.equals("Very Positive")) {
            outOfVerdict.setTextColor(Color.GREEN);
            smileyFace.setImageResource(R.drawable.ic_positive_smiley);
        } else if (mostSentiment.equals("Negative") || mostSentiment.equals("Very Negative")) {
            outOfVerdict.setTextColor(Color.RED);
            smileyFace.setImageResource(R.drawable.ic_negative_smiley);
        } else {
            outOfVerdict.setTextColor(Color.GRAY);
            smileyFace.setImageResource(R.drawable.ic_neutral_smiley);
        }
    }

    private static String findMostSentiment(int veryPositive, int positive, int neutral, int negative, int veryNegative) {
        if (veryPositive >= positive && veryPositive >= neutral && veryPositive >= negative && veryPositive >= veryNegative) {
            return "Very Positive";
        } else if (positive >= veryPositive && positive >= neutral && positive >= negative && positive >= veryNegative) {
            return "Positive";
        } else if (neutral >= veryPositive && neutral >= positive && neutral >= negative && neutral >= veryNegative) {
            return "Neutral";
        } else if (negative >= veryPositive && negative >= positive && negative >= neutral && negative >= veryNegative) {
            return "Negative";
        } else {
            return "Very Negative";
        }
    }

    private static int getMostCount(int veryPositive, int positive, int neutral, int negative, int veryNegative) {
        return Math.max(Math.max(Math.max(veryPositive, positive), Math.max(neutral, negative)), veryNegative);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupTweetHourBarChart(HorizontalBarChart chart, Map<DayOfWeek, Integer> dayOfWeekCountMap) {
        // Create the data for the chart
        List<BarEntry> entries = new ArrayList<>();

        // Define the order of days from Sunday to Saturday
        DayOfWeek[] daysOfWeek = {
                DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
        };

        // Populate the entries from the dayOfWeekCountMap in reverse order
        for (int i = daysOfWeek.length - 1; i >= 0; i--) {
            DayOfWeek dayOfWeek = daysOfWeek[i];
            float xValue = daysOfWeek.length - 1 - i; // Reverse the x-value
            int count = dayOfWeekCountMap.getOrDefault(dayOfWeek, 0);

            // Add a small offset to ensure non-zero values are visible
            if (count == 0) {
                count = 1;  // Ensure even zero counts show something (if needed)
            }
            entries.add(new BarEntry(xValue, count));
        }

        Log.i("Test data", " entries: " + entries);

        // Creating a dataset and assigning colors for each day
        BarDataSet dataSet = new BarDataSet(entries, "Daily Counts of Last 30 Days");
        dataSet.setColors(Color.CYAN);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        dataSet.setValueFormatter(new RoundedValueFormatter());

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f); // Adjust bar width if necessary

        // Customizing the X-axis (which shows the day labels for horizontal chart)
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);  // Ensure there are 7 labels for the 7 days
        xAxis.setValueFormatter(new ValueFormatter() {
            private final String[] days = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

            @Override
            public String getFormattedValue(float value) {
                return days[6 - ((int) value % days.length)]; // Reverse the index
            }
        });

        // Customizing the left Y-axis (not used in horizontal bar chart)
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setEnabled(false); // Disable if not needed

        // Customizing the right Y-axis for value range
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setAxisMinimum(0f);  // Ensure no negative values
        rightAxis.setGranularity(1f);  // Control how values appear on the Y-axis
        rightAxis.setEnabled(true);

        // Remove chart description
        chart.getDescription().setEnabled(false);

        // Allow chart to fit bars properly
        chart.setFitBars(true);

        // Refresh chart with the data
        chart.setData(data);
        chart.animateY(1500);
        chart.invalidate();
    }



    //////////////   ///////////////////////////////////////////////////////////////////////////////////
    //creating ActionBar Menutweets

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflater menu Items
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //when any menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(AdminActivity.this);
        } else if (id == R.id.menu_refresh) {
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
        }else if (id == R.id.my_profile) {
            Intent intent = new Intent(AdminActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();

        }else if (id == R.id.menu_update_profile) {
            Intent intent = new Intent(AdminActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.menu_change_password) {
            Intent intent = new Intent(AdminActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_delete_profile) {
            Intent intent = new Intent(AdminActivity.this, DeleteActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_logout) {
            auth.signOut();
            Toast.makeText(AdminActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(AdminActivity.this, MainActivity.class);

            //clear
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();//close user profile Activity

        } else {
            Toast.makeText(AdminActivity.this, "Something went wrong ", Toast.LENGTH_LONG).show();


        }

        return super.onOptionsItemSelected(item);
    }

////////////////////////////////////////////////////////////////////////


    private void getUsersList() {
        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(AdminActivity.this, "Something went wrong! User details are not available at the moment !  ", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        assert firebaseUser != null;
        DatabaseReference tweetsHolder = FirebaseDatabase.getInstance().getReference("Users");
        tweetsHolder.get().addOnCompleteListener(task -> {
            List<User> valuesList = new ArrayList<>();
            if (task.isSuccessful()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    User value = snapshot.getValue(User.class); // Value of the child
                    valuesList.add(value);
                }
                users = valuesList;
                Log.i("test", "users: " + users.toString());
                setupGenderPieChart(genderPieChart, users);
                setupPositionBarChart(positionBarChart, users);
            } else {
                Toast.makeText(AdminActivity.this, "Failed to get tweets" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    public void setupPositionBarChart(PieChart pieChart, List<User> users) {
        Map<String, Integer> positionCountMap = new HashMap<>();

      /*  for (User user : users) {
            String position = user.getActivity();
            positionCountMap.put(position, positionCountMap.getOrDefault(position, 0)+1);
        }*/
        for (User user : users) {
            String position = user.getActivity();

            Log.i("chart", "position: " + position);
            if (positionCountMap.containsKey(position)) {
                positionCountMap.put(position, positionCountMap.get(position) + 1);
            } else {
                positionCountMap.put(position, 1);
            }
        }

        System.out.println("positionCountmap " + positionCountMap);

        List<PieEntry> entries = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : positionCountMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        // Set custom colors
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#FF5733")); // Example custom color
        colors.add(Color.parseColor("#33FF57"));
        colors.add(Color.parseColor("#3357FF"));
        colors.add(Color.parseColor("#F3FF33"));
        colors.add(Color.parseColor("#33FFF9"));

        dataSet.setColors(colors); // Apply custom colors to the dataset
       // dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10);
        dataSet.setValueFormatter(new RoundedValueFormatter());


        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        Description desc = new Description();
        desc.setText("Work Positions");
        pieChart.setDescription(desc);
        // Customize the size of the chart
        pieChart.setEntryLabelColor(Color.BLACK); // Set department titles (entry labels) to black
        pieChart.setHoleRadius(30f); // Adjust this value to increase/decrease the size of the center hole
        pieChart.setTransparentCircleRadius(15f); // Adjust the transparent circle radius

        // Disable the legend
        pieChart.getLegend().setEnabled(false);



        pieChart.invalidate(); // refresh
    }

    public void setupGenderPieChart(PieChart pieChart, List<User> users) {
        int maleCount = 0;
        int femaleCount = 0;

        for (User user : users) {
            if ("Male".equalsIgnoreCase(user.getGender())) {
                maleCount++;
            } else if ("Female".equalsIgnoreCase(user.getGender())) {
                femaleCount++;
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(maleCount, "Male"));
        entries.add(new PieEntry(femaleCount, "Female"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.BLUE, Color.rgb(255, 105, 180)); // Blue for Male, Pink for Female
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(14);
        dataSet.setValueFormatter(new RoundedValueFormatter());


        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        Description desc = new Description();
        desc.setText("Gender Distribution");
        pieChart.setDescription(desc);
        pieChart.setDrawEntryLabels(false);
        pieChart.invalidate(); // refresh
    }

    private void loadSentimentData() {
        DatabaseReference sentimentRef = FirebaseDatabase.getInstance().getReference("predictionResult");
        sentimentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int positiveCount = 0;
                int negativeCount = 0;
                int neutralCount = 0;

                for (DataSnapshot sentimentSnapshot : snapshot.getChildren()) {
                    String sentiment = sentimentSnapshot.child("predictionResult").getValue(String.class);
                    if ("positive".equalsIgnoreCase(sentiment)) {
                        positiveCount++;
                    } else if ("negative".equalsIgnoreCase(sentiment)) {
                        negativeCount++;
                    } else if ("neutral".equalsIgnoreCase(sentiment)) {
                        neutralCount++;
                    }
                }

                setupSentimentPieChart(sentimentPieChart, positiveCount, negativeCount, neutralCount);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminActivity.this, "Failed to load sentiment data: " + error.getMessage(), Toast.LENGTH_LONG).show();


            }
        });
    }

    private void setupSentimentPieChart(PieChart pieChart, int positiveCount, int negativeCount, int neutralCount) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(positiveCount, "Positive"));
        entries.add(new PieEntry(negativeCount, "Negative"));
        entries.add(new PieEntry(neutralCount, "Neutral"));

        PieDataSet dataSet = new PieDataSet(entries, "Sentiment Analysis");
        dataSet.setColors(new int[]{Color.GREEN, Color.RED, Color.YELLOW});

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setCenterText("Sentiment Analysis");
        pieChart.invalidate(); // refresh
    }

    private static Calendar parseDate(String dateStr, SimpleDateFormat sdf) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sdf.parse(dateStr));
        return calendar;
    }

    private static Calendar getMostRecentDate(List<Calendar> calendars) {
        Calendar mostRecentDate = Calendar.getInstance();
        mostRecentDate.setTimeInMillis(Long.MIN_VALUE); // Start with the smallest value

        for (Calendar calendar : calendars) {
            if (calendar.after(mostRecentDate)) {
                mostRecentDate = calendar;
            }
        }
        return mostRecentDate;
    }

    private static Calendar getStartOfMonth(Calendar date) {
        Calendar startOfMonth = (Calendar) date.clone();
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        return startOfMonth;
    }

    private static Calendar getEndOfMonth(Calendar startOfMonth) {
        Calendar endOfMonth = (Calendar) startOfMonth.clone();
        endOfMonth.add(Calendar.MONTH, 1);
        endOfMonth.add(Calendar.DAY_OF_MONTH, -1);
        return endOfMonth;
    }

    private void setupPieChart() {
        gaugePieChart.setRotationAngle(0);
        gaugePieChart.setMaxAngle(360f);  // Display half of the pie chart
        gaugePieChart.setDrawHoleEnabled(true);  // Display a hole in the center
        gaugePieChart.setUsePercentValues(true);  // Display values as percentages
        gaugePieChart.getDescription().setEnabled(false);  // Disable description text
        gaugePieChart.getLegend().setEnabled(false);  // Disable the chart legend
    }

    private void fetchSentimentData() {
        DatabaseReference tweetRef = FirebaseDatabase.getInstance().getReference("Tweets");
        // Add a listener to get data from Firebase Realtime Database
        tweetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int veryPositiveCount = 0;
                int positiveCount = 0;
                int neutralCount = 0;
                int negativeCount = 0;
                int veryNegativeCount = 0;
                int totalCount = 0;


                // Iterate through all tweets in the database
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String sentiment = snapshot.child("predictionResult").getValue(String.class);
                    Log.i("test22", sentiment);
                    if (sentiment != null) {
                        switch (sentiment) {
                            case "Very Positive":
                                veryPositiveCount++;
                                break;
                            case "Positive":
                                positiveCount++;
                                break;
                            case "Neutral":
                                neutralCount++;
                                break;
                            case "Negative":
                                negativeCount++;
                                break;
                            case "Very Negative":
                                veryNegativeCount++;
                                break;
                        }
                        totalCount++;
                    }
                }
                if (totalCount == 0) {
                    Log.e("PieChart", "No data available");
                    return;
                }

                // Calculate the percentages for each sentiment
                float veryPositivePercent = (veryPositiveCount / (float) totalCount) * 100;
                float positivePercent = (positiveCount / (float) totalCount) * 100;
                float neutralPercent = (neutralCount / (float) totalCount) * 100;
                float negativePercent = (negativeCount / (float) totalCount) * 100;
                float veryNegativePercent = (veryNegativeCount / (float) totalCount) * 100;

                Log.i("test", veryPositivePercent + " " + positivePercent + " " + neutralPercent + " " + negativePercent + " " + veryNegativePercent);
                // Update the chart with the calculated percentages
                //updateChart(veryPositivePercent, positivePercent, neutralPercent, negativePercent, veryNegativePercent);
                updateGauge(veryPositivePercent, positivePercent, neutralPercent, negativePercent, veryNegativePercent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Log.e("FirebaseError", databaseError.getMessage());
            }
        });
    }

    private void updateChart(float veryPositivePercent, float positivePercent, float neutralPercent, float negativePercent, float veryNegativePercent) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(veryPositivePercent, "Very Positive"));
        entries.add(new PieEntry(positivePercent, "Positive"));
        entries.add(new PieEntry(neutralPercent, "Neutral"));
        entries.add(new PieEntry(negativePercent, "Negative"));
        entries.add(new PieEntry(veryNegativePercent, "Very Negative"));

        PieDataSet dataSet = new PieDataSet(entries, "Sentiments");
        dataSet.setColors(new int[]{Color.GREEN, Color.BLUE, Color.GRAY, Color.YELLOW, Color.RED});

        PieData data = new PieData(dataSet);
        gaugePieChart.setData(data);
        gaugePieChart.invalidate();  // Refresh the chart
        Log.i("test", data.toString());

    }

    private void updateGauge(float veryPositivePercent, float positivePercent, float neutralPercent, float negativePercent, float veryNegativePercent) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(veryPositivePercent, "Very Positive"));
        entries.add(new PieEntry(positivePercent, "Positive"));
        entries.add(new PieEntry(neutralPercent, "Neutral"));
        entries.add(new PieEntry(negativePercent, "Negative"));
        entries.add(new PieEntry(veryNegativePercent, "Very Negative"));

        Double cumulative = 0.0;

        // Very positive
        Range range = new Range();
        range.setColor(Color.MAGENTA);
        range.setFrom(0.0);
        range.setTo(veryPositivePercent);

        cumulative += veryPositivePercent;

        //Positive
        Range range2 = new Range();
        range2.setColor(Color.GREEN);
        range2.setFrom(cumulative);
        range2.setTo(cumulative + positivePercent);

        cumulative += positivePercent;

        Range range3 = new Range();
        range3.setColor(Color.BLUE);
        range3.setFrom(cumulative);
        range3.setTo(cumulative + neutralPercent);

        cumulative += neutralPercent;

        Range range4 = new Range();
        range4.setColor(Color.YELLOW);
        range4.setFrom(cumulative);
        range4.setTo(cumulative + negativePercent);

        cumulative += negativePercent;

        Range range5 = new Range();
        range5.setColor(Color.RED);
        range5.setFrom(cumulative);
        range5.setTo(cumulative + veryNegativePercent);

        cumulative += veryNegativePercent;

// Add color ranges to gauge
        gauge.addRange(range);
        gauge.addRange(range2);
        gauge.addRange(range3);
        gauge.addRange(range4);
        gauge.addRange(range5);

// Set min, max, and current value
        gauge.setMinValue(0.0);
        gauge.setMaxValue(cumulative);
        Float max = Math.max(veryPositivePercent, Math.max(positivePercent, Math.max(neutralPercent, Math.max(negativePercent, veryNegativePercent))));
        gauge.setValue(cumulative - max);

    }

    private void loadGenderSentimentData() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.get().addOnCompleteListener(task -> {
            HashMap<String, User> userIds = new HashMap<>();
            if (task.isSuccessful()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    User value = snapshot.getValue(User.class); // Value of the child
                    String key = snapshot.getKey();
                    userIds.put(key, value);
                }

                DatabaseReference tweetRef = FirebaseDatabase.getInstance().getReference("Tweets");
                tweetRef.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Tweet> tweets = new ArrayList<>();

                        for (DataSnapshot tweetSnapshot : snapshot.getChildren()) {
                            Tweet value = tweetSnapshot.getValue(Tweet.class); // Value of the child
                            tweets.add(value);
                        }

                        // Group tweets by userId and count prediction results
                        Map<String, Map<String, Long>> userPredictionCounts = tweets.stream()
                                .collect(Collectors.groupingBy(Tweet::getUserId, Collectors.groupingBy(Tweet::getPredictionResult, Collectors.counting())));

                        // Group prediction counts by gender
                        Map<String, Map<String, Long>> genderPredictionCounts = new HashMap<>();
                        for (Map.Entry<String, Map<String, Long>> entry : userPredictionCounts.entrySet()) {
                            String userId = entry.getKey();
                            Map<String, Long> predictions = entry.getValue();

                            User user = userIds.get(userId);

                            if (user != null) {
                                String gender = user.getGender(); // Assuming getGender() returns "Male", "Female", etc.

                                genderPredictionCounts.putIfAbsent(gender, new HashMap<>());
                                Map<String, Long> genderPredictions = genderPredictionCounts.get(gender);

                                for (Map.Entry<String, Long> predictionEntry : predictions.entrySet()) {
                                    genderPredictions.merge(predictionEntry.getKey(), predictionEntry.getValue(), Long::sum);
                                }
                            }
                        }

                        // Separate data for female and male users
                        Map<String, Long> femalePredictions = genderPredictionCounts.get("Female");
                        Map<String, Long> malePredictions = genderPredictionCounts.get("Male");

                        if (femalePredictions != null) {
                            loadPieChart(femalePredictions, femalePieChart, "Female");
                        }

                        if (malePredictions != null) {
                            loadPieChart(malePredictions, malePieChart, "Male");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Handle errors
                        Toast.makeText(AdminActivity.this, "Error:" + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(AdminActivity.this, "Failed to get Users:" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadPieChart(Map<String, Long> predictionMap, PieChart pieChart, String gender) {
        // Ensure PieChart is not null before proceeding
        if (pieChart == null) {
            Log.e("PieChartError", gender + " PieChart is null");
            return;
        }

        long totalTweets = predictionMap.values().stream().mapToLong(Long::longValue).sum();

        // Prepare pie chart entries
        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(predictionMap.getOrDefault("Very Positive", 0L) * 100f / totalTweets, "Very Positive"));
        pieEntries.add(new PieEntry(predictionMap.getOrDefault("Positive", 0L) * 100f / totalTweets, "Positive"));
        pieEntries.add(new PieEntry(predictionMap.getOrDefault("Neutral", 0L) * 100f / totalTweets, "Neutral"));
        pieEntries.add(new PieEntry(predictionMap.getOrDefault("Negative", 0L) * 100f / totalTweets, "Negative"));
        pieEntries.add(new PieEntry(predictionMap.getOrDefault("Very Negative", 0L) * 100f / totalTweets, "Very Negative"));

        PieDataSet dataSet = new PieDataSet(pieEntries, "Sentiment for " + gender);

        // Set colors for each sentiment
        if (gender.equals("Male")) {
            dataSet.setColors(new int[]{
                    Color.parseColor("#7ae582"), // Custom color for Male - Very Positive
                    Color.parseColor("#25a18e"), // Custom color for Male - Positive
                    Color.parseColor("#9fffcb"), // Custom color for Male - Neutral
                    Color.parseColor("#00a5cf"), // Custom color for Male - Negative
                    Color.parseColor("#004e64")  // Custom color for Male - Very Negative
            });
        } else if (gender.equals("Female")) {
            dataSet.setColors(new int[]{
                    Color.parseColor("#ffc2d4"), // Very Positive - Green
                    Color.parseColor("#ff7aa2"), // Positive - Light Green
                    Color.parseColor("#ffe0e9"), // Neutral - Yellow
                    Color.parseColor("#8a2846"), // Negative - Orange
                    Color.parseColor("#602437")  // Very Negative - Red
            });
        }
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(5f);

// Set PieChart data
        pieChart.setUsePercentValues(true);
        pieChart.setDrawEntryLabels(false); // Remove labels from slices

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f); // Set text size
        data.setValueFormatter(new PercentFormatter(pieChart)); // Attach PercentFormatter
        data.setValueTextColor(Color.BLACK); // Set text color

        pieChart.setData(data);
        pieChart.invalidate(); // Refresh the chart

// Customize the pie chart appearance
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(false);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

 /*
 // Customize Legend
       Legend legend = pieChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE); // Shape of the legend entries
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM); // Align the legend to the bottom
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER); // Center align the legend horizontally
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL); // Make the legend horizontal (single line)
        legend.setDrawInside(false); // Position the legend outside of the chart
        legend.setWordWrapEnabled(true); // Enable word wrapping
        legend.setTextSize(14f); // Set text size for legend entries
        legend.setYOffset(10f); // Add padding to the Y-axis of the legend (optional for better layout)*/

// Refresh the chart
        pieChart.invalidate(); // Refresh to apply all changes


    }
}